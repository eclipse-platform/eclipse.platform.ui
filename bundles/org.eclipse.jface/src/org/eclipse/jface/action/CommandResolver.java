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

public final class CommandResolver {

	public static interface ICallback {
		
		Integer getAccelerator(String commandId);
	
		String getAcceleratorText(String commandId);

		boolean isActive(String commandId);
	}

	private static CommandResolver instance;

	public static CommandResolver getInstance() {
		if (instance == null)
			instance = new CommandResolver();
			
		return instance;	
	}

	private ICallback commandResolver;

	private CommandResolver() {
	}
	
	public ICallback getCommandResolver() {
		return commandResolver;
	}
	
	public void setCommandResolver(ICallback commandResolver) {
		this.commandResolver = commandResolver;
	}
}
