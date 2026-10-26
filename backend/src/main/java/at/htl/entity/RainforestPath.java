package at.htl.entity;

import javax.json.bind.annotation.JsonbDateFormat;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The class RainforestPath is the main class that is used to transfer data from
 * and to the clients. It holds data necessary to display and explain the path.
 * The name RainforestPath was chosen over a simpler version like Path because
 * there are already multiple instances of a Path class in libraries that may
 * end up conflicting with the code further on, thus this naming approach was
 * chosen.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Path",schema = "urwaldroute")
public class RainforestPath implements Serializable {
    /**
     * Long id: This field is used to reference the RainforestPath in the database.
     */
    @Id
    @SequenceGenerator(
            name = "pathSequence",
            sequenceName = "path_id_seq",
            initialValue = 1,
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
            ,generator = "pathSequence")
    private Long id;
    /**
     * String name: This field holds the name chosen for the RainforestPath.
     */
    private String name;
    /**
     * String pathLength: This field holds the length of the RainforestPath in kilometer (km).
     * The length is calculated with the Google Maps Api from the Admin-Frontend.
     */
    private String pathLength;
    /**
     * String pathDuration: This field holds the duration of the RainforestPath in minutes.
     * The duration is calculated with the Google Maps Api from the Admin-Frontend.
     */
    private String pathDuration;
    /**
     * List<Checkpoint> checkpoints: This field holds all the checkpoints associated with the RainforestPath.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @OrderColumn
    private List<Checkpoint> checkpoints = new ArrayList();

    /**
     * copyProperties is a method that transfers data from one RainforestPath (rfp) to another.
     * To ensure a clean order of the checkpoints and to avoid any unnecessary faults with the swapping of checkpoints,
     * every checkpoint gets cleared and added again.
     * @param rfp is the entity that holds key information for the transfer of data.
     */
    public void copyProperties(RainforestPath rfp) {
        this.name = rfp.getName();
        this.pathLength = rfp.getPathLength();
        this.pathDuration = rfp.getPathDuration();
        getCheckpoints().clear();
        getCheckpoints().addAll(rfp.getCheckpoints());
    }

    /**
     * addCheckpoint is called when a checkpoint needs to be added to the List of checkpoints.
     * @param cs a single entity of Checkpoint that holds data about it.
     */
    public void addCheckpoint(Checkpoint cs) {
        getCheckpoints().add(cs);
    }
}
