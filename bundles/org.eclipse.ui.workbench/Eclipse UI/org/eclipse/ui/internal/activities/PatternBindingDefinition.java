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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

final class PatternBindingDefinition implements IPatternBindingDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = PatternBindingDefinition.class.getName().hashCode();

	static Map patternBindingDefinitionsByActivityId(Collection patternBindingDefinitions) {
		if (patternBindingDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = patternBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IPatternBindingDefinition.class);
			IPatternBindingDefinition patternBindingDefinition = (IPatternBindingDefinition) object;
			String activityId = patternBindingDefinition.getActivityId();

			if (activityId != null) {
				Collection patternBindingDefinitions2 = (Collection) map.get(activityId);

				if (patternBindingDefinitions2 == null) {
					patternBindingDefinitions2 = new ArrayList();
					map.put(activityId, patternBindingDefinitions2);
				}

				patternBindingDefinitions2.add(patternBindingDefinition);
			}
		}

		return map;
	}

	private String activityId;
	private boolean inclusive;
	private String pattern;
	private String pluginId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;

	PatternBindingDefinition(String activityId, boolean inclusive, String pattern, String pluginId) {
		this.activityId = activityId;
		this.inclusive = inclusive;
		this.pattern = pattern;
		this.pluginId = pluginId;
	}

	public int compareTo(Object object) {
		PatternBindingDefinition castedObject = (PatternBindingDefinition) object;
		int compareTo = Util.compare(activityId, castedObject.activityId);

		if (compareTo == 0) {
			compareTo = Util.compare(inclusive, castedObject.inclusive);

			if (compareTo == 0) {
				compareTo = Util.compare(pattern, castedObject.pattern);

				if (compareTo == 0)
					compareTo = Util.compare(pluginId, castedObject.pluginId);
			}
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof PatternBindingDefinition))
			return false;

		PatternBindingDefinition castedObject = (PatternBindingDefinition) object;
		boolean equals = true;
		equals &= Util.equals(activityId, castedObject.activityId);
		equals &= Util.equals(inclusive, castedObject.inclusive);
		equals &= Util.equals(pattern, castedObject.pattern);
		equals &= Util.equals(pluginId, castedObject.pluginId);
		return equals;
	}

	public String getActivityId() {
		return activityId;
	}

	public String getPattern() {
		return pattern;
	}

	public String getPluginId() {
		return pluginId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(activityId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(inclusive);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pattern);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pluginId);
			hashCodeComputed = true;
		}

		return hashCode;
	}

	public boolean isInclusive() {
		return inclusive;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(activityId);
			stringBuffer.append(',');
			stringBuffer.append(inclusive);
			stringBuffer.append(',');
			stringBuffer.append(pattern);
			stringBuffer.append(',');
			stringBuffer.append(pluginId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
