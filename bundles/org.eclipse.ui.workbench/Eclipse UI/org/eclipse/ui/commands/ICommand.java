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
public interface ICommand extends Comparable {

	// TODO ? SortedSet getContextIds(), Map getImageUrisByStyle(), SortedSet getKeySequences()
	
	/**
	 * Registers an ICommandListener instance with this command.
	 *
	 * @param commandListener the ICommandListener instance to register.
	 * @throws NullPointerException
	 */	
	void addCommandListener(ICommandListener commandListener);

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	String getCategoryId()
		throws NotDefinedException;

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	SortedSet getContextBindings()
		throws NotDefinedException;

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
	String getHelpId()
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
	SortedSet getImageBindings()
		throws NotDefinedException;

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	SortedSet getKeyBindings()
		throws NotDefinedException;	
	
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
	boolean isActive();

	/**
	 * JAVADOC
	 * 
	 * @return
	 */	
	boolean isDefined();
	
	/**
	 * Unregisters an ICommandListener instance with this command.
	 *
	 * @param commandListener the ICommandListener instance to unregister.
	 * @throws NullPointerException
	 */
	void removeCommandListener(ICommandListener commandListener);
}
