/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.variables;

/**
 * Value variable initializers compute an initial value for a value
 * variable contributed by an extension, which is not defined with an initial
 * value. This provides a mechanism for programmatically computing the initial
 * value of a value variable.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 3.0
 */
public interface IValueVariableInitializer {
	/**
	 * Initializes the specified variable. 
	 * 
	 * @param variable variable to initialize
	 */
	public void initialize(IValueVariable variable);
}
