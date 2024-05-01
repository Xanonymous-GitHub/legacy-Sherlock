package uk.ac.warwick.dcs.sherlock.module.web.data.wrappers;

import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Account;
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.TDetector;
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.TParameter;
import uk.ac.warwick.dcs.sherlock.module.web.data.models.db.Template;
import uk.ac.warwick.dcs.sherlock.module.web.data.models.forms.TemplateForm;
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TDetectorRepository;
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TParameterRepository;
import uk.ac.warwick.dcs.sherlock.module.web.data.repositories.TemplateRepository;
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.NotTemplateOwner;
import uk.ac.warwick.dcs.sherlock.module.web.exceptions.TemplateNotFound;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The wrapper that manages the job templates
 */
public class TemplateWrapper {
    /**
     * The job template entity
     */
    private Template template;

    /**
     * Whether the current user owns the template
     */
    // FIXME: this value should be set to false if the user is not the owner.
    private boolean isOwner;

    /**
     * Initialise the wrapper using the form to create a new template
     *
     * @param templateForm        the form to use
     * @param account             the account of current user
     * @param templateRepository  the database repository
     * @param tDetectorRepository the database repository
     * @throws NotTemplateOwner if the user is not the owner of the template
     */
    public TemplateWrapper(
        TemplateForm templateForm,
        Account account,
        TemplateRepository templateRepository,
        TDetectorRepository tDetectorRepository
    ) throws NotTemplateOwner {
        this.template = new Template();
        this.template.account = account;
        this.isOwner = true;
        this.update(templateForm, templateRepository, tDetectorRepository);
    }

    /**
     * Initialise the template wrapper using an id to find one in the database
     *
     * @param id                 the id of the template
     * @param account            the account of the current user
     * @param templateRepository the database repository
     * @throws TemplateNotFound if the template was not found
     */
    public TemplateWrapper(
        long id,
        Account account,
        TemplateRepository templateRepository
    ) throws TemplateNotFound {
        this.template = templateRepository.findByIdAndPublic(id, account);

        if (this.template == null)
            throw new TemplateNotFound("Template not found.");

        assert this.template.account != null;
        this.isOwner = this.template.account.equals(account);
    }

    /**
     * Initialise the template wrapper using an existing template
     *
     * @param template the template to manage
     * @param account  the account of the current user
     */
    public TemplateWrapper(Template template, Account account) {
        this.template = template;
        assert this.template.account != null;
        this.isOwner = this.template.account.equals(account);
    }

    /**
     * Get the list of templates that are public or owned by the user
     *
     * @param account            the account of the current user
     * @param templateRepository the database repository
     * @return the list of templates
     */
    public static List<TemplateWrapper> findByAccountAndPublic(Account account, TemplateRepository templateRepository) {
        List<TemplateWrapper> wrapperList = new ArrayList<>();
        List<Template> templateList = templateRepository.findByAccountAndPublic(account);
        Objects.requireNonNull(templateList).forEach(t -> wrapperList.add(new TemplateWrapper(t, account)));
        return wrapperList;
    }

    /**
     * Get the list of templates that are public, owned by the user and filter
     * by the language supplied
     *
     * @param account            the account of the current user
     * @param templateRepository the database repository
     * @param language           the language to filter by
     * @return the list of templates
     */
    public static List<TemplateWrapper> findByAccountAndPublicAndLanguage(Account account, TemplateRepository templateRepository, String language) {
        List<TemplateWrapper> wrapperList = new ArrayList<>();
        List<Template> templateList = templateRepository.findByAccountAndPublicAndLanguage(account, language);
        Objects.requireNonNull(templateList).forEach(t -> wrapperList.add(new TemplateWrapper(t, account)));
        return wrapperList;
    }

    /**
     * Get the template
     *
     * @return the template
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * Set the template
     *
     * @param template the new template
     */
    public void setTemplate(Template template) {
        this.template = template;
    }

    /**
     * Whether the template is owned by the current user
     *
     * @return the result
     */
    public boolean isOwner() {
        return isOwner;
    }

