package uk.ac.warwick.dcs.sherlock.module.web.data.wrappers

import uk.ac.warwick.dcs.sherlock.api.annotation.AdjustableParameterObj
import uk.ac.warwick.dcs.sherlock.api.model.detection.IDetector
import uk.ac.warwick.dcs.sherlock.api.registry.SherlockRegistry
import uk.ac.warwick.dcs.sherlock.engine.SherlockEngine
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Account
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.TDetector
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.TParameter
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.ParameterForm
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TDetectorRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TParameterRepository
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.DetectorNotFound
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.NotTemplateOwner
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.ParameterNotFound

/**
 * The wrapper that manages database detectors
 */
class DetectorWrapper {
    /**
     * The detector to manage
     */
    var detector: TDetector? = null

    /**
     * Whether the template is owned by the current user
     *
     * @return the result
     */
    /**
     * Whether the current account owns the detector
     */
    var isOwner: Boolean = false
        private set

    /**
     * Initialise the wrapper with a template that has already been loaded
     *
     * @param tDetector the detector to manage
     * @param isOwner   whether the account owns the detector
     */
    constructor(tDetector: TDetector?, isOwner: Boolean) {
        this.detector = tDetector
        this.isOwner = isOwner
    }

    /**
     * Initialise the wrapper by trying to find a detector that is
     * either owned by the account or is public
     *
     * @param id                  the template id to find
     * @param account             the account of the current user
     * @param tDetectorRepository the detector database repository
     * @throws DetectorNotFound if the detector wasn't found
     */
    constructor(
        id: Long,
        account: Account?,
        tDetectorRepository: TDetectorRepository?
    ) {
        val optional = tDetectorRepository?.findById(id)

        if (optional?.isEmpty == true) {
            throw DetectorNotFound("Detector not found.")
        }

        this.detector = optional?.get()

        val detectorTemplate = this.detector?.template

        if (detectorTemplate != null) {
            val templateWrapper = TemplateWrapper(detectorTemplate, account)

            if (!templateWrapper.isOwner && !templateWrapper.template.isPublic) {
                throw DetectorNotFound("Detector not found.")
            }
        }

        this.isOwner = detectorTemplate?.account == account
    }

    @get:Throws(DetectorNotFound::class)
    val engineParameters: List<AdjustableParameterObj>
        /**
         * Get the list of adjustable parameters for this detector
         *
         * @return the list of adjustable parameters
         * @throws DetectorNotFound if the detector no longer exists
         */
        get() {
            val result: MutableList<AdjustableParameterObj> = ArrayList()

            val detector = SherlockRegistry.getDetectorAdjustableParameters(
                engineDetector
            )
            if (detector != null) {
                result.addAll(detector)
            }

            return result
        }

    @get:Throws(DetectorNotFound::class)
    val enginePostProcessingParameters: List<AdjustableParameterObj>
        /**
         * Get the list of adjustable postprocessing parameters for this detector
         *
         * @return the list of adjustable postprocessing parameters
         * @throws DetectorNotFound if the detector no longer exists
         */
        get() {
            val result: MutableList<AdjustableParameterObj> = ArrayList()

            val post = SherlockRegistry.getPostProcessorAdjustableParametersFromDetector(
                engineDetector
            )
            if (post != null) {
                result.addAll(post)
            }

            return result
        }

    @get:Throws(DetectorNotFound::class)
    val engineParametersMap: Map<String, AdjustableParameterObj>
        /**
         * Get the adjustable parameters for this detector as a map
         *
         * @return the map of adjustable parameters
         * @throws DetectorNotFound if the detector no longer exists
         */
        get() {
            val map: MutableMap<String, AdjustableParameterObj> = HashMap()
            for (p in this.engineParameters) {
                map[p.name] = p
            }
            return map
        }

    @get:Throws(DetectorNotFound::class)
    val enginePostProcessingParametersMap: Map<String, AdjustableParameterObj>
        get() {
            val map: MutableMap<String, AdjustableParameterObj> = HashMap()
            for (p in this.enginePostProcessingParameters) {
                map[p.name] = p
            }
            return map
        }

    val id: Long
        /**
         * Get the id of the detector
         *
         * @return the id
         */
        get() = detector!!.id

    @Suppress("UNCHECKED_CAST")
    @get:Throws(DetectorNotFound::class)
    val engineDetector: Class<out IDetector<*>?>
        /**
         * Get the engine object for this detector
         *
         * @return the IDetector
         * @throws DetectorNotFound if the engine detector no longer exists
         */
        get() {
            try {
                return Class.forName(
                    this.detector!!.name,
                    true, SherlockEngine.classloader
                ) as Class<out IDetector<*>?>
            } catch (e: ClassNotFoundException) {
                throw DetectorNotFound("Detector no longer exists")
            }
        }

    @get:Throws(DetectorNotFound::class)
    val wrapper: EngineDetectorWrapper
        /**
         * Get the engine wrapper for this detector
         *
         * @return the engine detector wrapper
         * @throws DetectorNotFound if the engine detector no longer exists
         */
        get() {
            val detector = this.engineDetector

            return EngineDetectorWrapper(detector)
        }

    @get:Throws(DetectorNotFound::class, ParameterNotFound::class)
    val parametersList: List<ParameterWrapper>
        /**
         * Get the list of parameters for this detector
         *
         * @return the list of parameters
         * @throws ParameterNotFound
         * @throws DetectorNotFound  if the engine detector no longer exists
         */
        get() {
            val list: MutableList<ParameterWrapper> = ArrayList()

            for (p in detector!!.parameters) {
                if (p.postprocessing) {
                    list.add(ParameterWrapper(p, this.enginePostProcessingParametersMap))
                } else {
                    list.add(ParameterWrapper(p, this.engineParametersMap))
                }
            }

            return list
        }

    /**
     * Update the parameters for this detector
     *
     * @param parameterForm        the form to use
     * @param tParameterRepository the database repository
     */
    @Throws(NotTemplateOwner::class)
    fun updateParameters(parameterForm: ParameterForm, tParameterRepository: TParameterRepository?) {
        if (!this.isOwner) throw NotTemplateOwner("You are not the owner of this template.")

        val currentParameters = tParameterRepository?.findBytDetector(this.detector)!!
        tParameterRepository.deleteAll(currentParameters)

        for ((key, value) in parameterForm.parameters) {
            val parameter = TParameter(key, value!!, false, detector!!)
            tParameterRepository.save(parameter)
        }

        for ((key, value) in parameterForm.postprocessing) {
            val postparameter = TParameter(
                key, value!!, true,
                detector!!
            )
            tParameterRepository.save(postparameter)
        }
    }
}
