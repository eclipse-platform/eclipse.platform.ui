/************************************************************************
Copyright (c) 2002, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * A working set descriptor stores the plugin registry data for 
 * a working set page extension.
 * 
 * @since 2.0
 */
public class WorkingSetDescriptor {
	private String id;
	private String name;
	private String icon;	
	private String pageClassName;
	private IConfigurationElement configElement;

	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_NAME = "name"; //$NON-NLS-1$
	private static final String ATT_ICON = "icon"; //$NON-NLS-1$	
	private static final String ATT_PAGE_CLASS = "pageClass"; //$NON-NLS-1$	

	/**
	 * Creates a descriptor from a configuration element.
	 * 
	 * @param configElement configuration element to create a descriptor from
	 */
	public WorkingSetDescriptor(IConfigurationElement configElement) throws CoreException {
		super();
		this.configElement = configElement;
		id = configElement.getAttribute(ATT_ID);
		name = configElement.getAttribute(ATT_NAME);
		icon = configElement.getAttribute(ATT_ICON);		
		pageClassName = configElement.getAttribute(ATT_PAGE_CLASS);

		if (name == null) {
			throw new CoreException(new Status(
				IStatus.ERROR, 
				WorkbenchPlugin.PI_WORKBENCH, 
				0, 
				"Invalid extension (missing class name): " + id, 		//$NON-NLS-1$
				null));
		}
		if (pageClassName == null) {
			throw new CoreException(new Status(
				IStatus.ERROR,
				WorkbenchPlugin.PI_WORKBENCH,
				0,
				"Invalid extension (missing page class name): " + id,	//$NON-NLS-1$
				null));
		}
	}
	/**
	 * Creates a working set page from this extension descriptor.
	 * 
	 * @return a working set page created from this extension 
	 * 	descriptor.
	 */
	public IWorkingSetPage createWorkingSetPage() {
		Object page = null;

		if (pageClassName != null) {
			try {
				page = WorkbenchPlugin.createExtension(configElement, ATT_PAGE_CLASS);
			} catch (CoreException exception) {
				WorkbenchPlugin.log("Unable to create working set page: " + //$NON-NLS-1$
				pageClassName, exception.getStatus());
			}
		}
		return (IWorkingSetPage) page;
	}
	/**
	 * Returns the page's icon
	 * 
	 * @return the page's icon
	 */
	public ImageDescriptor getIcon() {
		if (icon == null)
			return null;

		return WorkbenchImages.getImageDescriptorFromExtension(
			configElement.getDeclaringExtension(), 
			icon); 
	}
	/**
	 * Returns the working set page id.
	 * 
	 * @return the working set page id.
	 */
	public String getId() {
		return id;
	}
	/**
	 * Returns the working set page class name
	 * 
	 * @return the working set page class name
	 */
	public String getPageClassName() {
		return pageClassName;
	}
	/**
	 * Returns the name of the working set element type the 
	 * page works with.
	 * 
	 * @return the working set element type name
	 */
	public String getName() {
		return name;
	}
}