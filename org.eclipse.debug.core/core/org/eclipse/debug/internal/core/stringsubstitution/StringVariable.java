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
package org.eclipse.debug.internal.core.stringsubstitution;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Common implementation of context and value variables
 */
public abstract class StringVariable implements IStringVariable {
	
	/**
	 * Variable name
	 */
	private String fName;
	
	/**
	 * Variable description, or <code>null</code>
	 */
	private String fDescription;
	
	/**
	 * Configuration element associated with this variable, or <code>null</code>
	 */
	private IConfigurationElement fConfigurationElement;

	/**
	 * Constructs a new variable with the given name and description.
	 * 
	 * @param name variable name
	 * @param description variable description, or <code>null</code>
	 */
	public StringVariable(String name, String description, IConfigurationElement configurationElement) {
		fName = name;
		fDescription = description;
		fConfigurationElement = configurationElement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariable#getName()
	 */
	public String getName() {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariable#getDescription()
	 */
	public String getDescription() {
		return fDescription;
	}
	
	/**
	 * Returns the configuration element associated with this variable, or <code>null</code>
	 * if none.
	 * 
	 * @return configuration element or <code>null</code>
	 */
	protected IConfigurationElement getConfigurationElement() {
		return fConfigurationElement;
	}

}
