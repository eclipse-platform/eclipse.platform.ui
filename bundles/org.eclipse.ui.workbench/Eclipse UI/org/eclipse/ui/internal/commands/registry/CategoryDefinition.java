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

package org.eclipse.ui.internal.commands.registry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

public final class CategoryDefinition implements ICategoryDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = CategoryDefinition.class.getName().hashCode();

	public static Map categoryDefinitionsById(List categoryDefinitions, boolean allowNullIds) {
		if (categoryDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();			
		Iterator iterator = categoryDefinitions.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ICategoryDefinition.class);				
			ICategoryDefinition categoryDefinition = (ICategoryDefinition) object;
			String id = categoryDefinition.getId();
			
			if (allowNullIds || id != null)
				map.put(id, categoryDefinition);		
		}			
		
		return map;
	}

	public static Map categoryDefinitionsByName(List categoryDefinitions, boolean allowNullNames) {
		if (categoryDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();			
		Iterator iterator = categoryDefinitions.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ICategoryDefinition.class);			
			ICategoryDefinition categoryDefinition = (ICategoryDefinition) object;
			String name = categoryDefinition.getName();
			
			if (allowNullNames || name != null)
				map.put(name, categoryDefinition);									
		}			
		
		return map;
	}

	private String description;
	private String id;
	private String name;
	private String pluginId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	CategoryDefinition(String description, String id, String name, String pluginId) {
		this.description = description;
		this.id = id;
		this.name = name;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		CategoryDefinition categoryDefinition = (CategoryDefinition) object;
		int compareTo = Util.compare(description, categoryDefinition.description);
		
		if (compareTo == 0) {		
			compareTo = Util.compare(id, categoryDefinition.id);	
		
			if (compareTo == 0) {
				compareTo = Util.compare(name, categoryDefinition.name);
				
				if (compareTo == 0)
					compareTo = Util.compare(pluginId, categoryDefinition.pluginId);								
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof CategoryDefinition))
			return false;

		CategoryDefinition categoryDefinition = (CategoryDefinition) object;	
		boolean equals = true;
		equals &= Util.equals(description, categoryDefinition.description);
		equals &= Util.equals(id, categoryDefinition.id);
		equals &= Util.equals(name, categoryDefinition.name);
		equals &= Util.equals(pluginId, categoryDefinition.pluginId);
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

	public String getPluginId() {
		return pluginId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
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
			stringBuffer.append(pluginId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;		
	}
}