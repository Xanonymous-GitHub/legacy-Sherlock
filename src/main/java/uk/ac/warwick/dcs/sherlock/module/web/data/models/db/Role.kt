package uk.ac.warwick.dcs.sherlock.module.web.data.models.db

import jakarta.persistence.*

/**
 * The database table storing the roles assigned to each account
 */
@Entity
@Table(name = "role")
open class Role(
    @JvmField
    @Column(name = "name")
    var name: String?,

    @JvmField
    @ManyToOne
    @JoinColumn(name = "account")
    var account: Account?
) {
    @JvmField
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    var id: Long = 0
}
