/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.expressions;

import org.eclipse.core.runtime.CoreException;

/**
 * An evaluation context is used to manage a set of objects needed during
 * XML expression evaluation. A context has a parent context, can manage
 * a set of named variables and has a default variable. The default variable 
 * is used during XML expression evaluation if no explicit variable is 
 * referenced.
 * <p>
 * This interface is not intended to be implemented by clients. Clients
 * are allowed to instantiate <code>EvaluationContext</code>. 
 * </p>
 * 
 * @since 3.0
 */
public interface IEvaluationContext {

	/**
	 * Returns the parent context or <code>null</code> if 
	 * this is the root of the evaluation context hierarchy.
	 * 
	 * @return the parent evaluation context or <code>null</code>
	 */
	public IEvaluationContext getParent();
	
	/**
	 * Returns the root evaluation context.
	 * 
	 * @return the root evaluation context
	 */
	public IEvaluationContext getRoot();
	
	/**
	 * Returns the default variable.
	 * 
	 * @return the default variable or <code>null</code> if
	 *  no default variable is managed.
	 */
	public Object getDefaultVariable();
	
	/**
	 * Adds a new named variable to this context. If a variable
	 * with the name already exists the new one overrides the
	 * existing one.
	 * 
	 * @param name the variable's name
	 * @param value the variable's value
	 */
	public void addVariable(String name, Object value);
	
	/**
	 * Removes the variable managed under the given name
	 * from this evaluation context.
	 * 
	 * @param name the variable's name
	 * @return the currently stored value or <code>null</code> if
	 *  the variable doesn't exist
	 */
	public Object removeVariable(String name);
	
	/**
	 * Returns the variable managed under the given name.
	 * 
	 * @param name the variable's name
	 * @return the variable's value or <code>null</code> if the content
	 *  doesn't manage a variable with the given name 
	 */
	public Object getVariable(String name);
	
	/**
	 * Resolves a variable for the given name and arguments. This
	 * method can be used to dynamically resolve variable such as
	 * plug-in descriptors, resources, etc. The method is used
	 * by the <code>resolve</code> expression.
	 * 
	 * @param name the variable to resolve
	 * @param args an object array of arguments used to resolve the
	 *  variable
	 * @return the variable's value or <code>null</code> if no variable
	 *  can be resolved for the given name and arguments
	 * @exception CoreException if an errors occurs while resolving
	 *  the variable
	 */
	public Object resolveVariable(String name, Object[] args) throws CoreException;
}
