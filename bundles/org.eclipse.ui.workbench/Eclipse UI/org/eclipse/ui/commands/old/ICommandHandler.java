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

package org.eclipse.ui.commands.old;

import org.eclipse.swt.widgets.Event;

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
public interface ICommandHandler {

	/**
	 * Registers an IPropertyListener instance with this property.
	 *
	 * @param propertyListener the IPropertyListener instance to register.
	 */	
	void addPropertyListener(IPropertyListener propertyListener);

	/**
	 * TODO javadoc
	 */	
	void execute();

	/**
	 * TODO temporary method
	 */	
	void execute(Event event);

	/**
	 * TODO javadoc
	 * 
	 * @param name
	 * @return
	 */	
	Object getProperty(String name)
		throws Exception;

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */	
	String[] getPropertyNames()
		throws Exception;

	/**
	 * TODO temporary method
	 */	
	boolean isEnabled();
	
	/**
	 * Unregisters an IPropertyListener instance with this property.
	 *
	 * @param propertyListener the IPropertyListener instance to unregister.
	 */
	void removePropertyListener(IPropertyListener propertyListener);
}
