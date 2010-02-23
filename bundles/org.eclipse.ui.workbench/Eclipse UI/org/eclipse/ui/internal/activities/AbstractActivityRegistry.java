/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractActivityRegistry implements IActivityRegistry {
    protected List activityRequirementBindingDefinitions = Collections.EMPTY_LIST;

    protected List activityDefinitions = Collections.EMPTY_LIST;

    protected List activityPatternBindingDefinitions = Collections.EMPTY_LIST;

    private ActivityRegistryEvent activityRegistryEvent;

    private List activityRegistryListeners;

    protected List categoryActivityBindingDefinitions = Collections.EMPTY_LIST;

    protected List categoryDefinitions = Collections.EMPTY_LIST;

    protected List defaultEnabledActivities = Collections.EMPTY_LIST;

    protected AbstractActivityRegistry() {
    }

    public void addActivityRegistryListener(
            IActivityRegistryListener activityRegistryListener) {
        if (activityRegistryListener == null) {
			throw new NullPointerException();
		}

        if (activityRegistryListeners == null) {
			activityRegistryListeners = new ArrayList();
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

                ((IActivityRegistryListener) activityRegistryListeners.get(i))
                        .activityRegistryChanged(activityRegistryEvent);
            }
        }
    }

    public List getActivityRequirementBindingDefinitions() {
        return activityRequirementBindingDefinitions;
    }

    public List getActivityDefinitions() {
        return activityDefinitions;
    }

    public List getActivityPatternBindingDefinitions() {
        return activityPatternBindingDefinitions;
    }

    public List getCategoryActivityBindingDefinitions() {
        return categoryActivityBindingDefinitions;
    }

    public List getCategoryDefinitions() {
        return categoryDefinitions;
    }

    public void removeActivityRegistryListener(
            IActivityRegistryListener activityRegistryListener) {
        if (activityRegistryListener == null) {
			throw new NullPointerException();
		}

        if (activityRegistryListeners != null) {
			activityRegistryListeners.remove(activityRegistryListener);
		}
    }

    public List getDefaultEnabledActivities() {
        return defaultEnabledActivities;
    }
}
