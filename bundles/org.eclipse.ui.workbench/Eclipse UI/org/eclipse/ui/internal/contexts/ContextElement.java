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

package org.eclipse.ui.internal.contexts;

import java.text.Collator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.ui.internal.util.Util;

final class ContextElement implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ContextElement.class.getName().hashCode();

	private static Comparator nameComparator;
	
	static ContextElement create(String description, String id, String name, String parentId, String pluginId)
		throws IllegalArgumentException {
		return new ContextElement(description, id, name, parentId, pluginId);
	}

	static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((ContextElement) left).name, ((ContextElement) right).name);
				}	
			};		
		
		return nameComparator;
	}

	static SortedMap sortedMapById(List contextElements)
		throws IllegalArgumentException {
		if (contextElements == null)
			throw new IllegalArgumentException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = contextElements.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (!(object instanceof ContextElement))
				throw new IllegalArgumentException();
				
			ContextElement contextElement = (ContextElement) object;
			sortedMap.put(contextElement.id, contextElement);									
		}			
		
		return sortedMap;
	}

	static SortedMap sortedMapByName(List contextElements)
		throws IllegalArgumentException {
		if (contextElements == null)
			throw new IllegalArgumentException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = contextElements.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (!(object instanceof ContextElement))
				throw new IllegalArgumentException();
				
			ContextElement contextElement = (ContextElement) object;
			sortedMap.put(contextElement.name, contextElement);									
		}			
		
		return sortedMap;
	}

	private String description;
	private String id;
	private String name;
	private String parentId;
	private String pluginId;
	
	private ContextElement(String description, String id, String name, String parentId, String pluginId)
		throws IllegalArgumentException {
		super();
		
		if (id == null || name == null)
			throw new IllegalArgumentException();
		
		this.description = description;
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		ContextElement item = (ContextElement) object;
		int compareTo = id.compareTo(item.id);
		
		if (compareTo == 0) {		
			compareTo = name.compareTo(item.name);			
		
			if (compareTo == 0) {
				Util.compare(description, item.description);
				
				if (compareTo == 0) {
					compareTo = Util.compare(parentId, item.parentId);

					if (compareTo == 0)
						compareTo = Util.compare(pluginId, item.pluginId);								
				}							
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ContextElement))
			return false;

		ContextElement contextElement = (ContextElement) object;	
		return Util.equals(description, contextElement.description) && id.equals(contextElement.id) && name.equals(contextElement.name) && Util.equals(parentId, contextElement.parentId) && Util.equals(pluginId, contextElement.pluginId);
	}

	String getDescription() {
		return description;	
	}
	
	String getId() {
		return id;	
	}
	
	String getName() {
		return name;
	}	

	String getParentId() {
		return parentId;
	}

	String getPluginId() {
		return pluginId;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + Util.hashCode(description);
		result = result * HASH_FACTOR + id.hashCode();
		result = result * HASH_FACTOR + name.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(parentId);
		result = result * HASH_FACTOR + Util.hashCode(pluginId);
		return result;
	}

	public String toString() {
		return id;
	}
}
