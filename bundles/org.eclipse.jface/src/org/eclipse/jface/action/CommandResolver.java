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

package org.eclipse.jface.action;

public class CommandResolver {

	private static CommandResolver instance;

	public static CommandResolver getInstance() {
		if (instance == null)
			instance = new CommandResolver();
			
		return instance;	
	}

	private ICommandResolver commandResolver;

	private CommandResolver() {
	}
	
	public ICommandResolver getCommandResolver() {
		return commandResolver;
	}
	
	public void setCommandResolver(ICommandResolver commandResolver) {
		this.commandResolver = commandResolver;
	}
}
