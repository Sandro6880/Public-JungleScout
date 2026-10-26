package at.htl.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;


/**
 * This class is mainly used to transfer the data of the pictures and the given email from the Android-Frontend (App)
 */
@Getter
@Setter
@NoArgsConstructor
public class Photobook {
    /**
     * Holds a list of ImageReferences that needed to associate the pictures with the checkpoints.
     * This list is unique to the Photobook instance.
     */
    public List<ImageReference> photos = new ArrayList<>();
    /**
     * Holds the given email by the user that is later on used as a destination target for the finished Photobook.
     */
    public String email;
}
