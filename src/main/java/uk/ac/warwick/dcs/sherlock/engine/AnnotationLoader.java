package uk.ac.warwick.dcs.sherlock.engine;

import org.apache.commons.io.FilenameUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.Scanners;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.warwick.dcs.sherlock.api.annotation.SherlockModule;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Responsible for all classloading and reflection
 */
public class AnnotationLoader {

    static final Logger logger = LoggerFactory.getLogger(AnnotationLoader.class);
    private final Reflections ref;

    /**
     * Load modules from directory, and initialise the reflection
     */
    AnnotationLoader() {
        boolean useDefaultPath = (SherlockEngine.overrideModulesPath == null || SherlockEngine.overrideModulesPath.isEmpty());
        String modulesPath = useDefaultPath ? SherlockEngine.configuration.getDataPath() : SherlockEngine.overrideModulesPath;
        if (!modulesPath.endsWith("/")) {
            modulesPath += "/";
        }
        if (useDefaultPath) {
            modulesPath += "Module/";
        }
        modulesPath = FilenameUtils.separatorsToSystem(modulesPath);

        boolean continueFlag = SherlockEngine.configuration.getEnableExternalModules();
        File modules = null;
        if (continueFlag) {
            modules = new File(modulesPath);
            if (!modules.exists()) {
                if (!modules.mkdir()) {
                    logger.error("Could not create module directory '{}', modules cannot be loaded", modulesPath);
                    continueFlag = false;
                }
            }
        }

        List<URL> moduleURLS = new LinkedList<>();
        if (continueFlag) {
            List<URL> classpathURLs = new LinkedList<>();
            moduleURLS.addAll(Arrays.stream(Objects.requireNonNull(modules.listFiles())).map(f -> {
                try {
                    URL url = f.toURI().toURL();
                    classpathURLs.add(url);
                    return url;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                return null;
            }).toList());

            //Load libs to
            File libs = new File(FilenameUtils.separatorsToSystem(modulesPath + "libs/"));
            if (!libs.exists()) {
                libs.mkdir();
            }

            Arrays.stream(Objects.requireNonNull(libs.listFiles())).forEach(f -> {
                try {
                    classpathURLs.add(f.toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            });


            final URL[] urls = classpathURLs.toArray(new URL[0]);
            try (CustomURLClassLoader classLoader = new CustomURLClassLoader(urls, ClassLoader.getSystemClassLoader())) {
                for (URL url : classpathURLs) {
                    classLoader.addURL(url);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        moduleURLS.addAll(ClasspathHelper.forPackage("uk.ac.warwick.dcs.sherlock.engine"));
        moduleURLS.addAll(ClasspathHelper.forPackage("uk.ac.warwick.dcs.sherlock.module"));
        moduleURLS.addAll(ClasspathHelper.forPackage("uk.ac.warwick.dcs.sherlock.launch"));

        ConfigurationBuilder config = new ConfigurationBuilder();
        config.addClassLoaders(SherlockEngine.classloader);
        config.setUrls(moduleURLS);
        config.setScanners(
                Scanners.SubTypes,
                Scanners.TypesAnnotated,
                Scanners.MethodsAnnotated
        );
        config.filterInputsBy(new FilterBuilder().includePattern(".*class"));
        this.ref = new Reflections(config);
    }

    /**
     * Register external modules found to the event bus, ready for initialisation events
     */
    void registerModules() {
        this.ref.getTypesAnnotatedWith(SherlockModule.class).stream().peek(x -> logger.info("Registering Sherlock module: {}", x.getName())).forEach(SherlockEngine.eventBus::registerModule);
    }
}
