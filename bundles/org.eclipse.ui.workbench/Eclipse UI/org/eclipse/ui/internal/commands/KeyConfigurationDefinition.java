/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.internal.util.Util;

public final class KeyConfigurationDefinition
	implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		KeyConfigurationDefinition.class.getName().hashCode();

	public static Map keyConfigurationDefinitionsById(
		Collection keyConfigurationDefinitions,
		boolean allowNullIds) {
		if (keyConfigurationDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = keyConfigurationDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, KeyConfigurationDefinition.class);
			KeyConfigurationDefinition keyConfigurationDefinition =
				(KeyConfigurationDefinition) object;
			String id = keyConfigurationDefinition.getId();

			if (allowNullIds || id != null)
				map.put(id, keyConfigurationDefinition);
		}

		return map;
	}

	public static Map keyConfigurationDefinitionsByName(
		Collection keyConfigurationDefinitions,
		boolean allowNullNames) {
		if (keyConfigurationDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = keyConfigurationDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, KeyConfigurationDefinition.class);
			KeyConfigurationDefinition keyConfigurationDefinition =
				(KeyConfigurationDefinition) object;
			String name = keyConfigurationDefinition.getName();

			if (allowNullNames || name != null) {
				Set keyConfigurationDefinitions2 = (Set) map.get(name);

				if (keyConfigurationDefinitions2 == null) {
					keyConfigurationDefinitions2 = new HashSet();
					map.put(name, keyConfigurationDefinitions2);
				}

				keyConfigurationDefinitions2.add(keyConfigurationDefinition);
			}
		}

		return map;
	}

	private String description;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String id;
	private String name;
	private String parentId;
	private String sourceId;
	private transient String string;

	public KeyConfigurationDefinition(
		String description,
		String id,
		String name,
		String parentId,
		String sourceId) {
		this.description = description;
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.sourceId = sourceId;
	}

	public int compareTo(Object object) {
		KeyConfigurationDefinition castedObject =
			(KeyConfigurationDefinition) object;
		int compareTo = Util.compare(description, castedObject.description);

		if (compareTo == 0) {
			compareTo = Util.compare(id, castedObject.id);

			if (compareTo == 0) {
				compareTo = Util.compare(name, castedObject.name);

				if (compareTo == 0) {
					compareTo = Util.compare(parentId, castedObject.parentId);

					if (compareTo == 0)
						compareTo =
							Util.compare(sourceId, castedObject.sourceId);
				}
			}
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof KeyConfigurationDefinition))
			return false;

		KeyConfigurationDefinition castedObject =
			(KeyConfigurationDefinition) object;
		boolean equals = true;
		equals &= Util.equals(description, castedObject.description);
		equals &= Util.equals(id, castedObject.id);
		equals &= Util.equals(name, castedObject.name);
		equals &= Util.equals(parentId, castedObject.parentId);
		equals &= Util.equals(sourceId, castedObject.sourceId);
		return equals;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getParentId() {
		return parentId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(sourceId);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(',');
			stringBuffer.append(parentId);
			stringBuffer.append(',');
			stringBuffer.append(sourceId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
