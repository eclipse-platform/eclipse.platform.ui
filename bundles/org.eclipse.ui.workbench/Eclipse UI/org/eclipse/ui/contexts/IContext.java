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

package org.eclipse.ui.contexts;

/**
 * <p>
 * TODO javadoc
 * </p>
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface IContext {

	/**
	 * Registers an IContextListener instance with this context.
	 *
	 * @param contextListener the IContextListener instance to register.
	 */	
	void addContextListener(IContextListener contextListener);
		
	/**
	 * TODO javadoc
	 * 
	 * @return
	 * @throws Exception
	 */	
	String getDescription()
		throws Exception;
		
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	String getId();
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 * @throws Exception
	 */	
	String getName()
		throws Exception;	

	/**
	 * TODO javadoc
	 * 
	 * @return
	 * @throws Exception
	 */	
	String getParentId()
		throws Exception;
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 * @throws Exception
	 */	
	String getPluginId()
		throws Exception;

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	boolean isActive()
		throws Exception;

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	boolean isDefined();

	/**
	 * Unregisters an IContextListener instance with this context.
	 *
	 * @param contextListener the IContextListener instance to unregister.
	 */
	void removeContextListener(IContextListener contextListener);
}
