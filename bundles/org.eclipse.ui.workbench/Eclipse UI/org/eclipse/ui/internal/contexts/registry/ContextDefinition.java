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

package org.eclipse.ui.internal.contexts.registry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

public final class ContextDefinition implements Comparable, IContextDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ContextDefinition.class.getName().hashCode();

	public static Map contextDefinitionsById(List contextDefinitions, boolean allowNullIds) {
		if (contextDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();			
		Iterator iterator = contextDefinitions.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IContextDefinition.class);				
			IContextDefinition contextDefinition = (IContextDefinition) object;
			String id = contextDefinition.getId();
			
			if (allowNullIds || id != null)
				map.put(id, contextDefinition);		
		}			
		
		return map;
	}

	public static Map contextDefinitionsByName(List contextDefinitions, boolean allowNullNames) {
		if (contextDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();			
		Iterator iterator = contextDefinitions.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IContextDefinition.class);			
			IContextDefinition contextDefinition = (IContextDefinition) object;
			String name = contextDefinition.getName();
			
			if (allowNullNames || name != null)
				map.put(name, contextDefinition);									
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
	
	ContextDefinition(String description, String id, String name, String parentId, String pluginId) {
		this.description = description;
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		ContextDefinition contextDefinition = (ContextDefinition) object;
		int compareTo = Util.compare(description, contextDefinition.description);
		
		if (compareTo == 0) {		
			compareTo = Util.compare(id, contextDefinition.id);			
		
			if (compareTo == 0) {
				compareTo = Util.compare(name, contextDefinition.name);
				
				if (compareTo == 0) {
					compareTo = Util.compare(parentId, contextDefinition.parentId);

					if (compareTo == 0)
						compareTo = Util.compare(pluginId, contextDefinition.pluginId);								
				}							
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ContextDefinition))
			return false;

		ContextDefinition contextDefinition = (ContextDefinition) object;	
		boolean equals = true;
		equals &= Util.equals(description, contextDefinition.description);
		equals &= Util.equals(id, contextDefinition.id);
		equals &= Util.equals(name, contextDefinition.name);
		equals &= Util.equals(parentId, contextDefinition.parentId);
		equals &= Util.equals(pluginId, contextDefinition.pluginId);
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
