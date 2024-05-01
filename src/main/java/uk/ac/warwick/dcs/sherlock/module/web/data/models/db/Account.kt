package uk.ac.warwick.dcs.sherlock.module.web.data.models.db

import jakarta.persistence.*

/**
 * The account database table
 */
@Entity
@Table(name = "account")
open class Account(
    @JvmField
    @Column(name = "email", nullable = false, unique = true)
    var email: String?,

    @JvmField
    @Column(name = "password")
    var password: String?,

    @JvmField
    @Column(name = "name")
    var username: String?
) {
    @JvmField
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0

    @JvmField
    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], mappedBy = "account")
    var roles: MutableSet<Role> = mutableSetOf()

    @JvmField
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account", cascade = [CascadeType.ALL])
    var workspaces: MutableSet<Workspace> = mutableSetOf()

    @JvmField
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account", cascade = [CascadeType.ALL])
    var templates: MutableSet<Template> = mutableSetOf()

    override operator fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Account || javaClass != other.javaClass) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}