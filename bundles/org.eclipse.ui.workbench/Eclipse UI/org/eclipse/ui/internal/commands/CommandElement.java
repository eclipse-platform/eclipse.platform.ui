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

final class CommandElement implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = CommandElement.class.getName().hashCode();

	private static Comparator nameComparator;
	
	static CommandElement create(String description, String id, String name, String parentId, String pluginId)
		throws IllegalArgumentException {
		return new CommandElement(description, id, name, parentId, pluginId);
	}

	static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((CommandElement) left).name, ((CommandElement) right).name);
				}	
			};		
		
		return nameComparator;
	}

	static SortedMap sortedMapById(List commandElements)
		throws IllegalArgumentException {
		if (commandElements == null)
			throw new IllegalArgumentException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = commandElements.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (!(object instanceof CommandElement))
				throw new IllegalArgumentException();
				
			CommandElement commandElement = (CommandElement) object;
			sortedMap.put(commandElement.id, commandElement);									
		}			
		
		return sortedMap;
	}

	static SortedMap sortedMapByName(List commandElements)
		throws IllegalArgumentException {
		if (commandElements == null)
			throw new IllegalArgumentException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = commandElements.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (!(object instanceof CommandElement))
				throw new IllegalArgumentException();
				
			CommandElement commandElement = (CommandElement) object;
			sortedMap.put(commandElement.name, commandElement);									
		}			
		
		return sortedMap;
	}

	private String description;
	private String id;
	private String name;
	private String parentId;
	private String pluginId;
	
	private CommandElement(String description, String id, String name, String parentId, String pluginId)
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
		CommandElement item = (CommandElement) object;
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
		if (!(object instanceof CommandElement))
			return false;

		CommandElement commandElement = (CommandElement) object;	
		return Util.equals(description, commandElement.description) && id.equals(commandElement.id) && name.equals(commandElement.name) && Util.equals(parentId, commandElement.parentId) && Util.equals(pluginId, commandElement.pluginId);
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
