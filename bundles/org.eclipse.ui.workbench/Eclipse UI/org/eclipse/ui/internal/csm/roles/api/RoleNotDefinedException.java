/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.csm.roles.api;

/**
 * <p>
 * Signals that an attempt was made to access the attributes of an undefined
 * role.
 * </p>
 * <p>
 * Clients are not permitted to extend this class.
 * </p> 
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public final class RoleNotDefinedException extends Exception {

	/**
	 * Constructs a <code>RoleNotDefinedException</code> with no specified 
	 * detail message. 
	 */	
	public RoleNotDefinedException() {
	}

	/**
	 * Constructs a <code>RoleNotDefinedException</code> with the specified 
	 * detail message. 
	 *
	 * @param s	the detail message.
	 */	
	public RoleNotDefinedException(String s) {
		super(s);
	}
}
