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
 * Resolves the value for a context variable. A context variable extension
 * contributes a resolver for context sensitive variables, which must implement this
 * interface.
 * 
 * @since 3.0
 */
public interface IContextVariableResolver {

	/**
	 * Resolves and returns a value for the specified variable when referenced
	 * with the given argument, possibly <code>null</code>
	 *  
	 * @param variable variable to resolve a value for
	 * @param argument argument present in expression or <code>null</code> if none
	 * @return variable value, possibly <code>null</code>
	 * @throws CoreException if unable to resolve a value for the given variable
	 */
	public String resolveValue(IContextVariable variable, String argument) throws CoreException;
}
