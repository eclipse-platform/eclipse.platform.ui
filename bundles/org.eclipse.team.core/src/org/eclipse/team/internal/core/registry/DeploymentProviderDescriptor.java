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
package org.eclipse.team.internal.core.registry;

import org.eclipse.core.runtime.*;
import org.eclipse.team.core.DeploymentProvider;

public class DeploymentProviderDescriptor {
		
	public  static final String ATT_ID = "id"; //$NON-NLS-1$
	public  static final String ATT_NAME = "name"; //$NON-NLS-1$
	public  static final String ATT_CLASS = "class"; //$NON-NLS-1$
	
	private String name;
	private String className;
	private String id;
	private String description;
	
	private IConfigurationElement configElement;
	
	/**
	 * Create a new ViewDescriptor for an extension.
	 */
	public DeploymentProviderDescriptor(IConfigurationElement e, String desc) throws CoreException {
		configElement = e;
		description = desc;
		loadFromExtension();
	}
	
	
	public IConfigurationElement getConfigurationElement() {
		return configElement;
	}

	public DeploymentProvider createProvider() throws CoreException {
		return (DeploymentProvider)configElement.createExecutableExtension(ATT_CLASS);
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	private void loadFromExtension() throws CoreException {
		String identifier = configElement.getAttribute(ATT_ID);
		name = configElement.getAttribute(ATT_NAME);
		className = configElement.getAttribute(ATT_CLASS);
		
		// Sanity check.
		if ((name == null) || (className == null) || (identifier == null)) {
			throw new CoreException(new Status(IStatus.ERROR, configElement.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier(), 0, "Invalid extension (missing label or class name): " + id, //$NON-NLS-1$
							null));
		}
		
		id = identifier;
	}
	
	/**
	 * Returns a string representation of this descriptor. For debugging
	 * purposes only.
	 */
	public String toString() {
		return "Team Provider(" + getId() + ")"; //$NON-NLS-2$ //$NON-NLS-1$
	}
}
