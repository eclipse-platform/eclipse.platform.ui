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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

final class RoleActivityBindingDefinition implements IRoleActivityBindingDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = RoleActivityBindingDefinition.class.getName().hashCode();

	static Map roleActivityBindingDefinitionsByRoleId(Collection roleActivityBindingDefinitions, boolean allowNullNames) {
		if (roleActivityBindingDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();			
		Iterator iterator = roleActivityBindingDefinitions.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IRoleActivityBindingDefinition.class);			
			IRoleActivityBindingDefinition roleActivityBindingDefinition = (IRoleActivityBindingDefinition) object;
			String roleId = roleActivityBindingDefinition.getRoleId();
			
			if (allowNullNames || roleId != null) {
				Collection roleActivityBindingDefinitions2 = (Collection) map.get(roleId);
					
				if (roleActivityBindingDefinitions2 == null) {
					roleActivityBindingDefinitions2 = new HashSet();
					map.put(roleId, roleActivityBindingDefinitions2);					
				}
	
				roleActivityBindingDefinitions2.add(roleActivityBindingDefinition);		
			}											
		}				
	
		return map;
	}	
	
	private String activityId;
	private String pluginId;
	private String roleId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;

	RoleActivityBindingDefinition(String activityId, String pluginId, String roleId) {
		this.activityId = activityId;
		this.pluginId = pluginId;
		this.roleId = roleId;
	}
	
	public int compareTo(Object object) {
		RoleActivityBindingDefinition roleActivityBindingDefinition = (RoleActivityBindingDefinition) object;
		int compareTo = Util.compare(activityId, roleActivityBindingDefinition.activityId);

		if (compareTo == 0) {		
			compareTo = Util.compare(pluginId, roleActivityBindingDefinition.pluginId);			
		
			if (compareTo == 0)	
				compareTo = Util.compare(roleId, roleActivityBindingDefinition.roleId);				
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof RoleActivityBindingDefinition))
			return false;

		RoleActivityBindingDefinition roleActivityBindingDefinition = (RoleActivityBindingDefinition) object;	
		boolean equals = true;
		equals &= Util.equals(activityId, roleActivityBindingDefinition.activityId);
		equals &= Util.equals(pluginId, roleActivityBindingDefinition.pluginId);
		equals &= Util.equals(roleId, roleActivityBindingDefinition.roleId);
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
