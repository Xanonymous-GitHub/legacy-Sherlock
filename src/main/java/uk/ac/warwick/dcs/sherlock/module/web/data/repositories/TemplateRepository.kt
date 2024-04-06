package uk.ac.warwick.dcs.sherlock.module.web.data.repositories

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Account
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Template

/**
 * The database repository storing the job templates
 */
@Repository
interface TemplateRepository : CrudRepository<Template?, Long?> {
    /**
     * Finds the template with the id supplied only if it is
     * owned by the account supplied or is public
     *
     * @param id the id of the template to find
     * @param account the account object of the current user
     *
     * @return the template found
     */
    @Query("SELECT t FROM Template t WHERE t.id = :id AND (t.account = :account OR t.isPublic = true)")
    fun findByIdAndPublic(@Param("id") id: Long, @Param("account") account: Account?): Template?

    /**
     * Finds all templates that are owned by the account or
     * are public
     *
     * @param account the account object of the current user
     *
     * @return the list of templates found
     */
    @Query("SELECT t FROM Template t WHERE t.account = :account OR t.isPublic = true")
    fun findByAccountAndPublic(@Param("account") account: Account?): List<Template?>?


    /**
     * Finds all templates of a specific language that are
     * owned by the account or are public
     *
     * @param account the account object of the current user
     * @param language the language to filter by
     *
     * @return the list of templates found
     */
    @Query("SELECT t FROM Template t WHERE t.language = :language AND (t.account = :account OR t.isPublic = true)")
    fun findByAccountAndPublicAndLanguage(
        @Param("account") account: Account?,
        @Param("language") language: String?
    ): List<Template?>?
}