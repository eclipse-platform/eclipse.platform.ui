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

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.variables.ILaunchVariableInitializer;
import org.eclipse.debug.core.variables.ISimpleLaunchVariable;

/**
 * Implementation of a simple launch variable.
 * 
 * @since 3.0
 */

public class SimpleLaunchVariable extends LaunchVariable implements ISimpleLaunchVariable {
	
	private ILaunchVariableInitializer fVariableInitializer= null;
	private String fValue= null;
	
	/**
	 * Creates a new launch configuration variable with the given initializer or <code>null</code>
	 * if none is available.
	 * @param initialValue the variable's initial value or <code>null</code> if no initial value
	 * should be set.
	 * @param description the variable's description or <code>null</code> if no
	 * description is specified.
	 * @param element configuration element if this variable was defined by an extension point,
	 * 	otherwise <code>null</code>
	 */
	public SimpleLaunchVariable(String name, String initialValue, String description, IConfigurationElement element) {
		super(name, description, element);
		fValue= initialValue;
	}
	
	/**
	 * Creates a new launch configuration varible with the given name.
	 * @param name
	 */
	public SimpleLaunchVariable(String name) {
		this(name, null, null, null);
	}
	
	/**
	 * Instantiates and returns this variable's initializer, if any.
	 * 
	 * @return variable initializer or <code>null</code> if none.
	 */
	public ILaunchVariableInitializer getInitializer() {
		if (hasInitializer()) {
			if (fVariableInitializer == null) {
				try {
					fVariableInitializer = (ILaunchVariableInitializer) getConfigurationElement().createExecutableExtension(SimpleLaunchVariableRegistry.ATTR_INITIALIZER_CLASS);
				} catch (CoreException e) {
					DebugPlugin.logMessage(MessageFormat.format("Failed to load launch variable initializer: {0}", new String[] {getConfigurationElement().getAttribute(SimpleLaunchVariableRegistry.ATTR_INITIALIZER_CLASS)}), e); //$NON-NLS-1$
				}
			}
		}
		return fVariableInitializer;
	}
	
	/**
	 * Returns whehter this variable has an initializer.
	 * 
	 * @return whehter this variable has an initializer
	 */
	protected boolean hasInitializer() {
		return getConfigurationElement() != null && getConfigurationElement().getAttribute(SimpleLaunchVariableRegistry.ATTR_INITIALIZER_CLASS) != null;	
	}

	/**
	 * @see ISimpleLaunchVariable#getText()
	 */
	public String getValue() {
		if (fValue == null && hasInitializer()) {
			fValue= getInitializer().getText();
		}
		return fValue;
	}

	/**
	 * @see ISimpleLaunchVariable#setText(String)
	 */
	public void setValue(String value) {
		fValue= value;
		fireSimpleVariableChanged();
	}

	/**
	 * @see ISimpleLaunchVariable#setDescription(String)
	 */
	public void setDescription(String description) {
		fDescription= description;
		fireSimpleVariableChanged();
	}
	
	/**
	 * Fires notification that this variable has changed.
	 */
	private void fireSimpleVariableChanged() {
		LaunchVariableManager manager = (LaunchVariableManager)DebugPlugin.getDefault().getLaunchVariableManager();
		manager.simpleLaunchVariableChanged(this);
	}

}
