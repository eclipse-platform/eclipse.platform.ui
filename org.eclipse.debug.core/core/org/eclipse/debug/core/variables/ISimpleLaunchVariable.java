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
 * A variable that can be assigned a value. Simple variables are
 * key/value pairs that are typically created and managed by the
 * user. Plug-ins that wish to contribute variables may do so using
 * the <code>org.eclipse.debug.core.simpleLaunchVariables</code>
 * extension point.
 * <p>
 * Extenders may provide an initial value, or an initializer class, or
 * neither. The initializer class, which must implement
 * <code>org.eclipse.debug.core.variables.ILaunchVariableInitializer</code>,
 * will be queried for a value if the variable is queried before a value has been set.
 * <p>
 * For example, the following is a definition of a simple launch variable with
 * an initializer.
 * <pre>
 * &lt;extension point="org.eclipse.debug.core.simpleLaunchVariables"&gt;
 *   &lt;variable 
 *      name="FOO_HOME"
 *      initializerClass="com.example.FooInitializer"
 *   &lt;/variable&gt;
 * &lt;/extension&gt;
 * </pre>
 * The following is a definition of a simple launch variable with
 * an initial value.
 * <pre>
 * &lt;extension point="org.eclipse.debug.core.simpleLaunchVariables"&gt;
 *   &lt;variable 
 *      name="FOO_HOME"
 *      initialValue="/usr/local/foo"
 *   &lt;/variable&gt;
 * &lt;/extension&gt;
 * </pre>
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @since 3.0
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
	public String getValue();
	/**
	 * Sets the text value of this variable
	 * @param value the value to assign to this variable
	 */
	public void setValue(String value);
}
