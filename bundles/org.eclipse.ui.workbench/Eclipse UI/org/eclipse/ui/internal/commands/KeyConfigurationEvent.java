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

import org.eclipse.ui.internal.commands.api.IKeyConfiguration;
import org.eclipse.ui.internal.commands.api.IKeyConfigurationEvent;

final class KeyConfigurationEvent implements IKeyConfigurationEvent {

	private boolean activeChanged;
	private IKeyConfiguration keyConfiguration;
	private boolean definedChanged;
	private boolean descriptionChanged;
	private boolean nameChanged;
	private boolean parentIdChanged;

	KeyConfigurationEvent(IKeyConfiguration keyConfiguration, boolean activeChanged, boolean definedChanged, boolean descriptionChanged, boolean nameChanged, boolean parentIdChanged) {
		if (keyConfiguration == null)
			throw new NullPointerException();
		
		this.keyConfiguration = keyConfiguration;
		this.activeChanged = activeChanged;
		this.definedChanged = definedChanged;
		this.descriptionChanged = descriptionChanged;
		this.nameChanged = nameChanged;
		this.parentIdChanged = parentIdChanged;
	}

	public IKeyConfiguration getKeyConfiguration() {
		return keyConfiguration;
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
	
	public boolean hasParentIdChanged() {
		return parentIdChanged;
	}
}
