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
 * Variables can be contributed by extensions or programmatically. There are two
 * kinds of variables.
 * <ul>
 * <li><code>IValueVariable</code> - variables that have a value (with getter and setter), and
 *       accept no arguments</li>
 * <li><code>IContextVariable</code> - variables whose value is determined by a combination of
 *       the context in which they are referenced and optionally accept an argument.
 * </ul>
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
	
}
