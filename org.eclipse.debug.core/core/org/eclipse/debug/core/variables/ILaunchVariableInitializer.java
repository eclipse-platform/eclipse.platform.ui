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
 * Launch variable initializers compute a value for a simple launch
 * variable. Simple launch variable values are normally specified by the user,
 * but programatically contributed variables may not have a value.
 * The initializer is used to provide a value in this case.
 */
public interface ILaunchVariableInitializer {
	/**
	 * Runs this initializer and returns the computed value.
	 * 
	 * @return the variable value computed by this initializer
	 */
	public String getText();
}
