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

/**
 * A variable whose value is calculated dynamically by an expander.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 3.0
 */
public interface IContextLaunchVariable extends ILaunchVariable {
	/**
	 * Returns the object that can expand this variable.
	 * 
	 * @return variable expander
	 */
	public IVariableExpander getExpander();
}
