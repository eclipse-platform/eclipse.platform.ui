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

package org.eclipse.ui.handles;

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
public interface IHandle {

	/**
	 * Registers an IHandleListener instance with this handle.
	 *
	 * @param handleListener the IHandleListener instance to register.
	 * @throws IllegalArgumentException
	 */	
	void addHandleListener(IHandleListener handleListener)
		throws IllegalArgumentException;
	
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
	 * @throws NotDefinedException;
	 */	
	Object getObject()
		throws NotDefinedException;
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	boolean isDefined();
	
	/**
	 * Unregisters an IHandleListener instance with this handle.
	 *
	 * @param handleListener the IHandleListener instance to unregister.
	 * @throws IllegalArgumentException
	 */
	void removeHandleListener(IHandleListener handleListener)
		throws IllegalArgumentException;
}
