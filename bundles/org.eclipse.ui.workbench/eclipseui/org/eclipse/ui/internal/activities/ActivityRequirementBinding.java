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
import org.eclipse.ui.activities.IActivityRequirementBinding;
import org.eclipse.ui.internal.util.Util;

public final class ActivityRequirementBinding implements IActivityRequirementBinding {
	private static final int HASH_FACTOR = 89;

	private static final int HASH_INITIAL = ActivityRequirementBinding.class.getName().hashCode();

	private String requiredActivityId;

	private transient int hashCode = HASH_INITIAL;

	private String activityId;

	private transient String string;

	public ActivityRequirementBinding(String requiredActivityId, String activityId) {
		if (requiredActivityId == null || activityId == null) {
			throw new NullPointerException();
		}

		this.requiredActivityId = requiredActivityId;
		this.activityId = activityId;
	}

	@Override
	public int compareTo(IActivityRequirementBinding object) {
		ActivityRequirementBinding castedObject = (ActivityRequirementBinding) object;
		int compareTo = Util.compare(requiredActivityId, castedObject.requiredActivityId);

		if (compareTo == 0) {
			compareTo = Util.compare(activityId, castedObject.activityId);
		}

		return compareTo;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ActivityRequirementBinding)) {
			return false;
		}

		final ActivityRequirementBinding castedObject = (ActivityRequirementBinding) object;
		return Objects.equals(requiredActivityId, castedObject.requiredActivityId)
				&& Objects.equals(activityId, castedObject.activityId);
	}

	@Override
	public String getRequiredActivityId() {
		return requiredActivityId;
	}

	@Override
	public String getActivityId() {
		return activityId;
	}

	@Override
	public int hashCode() {
		if (hashCode == HASH_INITIAL) {
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(requiredActivityId);
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(activityId);
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
			stringBuffer.append(requiredActivityId);
			stringBuffer.append(',');
			stringBuffer.append(activityId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
