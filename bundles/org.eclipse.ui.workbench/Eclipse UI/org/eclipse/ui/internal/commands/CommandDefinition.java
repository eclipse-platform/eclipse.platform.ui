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

import org.eclipse.ui.internal.util.Util;

public final class CommandDefinition
	implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		CommandDefinition.class.getName().hashCode();

	public static Map commandDefinitionsById(
		Collection commandDefinitions,
		boolean allowNullIds) {
		if (commandDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = commandDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, CommandDefinition.class);
			CommandDefinition commandDefinition = (CommandDefinition) object;
			String id = commandDefinition.getId();

			if (allowNullIds || id != null)
				map.put(id, commandDefinition);
		}

		return map;
	}

	public static Map commandDefinitionsByName(
		Collection commandDefinitions,
		boolean allowNullNames) {
		if (commandDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = commandDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, CommandDefinition.class);
			CommandDefinition commandDefinition = (CommandDefinition) object;
			String name = commandDefinition.getName();

			if (allowNullNames || name != null) {
				Collection commandDefinitions2 = (Collection) map.get(name);

				if (commandDefinitions2 == null) {
					commandDefinitions2 = new HashSet();
					map.put(name, commandDefinitions2);
				}

				commandDefinitions2.add(commandDefinition);
			}
		}

		return map;
	}

	private String categoryId;
	private String description;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String id;
	private String name;
	private String sourceId;
	private transient String string;

	public CommandDefinition(
		String categoryId,
		String description,
		String id,
		String name,
		String sourceId) {
		this.categoryId = categoryId;
		this.description = description;
		this.id = id;
		this.name = name;
		this.sourceId = sourceId;
	}

	public int compareTo(Object object) {
		CommandDefinition castedObject = (CommandDefinition) object;
		int compareTo = Util.compare(categoryId, castedObject.categoryId);

		if (compareTo == 0) {
			compareTo = Util.compare(description, castedObject.description);

			if (compareTo == 0) {
				compareTo = Util.compare(id, castedObject.id);

				if (compareTo == 0) {
					compareTo = Util.compare(name, castedObject.name);

					if (compareTo == 0)
						compareTo =
							Util.compare(sourceId, castedObject.sourceId);
				}
			}
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof CommandDefinition))
			return false;

		CommandDefinition castedObject = (CommandDefinition) object;
		boolean equals = true;
		equals &= Util.equals(categoryId, castedObject.categoryId);
		equals &= Util.equals(description, castedObject.description);
		equals &= Util.equals(id, castedObject.id);
		equals &= Util.equals(name, castedObject.name);
		equals &= Util.equals(sourceId, castedObject.sourceId);
		return equals;
	}

	public String getCategoryId() {
		return categoryId;
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

	public String getSourceId() {
		return sourceId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(categoryId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(sourceId);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(categoryId);
			stringBuffer.append(',');
			stringBuffer.append(description);
			stringBuffer.append(',');
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
}
