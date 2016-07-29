package controllers.api

import com.google.common.base.Optional
import domain.model._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.{JsNumber, JsValue, Json, Writes}
import play.api.mvc.{Action, Controller, Request, Result}
import play.db.ebean.Transactional

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

object Teams extends Controller {
  private[api] case class TeamName(name: String)
  private[api] val teamNameForm: Form[TeamName] =
    Form(mapping("name" -> nonEmptyText)(TeamName.apply)(TeamName.unapply))

  private[api] case class MemberId(memberId: String)
  private[api] val memberIdForm: Form[MemberId] =
    Form(mapping("identity" -> nonEmptyText)(MemberId.apply)(MemberId.unapply))

  /**
    * Creates a new Team
    *
    * Input:
    *
    * { "name": "Team Foo" }
    *
    * Response:
    *
    * 201, { "id": 1, "name": "Team Foo", "members": 0 }
    *
    * This is one of the required endpoints.
    *
    */
  def createTeam() = Action.async(parse.json) { implicit rq: Request[JsValue] =>
    teamNameForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(formWithErrors.errorsAsJson))
      },
      {
        case TeamName(name) =>
          (for {
            _ <- Future(createNewTeamByName(name))
            json <- teamJson(forName(name))
          } yield Created(json))
          .recover {
            case e @ (_: TeamNameAlreadyTakenException | _: NoSuchElementException) =>
              BadRequest(Json.obj("name" -> e.getMessage))
            case NonFatal(e) => InternalServerError(Json.obj("error" -> e.toString))
          }
      }
    )
  }

  @Transactional
  @throws[TeamNameAlreadyTakenException]
  def createNewTeamByName(name: String) = {
    val team = new Team()
    team.name = name
    team.add()
  }

  /**
    * Adds the Team Member to the Team if not already present
    *
    * Input:
    *
    * { "identity" : "johndoe" }
    *
    * Response:
    *
    * 201
    *
    * [{"identity" : "johndoe", "email" : "john@example.com", "name" : "John Doe"}]
    *
    * (the new list of team members)
    *
    * This is one of the required endpoints.
    *
    */
  def addMember(teamId: Long) = Action.async(parse.json) { implicit rq: Request[JsValue] =>
    memberIdForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(formWithErrors.errorsAsJson))
      },
      {
        case MemberId(memberId) =>
          (for {
            _ <- Future(addMemberToTeam(teamId, memberId))
            json <- membersJson(forId(teamId))
          } yield Created(json))
            .recover {
              case e: NoSuchElementException => BadRequest(Json.obj("error" -> e.getMessage))
              case NonFatal(e) => InternalServerError(Json.obj("error" -> e.toString))
            }
      }
    )
  }

  @Transactional
  @throws[NoSuchElementException]
  def addMemberToTeam(teamId: Long, memberId: String) = {
    val team: Team = getTeamByIdFromOpt(teamId)(Team.forId(teamId))
    val newMember = getFromOpt(s"User with id $memberId cannot be found")(User.forId(memberId))
    team.members.add(newMember)
    team.update()
  }

  /**
    * Returns the Team Members of the Team with the given id
    *
    * Response:
    *
    * 200
    * [{"identity" : "johndoe", "email" : "john@example.com", "name" : "John Doe"}]
    *
    * This is one of the required endpoints.
    *
    */
  def getMembers(teamId: Long) = Action.async {
    (for {
      json <- membersJson(forId(teamId))
    } yield Ok(json))
      .recover {
        case e: NoSuchElementException => BadRequest(Json.obj("teamId" -> e.getMessage))
        case NonFatal(e) => InternalServerError(Json.obj("error" -> e.toString))
      }
  }

  /**
    * Returns the Team with the given id
    *
    * Response:
    *
    * 200
    * {"id": 1, "name": "Team Foo", "members": 0}
    *
    */
  def getTeam(teamId: Long) = Action.async {
    (for {
      json <- teamJson(forId(teamId))
    } yield Ok(json))
      .recover {
        case e: NoSuchElementException => BadRequest(Json.obj("id" -> e.getMessage))
        case NonFatal(e) => InternalServerError(Json.obj("error" -> e.toString))
      }
  }

  /**
    * Removes the Team Member from the given Team if they are in the team
    *
    * Response:
    * 200
    * [] (the new list of team members, in this example an empty JSON array
    *
    */
  def removeMember(teamId: Long, memberId: String) = {
    TODO
  }

  private[api] val teamWrites = new Writes[Team] {
    def writes(t: Team): JsValue = Json.obj(
      "id" -> t.id,
      "name" -> t.name,
      "members" -> t.members.size
    )
  }

  private[api] val memberWrites = new Writes[User] {
    def writes(u: User): JsValue = Json.obj(
      "identity" -> u.username,
      "email" -> u.getProfile.email,
      "name" -> s"${u.getProfile.firstName} ${u.getProfile.lastName}"
    )
  }

  private[api] def teamJson(tf: Future[Team]): Future[JsValue] =
    tf.map(Json.toJson(_)(teamWrites))

  private[api] def membersJson(tf: Future[Team]): Future[JsValue] = {
    implicit val writes = memberWrites
    import scala.collection.JavaConverters._
    tf.map(t => Json.toJson(t.members.asScala))
  }

  private[api] def forId(id: Long): Future[Team] =
    Future(domain.model.Team.forId(id)).map(getTeamByIdFromOpt(id))

  private[api] def forName(name: String): Future[Team] =
    Future(domain.model.Team.forName(name)).map(getFromOpt(s"No team found with name $name"))

  private[api] def getTeamByIdFromOpt(id: Long)(teamOpt: Optional[Team]) =
    getFromOpt(s"No team found with id $id")(teamOpt)

  @throws[NoSuchElementException]
  private[api] def getFromOpt[T](errMsg: String)(ot: Optional[T]): T =
    if (ot.isPresent) ot.get else throw new NoSuchElementException(errMsg)

}