/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.contexts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.ui.internal.util.ConfigurationElementMemento;

final class ExtensionContextRegistry extends AbstractContextRegistry {
    private List contextContextBindingDefinitions;

    private List contextDefinitions;

    private IExtensionRegistry extensionRegistry;

    ExtensionContextRegistry(IExtensionRegistry extensionRegistry) {
        if (extensionRegistry == null)
            throw new NullPointerException();

        this.extensionRegistry = extensionRegistry;

        this.extensionRegistry
                .addRegistryChangeListener(new IRegistryChangeListener() {
                    public void registryChanged(
                            IRegistryChangeEvent registryChangeEvent) {
                        IExtensionDelta[] extensionDeltas = registryChangeEvent
                                .getExtensionDeltas(Persistence.PACKAGE_PREFIX,
                                        Persistence.PACKAGE_BASE);

                        if (extensionDeltas.length != 0)
                            try {
                                load();
                            } catch (IOException eIO) {
                            }
                    }
                });

        try {
            load();
        } catch (IOException eIO) {
        }
    }

    private String getNamespace(IConfigurationElement configurationElement) {
        String namespace = null;

        if (configurationElement != null) {
            IExtension extension = configurationElement.getDeclaringExtension();

            if (extension != null)
                namespace = extension.getNamespace();
        }

        return namespace;
    }

    private void load() throws IOException {
        if (contextContextBindingDefinitions == null)
            contextContextBindingDefinitions = new ArrayList();
        else
            contextContextBindingDefinitions.clear();

        if (contextDefinitions == null)
            contextDefinitions = new ArrayList();
        else
            contextDefinitions.clear();

        // TODO deprecated start
        IConfigurationElement[] deprecatedConfigurationElements = extensionRegistry
                .getConfigurationElementsFor("org.eclipse.ui.acceleratorScopes"); //$NON-NLS-1$

        for (int i = 0; i < deprecatedConfigurationElements.length; i++) {
            IConfigurationElement deprecatedConfigurationElement = deprecatedConfigurationElements[i];
            String name = deprecatedConfigurationElement.getName();

            if ("acceleratorScope".equals(name)) //$NON-NLS-1$
                readContextDefinition(deprecatedConfigurationElement);
        }

        deprecatedConfigurationElements = extensionRegistry
                .getConfigurationElementsFor("org.eclipse.ui.commands"); //$NON-NLS-1$

        for (int i = 0; i < deprecatedConfigurationElements.length; i++) {
            IConfigurationElement deprecatedConfigurationElement = deprecatedConfigurationElements[i];
            String name = deprecatedConfigurationElement.getName();

            if ("scope".equals(name)) //$NON-NLS-1$
                readContextDefinition(deprecatedConfigurationElement);
        }
        // TODO deprecated end

        IConfigurationElement[] configurationElements = extensionRegistry
                .getConfigurationElementsFor(Persistence.PACKAGE_FULL);

        for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement configurationElement = configurationElements[i];
            String name = configurationElement.getName();

            if (Persistence.TAG_CONTEXT.equals(name))
                readContextDefinition(configurationElement);
        }

        boolean contextRegistryChanged = false;

        if (!contextContextBindingDefinitions
                .equals(super.contextContextBindingDefinitions)) {
            super.contextContextBindingDefinitions = Collections
                    .unmodifiableList(contextContextBindingDefinitions);
            contextRegistryChanged = true;
        }

        if (!contextDefinitions.equals(super.contextDefinitions)) {
            super.contextDefinitions = Collections
                    .unmodifiableList(contextDefinitions);
            contextRegistryChanged = true;
        }

        if (contextRegistryChanged)
            fireContextRegistryChanged();
    }

    private void readContextDefinition(
            IConfigurationElement configurationElement) {
        ContextDefinition contextDefinition = Persistence
                .readContextDefinition(new ConfigurationElementMemento(
                        configurationElement),
                        getNamespace(configurationElement));

        if (contextDefinition != null)
            contextDefinitions.add(contextDefinition);
    }
}