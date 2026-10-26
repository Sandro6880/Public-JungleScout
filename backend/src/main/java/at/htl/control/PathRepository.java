package at.htl.control;


import at.htl.entity.RainforestPath;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

/**
 * The PathRepository class handles the management of the RainforestPath entities and does changes to the database when necessary.
 * All the methods needed are derived from the PanacheRepositoryBase extension.
 */
@Transactional
@ApplicationScoped
public class PathRepository implements PanacheRepositoryBase<RainforestPath,Long> {
}
