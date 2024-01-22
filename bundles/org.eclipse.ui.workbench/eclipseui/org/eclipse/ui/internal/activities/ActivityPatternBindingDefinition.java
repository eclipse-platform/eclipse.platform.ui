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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import org.eclipse.ui.internal.util.Util;

public final class ActivityPatternBindingDefinition {
	private static final int HASH_FACTOR = 89;

	private static final int HASH_INITIAL = ActivityPatternBindingDefinition.class.getName().hashCode();

	static Map<String, Collection<ActivityPatternBindingDefinition>> activityPatternBindingDefinitionsByActivityId(
			Collection<ActivityPatternBindingDefinition> activityPatternBindingDefinitions) {
		if (activityPatternBindingDefinitions == null) {
			throw new NullPointerException();
		}

		Map<String, Collection<ActivityPatternBindingDefinition>> map = new HashMap<>();
		Iterator<ActivityPatternBindingDefinition> iterator = activityPatternBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			ActivityPatternBindingDefinition activityPatternBindingDefinition = iterator.next();
			String activityId = activityPatternBindingDefinition.getActivityId();

			if (activityId != null) {
				Collection<ActivityPatternBindingDefinition> activityPatternBindingDefinitions2 = map.get(activityId);

				if (activityPatternBindingDefinitions2 == null) {
					activityPatternBindingDefinitions2 = new ArrayList<>();
					map.put(activityId, activityPatternBindingDefinitions2);
				}

				activityPatternBindingDefinitions2.add(activityPatternBindingDefinition);
			}
		}

		return map;
	}

	private String activityId;

	private transient int hashCode = HASH_INITIAL;

	private String pattern;

	private String sourceId;

	private transient String string;

	/**
	 * If the string is taken "as is", without interpreting it as a regular
	 * expression.
	 */
	private boolean isEqualityPattern;

	public ActivityPatternBindingDefinition(String activityId, String pattern, String sourceId) {
		this(activityId, pattern, sourceId, false);
	}

	public ActivityPatternBindingDefinition(String activityId, String pattern, String sourceId,
			boolean isEqualityPattern) {
		this.activityId = activityId;
		this.pattern = pattern;
		this.sourceId = sourceId;
		this.isEqualityPattern = isEqualityPattern;
	}

	public int compareTo(Object object) {
		ActivityPatternBindingDefinition castedObject = (ActivityPatternBindingDefinition) object;
		int compareTo = Util.compare(activityId, castedObject.activityId);

		if (compareTo == 0) {
			compareTo = Util.compare(pattern, castedObject.pattern);

			if (compareTo == 0) {
				compareTo = Util.compare(isEqualityPattern, castedObject.isEqualityPattern);

				if (compareTo == 0)
					compareTo = Util.compare(sourceId, castedObject.sourceId);
			}
		}

		return compareTo;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ActivityPatternBindingDefinition)) {
			return false;
		}

		final ActivityPatternBindingDefinition castedObject = (ActivityPatternBindingDefinition) object;
		return Objects.equals(activityId, castedObject.activityId) && Objects.equals(pattern, castedObject.pattern)
				&& isEqualityPattern == castedObject.isEqualityPattern
				&& Objects.equals(sourceId, castedObject.sourceId);
	}

	public String getActivityId() {
		return activityId;
	}

	public String getPattern() {
		return pattern;
	}

	public String getSourceId() {
		return sourceId;
	}

	public boolean isEqualityPattern() {
		return isEqualityPattern;
	}

	@Override
	public int hashCode() {
		if (hashCode == HASH_INITIAL) {
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(activityId);
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(pattern);
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
			stringBuffer.append(activityId);
			stringBuffer.append(',');
			stringBuffer.append(isEqualityPattern);
			stringBuffer.append(',');
			stringBuffer.append(pattern);
			stringBuffer.append(',');
			stringBuffer.append(sourceId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
