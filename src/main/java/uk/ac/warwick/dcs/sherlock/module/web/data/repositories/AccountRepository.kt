package uk.ac.warwick.dcs.sherlock.module.web.data.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Account

/**
 * The database repository storing the account details
 */
@Repository
interface AccountRepository : CrudRepository<Account?, Long?> {
    /**
     * Find the account with the supplied email
     *
     * @param email the email to find the account for
     *
     * @return the account found
     */
    fun findByEmail(email: String?): Account?
}