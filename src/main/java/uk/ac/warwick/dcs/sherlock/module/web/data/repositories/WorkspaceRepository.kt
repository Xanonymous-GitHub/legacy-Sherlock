package uk.ac.warwick.dcs.sherlock.module.web.data.repositories

import org.springframework.data.repository.CrudRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Account
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Workspace

/**
 * The database repository storing all workspaces
 */
interface WorkspaceRepository : CrudRepository<Workspace?, Long?> {
    /**
     * Finds all workspaces owned by the supplied account
     *
     * @param account the account to filter by
     *
     * @return the list of workspaces found
     */
    fun findByAccount(account: Account?): List<Workspace?>?

    /**
     * Finds a workspace with the supplied id and owned by the
     * supplied account
     *
     * @param id the if of the workspace to find
     * @param account the account to filter by
     *
     * @return the workspace found
     */
    fun findByIdAndAccount(id: Long, account: Account?): Workspace?
}