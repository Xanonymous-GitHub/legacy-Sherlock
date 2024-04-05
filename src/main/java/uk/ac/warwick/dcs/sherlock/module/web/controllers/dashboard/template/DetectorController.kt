package uk.ac.warwick.dcs.sherlock.module.web.controllers.dashboard.template

import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.ParameterForm
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TDetectorRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TParameterRepository
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.AccountWrapper
import uk.ac.warwick.dcs.sherlock.module.web.data.wrappers.DetectorWrapper
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.DetectorNotFound
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.NotTemplateOwner

/**
 * The controller that deals with the template detector pages
 */
@Controller
class DetectorController {
    @Autowired
    private val tDetectorRepository: TDetectorRepository? = null

    @Autowired
    private val tParameterRepository: TParameterRepository? = null

    /**
     * Handles GET requests to the detector parameter page
     *
     * @param detectorWrapper the detector being managed
     * @param model holder for model attributes
     *
     * @return the path of the parameter template
     *
     * @throws DetectorNotFound if the detector was not found
     */
    @GetMapping("/dashboard/templates/manage/detectors/{pathid}/parameters")
    @Throws(DetectorNotFound::class)
    fun parametersGet(
        @ModelAttribute("detector") detectorWrapper: DetectorWrapper,
        model: Model
    ): String {
        model.addAttribute("parameterForm", ParameterForm(detectorWrapper))
        model.addAttribute("parametersMap", detectorWrapper.engineParametersMap)
        model.addAttribute("postprocessingMap", detectorWrapper.enginePostProcessingParametersMap)
        return "dashboard/templates/parameters"
    }

    /**
     * Handles POST request to the detector parameter page
     *
     * @param parameterForm the form that should be submitted in the request
     * @param result the results of the validation on the form above
     * @param detectorWrapper the detector being managed
     * @param model holder for model attributes
     *
     * @return the path to the parameter template
     *
     * @throws DetectorNotFound if the detector was not found
     */
    @PostMapping("/dashboard/templates/manage/detectors/{pathid}/parameters")
    @Throws(DetectorNotFound::class, NotTemplateOwner::class)
    fun parametersPost(
        @ModelAttribute parameterForm: @Valid ParameterForm?,
        result: BindingResult,
        @ModelAttribute("detector") detectorWrapper: DetectorWrapper,
        model: Model
    ): String {
        val result = parameterForm!!.validate(
            result,
            detectorWrapper.engineParameters,
            detectorWrapper.enginePostProcessingParameters
        )

        if (!result.hasErrors()) {
            detectorWrapper.updateParameters(parameterForm, tParameterRepository)
            model.addAttribute("success_msg", "templates.parameters.updated")
        }

        model.addAttribute("parametersMap", detectorWrapper.engineParametersMap)
        model.addAttribute("postprocessingMap", detectorWrapper.enginePostProcessingParametersMap)
        return "dashboard/templates/parameters"
    }

    /**
     * Gets the detector where the id equals the "pathid" path variable
     *
     * @param account the account of the current user
     * @param pathid the id of the template
     * @param model holder for model attributes
     *
     * @return the detector wrapper
     *
     * @throws DetectorNotFound if the detector was not found
     */
    @ModelAttribute("detector")
    @Throws(DetectorNotFound::class)
    private fun getDetectorWrapper(
        @ModelAttribute("account") account: AccountWrapper,
        @PathVariable(value = "pathid") pathid: Long,
        model: Model
    ): DetectorWrapper {
        val detectorWrapper = DetectorWrapper(pathid, account.account, tDetectorRepository)
        model.addAttribute("detector", detectorWrapper)
        return detectorWrapper
    }
}