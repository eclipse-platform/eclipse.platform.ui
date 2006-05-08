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

import org.eclipse.ui.activities.IActivityRequirementBinding;
import org.eclipse.ui.internal.util.Util;

public final class ActivityRequirementBinding implements
        IActivityRequirementBinding {
    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = ActivityRequirementBinding.class
            .getName().hashCode();

    private String requiredActivityId;

    private transient int hashCode = HASH_INITIAL;

    private String activityId;

    private transient String string;

    public ActivityRequirementBinding(String requiredActivityId,
            String activityId) {
        if (requiredActivityId == null || activityId == null) {
			throw new NullPointerException();
		}

        this.requiredActivityId = requiredActivityId;
        this.activityId = activityId;
    }

    public int compareTo(Object object) {
        ActivityRequirementBinding castedObject = (ActivityRequirementBinding) object;
        int compareTo = Util.compare(requiredActivityId,
                castedObject.requiredActivityId);

        if (compareTo == 0) {
			compareTo = Util.compare(activityId, castedObject.activityId);
		}

        return compareTo;
    }

    public boolean equals(Object object) {
        if (!(object instanceof ActivityRequirementBinding)) {
			return false;
		}

        final ActivityRequirementBinding castedObject = (ActivityRequirementBinding) object;
        if (!Util.equals(requiredActivityId, castedObject.requiredActivityId)) {
            return false;
        }

        return Util.equals(activityId, castedObject.activityId);
    }

    public String getRequiredActivityId() {
        return requiredActivityId;
    }

    public String getActivityId() {
        return activityId;
    }

    public int hashCode() {
        if (hashCode == HASH_INITIAL) {
            hashCode = hashCode * HASH_FACTOR
                    + Util.hashCode(requiredActivityId);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(activityId);
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
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }
}
