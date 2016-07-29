package domain.model;

import com.google.common.base.Optional;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Embedded;
import javax.persistence.Id;
import javax.validation.Valid;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Team extends Model {
    @Id
    public long id;

    @Constraints.Required
    @Formats.NonEmpty
    public String name;

    @Constraints.Required
    public Set<User> members = new HashSet<>();

    public final static Finder<String, Team> find = new Finder<>(String.class, Team.class);

    public static Optional<Team> forName(final String name) {
        return Optional.fromNullable(find.byId(name));
    }

    public final void add() throws TeamNameAlreadyTakenException {
        if (forName(name).isPresent()) {
            throw new UserNameAlreadyTakenException("Team name " + name + " is already taken");
        } else {
            save();
        }
    }

}
