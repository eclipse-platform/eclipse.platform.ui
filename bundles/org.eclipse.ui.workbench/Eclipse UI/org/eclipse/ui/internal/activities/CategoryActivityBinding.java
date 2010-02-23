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

import org.eclipse.ui.activities.ICategoryActivityBinding;
import org.eclipse.ui.internal.util.Util;

public final class CategoryActivityBinding implements ICategoryActivityBinding {
    private final static int HASH_FACTOR = 89;

    private final static int HASH_INITIAL = CategoryActivityBinding.class
            .getName().hashCode();

    private String activityId;

    private String categoryId;

    private transient int hashCode = HASH_INITIAL;

    private transient String string;

    public CategoryActivityBinding(String activityId, String categoryId) {
        if (activityId == null || categoryId == null) {
			throw new NullPointerException();
		}

        this.activityId = activityId;
        this.categoryId = categoryId;
    }

    public int compareTo(Object object) {
        CategoryActivityBinding castedObject = (CategoryActivityBinding) object;
        int compareTo = Util.compare(activityId, castedObject.activityId);

        if (compareTo == 0) {
			compareTo = Util.compare(categoryId, castedObject.categoryId);
		}

        return compareTo;
    }

    public boolean equals(Object object) {
        if (!(object instanceof CategoryActivityBinding)) {
			return false;
		}

        final CategoryActivityBinding castedObject = (CategoryActivityBinding) object;
        if (!Util.equals(activityId, castedObject.activityId)) {
            return false;
        }

        return Util.equals(categoryId, castedObject.categoryId);
    }

    public String getActivityId() {
        return activityId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public int hashCode() {
        if (hashCode == HASH_INITIAL){
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(activityId);
            hashCode = hashCode * HASH_FACTOR + Util.hashCode(categoryId);
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
            stringBuffer.append(activityId);
            stringBuffer.append(',');
            stringBuffer.append(categoryId);
            stringBuffer.append(']');
            string = stringBuffer.toString();
        }

        return string;
    }
}
