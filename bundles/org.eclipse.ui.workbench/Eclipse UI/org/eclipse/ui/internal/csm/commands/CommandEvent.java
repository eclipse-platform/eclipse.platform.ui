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

package org.eclipse.ui.internal.csm.commands;

import org.eclipse.ui.internal.csm.commands.api.ICommand;
import org.eclipse.ui.internal.csm.commands.api.ICommandEvent;

final class CommandEvent implements ICommandEvent {

	private boolean activeChanged;
	private ICommand command;
	private boolean definedChanged;
	private boolean descriptionChanged;
	private boolean enabledChanged;
	private boolean nameChanged;
	private boolean parentIdChanged;
	private boolean patternBindingsChanged;

	CommandEvent(ICommand command, boolean activeChanged, boolean definedChanged, boolean descriptionChanged, boolean enabledChanged, boolean nameChanged, boolean parentIdChanged, boolean patternBindingsChanged) {
		if (command == null)
			throw new NullPointerException();
		
		this.command = command;
		this.activeChanged = activeChanged;
		this.definedChanged = definedChanged;
		this.descriptionChanged = descriptionChanged;
		this.enabledChanged = enabledChanged;
		this.nameChanged = nameChanged;
		this.parentIdChanged = parentIdChanged;		
		this.patternBindingsChanged = patternBindingsChanged;		
	}

	public ICommand getCommand() {
		return command;
	}

	public boolean hasActiveChanged() {
		return activeChanged;
	}
	
	public boolean hasDefinedChanged() {
		return definedChanged;
	}	
	
	public boolean hasDescriptionChanged() {
		return descriptionChanged;
	}
	
	public boolean hasEnabledChanged() {
		return enabledChanged;
	}

	public boolean hasNameChanged() {
		return nameChanged;
	}

	public boolean hasParentIdChanged() {
		return parentIdChanged;
	}
	
	public boolean havePatternBindingsChanged() {
		return patternBindingsChanged;
	}
}
