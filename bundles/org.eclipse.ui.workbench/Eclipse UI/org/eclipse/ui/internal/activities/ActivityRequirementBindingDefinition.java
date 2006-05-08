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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

public final class ActivityRequirementBindingDefinition {
    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = ActivityRequirementBindingDefinition.class
            .getName().hashCode();

    static Map activityRequirementBindingDefinitionsByActivityId(
            Collection activityRequirementBindingDefinitions) {
        if (activityRequirementBindingDefinitions == null) {
			throw new NullPointerException();
		}

        Map map = new HashMap();
        Iterator iterator = activityRequirementBindingDefinitions.iterator();

        while (iterator.hasNext()) {
            Object object = iterator.next();
            Util.assertInstance(object,
                    ActivityRequirementBindingDefinition.class);
            ActivityRequirementBindingDefinition activityRequirementBindingDefinition = (ActivityRequirementBindingDefinition) object;
            String parentActivityId = activityRequirementBindingDefinition
                    .getActivityId();

            if (parentActivityId != null) {
                Collection activityRequirementBindingDefinitions2 = (Collection) map
                        .get(parentActivityId);

                if (activityRequirementBindingDefinitions2 == null) {
                    activityRequirementBindingDefinitions2 = new HashSet();
                    map.put(parentActivityId,
                            activityRequirementBindingDefinitions2);
                }

                activityRequirementBindingDefinitions2
                        .add(activityRequirementBindingDefinition);
            }
        }

        return map;
    }

    private String requiredActivityId;

    private transient int hashCode = HASH_INITIAL;

    private String activityId;

    private String sourceId;

    private transient String string;

    public ActivityRequirementBindingDefinition(String requiredActivityId,
            String activityId, String sourceId) {
        this.requiredActivityId = requiredActivityId;
        this.activityId = activityId;
        this.sourceId = sourceId;
    }

    public int compareTo(Object object) {
        ActivityRequirementBindingDefinition castedObject = (ActivityRequirementBindingDefinition) object;
        int compareTo = Util.compare(requiredActivityId,
                castedObject.requiredActivityId);

        if (compareTo == 0) {
            compareTo = Util.compare(activityId, castedObject.activityId);

            if (compareTo == 0) {
				compareTo = Util.compare(sourceId, castedObject.sourceId);
			}
        }

        return compareTo;
    }

    public boolean equals(Object object) {
        if (!(object instanceof ActivityRequirementBindingDefinition)) {
			return false;
		}

        final ActivityRequirementBindingDefinition castedObject = (ActivityRequirementBindingDefinition) object;
        if (!Util.equals(requiredActivityId,
                castedObject.requiredActivityId)) {
            return false;
        }
        
        if (!Util.equals(activityId, castedObject.activityId)) {
            return false;
        }
        
        return Util.equals(sourceId, castedObject.sourceId);
    }

    public String getRequiredActivityId() {
        return requiredActivityId;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public int hashCode() {
        if (hashCode == HASH_INITIAL) {
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(requiredActivityId);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(activityId);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(sourceId);
            if (hashCode == HASH_INITIAL) {
				hashCode++;
			}
        }

        return hashCode;
    }

    public String toString() {
        if (string == null) {
            final StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append('[');
            stringBuffer.append(requiredActivityId);
            stringBuffer.append(',');
            stringBuffer.append(activityId);
            stringBuffer.append(',');
            stringBuffer.append(sourceId);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }
}
