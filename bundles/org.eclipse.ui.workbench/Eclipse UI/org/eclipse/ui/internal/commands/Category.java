/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

import java.text.Collator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public final class Category implements Comparable {

	private final static int HASH_INITIAL = 31;
	private final static int HASH_FACTOR = 41;

	private static Comparator nameComparator;
	
	public static Category create(String description, String id, String name, String plugin)
		throws IllegalArgumentException {
		return new Category(description, id, name, plugin);
	}

	public static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((Category) left).name, ((Category) right).name);
				}	
			};		
		
		return nameComparator;
	}

	public static SortedMap sortedMap(List categories)
		throws IllegalArgumentException {
		if (categories == null)
			throw new IllegalArgumentException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = categories.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (!(object instanceof Category))
				throw new IllegalArgumentException();
				
			Category category = (Category) object;
			sortedMap.put(category.id, category);									
		}			
		
		return sortedMap;
	}

	private String description;
	private String id;
	private String name;
	private String plugin;
	
	private Category(String description, String id, String name, String plugin)
		throws IllegalArgumentException {
		super();
		
		if (id == null || name == null)
			throw new IllegalArgumentException();
		
		this.description = description;
		this.id = id;
		this.name = name;
		this.plugin = plugin;
	}
	
	public int compareTo(Object object) {
		Category category = (Category) object;
		int compareTo = Util.compare(description, category.description);
		
		if (compareTo == 0) {		
			compareTo = id.compareTo(category.id);			
		
			if (compareTo == 0) {
				compareTo = name.compareTo(category.name);
				
				if (compareTo == 0)
					compareTo = Util.compare(plugin, category.plugin);				
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Category))
			return false;

		Category category = (Category) object;	
		return Util.equals(description, category.description) && id.equals(category.id) && name.equals(category.name) && Util.equals(plugin, category.plugin);
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

	public String getPlugin() {
		return plugin;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + Util.hashCode(description);
		result = result * HASH_FACTOR + id.hashCode();
		result = result * HASH_FACTOR + name.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(plugin);
		return result;
	}
	
	public String toString() {
		return name + '(' + id + ')';	
	}
}
