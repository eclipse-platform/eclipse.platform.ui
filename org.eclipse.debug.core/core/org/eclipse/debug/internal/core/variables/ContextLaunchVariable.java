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


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.variables.DefaultVariableExpander;
import org.eclipse.debug.core.variables.IContextLaunchVariable;
import org.eclipse.debug.core.variables.IVariableExpander;

/**
 * Abstract representation of launch configuration variables.
 * @since 3.0
 */
public class ContextLaunchVariable extends LaunchVariable implements IContextLaunchVariable {
	
	private IVariableExpander expander;

	/**
	 * Creates an variable definition
	 * 
	 * @param name the variable name
	 * @param description a short description of what the variable will expand to
	 * @param element the configuration element
	 */
	public ContextLaunchVariable(String name, String description, IConfigurationElement element) {
		super(name, description, element);
	}
	
	/**
	 * @see IContextLaunchVariable
	 */
	public IVariableExpander getExpander() {
		if (expander == null) {
			try {
				expander = (IVariableExpander) createObject(ContextLaunchVariableRegistry.TAG_EXPANDER_CLASS);
			} catch (ClassCastException exception) {
			}
			if (expander == null) {
				return DefaultVariableExpander.getDefault();
			}
		}
		return expander;
	}
	
	/**
	 * Creates an instance of the class specified by
	 * the given element attribute name. Can return
	 * <code>null</code> if none or if problems creating
	 * the instance.
	 */
	protected final Object createObject(String attributeName) {
		try {
			return getConfigurationElement().createExecutableExtension(attributeName);
		} catch (CoreException e) {
			DebugPlugin.log(e.getStatus());
			return null;
		}
	}
	
}
