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

import org.eclipse.ui.commands.IActiveKeyConfigurationDefinition;
import org.eclipse.ui.commands.ICategoryDefinition;
import org.eclipse.ui.commands.ICommandDefinition;
import org.eclipse.ui.commands.IContextBindingDefinition;
import org.eclipse.ui.commands.IImageBindingDefinition;
import org.eclipse.ui.commands.IKeyBindingDefinition;
import org.eclipse.ui.commands.IKeyConfigurationDefinition;
import org.eclipse.ui.internal.util.Util;

abstract class AbstractMutableRegistry extends AbstractRegistry implements IMutableRegistry {

	protected AbstractMutableRegistry() {
		super();
	}

	public void setActiveKeyConfigurationDefinitions(List activeKeyConfigurationDefinitions) {
		this.activeKeyConfigurationDefinitions = Util.safeCopy(activeKeyConfigurationDefinitions, IActiveKeyConfigurationDefinition.class);
	}
	
	public void setCategoryDefinitions(List categoryDefinitions) {
		this.categoryDefinitions = Util.safeCopy(categoryDefinitions, ICategoryDefinition.class);	
	}
	
	public void setCommandDefinitions(List commandDefinitions) {
		this.commandDefinitions = Util.safeCopy(commandDefinitions, ICommandDefinition.class);	
	}

	public void setContextBindingDefinitions(List contextBindingDefinitions) {
		this.contextBindingDefinitions = Util.safeCopy(contextBindingDefinitions, IContextBindingDefinition.class);	
	}

	public void setImageBindingDefinitions(List imageBindingDefinitions) {
		this.imageBindingDefinitions = Util.safeCopy(imageBindingDefinitions, IImageBindingDefinition.class);	
	}
	
	public void setKeyBindingDefinitions(List keyBindingDefinitions) {
		this.keyBindingDefinitions = Util.safeCopy(keyBindingDefinitions, IKeyBindingDefinition.class);	
	}
	
	public void setKeyConfigurationDefinitions(List keyConfigurationDefinitions) {
		this.keyConfigurationDefinitions = Util.safeCopy(keyConfigurationDefinitions, IKeyConfigurationDefinition.class);		
	}
}
