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
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.INavigatorTreeContentProvider;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * 
 * @since 3.0
 */
public class NavigatorAbstractContentDescriptor {
	private static final String ATT_ID = "id"; //$NON-NLS-1$
	private static final String ATT_NAME = "name"; //$NON-NLS-1$	
	private static final String ATT_CLASS = "class"; //$NON-NLS-1$	

	private String id;	
	private String name;
	private String className;
	private IConfigurationElement configElement;
	private INavigatorTreeContentProvider contentProvider = null;
	
	/**
	 * Creates a descriptor from a configuration element.
	 * 
	 * @param configElement configuration element to create a descriptor from
	 */
	public NavigatorAbstractContentDescriptor(IConfigurationElement configElement) throws WorkbenchException {
		super();
		this.configElement = configElement;
	}
	public INavigatorTreeContentProvider createContentProvider() {
		if  (contentProvider != null) return contentProvider;

		try {
			contentProvider = (INavigatorTreeContentProvider)WorkbenchPlugin.createExtension(configElement, ATT_CLASS);
		} catch (CoreException exception) {
			WorkbenchPlugin.log("Unable to create content provider: " + //$NON-NLS-1$
				className, exception.getStatus());
		}
		return contentProvider;		
	}
	/**
	 */
	public String getId() {
		return id;
	}
	public String getClassName() {
		return className;
	}
	protected IConfigurationElement getConfigurationElement() {
		return configElement;
	}
	/**
	 */
	public String getName() {
		return name;
	}
	protected void readConfigElement() throws WorkbenchException {
		id = configElement.getAttribute(ATT_ID);
		name = configElement.getAttribute(ATT_NAME);
		className = configElement.getAttribute(ATT_CLASS);

		if (id == null) {
			throw new WorkbenchException("Missing attribute: " +//$NON-NLS-1$
				ATT_ID +
				" in navigator extension: " +//$NON-NLS-1$
				configElement.getDeclaringExtension().getUniqueIdentifier());				
		}
		if (className == null) {
			throw new WorkbenchException("Missing attribute: " +//$NON-NLS-1$
				ATT_CLASS +
				" in navigator extension: " +//$NON-NLS-1$
				configElement.getDeclaringExtension().getUniqueIdentifier());				
		}
	}
}
