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

public class LaunchVariableManager implements ILaunchVariableManager {
	
	private static SimpleLaunchVariableRegistry fgSimpleVariableRegistry= new SimpleLaunchVariableRegistry();
	private static ContextLaunchVariableRegistry fgContextVariableRegistry= new ContextLaunchVariableRegistry();
	private static RefreshLaunchVariableRegistry fgRefreshVariableRegistry= new RefreshLaunchVariableRegistry();

	public IContextLaunchVariable getContextVariable(String name) {
		return fgContextVariableRegistry.getVariable(name);
	}

	public IContextLaunchVariable[] getContextVariables() {
		return fgContextVariableRegistry.getVariables();
	}

	public IContextLaunchVariable getRefreshVariable(String name) {
		return fgRefreshVariableRegistry.getVariable(name);
	}

	public IContextLaunchVariable[] getRefreshVariables() {
		return fgRefreshVariableRegistry.getVariables();
	}

	public void addSimpleVariables(ISimpleLaunchVariable[] variables) {
		fgSimpleVariableRegistry.addVariables(variables);
	}

	public void removeSimpleVariables(ISimpleLaunchVariable[] variables) {
		fgSimpleVariableRegistry.removeVariables(variables);
	}

	public ISimpleLaunchVariable getSimpleVariable(String name) {
		return fgSimpleVariableRegistry.getVariable(name);
	}

	public ISimpleLaunchVariable[] getSimpleVariables() {
		return fgSimpleVariableRegistry.getVariables();
	}

}
