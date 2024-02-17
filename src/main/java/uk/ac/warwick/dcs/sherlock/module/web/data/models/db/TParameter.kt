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
    @Column(name = "value")
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
}
