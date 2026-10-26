package at.htl.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The Checkpoint class holds the necessary data that a checkpoint is in need of.
 * It is mainly used as the component of the RainforestPath class.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Checkpoint",schema = "urwaldroute")
public class Checkpoint implements Serializable {
    /**
     * This field is used to give the checkpoint a unique identity.
     */
    @Id
    @SequenceGenerator(
            name = "cpSequence",
            sequenceName = "cp_id_seq",
            initialValue = 1,
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
            ,generator = "cpSequence")
    private Long id;
    /**
     * This field holds the location's name of the checkpoint.
     * This field is primarily auto-filled by the Google Maps Api of the Android-Frontend.
     */
    private String cpLocation;
    /**
     * This field holds the given name of the checkpoint.
     * The name is given by the Admin in the Admin-Frontend.
     */
    private String name;
    /**
     * This field holds the Longitude coordinates of the checkpoint.
     */
    private String gpsLng;
    /**
     * This field holds the Latitude coordinates of the checkpoint.
     */
    private String gpsLat;
    /**
     * This field holds a description of the checkpoint.
     */
    private String details;

    public void copyProperties(Checkpoint cs) {
        this.cpLocation = cs.getCpLocation();
        this.name = cs.getName();
        this.gpsLng = cs.getGpsLng();
        this.gpsLat = cs.getGpsLat();
        this.details = cs.getDetails();
    }
}
