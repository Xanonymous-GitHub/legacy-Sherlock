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
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "template", cascade = [CascadeType.ALL])
    var detectors: MutableSet<TDetector> = mutableSetOf()
}
