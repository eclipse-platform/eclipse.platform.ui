/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import org.eclipse.ui.internal.util.Util;

public abstract class AbstractMutableCommandRegistry
	extends AbstractCommandRegistry
	implements IMutableCommandRegistry {

	protected AbstractMutableCommandRegistry() {
	    // Nothing to initialize.
	}

	public void setActiveKeyConfigurationDefinitions(List activeKeyConfigurationDefinitions) {
		activeKeyConfigurationDefinitions =
			Util.safeCopy(
				activeKeyConfigurationDefinitions,
				ActiveKeyConfigurationDefinition.class);

		if (!activeKeyConfigurationDefinitions
			.equals(this.activeKeyConfigurationDefinitions)) {
			this.activeKeyConfigurationDefinitions =
				activeKeyConfigurationDefinitions;
			fireCommandRegistryChanged();
		}
	}

	public void setCategoryDefinitions(List categoryDefinitions) {
		categoryDefinitions =
			Util.safeCopy(categoryDefinitions, CategoryDefinition.class);

		if (!categoryDefinitions.equals(this.categoryDefinitions)) {
			this.categoryDefinitions = categoryDefinitions;
			fireCommandRegistryChanged();
		}
	}

	public void setCommandDefinitions(List commandDefinitions) {
		commandDefinitions =
			Util.safeCopy(commandDefinitions, CommandDefinition.class);

		if (!commandDefinitions.equals(this.commandDefinitions)) {
			this.commandDefinitions = commandDefinitions;
			fireCommandRegistryChanged();
		}
	}

	public void setKeyConfigurationDefinitions(List keyConfigurationDefinitions) {
		commandDefinitions =
			Util.safeCopy(
				keyConfigurationDefinitions,
				KeyConfigurationDefinition.class);

		if (!keyConfigurationDefinitions
			.equals(this.keyConfigurationDefinitions)) {
			this.keyConfigurationDefinitions = keyConfigurationDefinitions;
			fireCommandRegistryChanged();
		}
	}

	public void setKeySequenceBindingDefinitions(List keySequenceBindingDefinitions) {
		keySequenceBindingDefinitions =
			Util.safeCopy(
				keySequenceBindingDefinitions,
				KeySequenceBindingDefinition.class);

		if (!keySequenceBindingDefinitions
			.equals(this.keySequenceBindingDefinitions)) {
			this.keySequenceBindingDefinitions = keySequenceBindingDefinitions;
			fireCommandRegistryChanged();
		}
	}
}
