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

package org.eclipse.ui.internal.csm.activities;

import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.IActivityEvent;

final class ActivityEvent implements IActivityEvent {

	private boolean activeChanged;
	private IActivity activity;
	private boolean definedChanged;
	private boolean descriptionChanged;
	private boolean enabledChanged;
	private boolean nameChanged;
	private boolean parentIdChanged;
	private boolean patternBindingsChanged;

	ActivityEvent(IActivity activity, boolean activeChanged, boolean definedChanged, boolean descriptionChanged, boolean enabledChanged, boolean nameChanged, boolean parentIdChanged, boolean patternBindingsChanged) {
		if (activity == null)
			throw new NullPointerException();
		
		this.activity = activity;
		this.activeChanged = activeChanged;
		this.definedChanged = definedChanged;
		this.descriptionChanged = descriptionChanged;
		this.enabledChanged = enabledChanged;
		this.nameChanged = nameChanged;
		this.parentIdChanged = parentIdChanged;		
		this.patternBindingsChanged = patternBindingsChanged;		
	}

	public IActivity getActivity() {
		return activity;
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
