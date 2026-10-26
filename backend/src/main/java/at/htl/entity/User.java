package at.htl.entity;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * The user class holds all the data necessary for the user.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "User",schema = "urwaldroute")
public class User implements Serializable {
    /**
     * The id field is used as an identifier for the user.
     */
    @Id
    @SequenceGenerator(
            name = "userDiploSequence",
            sequenceName = "user_id_seq",
            initialValue = 1,
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
            ,generator = "userDiploSequence")
    private Long id;
    /**
     * The name field is used to display the name that the user has chosen for themselves.
     */
    @NotNull
    @NotEmpty(message = "Not allowed to be empty!")
    private String name;
    /**
     * The email field is used to convey the email destination when needed.
     */
    @NotNull
    @Email
    private String email;
    /**
     * The rainforestPaths field holds a list of all paths that the user has already visited.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    private List<RainforestPath> rainforestPaths = new ArrayList();

    public void copyProperties(User user) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.rainforestPaths = user.getRainforestPaths();
    }

    /**
     * The method addPaths is used to associate the user with the Path that the user chose.
     * @param path the chosen path of the user.
     */
    public void addPaths(RainforestPath path) {
        getRainforestPaths().add(path);
    }
}
