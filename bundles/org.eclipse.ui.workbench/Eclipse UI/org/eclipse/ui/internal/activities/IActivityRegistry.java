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

import java.util.List;

public interface IActivityRegistry {

    void addActivityRegistryListener(
            IActivityRegistryListener activityRegistryListener);

    List getActivityRequirementBindingDefinitions();

    List getActivityDefinitions();

    List getActivityPatternBindingDefinitions();

    List getCategoryActivityBindingDefinitions();

    List getCategoryDefinitions();

    List getDefaultEnabledActivities();

    void removeActivityRegistryListener(
            IActivityRegistryListener activityRegistryListener);
}
