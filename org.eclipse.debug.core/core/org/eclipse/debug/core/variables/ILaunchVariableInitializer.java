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
 * Launch variable initializers compute an initial value for a simple launch
 * variable contributed by an extension, which is not defined with an initial
 * value. This provides a mechnism for programatically computing the initial
 * value of a launch variable.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see org.eclipse.debug.core.variables.ISimpleLaunchVariable
 * @since 3.0
 */
public interface ILaunchVariableInitializer {
	/**
	 * Runs this initializer and returns the computed value.
	 * 
	 * @return the variable value computed by this initializer
	 */
	public String getText();
}
