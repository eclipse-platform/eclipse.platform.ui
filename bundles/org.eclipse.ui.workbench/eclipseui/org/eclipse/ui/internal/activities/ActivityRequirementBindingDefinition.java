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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import org.eclipse.ui.internal.util.Util;

public final class ActivityRequirementBindingDefinition {
	private static final int HASH_FACTOR = 89;

	private static final int HASH_INITIAL = ActivityRequirementBindingDefinition.class.getName().hashCode();

	static Map<String, Collection<ActivityRequirementBindingDefinition>> activityRequirementBindingDefinitionsByActivityId(
			Collection<ActivityRequirementBindingDefinition> activityRequirementBindingDefinitions) {
		if (activityRequirementBindingDefinitions == null) {
			throw new NullPointerException();
		}

		Map<String, Collection<ActivityRequirementBindingDefinition>> map = new HashMap<>();
		Iterator<ActivityRequirementBindingDefinition> iterator = activityRequirementBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ActivityRequirementBindingDefinition.class);
			ActivityRequirementBindingDefinition activityRequirementBindingDefinition = (ActivityRequirementBindingDefinition) object;
			String parentActivityId = activityRequirementBindingDefinition.getActivityId();

			if (parentActivityId != null) {
				Collection<ActivityRequirementBindingDefinition> activityRequirementBindingDefinitions2 = map
						.get(parentActivityId);

				if (activityRequirementBindingDefinitions2 == null) {
					activityRequirementBindingDefinitions2 = new HashSet<>();
					map.put(parentActivityId, activityRequirementBindingDefinitions2);
				}

				activityRequirementBindingDefinitions2.add(activityRequirementBindingDefinition);
			}
		}

		return map;
	}

	private String requiredActivityId;

	private transient int hashCode = HASH_INITIAL;

	private String activityId;

	private String sourceId;

	private transient String string;

	public ActivityRequirementBindingDefinition(String requiredActivityId, String activityId, String sourceId) {
		this.requiredActivityId = requiredActivityId;
		this.activityId = activityId;
		this.sourceId = sourceId;
	}

	public int compareTo(Object object) {
		ActivityRequirementBindingDefinition castedObject = (ActivityRequirementBindingDefinition) object;
		int compareTo = Util.compare(requiredActivityId, castedObject.requiredActivityId);

		if (compareTo == 0) {
			compareTo = Util.compare(activityId, castedObject.activityId);

			if (compareTo == 0) {
				compareTo = Util.compare(sourceId, castedObject.sourceId);
			}
		}

		return compareTo;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ActivityRequirementBindingDefinition)) {
			return false;
		}

		final ActivityRequirementBindingDefinition castedObject = (ActivityRequirementBindingDefinition) object;
		return Objects.equals(requiredActivityId, castedObject.requiredActivityId)
				&& Objects.equals(activityId, castedObject.activityId)
				&& Objects.equals(sourceId, castedObject.sourceId);
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

	@Override
	public int hashCode() {
		if (hashCode == HASH_INITIAL) {
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(requiredActivityId);
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(activityId);
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(sourceId);
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
			stringBuffer.append(',');
			stringBuffer.append(sourceId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
