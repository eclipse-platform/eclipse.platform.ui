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
 * The common variable interface shared by <code>ISimpleLaunchVariable</code>s
 * and <code>IContextLaunchVariable</code>s.
 * <p>
 * This interface is not intended to be implemented by clients. Clients
 * should implement either <code>IContextLaunchVariable</code> or
 * <code>ISimpleLaunchVariable</code>.
 * </p>
 * @since 3.0
 */
public interface ILaunchVariable {
	/**
	 * Returns this variable's name.
	 * 
	 * @return variable name
	 */
	public String getName();

	/**
	 * Returns this variable's description.
	 * 
	 * @return variable description
	 */
	public String getDescription();
}