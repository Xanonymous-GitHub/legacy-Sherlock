package uk.ac.warwick.dcs.sherlock.module.web.data.models.db

import jakarta.persistence.*

/**
 * The database table storing the workspaces owned by each account
 */
@Entity
@Table(name = "workspace")
open class Workspace {
    @JvmField
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0

    @JvmField
    @Column(name = "engine_id")
    var engineId: Long = 0

    @ManyToOne
    @JoinColumn(name = "account")
    private var account: Account? = null

    constructor(account: Account?) {
        this.account = account
        this.engineId = 0L
    }

    constructor(account: Account?, engineId: Long) {
        this.account = account
        this.engineId = engineId
    }

    override operator fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Workspace || javaClass != other.javaClass) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
