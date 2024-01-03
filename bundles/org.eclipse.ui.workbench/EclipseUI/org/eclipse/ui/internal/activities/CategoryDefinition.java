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

public final class CategoryDefinition implements Comparable<CategoryDefinition> {
	private static final int HASH_FACTOR = 89;

	private static final int HASH_INITIAL = CategoryDefinition.class.getName().hashCode();

	static Map<String, CategoryDefinition> categoryDefinitionsById(Collection<CategoryDefinition> categoryDefinitions,
			boolean allowNullIds) {
		if (categoryDefinitions == null) {
			throw new NullPointerException();
		}

		Map<String, CategoryDefinition> map = new HashMap<>();
		Iterator<CategoryDefinition> iterator = categoryDefinitions.iterator();

		while (iterator.hasNext()) {
			CategoryDefinition categoryDefinition = iterator.next();
			String id = categoryDefinition.getId();

			if (allowNullIds || id != null) {
				map.put(id, categoryDefinition);
			}
		}

		return map;
	}

	static Map<String, Collection<CategoryDefinition>> categoryDefinitionsByName(
			Collection<CategoryDefinition> categoryDefinitions, boolean allowNullNames) {
		if (categoryDefinitions == null) {
			throw new NullPointerException();
		}

		Map<String, Collection<CategoryDefinition>> map = new HashMap<>();
		Iterator<CategoryDefinition> iterator = categoryDefinitions.iterator();

		while (iterator.hasNext()) {
			CategoryDefinition categoryDefinition = iterator.next();
			String name = categoryDefinition.getName();

			if (allowNullNames || name != null) {
				Collection<CategoryDefinition> categoryDefinitions2 = map.get(name);

				if (categoryDefinitions2 == null) {
					categoryDefinitions2 = new HashSet<>();
					map.put(name, categoryDefinitions2);
				}

				categoryDefinitions2.add(categoryDefinition);
			}
		}

		return map;
	}

	private transient int hashCode = HASH_INITIAL;

	private String id;

	private String name;

	private String sourceId;

	private transient String string;

	private String description;

	public CategoryDefinition(String id, String name, String sourceId, String description) {
		this.id = id;
		this.name = name;
		this.sourceId = sourceId;
		this.description = description;
	}

	@Override
	public int compareTo(CategoryDefinition object) {
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
		if (!(object instanceof CategoryDefinition)) {
			return false;
		}

		final CategoryDefinition castedObject = (CategoryDefinition) object;
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
}
