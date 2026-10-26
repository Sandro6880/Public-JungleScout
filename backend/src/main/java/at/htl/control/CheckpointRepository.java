package at.htl.control;

import at.htl.entity.Checkpoint;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

/**
 * The CheckpointRepository class handles the management of the Checkpoint entities and does changes to the database when necessary.
 * All the methods needed are derived from the PanacheRepositoryBase extension.
 */
@Transactional
@ApplicationScoped
public class CheckpointRepository implements PanacheRepositoryBase<Checkpoint,Long> {
}
