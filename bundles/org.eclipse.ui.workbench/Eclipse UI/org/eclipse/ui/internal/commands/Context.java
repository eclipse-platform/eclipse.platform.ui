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

public final class Context implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Context.class.getName().hashCode();

	private static Comparator nameComparator;
	
	public static Context create(String description, String id, String name, String parent, String plugin)
		throws IllegalArgumentException {
		return new Context(description, id, name, parent, plugin);
	}

	public static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((Context) left).name, ((Context) right).name);
				}	
			};		
		
		return nameComparator;
	}

	public static SortedMap sortedMapById(List contexts)
		throws IllegalArgumentException {
		if (contexts == null)
			throw new IllegalArgumentException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = contexts.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (!(object instanceof Context))
				throw new IllegalArgumentException();
				
			Context context = (Context) object;
			sortedMap.put(context.id, context);									
		}			
		
		return sortedMap;
	}

	public static SortedMap sortedMapByName(List contexts)
		throws IllegalArgumentException {
		if (contexts == null)
			throw new IllegalArgumentException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = contexts.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (!(object instanceof Context))
				throw new IllegalArgumentException();
				
			Context context = (Context) object;
			sortedMap.put(context.name, context);									
		}			
		
		return sortedMap;
	}

	private String description;
	private String id;
	private String name;
	private String parent;
	private String plugin;
	
	private Context(String description, String id, String name, String parent, String plugin)
		throws IllegalArgumentException {
		super();
		
		if (id == null || name == null)
			throw new IllegalArgumentException();
		
		this.description = description;
		this.id = id;
		this.name = name;
		this.parent = parent;
		this.plugin = plugin;
	}
	
	public int compareTo(Object object) {
		Context item = (Context) object;
		int compareTo = id.compareTo(item.id);
		
		if (compareTo == 0) {		
			compareTo = name.compareTo(item.name);			
		
			if (compareTo == 0) {
				Util.compare(description, item.description);
				
				if (compareTo == 0) {
					compareTo = Util.compare(parent, item.parent);

					if (compareTo == 0)
						compareTo = Util.compare(plugin, item.plugin);								
				}							
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Context))
			return false;

		Context context = (Context) object;	
		return Util.equals(description, context.description) && id.equals(context.id) && name.equals(context.name) && Util.equals(parent, context.parent) && Util.equals(plugin, context.plugin);
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

	public String getParent() {
		return parent;
	}

	public String getPlugin() {
		return plugin;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + Util.hashCode(description);
		result = result * HASH_FACTOR + id.hashCode();
		result = result * HASH_FACTOR + name.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(parent);
		result = result * HASH_FACTOR + Util.hashCode(plugin);
		return result;
	}
	
	public String toString() {
		return name + " (" + id + ')';		 //$NON-NLS-1$
	}
}
