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

package org.eclipse.ui.internal.commands.registry;

import java.util.List;

import org.eclipse.ui.internal.util.Util;

abstract class AbstractMutableCommandRegistry extends AbstractCommandRegistry implements IMutableCommandRegistry {

	protected AbstractMutableCommandRegistry() {
	}

	public void setActiveKeyConfigurationDefinitions(List activeKeyConfigurationDefinitions) {
		activeKeyConfigurationDefinitions = Util.safeCopy(activeKeyConfigurationDefinitions, IActiveKeyConfigurationDefinition.class);	
		
		if (!activeKeyConfigurationDefinitions.equals(this.activeKeyConfigurationDefinitions)) {
			this.activeKeyConfigurationDefinitions = activeKeyConfigurationDefinitions;			
			fireCommandRegistryChanged();
		}
	}
	
	public void setCategoryDefinitions(List categoryDefinitions) {
		categoryDefinitions = Util.safeCopy(categoryDefinitions, ICategoryDefinition.class);	
		
		if (!categoryDefinitions.equals(this.categoryDefinitions)) {
			this.categoryDefinitions = categoryDefinitions;			
			fireCommandRegistryChanged();
		}
	}
	
	public void setCommandDefinitions(List commandDefinitions) {
		commandDefinitions = Util.safeCopy(commandDefinitions, ICommandDefinition.class);	
		
		if (!commandDefinitions.equals(this.commandDefinitions)) {
			this.commandDefinitions = commandDefinitions;			
			fireCommandRegistryChanged();
		}
	}

	public void setContextBindingDefinitions(List contextBindingDefinitions) {
		contextBindingDefinitions = Util.safeCopy(contextBindingDefinitions, IContextBindingDefinition.class);	
		
		if (!contextBindingDefinitions.equals(this.contextBindingDefinitions)) {
			this.contextBindingDefinitions = contextBindingDefinitions;			
			fireCommandRegistryChanged();
		}
	}

	public void setImageBindingDefinitions(List imageBindingDefinitions) {
		imageBindingDefinitions = Util.safeCopy(imageBindingDefinitions, IImageBindingDefinition.class);	
		
		if (!imageBindingDefinitions.equals(this.imageBindingDefinitions)) {
			this.imageBindingDefinitions = imageBindingDefinitions;			
			fireCommandRegistryChanged();
		}	
	}
	
	public void setKeyBindingDefinitions(List keyBindingDefinitions) {
		keyBindingDefinitions = Util.safeCopy(keyBindingDefinitions, IKeyBindingDefinition.class);	
		
		if (!keyBindingDefinitions.equals(this.keyBindingDefinitions)) {
			this.keyBindingDefinitions = keyBindingDefinitions;			
			fireCommandRegistryChanged();
		}
	}
	
	public void setKeyConfigurationDefinitions(List keyConfigurationDefinitions) {
		commandDefinitions = Util.safeCopy(keyConfigurationDefinitions, IKeyConfigurationDefinition.class);	
		
		if (!keyConfigurationDefinitions.equals(this.keyConfigurationDefinitions)) {
			this.keyConfigurationDefinitions = keyConfigurationDefinitions;			
			fireCommandRegistryChanged();
		}	
	}
}
