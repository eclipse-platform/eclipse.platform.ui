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

import org.eclipse.debug.core.variables.IContextLaunchVariable;
import org.eclipse.debug.core.variables.ILaunchVariableManager;
import org.eclipse.debug.core.variables.ISimpleLaunchVariable;

/**
 * Implementation of the debug platform's launch variable manager.
 * 
 * @see LaunchVariableManager
 */
public class LaunchVariableManager implements ILaunchVariableManager {
	
	private static SimpleLaunchVariableRegistry fgSimpleVariableRegistry= new SimpleLaunchVariableRegistry();
	private static ContextLaunchVariableRegistry fgContextVariableRegistry= new ContextLaunchVariableRegistry();
	private static RefreshLaunchVariableRegistry fgRefreshVariableRegistry= new RefreshLaunchVariableRegistry();

	/**
	 * @see ILaunchVariableManager#getContextVariable(String)
	 */
	public IContextLaunchVariable getContextVariable(String name) {
		return fgContextVariableRegistry.getVariable(name);
	}
	
	/**
	 * @see ILaunchVariableManager#getContextVariables()
	 */
	public IContextLaunchVariable[] getContextVariables() {
		return fgContextVariableRegistry.getVariables();
	}

	/**
	 * @see ILaunchVariableManager#getRefreshVariable(String)
	 */
	public IContextLaunchVariable getRefreshVariable(String name) {
		return fgRefreshVariableRegistry.getVariable(name);
	}

	/**
	 * @see ILaunchVariableManager#getRefreshVariables()
	 */
	public IContextLaunchVariable[] getRefreshVariables() {
		return fgRefreshVariableRegistry.getVariables();
	}

	/**
	 * @see ILaunchVariableManager#addSimpleVariables(ISimpleLaunchVariable[])
	 */
	public void addSimpleVariables(ISimpleLaunchVariable[] variables) {
		fgSimpleVariableRegistry.addVariables(variables);
	}

	/**
	 * @see ILaunchVariableManager#removeSimpleVariables(ISimpleLaunchVariable[])
	 */
	public void removeSimpleVariables(ISimpleLaunchVariable[] variables) {
		fgSimpleVariableRegistry.removeVariables(variables);
	}

	/**
	 * @see ILaunchVariableManager#getSimpleVariable(String)
	 */
	public ISimpleLaunchVariable getSimpleVariable(String name) {
		if (fgSimpleVariableRegistry == null) {
			// happens when registry is being initialized
			return null;
		}
		return fgSimpleVariableRegistry.getVariable(name);
	}

	/**
	 * @see ILaunchVariableManager#getSimpleVariables()
	 */
	public ISimpleLaunchVariable[] getSimpleVariables() {
		return fgSimpleVariableRegistry.getVariables();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.variables.ILaunchVariableManager#newSimpleVariable(java.lang.String, java.lang.String, java.lang.String)
	 */
	public ISimpleLaunchVariable newSimpleVariable(String name, String value, String description) {
		return new SimpleLaunchVariable(name, value, description, null);
	}
	
	/**
	 * The value of a simple launch variable has changed - persist its value if
	 * registered.
	 * 
	 * @param variable changed variable
	 */
	protected void simpleLaunchVariableChanged(ISimpleLaunchVariable variable) {
		ISimpleLaunchVariable simpleLaunchVariable = getSimpleVariable(variable.getName());
		if (simpleLaunchVariable != null) {
			fgSimpleVariableRegistry.storeVariables();
		}
	}

}
