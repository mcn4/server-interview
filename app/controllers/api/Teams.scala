package controllers.api

import domain.model.{Team, TeamNameAlreadyTakenException}
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
  def addMember(teamId: Long, memberId: String) = Action {
    NotImplemented[play.twirl.api.Html](views.html.defaultpages.todo())
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
  def getMembers(teamId: Long) = {
    TODO
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

  private[api] def teamJson(tf: Future[Team]): Future[JsValue] =
    tf.map(Json.toJson(_)(teamWrites))

  private[api] def forId(id: Long): Future[Team] =
    Future(domain.model.Team.forId(id)).map(getFromOptional(s"No team found with id $id"))

  private[api] def forName(name: String): Future[Team] =
    Future(domain.model.Team.forName(name)).map(getFromOptional(s"No team found with name $name"))

  private[api] def getFromOptional(errMsg: String)(ot: com.google.common.base.Optional[Team]): Team =
    if (ot.isPresent) ot.get else throw new NoSuchElementException(errMsg)

}