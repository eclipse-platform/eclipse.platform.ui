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

import java.util.List;

import org.eclipse.ui.commands.IActiveKeyConfiguration;
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.IContextBinding;
import org.eclipse.ui.commands.IImageBinding;
import org.eclipse.ui.commands.IKeyBinding;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.internal.util.Util;

abstract class AbstractMutableRegistry extends AbstractRegistry implements IMutableRegistry {

	protected AbstractMutableRegistry() {
		super();
	}

	public void setActiveKeyConfigurations(List activeKeyConfigurations) {
		this.activeKeyConfigurations = Util.safeCopy(activeKeyConfigurations, IActiveKeyConfiguration.class);
	}
	
	public void setCategories(List categories) {
		this.categories = Util.safeCopy(categories, ICategory.class);	
	}
	
	public void setCommands(List commands) {
		this.commands = Util.safeCopy(commands, ICommand.class);	
	}

	public void setContextBindings(List contextBindings) {
		this.contextBindings = Util.safeCopy(contextBindings, IContextBinding.class);	
	}

	public void setImageBindings(List imageBindings) {
		this.imageBindings = Util.safeCopy(imageBindings, IImageBinding.class);	
	}
	
	public void setKeyBindings(List keyBindings) {
		this.keyBindings = Util.safeCopy(keyBindings, IKeyBinding.class);	
	}
	
	public void setKeyConfigurations(List keyConfigurations) {
		this.keyConfigurations = Util.safeCopy(keyConfigurations, IKeyConfiguration.class);		
	}
}
