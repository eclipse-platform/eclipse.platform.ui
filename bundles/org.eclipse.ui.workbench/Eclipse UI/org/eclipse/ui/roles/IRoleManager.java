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

package org.eclipse.ui.roles;

import java.util.List;
import java.util.SortedSet;

/**
 * <p>
 * JAVADOC
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IRoleManager {

	/**
	 * Registers an IRoleManagerListener instance with this role manager.
	 *
	 * @param roleManagerListener the IRoleManagerListener instance to register.
	 * @throws NullPointerException
	 */	
	void addRoleManagerListener(IRoleManagerListener roleManagerListener);

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	List getActiveRoleIds();

	/**
	 * JAVADOC
	 *
	 * @param roleId
	 * @return
	 * @throws NullPointerException
	 */	
	IRole getRole(String roleId);

	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedSet getDefinedRoleIds();
	
	/**
	 * Unregisters an IRoleManagerListener instance with this role manager.
	 *
	 * @param roleManagerListener the IRoleManagerListener instance to unregister.
	 * @throws NullPointerException
	 */
	void removeRoleManagerListener(IRoleManagerListener roleManagerListener);
}
