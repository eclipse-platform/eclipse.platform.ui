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
 * A dynamic variable is a variable whose value is computed dynamically
 * by a resolver at the time a string substitution is performed. A dynamic
 * variable is contributed by an extension.
 * 
 * TODO: example extension
 * 
 * @since 3.0
 */
public interface IDynamicVariable extends IStringVariable {

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
