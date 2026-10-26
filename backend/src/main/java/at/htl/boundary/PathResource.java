package at.htl.boundary;

import at.htl.control.CheckpointRepository;
import at.htl.control.PathRepository;
import at.htl.entity.Checkpoint;
import at.htl.entity.RainforestPath;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * The Pathresource class is the path endpoint that can be reached via /api/paths.
 * This class handles all HTTP-Requests concerning the path entity.
 */
@Path("/paths")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Transactional
@ApplicationScoped
public class PathResource {
    @Inject
    PathRepository pathRepository;
    @Inject
    CheckpointRepository csRepository;

    /**
     * The getAll method searches and returns all the present Paths in the database.
     * @return Returns all Paths found.
     */
    @GET
    public List<RainforestPath> getAll(){
        return doLoader(pathRepository.findAll().list());
    }

    /**
     * The getPathById method returns a specific Path given by the id.
     * @param pid The id is used to find the path.
     * @return Returns a 200 Response in case the Path was found and a No Content Response if not.
     */
    @GET
    @Path("{id}")
    public Response getPathById(@PathParam("id") Long pid){
        RainforestPath read = pathRepository.findById(pid);
        if(read == null)return Response.noContent().build();
        doLoader(read);
        return Response.ok(read).build();
    }

    /**
     * The addPath method inserts a new provided path.
     * @param path The path entity that holds the data needed to persist.
     * @return Returns a 200 okay Response.
     */
    @POST
    public Response addPath(RainforestPath path){
        RainforestPath entry = new RainforestPath();
        entry.copyProperties(path);
        pathRepository.persist(entry);
        return Response.ok(entry).build();
    }

    /**
     * The deletePath method deletes a path by the given id.
     * @param pid The id needed to find and delete the Path.
     * @return Returns either a 200 or No Content Response.
     */
    @DELETE
    @Path("{id}")
    public Response deletePath(@PathParam("id") Long pid){
        if (pathRepository.findById(pid) != null){
            pathRepository.deleteById(pid);
            return Response.ok("Path got deleted").build();
        }
        return Response.noContent().build();
    }

    /**
     * The updatePath method changes the data of a present Path by the given data.
     * @param pid The id is used for finding the path in need of an update.
     * @param rfp Holds the data which is going to be updated to the database.
     * @return Returns a 200 or No Content Response depending on the result.
     */
    @PUT
    @Path("{id}")
    public Response updatePath(@PathParam("id") Long pid,RainforestPath rfp){
        RainforestPath change = pathRepository.findById(pid);
        if(change != null){
            doLoader(change);
            change.copyProperties(rfp);
            pathRepository.getEntityManager().merge(change);
            return Response.ok().build();
        }
        return Response.noContent().build();
    }

    /**
     * The getCheckpointsOfPath method returns all checkpoints associated with the path.
     * @param pid The id is used to find which specific path is needed.
     * @return Returns either a 200 or No Content Response depending on the result.
     */
    @GET
    @Path("{id}/checkpoints")
    public Response getCheckpointsOfPath(@PathParam("id") Long pid){
        RainforestPath read = pathRepository.findById(pid);
        if(read == null)return Response.noContent().build();
        return Response.ok(read.getCheckpoints()).build();
    }

    /**
     * The addCheckpointsToPath method inserts all checkpoints that are added to the path in the given order.
     * @param pid The id is used to find the path needed.
     * @param cpList The list of checkpoints that will be added to the path.
     * @return Returns a no Content or a 200 Response depending on the result.
     */
    @POST
    @Path("{id}/checkpoints")
    public Response addCheckpointsToPath(@PathParam("id") Long pid, Checkpoint[] cpList){
        RainforestPath read = pathRepository.findById(pid);
        if(read == null)return Response.noContent().build();
        Arrays.stream(cpList)
                .forEach(l -> {
                    Optional<Checkpoint> created =  csRepository.find("name",l.getName()).stream().findFirst();
                    created.ifPresent(read::addCheckpoint);
                });
        pathRepository.persist(read);
        return Response.ok(pathRepository.findById(pid)).build();
    }

    /**
     * The removeCheckpointsFromPath method removes the connections of the associated path with the given checkpoints.
     * @param pid The id which will specify which path is going to be used.
     * @param cpList The checkpoints in need of removal in the Path.
     * @return Returns either a 200 or No Content Response depending on the result.
     */
    @DELETE
    @Path("{id}/checkpoints")
    public Response removeCheckpointsFromPath(@PathParam("id") Long pid, Checkpoint[] cpList){
        RainforestPath change = pathRepository.findById(pid);
        if(change == null)return Response.noContent().build();
        change.getCheckpoints().stream()
                .forEach(l -> {
                    if(Arrays.stream(cpList).anyMatch(nl -> nl.equals(l))){
                        csRepository.delete(l);
                        change.getCheckpoints().remove(l);
                    }
                });
        pathRepository.persist(change);
        return Response.ok(change.getCheckpoints()).build();
    }
    /**
     * The doLoader method is a supporting method that initializes the lists that are returned with the Lazy loader.
     * @param paths The list of path entities in need of lazy initialization.
     */
    public List<RainforestPath> doLoader(List<RainforestPath> paths){
        paths.forEach(this::doLoader);
        return paths;
    }

    /**
     * The doLoader method is a supporting method that initializes the lists that are returned with the Lazy loader.
     * @param path The path entity in need of lazy initialization.
     */
    public void doLoader(RainforestPath path){
        path.getCheckpoints();
    }
}
