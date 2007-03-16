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
 * A strategy to read working set extensions from the registry.
 */
public class WorkingSetRegistryReader extends RegistryReader {
    

    private WorkingSetRegistry registry;

    /**
     * Create a new instance of this reader.
     */
    public WorkingSetRegistryReader() {
        super();
    }

    /**
     * Create a new instance of this reader.
     * 
     * @param registry the registry to populate
     */
    public WorkingSetRegistryReader(WorkingSetRegistry registry) {
        super();
        this.registry = registry;
    }

    /**
     * Overrides method in RegistryReader.
     * 
     * @see RegistryReader#readElement(IConfigurationElement)
     */
    public boolean readElement(IConfigurationElement element) {
        if (element.getName().equals(IWorkbenchRegistryConstants.TAG_WORKING_SET)) {
            try {
                WorkingSetDescriptor desc = new WorkingSetDescriptor(element);
                registry.addWorkingSetDescriptor(desc);
            } catch (CoreException e) {
                // log an error since its not safe to open a dialog here
                WorkbenchPlugin
                        .log(
                                "Unable to create working set descriptor.", e.getStatus());//$NON-NLS-1$
            }
            return true;
        }

        return false;
    }

    /**
     * Reads the working set extensions within a registry.
     * 
     * @param in the plugin registry to read from
     * @param out the working set registry to store read entries in.
     */
    public void readWorkingSets(IExtensionRegistry in, WorkingSetRegistry out) {
        registry = out;
        readRegistry(in, PlatformUI.PLUGIN_ID,
                IWorkbenchRegistryConstants.PL_WORKINGSETS);
    }
}
