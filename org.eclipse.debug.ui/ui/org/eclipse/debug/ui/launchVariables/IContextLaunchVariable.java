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
package org.eclipse.debug.ui.launchVariables;

/**
 * @since 3.0
 */
public interface IContextLaunchVariable {
	/**
	 * Returns the object that can expand the variable.
	 */
	public IVariableExpander getExpander();	
	/**
	 * Returns the component class to allow
	 * visual editing of the variable's value.
	 */
	public IVariableComponent getComponent();
	/**
	 * Returns the variable's description or <code>null</code> if none
	 * is specified.
	 */
	public String getDescription();
	/**
	 * Returns the variable's name.
	 */
	public String getName();
}
