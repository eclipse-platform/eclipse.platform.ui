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
package org.eclipse.debug.internal.core.stringsubstitution;

import org.eclipse.core.runtime.CoreException;

/**
 * A variable that can be referenced in an expression, which resolves to a string
 * value. Variables are referenced in expressions via there name, in the following
 * format.
 * <pre>
 * ${varname} or ${varname:argument}
 * </pre>
 * <p>
 * A variable is identified by its name, and optionally accepts an argument. When an
 * argument is present, a colon seperates the variable name from its argument.
 * </p>
 * <p>
 * Variables can be contributed by extensions or programmatically. A variable's value
 * is resolved by consulting its resolver (as defined by an extension). If a resolver
 * is not provided for a variable extension, its value is determined to be the last
 * value set for that variable.
 * </p>
 * @since 3.0
 */
public interface IStringVariable {

	/**
	 * Returns the name of this variable. A variable is uniquely identified by
	 * its name.
	 * 
	 * @return variable name
	 */
	public String getName();
	
	/**
	 * Returns a human readable description of this variable, possibly <code>null</code>
	 * 
	 * @return a description of this variable, or <code>null</code> if none
	 */
	public String getDescription();
	
	/**
	 * Returns whether this variable accepts its value to be set, via 
	 * <code>setValue(String)</code>. If a variable is context sensitive,
	 * it generally does not accept its value to be set.
	 * 
	 * @return whether the value of this variable can be set
	 */
	public boolean acceptsValue();
	
	/**
	 * Sets the value of this variable to the given String. Has no effect if
	 * this variable does not accept values. A value of <code>null</code> indicates
	 * the value of this variable is undefined.
	 * 
	 * @param value variable value, possibly <code>null</code>
	 */
	public void setValue(String value);
	
	/**
	 * Returns the value of this variable when referenced with the given
	 * argument, possibly <code>null</code>.
	 * 
	 * @param argument argument present in variable expression or <code>null</code>
	 *   if none
	 * @return value of this variable when referenced with the given argument, possibly
	 *   <code>null</code>
	 * @throws CoreException if unable to resolve a value for this variable
	 */
	public String getValue(String argument) throws CoreException;
}
