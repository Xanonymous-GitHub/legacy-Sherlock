package uk.ac.warwick.dcs.sherlock.api.component

/**
 * Helper interface, for fetching [ISourceFile] instances from their unique id
 */
interface ISourceFileHelper {
    /**
     * Fetches instance of the [ISourceFile] for the unique id passed
     * @param persistentId unique id of the file
     * @return instance
     */
    fun getSourceFile(persistentId: Long): ISourceFile
}
