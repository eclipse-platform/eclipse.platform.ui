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

package org.eclipse.ui.internal.commands;

final class CommandEnvelope implements Comparable {

	static CommandEnvelope create(String command) {
		return new CommandEnvelope(command);
	}

	private String command;
	
	private CommandEnvelope(String command) {
		super();
		this.command = command;
	}
	
	public int compareTo(Object object) {
		CommandEnvelope commandEnvelope = (CommandEnvelope) object;
		return Util.compare(command, commandEnvelope.command);
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof CommandEnvelope))
			return false;

		CommandEnvelope commandEnvelope = (CommandEnvelope) object;	
		return Util.equals(command, commandEnvelope.command);
	}

	String getCommand() {
		return command;	
	}

	public int hashCode() {
		return Util.hashCode(command);
	}
}
