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

package org.eclipse.ui.commands;

import org.eclipse.ui.internal.commands.util.Sequence;

/**
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public interface ICommand {

	/**
	 * Registers an ICommandListener instance with this command.
	 *
	 * @param commandListener the ICommandListener instance to register.
	 */	
	void addCommandListener(ICommandListener commandListener);
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	boolean exists();

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	String getCategoryId()
		throws Exception;

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	String[] getContextIds()
		throws Exception;
		
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	String getDescription()
		throws Exception;
		
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	Sequence[] getGestureSequences()
		throws Exception;

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	IHandler getHandler()
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
	 */	
	Sequence[] getKeySequences()
		throws Exception;	
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	String getName()
		throws Exception;	
	
	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	String getPluginId()
		throws Exception;

	/**
	 * Unregisters an ICommandListener instance with this command.
	 *
	 * @param commandListener the ICommandListener instance to unregister.
	 */
	void removeCommandListener(ICommandListener commandListener);
}
