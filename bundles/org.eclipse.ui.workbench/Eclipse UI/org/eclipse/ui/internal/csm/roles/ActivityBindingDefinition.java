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

package org.eclipse.ui.internal.csm.roles;

import org.eclipse.ui.internal.util.Util;

public final class ActivityBindingDefinition implements IActivityBindingDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ActivityBindingDefinition.class.getName().hashCode();

	private String activityId;
	private String pluginId;
	private String roleId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;

	public ActivityBindingDefinition(String activityId, String pluginId, String roleId) {
		this.activityId = activityId;
		this.pluginId = pluginId;
		this.roleId = roleId;
	}
	
	public int compareTo(Object object) {
		ActivityBindingDefinition activityBindingDefinition = (ActivityBindingDefinition) object;
		int compareTo = Util.compare(activityId, activityBindingDefinition.activityId);
		
		if (compareTo == 0) {		
			compareTo = Util.compare(pluginId, activityBindingDefinition.pluginId);			
		
			if (compareTo == 0)
				compareTo = Util.compare(roleId, activityBindingDefinition.roleId);								
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ActivityBindingDefinition))
			return false;

		ActivityBindingDefinition activityBindingDefinition = (ActivityBindingDefinition) object;	
		boolean equals = true;
		equals &= Util.equals(activityId, activityBindingDefinition.activityId);
		equals &= Util.equals(pluginId, activityBindingDefinition.pluginId);
		equals &= Util.equals(roleId, activityBindingDefinition.roleId);
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
