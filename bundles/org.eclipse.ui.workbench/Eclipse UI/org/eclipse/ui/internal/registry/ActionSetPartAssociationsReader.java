/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * A strategy to read action set part association extension from the registry.
 */
public class ActionSetPartAssociationsReader extends RegistryReader {
    private ActionSetRegistry registry;

    /**
     * Creates a new reader.
     */
    public ActionSetPartAssociationsReader() {
        super();
    }

    /**
     * Creates a new reader.
     * 
     * @param registry the registry to populate
     */
    public ActionSetPartAssociationsReader(ActionSetRegistry registry) {
        this.registry = registry;
    }

    /**
     * Process an extension.
     */
    private boolean processExtension(IConfigurationElement element) {
        String actionSetId = element.getAttribute(IWorkbenchRegistryConstants.ATT_TARGET_ID);
        IConfigurationElement[] children = element.getChildren();
        for (int i = 0; i < children.length; i++) {
            IConfigurationElement child = children[i];
            String type = child.getName();
            if (type.equals(IWorkbenchRegistryConstants.TAG_PART)) {
                String partId = child.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
                if (partId != null)
                    registry.addAssociation(actionSetId, partId);
            } else {
                WorkbenchPlugin.log("Unable to process element: " + //$NON-NLS-1$
                        type + " in action set part associations extension: " + //$NON-NLS-1$
                        element.getDeclaringExtension().getUniqueIdentifier());
            }
        }
        return true;
    }

    /**
     * Reads the given element.
     */
    //for dynamic UI - change access from protected to public
    public boolean readElement(IConfigurationElement element) {
        String type = element.getName();
        if (type.equals(IWorkbenchRegistryConstants.TAG_ACTION_SET_ASSOCIATION)) {
            return processExtension(element);
        }
        return false;
    }

    /**
     * Read the association extensions within a registry.
     * 
     * @param in the extension registry to read
     * @param out the registry to populate
     */
    public void readRegistry(IExtensionRegistry in, ActionSetRegistry out) {
        registry = out;
        readRegistry(in, PlatformUI.PLUGIN_ID,
                IWorkbenchConstants.PL_ACTION_SET_PART_ASSOCIATIONS);
    }
}
