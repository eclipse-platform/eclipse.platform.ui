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

import org.eclipse.ui.internal.csm.roles.RoleManager;

/**
 * <p>
 * This class allows clients to broker instances of <code>IRoleManager</code>. 
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
public final class RoleManagerFactory {

	/**
	 * Creates a new instance of IRoleManager.
	 * 
	 * @return a new instance of IRoleManager. Clients should not make 
	 *         assumptions about the concrete implementation outside the 
	 *         contract of <code>IRoleManager</code>. Guaranteed not to be 
	 *         <code>null</code>.
	 */
	public static IRoleManager getRoleManager() {
		return new RoleManager();
	}

	/**
	 * Private constructor to ensure that <code>RoleManagerFactory</code> can
	 * not be instantiated. 
	 */	
	private RoleManagerFactory() {		
	}
}
