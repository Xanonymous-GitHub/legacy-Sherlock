package uk.ac.warwick.dcs.sherlock.module.web.data.models.db

import jakarta.persistence.*

/**
 * The database table storing parameters for a template detector
 */
@Entity
@Table(name = "parameter")
open class TParameter(
    @JvmField
    @Column(name = "name") var name: String?,

    @JvmField
    @Column(name = "the_value")
    var value: Float,

    @JvmField
    @Column(name = "postprocessing")
    var postprocessing: Boolean,

    @JvmField
    @ManyToOne
    @JoinColumn(name = "tDetector")
    var tDetector: TDetector,
) {
    @JvmField
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0

    override operator fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TParameter || javaClass != other.javaClass) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
