package uk.ac.warwick.dcs.sherlock.module.web.data.models.db

import jakarta.persistence.*

/**
 * The database table storing job templates
 */
@Entity
@Table(name = "template")
open class Template {
    @JvmField
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0

    @JvmField
    @Column(name = "name")
    var name: String? = null

    @JvmField
    @Column(name = "language")
    var language: String? = null

    @JvmField
    @Column(name = "is_public")
    var isPublic: Boolean = false

    @JvmField
    @ManyToOne
    @JoinColumn(name = "account")
    var account: Account? = null

    @JvmField
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "template", cascade = [CascadeType.REMOVE])
    var detectors: MutableSet<TDetector> = mutableSetOf()

    override operator fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Template || javaClass != other.javaClass) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
