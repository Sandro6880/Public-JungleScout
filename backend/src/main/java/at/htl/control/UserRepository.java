package at.htl.control;

import at.htl.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

/**
 * The UserRepository class handles the management of the User entities and does changes to the database when necessary.
 * All the methods needed are derived from the PanacheRepositoryBase extension.
 */
@ApplicationScoped
@Transactional
public class UserRepository implements PanacheRepositoryBase<User,Long> {

}
