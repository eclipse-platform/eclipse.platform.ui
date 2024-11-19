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
import org.eclipse.core.expressions.Expression;
import org.eclipse.ui.internal.util.Util;

public final class ActivityDefinition implements Comparable<ActivityDefinition> {
	private static final int HASH_FACTOR = 89;

	private static final int HASH_INITIAL = ActivityDefinition.class.getName().hashCode();

	static Map<String, ActivityDefinition> activityDefinitionsById(Collection<ActivityDefinition> activityDefinitions,
			boolean allowNullIds) {
		if (activityDefinitions == null) {
			throw new NullPointerException();
		}

		Map<String, ActivityDefinition> map = new HashMap<>();
		Iterator<ActivityDefinition> iterator = activityDefinitions.iterator();

		while (iterator.hasNext()) {
			ActivityDefinition activityDefinition = iterator.next();
			String id = activityDefinition.getId();

			if (allowNullIds || id != null) {
				map.put(id, activityDefinition);
			}
		}

		return map;
	}

	static Map<String, Collection<ActivityDefinition>> activityDefinitionsByName(
			Collection<ActivityDefinition> activityDefinitions, boolean allowNullNames) {
		if (activityDefinitions == null) {
			throw new NullPointerException();
		}

		Map<String, Collection<ActivityDefinition>> map = new HashMap<>();
		Iterator<ActivityDefinition> iterator = activityDefinitions.iterator();

		while (iterator.hasNext()) {
			ActivityDefinition activityDefinition = iterator.next();
			String name = activityDefinition.getName();

			if (allowNullNames || name != null) {
				Collection<ActivityDefinition> activityDefinitions2 = map.get(name);

				if (activityDefinitions2 == null) {
					activityDefinitions2 = new HashSet<>();
					map.put(name, activityDefinitions2);
				}

				activityDefinitions2.add(activityDefinition);
			}
		}

		return map;
	}

	private transient int hashCode = HASH_INITIAL;

	private String id;

	private String name;

	private String sourceId;

	private String description;

	private transient String string;

	private Expression enabledWhen;

	public ActivityDefinition(String id, String name, String sourceId, String description) {
		this.id = id;
		this.name = name;
		this.sourceId = sourceId;
		this.description = description;
	}

	@Override
	public int compareTo(ActivityDefinition object) {
		int compareTo = Util.compare(id, object.id);

		if (compareTo == 0) {
			compareTo = Util.compare(name, object.name);

			if (compareTo == 0) {
				compareTo = Util.compare(sourceId, object.sourceId);
			}
		}

		return compareTo;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ActivityDefinition)) {
			return false;
		}

		final ActivityDefinition castedObject = (ActivityDefinition) object;
		return Objects.equals(id, castedObject.id) && Objects.equals(name, castedObject.name)
				&& Objects.equals(sourceId, castedObject.sourceId);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSourceId() {
		return sourceId;
	}

	@Override
	public int hashCode() {
		if (hashCode == HASH_INITIAL) {
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(name);
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
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(',');
			stringBuffer.append(sourceId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}

	public String getDescription() {
		return description;
	}

	void setEnabledWhen(Expression expression) {
		enabledWhen = expression;
	}

	public Expression getEnabledWhen() {
		return enabledWhen;
	}
}
