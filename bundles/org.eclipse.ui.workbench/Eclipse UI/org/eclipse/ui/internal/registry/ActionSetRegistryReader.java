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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * A strategy to read view extensions from the registry.
 */
public class ActionSetRegistryReader extends RegistryReader {
    private ActionSetRegistry registry;

    /**
     * Create a new instance of the reader.
     */
    public ActionSetRegistryReader() {
        super();
    }

    /**
     * Create a new instance of the reader. 
     * 
     * @param registry the registry to read into
     */
    public ActionSetRegistryReader(ActionSetRegistry registry) {
        this.registry = registry;
    }

    /**
     * readElement method comment.
     */
    //for dynamic UI: change access from protected to public
    public boolean readElement(IConfigurationElement element) {
        if (element.getName().equals(IWorkbenchRegistryConstants.TAG_ACTION_SET)) {
            try {
                ActionSetDescriptor desc = new ActionSetDescriptor(element);
                registry.addActionSet(desc);
            } catch (CoreException e) {
                // log an error since its not safe to open a dialog here
                WorkbenchPlugin
                        .log(
                                "Unable to create action set descriptor.", e.getStatus());//$NON-NLS-1$
            }
            return true;
        } 
        return false;
    }

    /**
     * Read the view extensions within a registry.
     * 
     * @param in the registry to read from
     * @param out the registry to read into
     */
    public void readRegistry(IExtensionRegistry in, ActionSetRegistry out) {
        registry = out;
        readRegistry(in, PlatformUI.PLUGIN_ID,
                IWorkbenchConstants.PL_ACTION_SETS);
        out.mapActionSetsToCategories();
    }
}
