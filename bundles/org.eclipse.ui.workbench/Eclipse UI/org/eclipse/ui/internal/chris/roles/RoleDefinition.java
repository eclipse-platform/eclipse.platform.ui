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

package org.eclipse.ui.internal.chris.roles;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.ui.internal.roles.api.IRoleDefinition;
import org.eclipse.ui.internal.util.Util;

public final class RoleDefinition implements Comparable, IRoleDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = RoleDefinition.class.getName().hashCode();

	public static Map roleDefinitionsById(Collection roleDefinitions, boolean allowNullIds) {
		if (roleDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();			
		Iterator iterator = roleDefinitions.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IRoleDefinition.class);				
			IRoleDefinition roleDefinition = (IRoleDefinition) object;
			String id = roleDefinition.getId();
			
			if (allowNullIds || id != null)
				map.put(id, roleDefinition);		
		}			
		
		return map;
	}

	public static Map roleDefinitionsByName(Collection roleDefinitions, boolean allowNullNames) {
		if (roleDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();			
		Iterator iterator = roleDefinitions.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IRoleDefinition.class);			
			IRoleDefinition roleDefinition = (IRoleDefinition) object;
			String name = roleDefinition.getName();
			
			if (allowNullNames || name != null) {
				Set roleDefinitions2 = (Set) map.get(name);
					
				if (roleDefinitions2 == null) {
					roleDefinitions2 = new HashSet();
					map.put(name, roleDefinitions2);					
				}
	
				roleDefinitions2.add(roleDefinition);		
			}											
		}				
	
		return map;
	}

	private String description;
	private String id;
	private String name;
	private String parentId;
	private String pluginId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	RoleDefinition(String description, String id, String name, String parentId, String pluginId) {
		this.description = description;
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		RoleDefinition roleDefinition = (RoleDefinition) object;
		int compareTo = Util.compare(description, roleDefinition.description);
		
		if (compareTo == 0) {		
			compareTo = Util.compare(id, roleDefinition.id);			
		
			if (compareTo == 0) {
				compareTo = Util.compare(name, roleDefinition.name);
				
				if (compareTo == 0) {
					compareTo = Util.compare(parentId, roleDefinition.parentId);

					if (compareTo == 0)
						compareTo = Util.compare(pluginId, roleDefinition.pluginId);								
				}							
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof RoleDefinition))
			return false;

		RoleDefinition roleDefinition = (RoleDefinition) object;	
		boolean equals = true;
		equals &= Util.equals(description, roleDefinition.description);
		equals &= Util.equals(id, roleDefinition.id);
		equals &= Util.equals(name, roleDefinition.name);
		equals &= Util.equals(parentId, roleDefinition.parentId);
		equals &= Util.equals(pluginId, roleDefinition.pluginId);
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

	public String getPluginId() {
		return pluginId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(parentId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pluginId);
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
			stringBuffer.append(pluginId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;		
	}
}
