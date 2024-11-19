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

public final class CategoryActivityBindingDefinition {
	private static final int HASH_FACTOR = 89;

	private static final int HASH_INITIAL = CategoryActivityBindingDefinition.class.getName().hashCode();

	static Map<String, Collection<CategoryActivityBindingDefinition>> categoryActivityBindingDefinitionsByCategoryId(
			Collection<CategoryActivityBindingDefinition> categoryActivityBindingDefinitions) {
		if (categoryActivityBindingDefinitions == null) {
			throw new NullPointerException();
		}

		Map<String, Collection<CategoryActivityBindingDefinition>> map = new HashMap<>();
		Iterator<CategoryActivityBindingDefinition> iterator = categoryActivityBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			CategoryActivityBindingDefinition categoryActivityBindingDefinition = iterator.next();
			String categoryId = categoryActivityBindingDefinition.getCategoryId();

			if (categoryId != null) {
				Collection<CategoryActivityBindingDefinition> categoryActivityBindingDefinitions2 = map.get(categoryId);

				if (categoryActivityBindingDefinitions2 == null) {
					categoryActivityBindingDefinitions2 = new HashSet<>();
					map.put(categoryId, categoryActivityBindingDefinitions2);
				}

				categoryActivityBindingDefinitions2.add(categoryActivityBindingDefinition);
			}
		}

		return map;
	}

	private String activityId;

	private String categoryId;

	private transient int hashCode = HASH_INITIAL;

	private String sourceId;

	private transient String string;

	public CategoryActivityBindingDefinition(String activityId, String categoryId, String sourceId) {
		this.activityId = activityId;
		this.categoryId = categoryId;
		this.sourceId = sourceId;
	}

	public int compareTo(Object object) {
		CategoryActivityBindingDefinition castedObject = (CategoryActivityBindingDefinition) object;
		int compareTo = Util.compare(activityId, castedObject.activityId);

		if (compareTo == 0) {
			compareTo = Util.compare(categoryId, castedObject.categoryId);

			if (compareTo == 0) {
				compareTo = Util.compare(sourceId, castedObject.sourceId);
			}
		}

		return compareTo;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof CategoryActivityBindingDefinition)) {
			return false;
		}

		final CategoryActivityBindingDefinition castedObject = (CategoryActivityBindingDefinition) object;
		return Objects.equals(activityId, castedObject.activityId)
				&& Objects.equals(categoryId, castedObject.categoryId)
				&& Objects.equals(sourceId, castedObject.sourceId);
	}

	public String getActivityId() {
		return activityId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public String getSourceId() {
		return sourceId;
	}

	@Override
	public int hashCode() {
		if (hashCode == HASH_INITIAL) {
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(activityId);
			hashCode = hashCode * HASH_FACTOR + Objects.hashCode(categoryId);
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
			stringBuffer.append(categoryId);
			stringBuffer.append(',');
			stringBuffer.append(sourceId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
