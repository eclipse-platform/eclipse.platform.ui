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
 * A variable resolver can be used to add additional variable resolving
 * stategies to a {@link EvaluationContext}.
 * 
 * @since 3.0
 * 
 * @see org.eclipse.core.expressions.EvaluationContext#resolveVariable(String, Object[])
 */
public interface IVariableResolver {

	/**
	 * Resolves a variable for the given name and arguments. The
	 * handler is allowed to return <code>null</code> to indicate
	 * that it is not able to resolve the requested variable.
	 * 
	 * @param name the variable to resolve
	 * @param args an object array of arguments used to resolve the
	 *  variable
	 * @return the variable's value or <code>null</code> if no variable
	 *  could be resolved
	 * @exception CoreException if an errors occurs while resolving
	 *  the variable
	 */
	public Object resolve(String name, Object[] args) throws CoreException;
}
