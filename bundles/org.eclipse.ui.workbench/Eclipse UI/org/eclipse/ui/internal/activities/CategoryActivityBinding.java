/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

import java.util.Objects;
import org.eclipse.ui.activities.ICategoryActivityBinding;
import org.eclipse.ui.internal.util.Util;

public final class CategoryActivityBinding implements ICategoryActivityBinding {
	private static final int HASH_FACTOR = 89;

	private static final int HASH_INITIAL = CategoryActivityBinding.class.getName().hashCode();

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

	@Override
	public int compareTo(ICategoryActivityBinding object) {
		CategoryActivityBinding castedObject = (CategoryActivityBinding) object;
		int compareTo = Util.compare(activityId, castedObject.activityId);

		if (compareTo == 0) {
			compareTo = Util.compare(categoryId, castedObject.categoryId);
		}

		return compareTo;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof CategoryActivityBinding)) {
			return false;
		}

		final CategoryActivityBinding castedObject = (CategoryActivityBinding) object;
		return Objects.equals(activityId, castedObject.activityId)
				&& Objects.equals(categoryId, castedObject.categoryId);
	}

	@Override
	public String getActivityId() {
		return activityId;
	}

	@Override
	public String getCategoryId() {
		return categoryId;
	}

	@Override
	public int hashCode() {
		if (hashCode == HASH_INITIAL) {
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(activityId);
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(categoryId);
			if (hashCode == HASH_INITIAL) {
				hashCode++;
			}
		}

		return hashCode;
	}

	@Override
	public String toString() {
		if (string == null) {
			final StringBuilder stringBuffer = new StringBuilder();
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
