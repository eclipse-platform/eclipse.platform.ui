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

package org.eclipse.ui.internal.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

public final class ActivityPatternBindingDefinition {
	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		ActivityPatternBindingDefinition.class.getName().hashCode();

	static Map activityPatternBindingDefinitionsByActivityId(Collection activityPatternBindingDefinitions) {
		if (activityPatternBindingDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = activityPatternBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ActivityPatternBindingDefinition.class);
			ActivityPatternBindingDefinition activityPatternBindingDefinition =
				(ActivityPatternBindingDefinition) object;
			String activityId =
				activityPatternBindingDefinition.getActivityId();

			if (activityId != null) {
				Collection activityPatternBindingDefinitions2 =
					(Collection) map.get(activityId);

				if (activityPatternBindingDefinitions2 == null) {
					activityPatternBindingDefinitions2 = new ArrayList();
					map.put(activityId, activityPatternBindingDefinitions2);
				}

				activityPatternBindingDefinitions2.add(
					activityPatternBindingDefinition);
			}
		}

		return map;
	}

	private String activityId;
	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String pattern;
	private String sourceId;
	private transient String string;

	public ActivityPatternBindingDefinition(
		String activityId,
		String pattern,
		String sourceId) {
		this.activityId = activityId;
		this.pattern = pattern;
		this.sourceId = sourceId;
	}

	public int compareTo(Object object) {
		ActivityPatternBindingDefinition castedObject =
			(ActivityPatternBindingDefinition) object;
		int compareTo = Util.compare(activityId, castedObject.activityId);

		if (compareTo == 0) {
			compareTo = Util.compare(pattern, castedObject.pattern);

			if (compareTo == 0)
				compareTo = Util.compare(sourceId, castedObject.sourceId);
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ActivityPatternBindingDefinition))
			return false;

		ActivityPatternBindingDefinition castedObject =
			(ActivityPatternBindingDefinition) object;
		boolean equals = true;
		equals &= Util.equals(activityId, castedObject.activityId);
		equals &= Util.equals(pattern, castedObject.pattern);
		equals &= Util.equals(sourceId, castedObject.sourceId);
		return equals;
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

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(activityId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pattern);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(sourceId);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(activityId);
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
