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

import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandEvent;

final class CommandEvent implements ICommandEvent {

	private boolean activeChanged;
	private boolean activityBindingsChanged;
	private boolean categoryIdChanged;
	private ICommand command;
	private boolean definedChanged;
	private boolean descriptionChanged;
	private boolean imageBindingsChanged;
	private boolean keySequenceBindingsChanged;
	private boolean nameChanged;

	CommandEvent(ICommand command, boolean activeChanged, boolean activityBindingsChanged, boolean categoryIdChanged, boolean definedChanged, boolean descriptionChanged, boolean imageBindingsChanged, boolean keySequenceBindingsChanged, boolean nameChanged) {
		if (command == null)
			throw new NullPointerException();
		
		this.command = command;
		this.activeChanged = activeChanged;
		this.activityBindingsChanged = activityBindingsChanged;		
		this.categoryIdChanged = categoryIdChanged;		
		this.definedChanged = definedChanged;
		this.descriptionChanged = descriptionChanged;
		this.imageBindingsChanged = imageBindingsChanged;		
		this.keySequenceBindingsChanged = keySequenceBindingsChanged;		
		this.nameChanged = nameChanged;
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
	
	public boolean hasNameChanged() {
		return nameChanged;
	}

	public boolean hasCategoryIdChanged() {
		return categoryIdChanged;
	}

	public boolean haveActivityBindingsChanged() {
		return activityBindingsChanged;
	}

	public boolean haveImageBindingsChanged() {
		return imageBindingsChanged;
	}

	public boolean haveKeySequenceBindingsChanged() {
		return keySequenceBindingsChanged;
	}
}
