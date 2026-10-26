package at.htl.boundary;

import at.htl.control.CheckpointRepository;
import at.htl.entity.Checkpoint;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * The CheckpointResource class is the checkpoint endpoint that can be reached via /api/checkpoints.
 * This class handles all HTTP-Requests concerning the checkpoint entity.
 */
@Path("/checkpoints")
@Produces(APPLICATION_JSON)
@Transactional
@ApplicationScoped
public class CheckpointResource {
    @Inject
    CheckpointRepository checkpointRepository;

    /**
     * The getAll method returns all Checkpoints in the database.
     * @return
     */
    @GET
    public List<Checkpoint> getAll(){
        return checkpointRepository.findAll().list();
    }

    /**
     * The addCheckpoint method inserts a new Checkpoint to the database.
     * @param cp The checkpoint entity that will be persisted.
     * @return Returns a 200 Response when the insert is finished.
     */
    @POST
    public Response addCheckpoint(Checkpoint cp){
        Checkpoint entry = new Checkpoint();
        entry.copyProperties(cp);
        checkpointRepository.persist(entry);
        return Response.ok(entry).build();
    }

    /**
     * The deleteCheckpoint method deletes a checkpoint from the database.
     * @param cid The id is used to find the checkpoint in need of deletion.
     * @return Returns either a 200 or No Content Response depending on the result.
     */
    @DELETE
    @Path("{id}")
    public Response deleteCheckpoint(@PathParam("id") Long cid){
        if (checkpointRepository.findById(cid) != null){
            checkpointRepository.deleteById(cid);
            return Response.ok("Checkpoint got deleted").build();
        }
        return Response.noContent().build();
    }

    /**
     * The updateCheckpoint method updates a specific checkpoint with the given data.
     * @param cid The id is used to find the checkpoint in need of an update.
     * @param cp The checkpoint entity that holds the data for the update.
     * @return Returns a 200 or No Content Response depending on the end result.
     */
    @PUT
    @Path("{id}")
    public Response updateCheckpoint(@PathParam("id") Long cid, Checkpoint cp){
        Checkpoint change = checkpointRepository.findById(cid);
        if(change != null){
            change.copyProperties(cp);
            checkpointRepository.getEntityManager().merge(change);
            return Response.ok().build();
        }
        return Response.noContent().build();
    }
}
