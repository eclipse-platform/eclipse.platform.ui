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
package org.eclipse.core.variables;

/**
 * A variable with a value that can be set and retrieved. The context in which
 * a value variable is referenced does not effect the value of the variable.
 * A value variable can be contributed by an extension or created programmatically.
 * A contributor may optionally specify an initial value for a variable, or
 * provide a delegate that will initialie the variable with a value. 
 * 
 * Example of a value variable contribution with an initial value, the specified
 * variable is created with the initial value "/usr/local/foo".
 * 
 * <p>
 * <pre>
 *  <extension point="org.eclipse.core.variables.valueVariables">
 *   <variable
 *    name="FOO_HOME"
 *    initialValue="/usr/local/foo">
 *   </variable>
 *  </extension>
 * </pre>
 * </p>
 * 
 * Example of a value variable contribution with an initializer class, the class
 * "com.example.FooLocator" will be used to initialize the value the first time
 * it's requested.
 * <p>
 * <pre>
 *  <extension point="org.eclipse.core.variables.valueVariables">
 *   <variable
 *    name="FOO_HOME"
 *    initializerClass="com.example.FooLocator">
 *   </variable>
 *  </extension>
 * </pre>
 * </p>
 * 
 * 
 * @since 3.0
 */
public interface IValueVariable extends IStringVariable {

	/**
	 * Sets the value of this variable to the given value. A value of
	 * <code>null</code> indicates the value of this variable is undefined.
	 * 
	 * @param value variable value, possibly <code>null</code>
	 */
	public void setValue(String value);
	
	/**
	 * Returns the value of this variable, or <code>null</code> if none.
	 * 
	 * @return the value of this variable, or <code>null</code> if none
	 */
	public String getValue();
	
	/**
	 * Returns whether this variable was contributed by an extension.
	 * 
	 * @return whether this variable was contributed by an extension
	 */
	public boolean isContributed();
	
	/**
	 * Sets the description of this variable to the given value.
	 * 
	 * @param description variable description, possibly <code>null</code>
	 */
	public void setDescription(String description);	
}
