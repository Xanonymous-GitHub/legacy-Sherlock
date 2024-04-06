package uk.ac.warwick.dcs.sherlock.module.web.data.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.TDetector
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.TParameter

/**
 * The database repository that stores the parameters for each detector
 */
@Repository
interface TParameterRepository : CrudRepository<TParameter?, Long?> {
    /**
     * Finds all the parameters that are linked to the supplied detector
     *
     * @param tDetector the detector to filter by
     *
     * @return the list of parameters
     */
    fun findBytDetector(tDetector: TDetector?): List<TParameter?>?
}