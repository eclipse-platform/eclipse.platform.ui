/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.intro.internal.extensions;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.intro.internal.model.*;
import org.eclipse.ui.intro.internal.util.*;

/**
 * Base class for handling Intro Extensions.
 */

public class BaseExtensionPointManager {

    // the config extension id
    protected static final String CONFIG = "org.eclipse.ui.intro.config";

    // the configExtension extension id
    protected static final String CONFIG_EXTENSION = "org.eclipse.ui.intro.configExtension";

    // the attribute in the config element to specify the intro part id.
    protected static final String CONFIG_INTRO_ID_ATTRIBUTE = "introId";

    // the attribute in the config element to specify the intro part id.
    protected static final String CONFIG_EXTENSION_CONFIG_ID_ATTRIBUTE = "configId";

    // the id attribute in any intro element.
    protected static final String ID_ATTRIBUTE = IntroElement.ID_ATTRIBUTE;

    protected Hashtable introModels = new Hashtable();
    protected IPluginRegistry registry;

    // holds all standbyPart extensions. Key is id, value is
    // IntroStandbyPartContent.
    protected Hashtable standbyParts = new Hashtable();

    /*
     * Prevent creation.
     */
    protected BaseExtensionPointManager() {
        registry = Platform.getPluginRegistry();
    }

    protected IntroModelRoot loadModel(String attrributeName,
            String attributeValue) {
        // get all Config extension point contributions. There could be more
        // than one config contribution, but there should only be one that maps
        // to the cached intro part id.
        IConfigurationElement introConfig = getIntroConfig(attrributeName,
                attributeValue);

        // load model with the config elements of the correct contribution. If
        // there are no valid contribution, model stays null.
        if (introConfig != null) {
            // we found matching config. Get all configExtension contributed to
            // this config and pass to model. Load generic config extensions as
            // well.
            String configId = introConfig.getAttribute(ID_ATTRIBUTE);
            IConfigurationElement[] introConfigExtensions = null;
            if (configId == null)
                // if id of config is null, pass empty array.
                introConfigExtensions = new IConfigurationElement[0];
            else
                introConfigExtensions = getIntroConfigExtensions(
                        CONFIG_EXTENSION_CONFIG_ID_ATTRIBUTE, configId);

            IntroModelRoot model = new IntroModelRoot(introConfig,
                    introConfigExtensions);
            model.loadModel();
            // add the current model to the hash table of models, only if it is
            // not null. They key is the model id, which is the id of the
            // config that defined this model.
            addCachedModel(model.getId(), model);

            // now load all generic standbyPart contributions.
            loadSharedExtensions();
            return model;
        }
        return null;
    }

    /**
     * Go through all the config elements and only return the correct config
     * that maps to the correct intro part id. If there is more than one config
     * thats maps to the same intro part id, log the fact, and return the first
     * one. If there are non, return null.
     * 
     * @param configElements
     * @return
     */
    protected IConfigurationElement getIntroConfig(String attrributeName,
            String attributeValue) {

        IConfigurationElement[] configElements = registry
                .getConfigurationElementsFor(CONFIG);

        IConfigurationElement config = getConfigurationFromAttribute(
                configElements, attrributeName, attributeValue);

        if (config == null)
            // if there is no valid config, log the fact.
            Logger.logWarning("No Intro configuration found with "
                    + attrributeName + " of value = " + attributeValue);

        return config;
    }

    /**
     * Go through all the configExtension elements and return an array of all
     * extensions that match the attribute and its value. If there are non,
     * return empty array. This also loads all standby contributions.
     */
    protected IConfigurationElement[] getIntroConfigExtensions(
            String attrributeName, String attributeValue) {

        IConfigurationElement[] configExtensionElements = registry
                .getConfigurationElementsFor(CONFIG_EXTENSION);

        IConfigurationElement[] configExtensions = getConfigurationsFromAttribute(
                configExtensionElements, attrributeName, attributeValue);

        return configExtensions;
    }

    /**
     * Add a model to the cache. This method is private because only this
     * manager class knows how to load an intro model.
     * 
     * @param modelId
     * @param model
     */
    protected void addCachedModel(String modelId, IntroModelRoot model) {
        introModels.put(modelId, model);
    }

    /**
     * Gets the given model from the cache.
     * 
     * @param modelId
     */
    protected IntroModelRoot getCachedModel(String configId) {
        return (IntroModelRoot) introModels.get(configId);
    }

