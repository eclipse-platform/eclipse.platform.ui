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

final class ActivityBindingDefinition implements IActivityBindingDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		ActivityBindingDefinition.class.getName().hashCode();

	static Map activityBindingDefinitionsByRoleId(Collection activityBindingDefinitions) {
		if (activityBindingDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = activityBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IActivityBindingDefinition.class);
			IActivityBindingDefinition activityBindingDefinition =
				(IActivityBindingDefinition) object;
			String roleId = activityBindingDefinition.getRoleId();

			if (roleId != null) {
				Collection activityBindingDefinitions2 =
					(Collection) map.get(roleId);

				if (activityBindingDefinitions2 == null) {
					activityBindingDefinitions2 = new HashSet();
					map.put(roleId, activityBindingDefinitions2);
				}

				activityBindingDefinitions2.add(activityBindingDefinition);
			}
		}

		return map;
	}

	private String activityId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String pluginId;
	private String roleId;
	private transient String string;

	ActivityBindingDefinition(
		String activityId,
		String categoryId,
		String pluginId) {
		this.activityId = activityId;
		this.pluginId = pluginId;
		this.roleId = categoryId;
	}

	public int compareTo(Object object) {
		ActivityBindingDefinition castedObject =
			(ActivityBindingDefinition) object;
		int compareTo = Util.compare(activityId, castedObject.activityId);

		if (compareTo == 0) {
			compareTo = Util.compare(pluginId, castedObject.pluginId);

			if (compareTo == 0)
				compareTo = Util.compare(roleId, castedObject.roleId);
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ActivityBindingDefinition))
			return false;

		ActivityBindingDefinition castedObject =
			(ActivityBindingDefinition) object;
		boolean equals = true;
		equals &= Util.equals(activityId, castedObject.activityId);
		equals &= Util.equals(pluginId, castedObject.pluginId);
		equals &= Util.equals(roleId, castedObject.roleId);
		return equals;
	}

	public String getActivityId() {
		return activityId;
	}

	public String getPluginId() {
		return pluginId;
	}

	public String getRoleId() {
		return roleId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(activityId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pluginId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(roleId);
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
			stringBuffer.append(pluginId);
			stringBuffer.append(',');
			stringBuffer.append(roleId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
