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

import org.eclipse.ui.internal.util.Util;

final class CategoryDefinition implements ICategoryDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = CategoryDefinition.class.getName().hashCode();

	private static Comparator nameComparator;
	
	static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((ICategoryDefinition) left).getName(), ((ICategoryDefinition) right).getName());
				}	
			};		
		
		return nameComparator;
	}

	static SortedMap sortedMapById(List categories) {
		if (categories == null)
			throw new NullPointerException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = categories.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ICategoryDefinition.class);				
			ICategoryDefinition categoryDefinition = (ICategoryDefinition) object;
			sortedMap.put(categoryDefinition.getId(), categoryDefinition);									
		}			
		
		return sortedMap;
	}

	static SortedMap sortedMapByName(List categories) {
		if (categories == null)
			throw new NullPointerException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = categories.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ICategoryDefinition.class);			
			ICategoryDefinition categoryDefinition = (ICategoryDefinition) object;
			sortedMap.put(categoryDefinition.getName(), categoryDefinition);									
		}			
		
		return sortedMap;
	}

	private String description;
	private String id;
	private String name;
	private String pluginId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	CategoryDefinition(String description, String id, String name, String pluginId) {
		if (id == null || name == null)
			throw new NullPointerException();
		
		this.description = description;
		this.id = id;
		this.name = name;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		CategoryDefinition categoryDefinition = (CategoryDefinition) object;
		int compareTo = Util.compare(description, categoryDefinition.description);
		
		if (compareTo == 0) {		
			compareTo = id.compareTo(categoryDefinition.id);	
		
			if (compareTo == 0) {
				compareTo = name.compareTo(categoryDefinition.name);
				
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
		equals &= id.equals(categoryDefinition.id);
		equals &= name.equals(categoryDefinition.name);
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
			hashCode = hashCode * HASH_FACTOR + id.hashCode();
			hashCode = hashCode * HASH_FACTOR + name.hashCode();
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