    /**
     * Go through all the config elements and only return the correct config
     * with an attribute of the given value. If there is more than one
     * configuration element that maps to the attribute value log the fact, and
     * return the first one. If there are non, return null.
     * 
     * @param configElements
     * @return
     */
    protected IConfigurationElement getConfigurationFromAttribute(
            IConfigurationElement[] configElements, String attributeName,
            String attributeValue) {

        // find all configs with given attribute and attibute value.
        IConfigurationElement[] filteredConfigElements = getConfigurationsFromAttribute(
                configElements, attributeName, attributeValue);
        // now validate that we got only one.
        IConfigurationElement config = validateSingleContribution(filteredConfigElements);
        return config;
    }

    /**
     * Go through all the config elements and return an array of matching
     * configs with an attribute of the given value. If there are non, return
     * empty array.
     */
    protected IConfigurationElement[] getConfigurationsFromAttribute(
            IConfigurationElement[] configElements, String attributeName,
            String attributeValue) {

        // find all configs with given attribute and attibute value.
        Vector elements = new Vector();
        for (int i = 0; i < configElements.length; i++) {
            String currentAttributeValue = configElements[i]
                    .getAttribute(attributeName);
            if (currentAttributeValue != null
                    && currentAttributeValue.equals(attributeValue))
                elements.add(configElements[i]);
        }

        // now return array.
        IConfigurationElement[] filteredConfigElements = new IConfigurationElement[elements
                .size()];
        elements.copyInto(filteredConfigElements);

        return filteredConfigElements;
    }

    /**
     * Loads all shared config extebnsions (ie: standby parts).
     */
    protected void loadSharedExtensions() {
        // simply create model classes for all standbyPart elements under a
        // configExtension.
        IConfigurationElement[] configExtensionElements = registry
                .getConfigurationElementsFor(CONFIG_EXTENSION);
        for (int i = 0; i < configExtensionElements.length; i++) {
            IConfigurationElement element = configExtensionElements[i];
            if (!isValidElementName(element,
                    StandbyPartContent.STANDBY_PART_ELEMENT))
                continue;
            StandbyPartContent standbyPartContent = new StandbyPartContent(
                    element);
            if (standbyPartContent.getId() == null)
                continue;
            standbyParts.put(standbyPartContent.getId(), standbyPartContent);
        }
    }

    /**
     * @return Returns a standbyPart basd on its registred id.
     */
    public StandbyPartContent getStandbyPart(String partId) {
        if (partId == null)
            return null;
        return (StandbyPartContent) standbyParts.get(partId);
    }


    // ========================================
    //   Util Methods for Extensions
    // ========================================

    /**
     * * Utility method to validate an elements name.
     * 
     * @param element
     * @param validName
     * @return
     */
    public static boolean isValidElementName(IConfigurationElement element,
            String validName) {

        if (element.getName().equals(validName))
            return true;
        else
            // bad element name.
            return false;
    }

    /**
     * Utility method to verify that there is only a single configElement in
     * the passed array of elements. If the array is empty, null is returned.
     * If there is more than one element in the array, the first one is picked,
     * but this fact is logged.
     * 
     * @param configElements
     * @return the first configElement in the array, or null if the array is
     *         empty.
     */
    public static IConfigurationElement validateSingleContribution(
            IConfigurationElement[] configElements) {

        IConfigurationElement configElement = null;
        if (configElements.length == 0)
            // No one contributed to extension. return null.
            return null;

        // we should only have one, so use first one.
        configElement = configElements[0];
        Logger.logInfo("Loaded " + configElement.getName() + " from "
                + getLogString(configElement));

        if (configElements.length != 1) {
            // we should only have one! Use first one, but warn in the log.
            configElement = configElements[0];
            for (int i = 1; i < configElements.length; i++)
                // log each extra extension.
                Logger.logWarning(getLogString(configElements[i])
                        + " ignored due to multiple contributions");
        }
        return configElement;
    }

    /**
     * Utility method to return a string to display in .log warnings when we
     * have a bad contribution.
     */
    public static String getLogString(IConfigurationElement element) {
        return "Plugin: "
                + element.getDeclaringExtension()
                        .getDeclaringPluginDescriptor().getUniqueIdentifier()
                + "  Extension: "
                + element.getDeclaringExtension()
                        .getExtensionPointUniqueIdentifier() + "  Element: "
                + element.getName() + " id= "
                + element.getAttribute(ID_ATTRIBUTE);
    }

}