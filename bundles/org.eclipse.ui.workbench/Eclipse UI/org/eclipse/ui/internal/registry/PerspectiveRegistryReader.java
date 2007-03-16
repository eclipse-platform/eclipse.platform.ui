/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * A strategy to read view extensions from the registry.
 */
public class PerspectiveRegistryReader extends RegistryReader {
    private PerspectiveRegistry registry;

    /**
     * RegistryViewReader constructor comment.
     * 
     * @param out the output registry
     */
    public PerspectiveRegistryReader(PerspectiveRegistry out) {
        super();
    	registry = out;
    }

    /**
     * readElement method comment.
     */
    // for dynamic UI - change access from protected to public
    protected boolean readElement(IConfigurationElement element) {
        if (element.getName().equals(IWorkbenchRegistryConstants.TAG_PERSPECTIVE)) {
            try {
                PerspectiveDescriptor desc = new PerspectiveDescriptor(element.getAttribute(IWorkbenchRegistryConstants.ATT_ID), element);
                registry.addPerspective(desc);
            } catch (CoreException e) {
                // log an error since its not safe to open a dialog here
                WorkbenchPlugin.log(
                        "Unable to create layout descriptor.", e.getStatus());//$NON-NLS-1$
            }
            return true;
        }

        return false;
    }

    /**
     * Read the view extensions within a registry.
     * 
     * @param in the registry to read
     */
    public void readPerspectives(IExtensionRegistry in) {
        readRegistry(in, PlatformUI.PLUGIN_ID,
                IWorkbenchRegistryConstants.PL_PERSPECTIVES);
    }
}
