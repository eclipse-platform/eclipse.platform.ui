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

import org.eclipse.ui.internal.csm.commands.api.ICategory;
import org.eclipse.ui.internal.csm.commands.api.ICategoryEvent;

final class CategoryEvent implements ICategoryEvent {

	private ICategory category;
	private boolean definedChanged;
	private boolean descriptionChanged;
	private boolean nameChanged;

	CategoryEvent(ICategory category, boolean definedChanged, boolean descriptionChanged, boolean nameChanged) {
		if (category == null)
			throw new NullPointerException();
		
		this.category = category;
		this.definedChanged = definedChanged;
		this.descriptionChanged = descriptionChanged;
		this.nameChanged = nameChanged;
	}

	public ICategory getCategory() {
		return category;
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
}
