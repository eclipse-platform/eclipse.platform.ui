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

import org.eclipse.ui.internal.csm.commands.api.ICommandManager;
import org.eclipse.ui.internal.csm.commands.api.ICommandManagerEvent;

final class CommandManagerEvent implements ICommandManagerEvent {

	private boolean activeCommandIdsChanged;
	private boolean activeKeyConfigurationIdsChanged;
	private ICommandManager commandManager;
	private boolean definedCategoryIdsChanged;
	private boolean definedCommandIdsChanged;
	private boolean definedKeyConfigurationIdsChanged;
	private boolean enabledCommandIdsChanged;

	CommandManagerEvent(ICommandManager commandManager, boolean activeCommandIdsChanged, boolean activeKeyConfigurationIdsChanged, boolean definedCategoryIdsChanged, boolean definedCommandIdsChanged, boolean definedKeyConfigurationIdsChanged, boolean enabledCommandIdsChanged) {
		if (commandManager == null)
			throw new NullPointerException();
		
		this.commandManager = commandManager;
		this.activeCommandIdsChanged = activeCommandIdsChanged;
		this.activeKeyConfigurationIdsChanged = activeKeyConfigurationIdsChanged;
		this.definedCategoryIdsChanged = definedCategoryIdsChanged;		
		this.definedCommandIdsChanged = definedCommandIdsChanged;
		this.definedKeyConfigurationIdsChanged = definedKeyConfigurationIdsChanged;
		this.enabledCommandIdsChanged = enabledCommandIdsChanged;
	}

	public ICommandManager getCommandManager() {
		return commandManager;
	}

	public boolean haveActiveCommandIdsChanged() {
		return activeCommandIdsChanged;
	}

	public boolean haveActiveKeyConfigurationIdsChanged() {
		return activeKeyConfigurationIdsChanged;
	}

	public boolean haveDefinedCategoryIdsChanged() {
		return definedCategoryIdsChanged;
	}
	
	public boolean haveDefinedCommandIdsChanged() {
		return definedCommandIdsChanged;
	}
	
	public boolean haveDefinedKeyConfigurationIdsChanged() {
		return definedKeyConfigurationIdsChanged;
	}
	
	public boolean haveEnabledCommandIdsChanged() {
		return enabledCommandIdsChanged;
	}
}
