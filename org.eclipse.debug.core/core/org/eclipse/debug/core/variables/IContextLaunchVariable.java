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
 * The expander is given a resource context in which to expand
 * the variable, typically the resource selected in the UI.
 * Plug-ins that wish to contribute variables may do so using
 * the <code>org.eclipse.debug.core.contextLaunchVariables</code>
 * extension point.
 * <p>
 * Extenders must provide a name, expander class and description
 * for the variable.. The expander class, which must implement
 * <code>org.eclipse.debug.core.variables.IVariableExpander</code>,
 * will be queried to compute the variable's value.
 * <p>
 * For example, the following is a definition of a context launch variable that
 * expands to the name of the selected resource.
 * <pre>
 * &lt;extension point="org.eclipse.debug.core.contextLaunchVariables"&gt;
 *   &lt;variable 
 *      name="resource_name"
 *      expanderClass="com.example.ResourceNameExpander"
 *      description="The name of the selected resource"
 *   &lt;/variable&gt;
 * &lt;/extension&gt;
 * </pre>
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
