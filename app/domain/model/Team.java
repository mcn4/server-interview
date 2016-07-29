package domain.model;

import com.google.common.base.Optional;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Team extends Model {
    @Id
    public long id;

    @Constraints.Required
    @Formats.NonEmpty
    public String name;

    @Constraints.Required
    public Set<User> members = new HashSet<>();

    public final static Finder<Long, Team> find = new Finder<>(Long.class, Team.class);

    public static Optional<Team> forId(final Long id) {
        return Optional.fromNullable(find.byId(id));
    }

    public static Optional<Team> forName(final String name) {
        return Optional.fromNullable(find.where().eq("name", name).findUnique());
    }

    public final void add() throws TeamNameAlreadyTakenException {
        if (forName(name).isPresent()) {
            throw new UserNameAlreadyTakenException("Team name " + name + " is already taken");
        } else {
            save();
        }
    }

}
