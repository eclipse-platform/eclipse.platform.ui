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

/**
 * TODO javadoc
 * 
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 */
public class CommandEvent {

	private ICommand command;

	/**
	 * TODO javadoc
	 * 
	 * @param command
	 * @throws IllegalArgumentException
	 */	
	public CommandEvent(ICommand command)
		throws IllegalArgumentException {		
		super();
		
		if (command == null)
			throw new IllegalArgumentException();
		
		this.command = command;
	}

	/**
	 * TODO javadoc
	 * 
	 * @return
	 */		
	public ICommand getCommand() {
		return command;
	}
}
