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

package org.eclipse.ui.internal.commands;

import java.text.Collator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.ui.commands.IGestureConfiguration;
import org.eclipse.ui.internal.util.Util;

final class GestureConfiguration implements Comparable, IGestureConfiguration {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = GestureConfiguration.class.getName().hashCode();

	private static Comparator nameComparator;
	
	static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((IGestureConfiguration) left).getName(), ((IGestureConfiguration) right).getName());
				}	
			};		
		
		return nameComparator;
	}

	static SortedMap sortedMapById(List gestureConfigurations) {
		if (gestureConfigurations == null)
			throw new NullPointerException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = gestureConfigurations.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IGestureConfiguration.class);				
			IGestureConfiguration gestureConfiguration = (IGestureConfiguration) object;
			sortedMap.put(gestureConfiguration.getId(), gestureConfiguration);									
		}			
		
		return sortedMap;
	}

	static SortedMap sortedMapByName(List gestureConfigurations) {
		if (gestureConfigurations == null)
			throw new NullPointerException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = gestureConfigurations.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IGestureConfiguration.class);				
			IGestureConfiguration gestureConfiguration = (IGestureConfiguration) object;
			sortedMap.put(gestureConfiguration.getName(), gestureConfiguration);									
		}			
		
		return sortedMap;
	}

	private String description;
	private String id;
	private String name;
	private String pluginId;
	
	GestureConfiguration(String description, String id, String name, String pluginId) {
		super();
		
		if (id == null || name == null)
			throw new NullPointerException();
		
		this.description = description;
		this.id = id;
		this.name = name;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		GestureConfiguration gestureConfiguration = (GestureConfiguration) object;
		int compareTo = id.compareTo(gestureConfiguration.id);
		
		if (compareTo == 0) {		
			compareTo = name.compareTo(gestureConfiguration.name);			
		
			if (compareTo == 0) {
				compareTo = Util.compare(description, gestureConfiguration.description);
				
				if (compareTo == 0)
					compareTo = Util.compare(pluginId, gestureConfiguration.pluginId);								
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof GestureConfiguration))
			return false;

		GestureConfiguration gestureConfiguration = (GestureConfiguration) object;	
		return Util.equals(description, gestureConfiguration.description) && id.equals(gestureConfiguration.id) && name.equals(gestureConfiguration.name) && Util.equals(pluginId, gestureConfiguration.pluginId);
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
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + Util.hashCode(description);
		result = result * HASH_FACTOR + id.hashCode();
		result = result * HASH_FACTOR + name.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(pluginId);
		return result;
	}

	public String toString() {
		return '[' + id + ',' + name + ',' + description + ',' + pluginId + ']';
	}
}