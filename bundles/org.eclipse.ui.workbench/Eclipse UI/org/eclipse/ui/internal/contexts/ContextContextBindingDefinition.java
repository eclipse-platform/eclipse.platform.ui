/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.contexts;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

final class ContextContextBindingDefinition {
	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		ContextContextBindingDefinition.class.getName().hashCode();

	static Map contextContextBindingDefinitionsByParentContextId(Collection contextContextBindingDefinitions) {
		if (contextContextBindingDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = contextContextBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ContextContextBindingDefinition.class);
			ContextContextBindingDefinition contextContextBindingDefinition =
				(ContextContextBindingDefinition) object;
			String parentContextId =
				contextContextBindingDefinition.getParentContextId();

			if (parentContextId != null) {
				Collection contextContextBindingDefinitions2 =
					(Collection) map.get(parentContextId);

				if (contextContextBindingDefinitions2 == null) {
					contextContextBindingDefinitions2 = new HashSet();
					map.put(parentContextId, contextContextBindingDefinitions2);
				}

				contextContextBindingDefinitions2.add(
					contextContextBindingDefinition);
			}
		}

		return map;
	}

	private String childContextId;
	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String parentContextId;
	private String sourceId;
	private transient String string;

	ContextContextBindingDefinition(
		String childContextId,
		String parentContextId,
		String sourceId) {
		this.childContextId = childContextId;
		this.parentContextId = parentContextId;
		this.sourceId = sourceId;
	}

	public int compareTo(Object object) {
		ContextContextBindingDefinition castedObject =
			(ContextContextBindingDefinition) object;
		int compareTo =
			Util.compare(childContextId, castedObject.childContextId);

		if (compareTo == 0) {
			compareTo =
				Util.compare(parentContextId, castedObject.parentContextId);

			if (compareTo == 0)
				compareTo = Util.compare(sourceId, castedObject.sourceId);
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ContextContextBindingDefinition))
			return false;

		ContextContextBindingDefinition castedObject =
			(ContextContextBindingDefinition) object;
		boolean equals = true;
		equals &= Util.equals(childContextId, castedObject.childContextId);
		equals &= Util.equals(parentContextId, castedObject.parentContextId);
		equals &= Util.equals(sourceId, castedObject.sourceId);
		return equals;
	}

	public String getChildContextId() {
		return childContextId;
	}

	public String getParentContextId() {
		return parentContextId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(childContextId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentContextId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(sourceId);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(childContextId);
			stringBuffer.append(',');
			stringBuffer.append(parentContextId);
			stringBuffer.append(',');
			stringBuffer.append(sourceId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
