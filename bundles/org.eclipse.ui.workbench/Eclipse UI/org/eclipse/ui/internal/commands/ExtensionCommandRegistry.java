/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

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
import org.eclipse.ui.commands.IHandler;

public final class ExtensionCommandRegistry extends AbstractCommandRegistry {

    private IExtensionRegistry extensionRegistry;

    /**
     * The valid handlers read from XML.  This list is <code>null</code> until
     * the first call to <code>load</code>.  After this, it will contain a list
     * of all the handlers read during the most recent call to 
     * <code>load</code>.
     */
    private List handlers;

    public ExtensionCommandRegistry(IExtensionRegistry extensionRegistry) {
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
                                // Do nothing
                            }
                    }
                });

        try {
            load();
        } catch (IOException eIO) {
            // Do nothing
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

        if (handlers == null) {
            handlers = new ArrayList();
        } else {
            handlers.clear();
        }

        IConfigurationElement[] configurationElements = extensionRegistry
                .getConfigurationElementsFor(Persistence.PACKAGE_FULL);

        for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement configurationElement = configurationElements[i];
            String name = configurationElement.getName();

            if (Persistence.TAG_HANDLER.equals(name))
                readHandlerSubmissionDefinition(configurationElement);
        }

        boolean commandRegistryChanged = false;

        if (!handlers.equals(super.handlers)) {
            super.handlers = Collections.unmodifiableList(handlers);
            commandRegistryChanged = true;
        }

        if (commandRegistryChanged)
            fireCommandRegistryChanged();
    }

    /**
     * Reads the handler definition from XML -- creating a proxy to submit to
     * the workbench command support.  If the handler definition is valid, then
     * it will be added to <code>handlers</code> to be picked up later.
     * 
     * @param configurationElement The configuration element from which to read;
     * must not be <code>null</code>.
     */
    private final void readHandlerSubmissionDefinition(
            final IConfigurationElement configurationElement) {
        final IHandler handler = Persistence
                .readHandlerSubmissionDefinition(configurationElement);

        if (handler != null)
            handlers.add(handler);
    }
}
