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

package org.eclipse.ui.internal.intro.impl.model.loader;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.intro.impl.model.*;

/**
 * Class for handling all shared Intro Config Extensions. These are StandbyPart
 * and Command contributions. Once defined these contributions are visible by
 * all intro configs.
 */

public class SharedConfigExtensionsManager {

    private IExtensionRegistry registry;

    // Holds all standbyPart extensions. Key is id, value is IntroStandbyPart.
    private Hashtable standbyParts = new Hashtable();

    // Holds all command extensions. Key is name, value is IntroURLCommand.
    private Hashtable commands = new Hashtable();

    /*
     * Prevent creation.
     */
    protected SharedConfigExtensionsManager(IExtensionRegistry registry) {
        this.registry = registry;
    }

    /**
     * Loads all shared config extennsions (ie: standby parts and commands.
     */
    protected void loadSharedConfigExtensions() {
        // simply create model classes for all standbyPart elements under a
        // configExtension.
        IConfigurationElement[] configExtensionElements = registry
                .getConfigurationElementsFor(BaseExtensionPointManager.CONFIG_EXTENSION);
        for (int i = 0; i < configExtensionElements.length; i++) {
            IConfigurationElement element = configExtensionElements[i];
            if (!ModelLoaderUtil.isValidElementName(element,
                    IntroStandbyPart.TAG_STANDBY_PART)
                    && !ModelLoaderUtil.isValidElementName(element,
                            IntroURLCommand.TAG_COMMAND))
                // if extension is not a standbypart or command, ignore.
                continue;
            createModelClass(element);
        }
    }


    /**
     * Create an intro standby part or an intro command model class.
     * 
     * @param element
     */
    private void createModelClass(IConfigurationElement element) {
        if (element.getName().equals(IntroStandbyPart.TAG_STANDBY_PART)) {
            IntroStandbyPart standbyPartContent = new IntroStandbyPart(element);
            if (standbyPartContent.getId() == null)
                // no id, ignore.
                return;
            standbyParts.put(standbyPartContent.getId(), standbyPartContent);
        } else {
            IntroURLCommand introURLCommand = new IntroURLCommand(element);
            if (introURLCommand.getName() == null
                    || introURLCommand.getResolvedValue() == null)
                // no name or resolvedValue, ignore.
                return;
            commands.put(introURLCommand.getName(), introURLCommand);
        }
    }



    /**
     * @return Returns a standbyPart basd on its registred id.
     */
    public IntroStandbyPart getStandbyPart(String partId) {
        if (partId == null)
            return null;
        return (IntroStandbyPart) standbyParts.get(partId);
    }

    /**
     * @return Returns the command from its name.
     */
    public IntroURLCommand getCommand(String commandName) {
        if (commandName == null)
            return null;
        return (IntroURLCommand) commands.get(commandName);
    }

}


