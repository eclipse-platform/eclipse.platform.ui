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
public interface IRole extends Comparable {

	/**
	 * Registers an IRoleListener instance with this role.
	 *
	 * @param roleListener the IRoleListener instance to register.
	 * @throws NullPointerException
	 */	
	void addRoleListener(IRoleListener roleListener);

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getDescription()
		throws NotDefinedException;
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getId();
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getName()
		throws NotDefinedException;

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getParentId()
		throws NotDefinedException;

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	boolean isActive();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	boolean isDefined();
	
	/**
	 * Unregisters an IRoleListener instance with this role.
	 *
	 * @param roleListener the IRoleListener instance to unregister.
	 * @throws NullPointerException
	 */
	void removeRoleListener(IRoleListener roleListener);
}
