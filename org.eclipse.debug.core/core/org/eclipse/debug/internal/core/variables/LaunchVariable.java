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
package org.eclipse.debug.internal.core.variables;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.variables.ILaunchVariable;

/**
 * Abstract implementation of launch variable. Common function for simple
 * and context launch variables.
 * 
 * @since 3.0
 */
public abstract class LaunchVariable implements ILaunchVariable {
	
	/**
	 * Variable name
	 */
	private String fName;
	
	/**
	 * Configuration element from plug-in XML, or <code>null</code> if none
	 */
	private IConfigurationElement fElement;
	
	/**
	 * Variable description or <code>null</code> if none
	 */
	protected String fDescription;
			
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ILaunchVariable) {
			return getName().equals(((ILaunchVariable)obj).getName());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getName().hashCode();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.variables.ILaunchVariable#getDescription()
	 */
	public String getDescription() {
		return fDescription != null ? fDescription : ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.variables.ILaunchVariable#getName()
	 */
	public String getName() {
		return fName;
	}
	
	/**
	 * Constructs a new launch variable with the given name, description, and
	 * configuration element.
	 * 
	 * @param name variable name, cannot be <code>null</code>
	 * @param description variable description, possibly <code>null</code>
	 * @param element configuration element, or <code>null</code>
	 */
	public LaunchVariable(String name, String description, IConfigurationElement element) {
		fName = name;
		fDescription = description;
		fElement = element;
	}
		
	/**
	 * Returns the configuration element for this variable, or <code>null</code> if none.
	 * 
	 * @return the configuration element for this variable, or <code>null</code> if none
	 */
	protected IConfigurationElement getConfigurationElement() {
		return fElement;
	}
}
