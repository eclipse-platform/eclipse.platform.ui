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
package org.eclipse.debug.core.variables;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.DebugPlugin;

/**
 * Abtract representation of launch configuration variables.
 * @since 3.0
 */
public class ContextLaunchVariable implements IContextLaunchVariable {
	
	private String tag;
	private String description;
	private IConfigurationElement element;
	private IVariableExpander expander;

	/**
	 * Creates an variable definition
	 * 
	 * @param tag the variable tag
	 * @param description a short description of what the variable will expand to
	 * @param element the configuration element
	 */
	public ContextLaunchVariable(String tag, String description, IConfigurationElement element) {
		super();
		this.tag = tag;
		this.description = description;
		this.element = element;
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
			return element.createExecutableExtension(attributeName);
		} catch (CoreException e) {
			DebugPlugin.log(e.getStatus());
			return null;
		}
	}
	
	/**
	 * @see IContextLaunchVariable#getDescription()
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * @see IContextLaunchVariable#getName()
	 */
	public final String getName() {
		return tag;
	}
}
