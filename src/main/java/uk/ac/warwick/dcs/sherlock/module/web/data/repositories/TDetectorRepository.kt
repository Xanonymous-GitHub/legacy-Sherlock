package uk.ac.warwick.dcs.sherlock.module.web.data.repositories

import org.springframework.data.repository.CrudRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.TDetector
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Template

/**
 * The database repository storing the detectors for each job template
 */
interface TDetectorRepository : CrudRepository<TDetector?, Long?> {
    /**
     * Finds the detector with the supplied name that is linked to the
     * supplied template
     *
     * @param name the name of the detector to find
     * @param template the template the detector is linked to
     *
     * @return the detector found
     */
    fun findByNameAndTemplate(name: String?, template: Template?): TDetector?
}