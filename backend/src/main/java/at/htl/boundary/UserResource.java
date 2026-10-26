package at.htl.boundary;

import at.htl.control.PathRepository;
import at.htl.control.UserRepository;
import at.htl.entity.RainforestPath;
import at.htl.entity.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


/**
 * UserResource is the user endpoint that can be reached with /api/users.
 * This class handles all HTTP-Requests concerning the user entity.
 */
@Path("/users")
@Produces(APPLICATION_JSON)
@Transactional
@ApplicationScoped
public class UserResource {
    @Inject
    UserRepository userRepository;
    @Inject
    PathRepository pathRepository;

    /**
     * The getUser method searches for a specific user and returns the entity.
     * @param uid the id needed to discern which user is needed.
     * @return Returns a 200 Response with the User entity in its body if the user with the id is present.
     * Otherwise, it will return a 404 Not Found Response.
     */
    @GET
    @Path("/{UserId}")
    public Response getUser(@PathParam("UserId") Long uid){
        User user = userRepository.findById(uid);
        if (user != null){
            return Response.ok().build();
        }
        else return Response
                .status(Response.Status.NOT_FOUND)
                .build();
    }

    /**
     * The getUserByEmail method searches for a specific user with the given email and returns the entity if present.
     * @param email The given email which will be searched for in the database.
     * @return Returns either a 200 Response with the user in its body or a 404 Not found Response with the given email as a header.
     */
    @GET
    @Path("/{email}")
    public Response getUserByEmail(@PathParam("email") String email){
        Optional<User> user = userRepository.find("email",email).stream().findAny();
        if(user.isEmpty()){
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .header(email,User.class)
                    .build();
        }
        return Response.ok(user).build();
    }

    /**
     * The getPathsOfUser method returns all Paths that are associated with the user.
     * @param uid Is used to find the user.
     * @return Either returns a 200 Repsonse if request is successful or a No Content Response.
     */
    @GET
    @Path("/{UserId}/path")
    public Response getPathsOfUser(@PathParam("UserId") Long uid){
        User user = userRepository.findById(uid);
        if(user == null){
            doLoader(user);
            return Response
                    .status(Response.Status.NO_CONTENT)
                    .header("User was not found!",User.class)
                    .build();
        }
        return Response.ok(user.getRainforestPaths()).build();
    }

    /**
     * The addPathToUser method adds a specific path to the users' collection of paths.
     * @param uid The id for finding the User.
     * @param pid The id for finding the Path.
     * @return Returns a 200 Response if everything went accordingly and in case of something
     * going wrong, it returns a No Content Response.
     */
    @POST
    @Path("{UserId}/paths/{PathId}")
    public Response addPathToUser(@PathParam("UserId") Long uid
            , @PathParam("PathId") Long pid){
        User user = userRepository.findById(uid);
        RainforestPath path = pathRepository.findById(pid);
        if(user == null){
            return Response
                    .status(Response.Status.NO_CONTENT)
                    .header("User was not found!",User.class)
                    .build();
        }
        else if(path == null){
            return Response
                    .status(Response.Status.NO_CONTENT)
                    .header("Course was not found!",RainforestPath.class)
                    .build();
        }
        path.getCheckpoints();
        doLoader(user);
        user.addPaths(path);
        userRepository.persist(user);
        return Response.ok(userRepository.findById(uid)).build();
    }

    /**
     * The addUser method persists a new user if not already present.
     * @param user The entity which provides the users needed data.
     * @return Returns a 200 Response in case of everything going accordingly and a Found Response
     * in case the user is already present.
     */
    @POST
    @Path("addUser")
    public Response addUser(User user){
        User entry = new User();
        if(userRepository.isPersistent(user) == true){
            return Response
                    .status(Response.Status.FOUND)
                    .header("User was found",User.class)
                    .build();
        }
        entry.copyProperties(user);
        userRepository.persist(entry);
        return Response.ok(entry).build();
    }

    /**
     * The method deletes a user by the given id.
     * @param uid The id that will be used to find the specific user in need for deletion.
     * @return Returns a Gone Response in case everything went accordingly and a No Content Response if the user was not found.
     */
    @DELETE
    @Path("{id}")
    public Response deleteUser(@PathParam("id")Long uid){
        if (userRepository.findById(uid) != null){
            userRepository.deleteById(uid);
            return Response.status(Response.Status.GONE)
                    .header("User deleted!",User.class)
                    .build();
        }
        return Response.noContent().build();
    }

    /**
     * The updateUser method searches for the user in need of an update and updates the user with the new data accordingly.
     * @param uid The id for finding the needed user.
     * @param user The data of the user that will be updated.
     * @return Returns a Found Response if the update went succesfully and a no Content Response in case it didn't.
     */
    @PUT
    @Path("{id}")
    public Response updateUser(@PathParam("id")Long uid,User user){
        User updated = userRepository.findById(uid);
        if (updated != null &&
                userRepository.isPersistent(user) == false){
            doLoader(updated);
            updated.copyProperties(user);
            userRepository.getEntityManager().merge(updated);
            return Response.status(Response.Status.FOUND)
                    .header("User updated!",User.class)
                    .build();
        }
        return Response.noContent().build();
    }

    /**
     * The doLoader method is a supporting method that initializes the lists that are returned with the Lazy loader.
     * @param user The user in need to be lazy initialized.
     */
    public void doLoader(User user){
        List<RainforestPath> load = user.getRainforestPaths();
        load.forEach(course -> course.getCheckpoints());
    }
}
