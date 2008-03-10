/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
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
public class ViewRegistryReader extends RegistryReader {
	/**
	 * General view category id 
	 */
	public static String GENERAL_VIEW_ID = "org.eclipse.ui";	//$NON-NLS-1$
	
	private ViewRegistry viewRegistry;

    /**
     * RegistryViewReader constructor comment.
     */
    public ViewRegistryReader() {
        super();
    }

    /**
     * Reads the category element.
     */
    protected void readCategory(IConfigurationElement element) {
        try {
            viewRegistry.add(new Category(element));
        } catch (CoreException e) {
            // log an error since its not safe to show a dialog here
            WorkbenchPlugin.log(
                    "Unable to create view category.", e.getStatus());//$NON-NLS-1$
        }
    }

    /**
     * readElement method comment.
     */
    protected boolean readElement(IConfigurationElement element) {
        String elementName = element.getName();
        if (elementName.equals(IWorkbenchRegistryConstants.TAG_VIEW)) {
            readView(element);
            return true;
        }
        if (elementName.equals(IWorkbenchRegistryConstants.TAG_CATEGORY)) {
            readCategory(element);
            readElementChildren(element);
            return true;
        }
        if (elementName.equals(IWorkbenchRegistryConstants.TAG_STICKYVIEW)) {
            readSticky(element);
            return true;
        }

        return false;
    }

    /**
     * Reads the sticky view element.
     */
    protected void readSticky(IConfigurationElement element) {
        try {
            viewRegistry.add(new StickyViewDescriptor(element));
        } catch (CoreException e) {
            // log an error since its not safe to open a dialog here
            WorkbenchPlugin.log(
                    "Unable to create sticky view descriptor.", e.getStatus());//$NON-NLS-1$

        }
    }

    /**
     * Reads the view element.
     */
    protected void readView(IConfigurationElement element) {
        try {
            viewRegistry.add(new ViewDescriptor(element));
        } catch (CoreException e) {
            // log an error since its not safe to open a dialog here
            WorkbenchPlugin.log(
                    "Unable to create view descriptor.", e.getStatus());//$NON-NLS-1$
        }
    }

    /**
     * Read the view extensions within a registry.
     * @param in the extension registry
     * @param out the view registry
     */
    public void readViews(IExtensionRegistry in, ViewRegistry out) {
        // this does not seem to really ever be throwing an the exception
        viewRegistry = out;
        readRegistry(in, PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_VIEWS);
    }
}
