package org.eclipse.ui.externaltools.internal.registry;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;

/**
 * Registry of all available argument variables.
 */
public class ArgumentVariableRegistry extends ExternalToolVariableRegistry {

	/**
	 * Creates the registry and loads the variables.
	 */
	public ArgumentVariableRegistry() {
		super(IExternalToolConstants.PL_ARGUMENT_VARIABLES);
	}

	/**
	 * Returns the argument variable for the given tag
	 * or <code>null</code> if none.
	 */
	public ArgumentVariable getArgumentVariable(String tag) {
		return (ArgumentVariable) findVariable(tag);
	}
	
	/**
	 * Returns the list of argument variables in the registry.
	 */
	public ArgumentVariable[] getArgumentVariables() {
		ArgumentVariable[] results = new ArgumentVariable[getVariableCount()];
		copyVariables(results);
		return results;
	}
	
	/* (non-Javadoc)
	 * Method declared on ExternalToolVariableRegistry.
	 */
	protected ExternalToolVariable newVariable(String tag, String description, IConfigurationElement element) {
		return new ArgumentVariable(tag, description, element);
	}
}
