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
 * A variable that can be assigned a value.
 */
public interface ISimpleLaunchVariable extends ILaunchVariable {
	/**
	 * Returns the value of this variable. If no value has been assigned
	 * to this variable, it will attempt to use the variable's initializer if
	 * one is defined. If no value is assigned and no initializer can set
	 * the variable's value, returns <code>null</code>.
	 * @return the variable's value or <code>null</code> if none can be
	 * determined
	 */
	public String getText();
	/**
	 * Sets the text value of this variable
	 * @param value the value to assign to this variable
	 */
	public void setText(String value);
}
