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
public interface IRoleActivationService {

	/**
	 * JAVADOC
	 *
	 * @param roleId
	 * @throws NullPointerException
	 */	
	void activateRole(String roleId);

	/**
	 * Registers an IRoleActivationServiceListener instance with this role activation service.
	 *
	 * @param roleActivationServiceListener the IRoleActivationServiceListener instance to register.
	 * @throws NullPointerException
	 */	
	void addRoleActivationServiceListener(IRoleActivationServiceListener roleActivationServiceListener);

	/**
	 * JAVADOC
	 *
	 * @param roleId
	 * @throws NullPointerException
	 */	
	void deactivateRole(String roleId);
		
	/**
	 * JAVADOC
	 *
	 * @return
	 */
	SortedSet getActiveRoleIds();
	
	/**
	 * Unregisters an IRoleActivationServiceListener instance with this role activation services.
	 *
	 * @param roleActivationServiceListener the IRoleActivationServiceListener instance to unregister.
	 * @throws NullPointerException
	 */
	void removeRoleActivationServiceListener(IRoleActivationServiceListener roleActivationServiceListener);
}
