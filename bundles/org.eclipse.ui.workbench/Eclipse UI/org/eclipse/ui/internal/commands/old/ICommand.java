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

package org.eclipse.ui.internal.commands.old;

import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.handles.NotDefinedException;

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
	//void addCommandListener(ICommandListener commandListener);

	/**
	 * JAVADOC
	 * 
	 * @throws NotDefinedException
	 * @throws NotHandledException
	 */	
	void execute()
		throws NotDefinedException, NotActiveException;

	/**
	 * TODO temporary method
	 * 
	 * @param event
	 * @throws NotDefinedException
	 * @throws NotHandledException
	 */	
	void execute(Event event)
		throws NotDefinedException, NotActiveException;

	/**
	 * JAVADOC
	 * 
	 * @return
	 * @throws NotDefinedException
	 */	
	String getCategoryId()
		throws NotDefinedException;

	/**
	 * JAVADOC
	 * 
	 * @return
	 * @throws NotDefinedException
	 */	
	String[] getContextIds()
		throws NotDefinedException;
		
	/**
	 * JAVADOC
	 * 
	 * @return
	 * @throws NotDefinedException
	 */	
	String getDescription()
		throws NotDefinedException;
		
	/**
	 * JAVADOC
	 * 
	 * @return
	 * @throws NotDefinedException
	 */	
	Sequence[] getGestureSequences()
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
	 * @throws NotDefinedException
	 */	
	Sequence[] getKeySequences()
		throws NotDefinedException;	
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 * @throws NotDefinedException
	 */	
	String getName()
		throws NotDefinedException;	
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 * @throws NotDefinedException
	 */	
	String getPluginId()
		throws NotDefinedException;

	/**
	 * JAVADOC
	 * 
	 * @param propertyName
	 * @return
	 * @throws NotDefinedException
	 * @throws NotHandledException
	 */	
	Object getProperty(String propertyName)
		throws NotDefinedException, NotActiveException;

	/**
	 * JAVADOC
	 * 
	 * @return
	 * @throws NotDefinedException
	 * @throws NotHandledException
	 */	
	String[] getPropertyNames()
		throws NotDefinedException, NotActiveException;

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	boolean isDefined();

	/**
	 * TODO temporary method
	 *
	 * @throws NotDefinedException
	 * @throws NotHandledException
	 */	
	boolean isEnabled()
		throws NotDefinedException, NotActiveException;
	
	/**
	 * JAVADOC
	 * 
	 * @return
	 * @throws NotDefinedException
	 */	
	boolean isHandled()
		throws NotDefinedException;

	/**
	 * Unregisters an ICommandListener instance with this command.
	 *
	 * @param commandListener the ICommandListener instance to unregister.
	 */
	//void removeCommandListener(ICommandListener commandListener);
}
