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

public final class Command implements Comparable {

	private final static int HASH_INITIAL = 41;
	private final static int HASH_FACTOR = 51;

	private static Comparator nameComparator;
	
	public static Command create(String category, String description, String id, String name, String plugin)
		throws IllegalArgumentException {
		return new Command(category, description, id, name, plugin);
	}

	public static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((Command) left).name, ((Command) right).name);
				}	
			};		
		
		return nameComparator;
	}

	public static SortedMap sortedMapById(List commands)
		throws IllegalArgumentException {
		if (commands == null)
			throw new IllegalArgumentException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = commands.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (!(object instanceof Command))
				throw new IllegalArgumentException();
				
			Command command = (Command) object;
			sortedMap.put(command.id, command);									
		}			
		
		return sortedMap;
	}

	public static SortedMap sortedMapByName(List commands)
		throws IllegalArgumentException {
		if (commands == null)
			throw new IllegalArgumentException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = commands.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (!(object instanceof Command))
				throw new IllegalArgumentException();
				
			Command command = (Command) object;
			sortedMap.put(command.name, command);									
		}			
		
		return sortedMap;
	}

	private String category;
	private String description;
	private String id;
	private String name;
	private String plugin;
	
	private Command(String category, String description, String id, String name, String plugin)
		throws IllegalArgumentException {
		super();
		
		if (id == null || name == null)
			throw new IllegalArgumentException();
		
		this.category = category;
		this.description = description;
		this.id = id;
		this.name = name;
		this.plugin = plugin;
	}
	
	public int compareTo(Object object) {
		Command command = (Command) object;
		int compareTo = Util.compare(category, command.category);

		if (compareTo == 0) {		
			compareTo = Util.compare(description, command.description);
		
			if (compareTo == 0) {		
				compareTo = id.compareTo(command.id);			
			
				if (compareTo == 0) {
					compareTo = name.compareTo(command.name);
						
					if (compareTo == 0)
						compareTo = Util.compare(plugin, command.plugin);				
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Command))
			return false;

		Command command = (Command) object;	
		return Util.equals(category, command.category) && Util.equals(description, command.description) && id.equals(command.id) && name.equals(command.name) && Util.equals(plugin, command.plugin);
	}

	public String getCategory() {
		return category;	
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
		result = result * HASH_FACTOR + Util.hashCode(category);
		result = result * HASH_FACTOR + Util.hashCode(description);
		result = result * HASH_FACTOR + id.hashCode();
		result = result * HASH_FACTOR + name.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(plugin);
		return result;
	}
	
	public String toString() {
		return name + " (" + id + ')';	
	}
}
