/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractActivityRegistry implements IActivityRegistry {
	protected List<ActivityRequirementBindingDefinition> activityRequirementBindingDefinitions = Collections
			.emptyList();

	protected List<ActivityDefinition> activityDefinitions = Collections.emptyList();

	protected List<ActivityPatternBindingDefinition> activityPatternBindingDefinitions = Collections.emptyList();

	private ActivityRegistryEvent activityRegistryEvent;

	private List<IActivityRegistryListener> activityRegistryListeners;

	protected List<CategoryActivityBindingDefinition> categoryActivityBindingDefinitions = Collections.emptyList();

	protected List<CategoryDefinition> categoryDefinitions = Collections.emptyList();

	protected List<String> defaultEnabledActivities = Collections.emptyList();

	protected AbstractActivityRegistry() {
	}

	@Override
	public void addActivityRegistryListener(IActivityRegistryListener activityRegistryListener) {
		if (activityRegistryListener == null) {
			throw new NullPointerException();
		}

		if (activityRegistryListeners == null) {
			activityRegistryListeners = new ArrayList<>();
		}

		if (!activityRegistryListeners.contains(activityRegistryListener)) {
			activityRegistryListeners.add(activityRegistryListener);
		}
	}

	protected void fireActivityRegistryChanged() {
		if (activityRegistryListeners != null) {
			for (int i = 0; i < activityRegistryListeners.size(); i++) {
				if (activityRegistryEvent == null) {
					activityRegistryEvent = new ActivityRegistryEvent(this);
				}

				activityRegistryListeners.get(i).activityRegistryChanged(activityRegistryEvent);
			}
		}
	}

	@Override
	public List<ActivityRequirementBindingDefinition> getActivityRequirementBindingDefinitions() {
		return activityRequirementBindingDefinitions;
	}

	@Override
	public List<ActivityDefinition> getActivityDefinitions() {
		return activityDefinitions;
	}

	@Override
	public List<ActivityPatternBindingDefinition> getActivityPatternBindingDefinitions() {
		return activityPatternBindingDefinitions;
	}

	@Override
	public List<CategoryActivityBindingDefinition> getCategoryActivityBindingDefinitions() {
		return categoryActivityBindingDefinitions;
	}

	@Override
	public List<CategoryDefinition> getCategoryDefinitions() {
		return categoryDefinitions;
	}

	@Override
	public void removeActivityRegistryListener(IActivityRegistryListener activityRegistryListener) {
		if (activityRegistryListener == null) {
			throw new NullPointerException();
		}

		if (activityRegistryListeners != null) {
			activityRegistryListeners.remove(activityRegistryListener);
		}
	}

	@Override
	public List<String> getDefaultEnabledActivities() {
		return defaultEnabledActivities;
	}
}
