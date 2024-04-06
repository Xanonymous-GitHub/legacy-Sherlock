package uk.ac.warwick.dcs.sherlock.module.web.data.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Role

/**
 * The database repository storing the roles for each accound
 */
@Repository
interface RoleRepository : CrudRepository<Role?, String?>