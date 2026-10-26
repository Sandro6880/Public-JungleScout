package at.htl.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This class holds the necessary data to reference the given pictures with the checkpoints from the database.
 * This class is not affiliated with the database and is only inquired when a request for a Photobook is made.
 */
@Getter
@Setter
@NoArgsConstructor
public class ImageReference {
    /**
     * The image field is an encoded Base64 String that holds the String equivalent data of the jpeg.
     */
    public String image;
    /**
     * This field is used to identify the ImageReference.
     */
    public int uid;
    /**
     * This field is used to reference the id of the checkpoint that is associated with the image field.
     */
    public Long cp_id;
}