    /**
     * Update the isOwned property
     *
     * @param owner the new value
     */
    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    /**
     * Get the name of the owner
     *
     * @return the name
     */
    public String getOwnerName() {
        return Objects.requireNonNull(this.template.account).username;
    }

    /**
     * Whether the template is public
     *
     * @return the result
     */
    public boolean isPublic() {
        return this.template.isPublic;
    }

    /**
     * Get the list of detector wrappers active in the template
     *
     * @return the list
     */
    public List<DetectorWrapper> getDetectors() {
        List<DetectorWrapper> wrapperList = new ArrayList<>();
        this.template.detectors.forEach(d -> wrapperList.add(new DetectorWrapper(d, this.isOwner)));
        return wrapperList;
    }

    /**
     * Update the template using the form supplied
     *
     * @param templateForm               the form to use
     * @param templateRepository         the database repository
     * @param templateDetectorRepository the database repository
     * @throws NotTemplateOwner if the user is not the template owner
     */
    public void update(
        TemplateForm templateForm,
        TemplateRepository templateRepository,
        TDetectorRepository templateDetectorRepository
    ) throws NotTemplateOwner {
        if (!this.isOwner)
            throw new NotTemplateOwner("You are not the owner of this template.");

        template.name = templateForm.getName();
        template.language = templateForm.getLanguage();
        template.isPublic = templateForm.isPublic();
        templateRepository.save(template);

        List<String> activeDetectors = EngineDetectorWrapper.getDetectorNames(template.language);

        List<String> toRemove = new ArrayList<>();

        List<String> toAdd = new ArrayList<>(templateForm.getDetectors());
        template.detectors.forEach(d -> toAdd.remove(d.name));

        template.detectors.forEach(d -> toRemove.add(d.name));
        toRemove.removeAll(templateForm.getDetectors());

        for (String add : toAdd) {
            templateDetectorRepository.save(new TDetector(add, template));
        }

        for (String remove : toRemove) {
            templateDetectorRepository.delete(
                Objects.requireNonNull(templateDetectorRepository.findByNameAndTemplate(remove, template))
            );
        }

        List<String> toCheck = new ArrayList<>(toAdd);
        template.detectors.forEach(d -> toCheck.add(d.name));
        toCheck.removeAll(toRemove);

        for (String check : toCheck) {
            if (!activeDetectors.contains(check)) {
                templateDetectorRepository.delete(
                    Objects.requireNonNull(templateDetectorRepository.findByNameAndTemplate(check, template))
                );
            }
        }
    }

    /**
     * Make a copy the template
     *
     * @param account              the account of the current user
     * @param templateRepository   the database repository
     * @param tDetectorRepository  the database repository
     * @param tParameterRepository the database repository
     * @return the new template
     */
    public Template copy(
        AccountWrapper account,
        TemplateRepository templateRepository,
        TDetectorRepository tDetectorRepository,
        TParameterRepository tParameterRepository
    ) {
        Template template = new Template();
        template.account = account.getAccount();
        template.language = this.template.language;
        template.isPublic = false;
        template.name = this.template.name + " - Copy";
        templateRepository.save(template);

        for (TDetector detector : this.template.detectors) {
            TDetector newDetector = new TDetector();
            newDetector.name = detector.name;
            newDetector.template = template;

            tDetectorRepository.save(newDetector);

            for (TParameter parameter : detector.parameters) {
                TParameter newParameter = new TParameter();
                newParameter.name = parameter.name;
                newParameter.value = parameter.value;
                newParameter.tDetector = newDetector;
                newParameter.postprocessing = parameter.postprocessing;

                tParameterRepository.save(newParameter);
            }
        }

        return template;
    }

    /**
     * Delete the template
     *
     * @param templateRepository the database repository
     * @throws NotTemplateOwner if the user is not the template owner
     */
    public void delete(TemplateRepository templateRepository) throws NotTemplateOwner {
        if (!this.isOwner)
            throw new NotTemplateOwner("You are not the owner of this template.");

        templateRepository.delete(this.template);
    }
}
