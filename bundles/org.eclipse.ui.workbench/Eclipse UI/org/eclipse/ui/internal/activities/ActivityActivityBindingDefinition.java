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

package org.eclipse.ui.internal.activities;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

public final class ActivityActivityBindingDefinition {
	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		ActivityActivityBindingDefinition.class.getName().hashCode();

	static Map activityActivityBindingDefinitionsByParentActivityId(Collection activityActivityBindingDefinitions) {
		if (activityActivityBindingDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = activityActivityBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(
				object,
				ActivityActivityBindingDefinition.class);
			ActivityActivityBindingDefinition activityActivityBindingDefinition =
				(ActivityActivityBindingDefinition) object;
			String parentActivityId =
				activityActivityBindingDefinition.getParentActivityId();

			if (parentActivityId != null) {
				Collection activityActivityBindingDefinitions2 =
					(Collection) map.get(parentActivityId);

				if (activityActivityBindingDefinitions2 == null) {
					activityActivityBindingDefinitions2 = new HashSet();
					map.put(
						parentActivityId,
						activityActivityBindingDefinitions2);
				}

				activityActivityBindingDefinitions2.add(
					activityActivityBindingDefinition);
			}
		}

		return map;
	}

	private String childActivityId;
	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String parentActivityId;
	private String sourceId;
	private transient String string;

	public ActivityActivityBindingDefinition(
		String childActivityId,
		String parentActivityId,
		String sourceId) {
		this.childActivityId = childActivityId;
		this.parentActivityId = parentActivityId;
		this.sourceId = sourceId;
	}

	public int compareTo(Object object) {
		ActivityActivityBindingDefinition castedObject =
			(ActivityActivityBindingDefinition) object;
		int compareTo =
			Util.compare(childActivityId, castedObject.childActivityId);

		if (compareTo == 0) {
			compareTo =
				Util.compare(parentActivityId, castedObject.parentActivityId);

			if (compareTo == 0)
				compareTo = Util.compare(sourceId, castedObject.sourceId);
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ActivityActivityBindingDefinition))
			return false;

		ActivityActivityBindingDefinition castedObject =
			(ActivityActivityBindingDefinition) object;
		boolean equals = true;
		equals &= Util.equals(childActivityId, castedObject.childActivityId);
		equals &= Util.equals(parentActivityId, castedObject.parentActivityId);
		equals &= Util.equals(sourceId, castedObject.sourceId);
		return equals;
	}

	public String getChildActivityId() {
		return childActivityId;
	}

	public String getParentActivityId() {
		return parentActivityId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(childActivityId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentActivityId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(sourceId);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(childActivityId);
			stringBuffer.append(',');
			stringBuffer.append(parentActivityId);
			stringBuffer.append(',');
			stringBuffer.append(sourceId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
