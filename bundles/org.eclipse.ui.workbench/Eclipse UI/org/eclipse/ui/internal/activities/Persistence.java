/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.activities;

import org.eclipse.ui.IMemento;

final class Persistence {
    final static String PACKAGE_BASE = "activities"; //$NON-NLS-1$

    final static String PACKAGE_FULL = "org.eclipse.ui.activities"; //$NON-NLS-1$

    final static String PACKAGE_PREFIX = "org.eclipse.ui"; //$NON-NLS-1$

    final static String TAG_ACTIVITY = "activity"; //$NON-NLS-1$	

    final static String TAG_ACTIVITY_REQUIREMENT_BINDING = "activityRequirementBinding"; //$NON-NLS-1$

    final static String TAG_DEFAULT_ENABLEMENT = "defaultEnablement"; //$NON-NLS-1$

    final static String TAG_ACTIVITY_ID = "activityId"; //$NON-NLS-1$	

    final static String TAG_ACTIVITY_PATTERN_BINDING = "activityPatternBinding"; //$NON-NLS-1$	

    final static String TAG_CATEGORY = "category"; //$NON-NLS-1$	

    final static String TAG_CATEGORY_ACTIVITY_BINDING = "categoryActivityBinding"; //$NON-NLS-1$	

    final static String TAG_CATEGORY_ID = "categoryId"; //$NON-NLS-1$

    final static String TAG_REQUIRED_ACTIVITY_ID = "requiredActivityId"; //$NON-NLS-1$		

    final static String TAG_ID = "id"; //$NON-NLS-1$

    final static String TAG_NAME = "name"; //$NON-NLS-1$	

    final static String TAG_PATTERN = "pattern"; //$NON-NLS-1$
    
    final static String TAG_IS_EQUALITY_PATTERN = "isEqualityPattern"; //$NON-NLS-1$

    final static String TAG_SOURCE_ID = "sourceId"; //$NON-NLS-1$

    final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$

    static ActivityRequirementBindingDefinition readActivityRequirementBindingDefinition(
            IMemento memento, String sourceIdOverride) {
        if (memento == null) {
			throw new NullPointerException();
		}

        String childActivityId = memento.getString(TAG_REQUIRED_ACTIVITY_ID);
        String parentActivityId = memento.getString(TAG_ACTIVITY_ID);
        if (childActivityId == null || parentActivityId == null) {
			return null;
		}
        String sourceId = sourceIdOverride != null ? sourceIdOverride : memento
                .getString(TAG_SOURCE_ID);
        return new ActivityRequirementBindingDefinition(childActivityId,
                parentActivityId, sourceId);
    }

    static String readDefaultEnablement(IMemento memento) {
        if (memento == null) {
			throw new NullPointerException();
		}

        return memento.getString(TAG_ID);
    }

    static ActivityDefinition readActivityDefinition(IMemento memento,
            String sourceIdOverride) {
        if (memento == null) {
			throw new NullPointerException();
		}

        String id = memento.getString(TAG_ID);
        if (id == null) {
			return null;
		}
        String name = memento.getString(TAG_NAME);
        if (name == null) {
			return null;
		}
        String description = memento.getString(TAG_DESCRIPTION);
        if (description == null) {
			description = ""; //$NON-NLS-1$
		}
        String sourceId = sourceIdOverride != null ? sourceIdOverride : memento
                .getString(TAG_SOURCE_ID);
        return new ActivityDefinition(id, name, sourceId, description);
    }

    static ActivityPatternBindingDefinition readActivityPatternBindingDefinition(
            IMemento memento, String sourceIdOverride) {
        if (memento == null) {
			throw new NullPointerException();
		}

        String activityId = memento.getString(TAG_ACTIVITY_ID);
        if (activityId == null) {
			return null;
		}
        String pattern = memento.getString(TAG_PATTERN);
        if (pattern == null) {
			return null;
		}
        String sourceId = sourceIdOverride != null ? sourceIdOverride : memento
                .getString(TAG_SOURCE_ID);
        
        final String isEqualityPatternStr = memento.getString(TAG_IS_EQUALITY_PATTERN);
        final boolean isEqualityPattern = (isEqualityPatternStr != null && isEqualityPatternStr
				.equals("true")); //$NON-NLS-1$
        
        return new ActivityPatternBindingDefinition(activityId, pattern,
                sourceId, isEqualityPattern);
    }

    static CategoryActivityBindingDefinition readCategoryActivityBindingDefinition(
            IMemento memento, String sourceIdOverride) {
        if (memento == null) {
			throw new NullPointerException();
		}

        String activityId = memento.getString(TAG_ACTIVITY_ID);
        if (activityId == null) {
			return null;
		}
        String categoryId = memento.getString(TAG_CATEGORY_ID);
        if (categoryId == null) {
			return null;
		}
        String sourceId = sourceIdOverride != null ? sourceIdOverride : memento
                .getString(TAG_SOURCE_ID);
        return new CategoryActivityBindingDefinition(activityId, categoryId,
                sourceId);
    }

    static CategoryDefinition readCategoryDefinition(IMemento memento,
            String sourceIdOverride) {
        if (memento == null) {
			throw new NullPointerException();
		}

        String id = memento.getString(TAG_ID);
        if (id == null) {
			return null;
		}
        String name = memento.getString(TAG_NAME);
        if (name == null) {
			return null;
		}
        String description = memento.getString(TAG_DESCRIPTION);
        if (description == null) {
			description = ""; //$NON-NLS-1$
		}
        String sourceId = sourceIdOverride != null ? sourceIdOverride : memento
                .getString(TAG_SOURCE_ID);
        return new CategoryDefinition(id, name, sourceId, description);
    }

    private Persistence() {
        //no-op
    }
}
