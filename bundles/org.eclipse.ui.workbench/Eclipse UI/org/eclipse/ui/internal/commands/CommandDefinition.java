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

import org.eclipse.ui.commands.ICommandDefinition;
import org.eclipse.ui.internal.util.Util;

final class CommandDefinition implements Comparable, ICommandDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = CommandDefinition.class.getName().hashCode();

	private static Comparator nameComparator;
	
	static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((ICommandDefinition) left).getName(), ((ICommandDefinition) right).getName());
				}	
			};		
		
		return nameComparator;
	}

	static SortedMap sortedMapById(List commands) {
		if (commands == null)
			throw new NullPointerException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = commands.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ICommandDefinition.class);				
			ICommandDefinition commandDefinition = (ICommandDefinition) object;
			sortedMap.put(commandDefinition.getId(), commandDefinition);									
		}			
		
		return sortedMap;
	}

	static SortedMap sortedMapByName(List commands) {
		if (commands == null)
			throw new NullPointerException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = commands.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ICommandDefinition.class);
			ICommandDefinition commandDefinition = (ICommandDefinition) object;
			sortedMap.put(commandDefinition.getName(), commandDefinition);									
		}			
		
		return sortedMap;
	}

	private String categoryId;
	private String description;
	private String id;
	private String name;
	private String pluginId;
	
	CommandDefinition(String categoryId, String description, String id, String name, String pluginId) {
		super();
		
		if (id == null || name == null)
			throw new NullPointerException();
		
		this.categoryId = categoryId;
		this.description = description;
		this.id = id;
		this.name = name;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		CommandDefinition commandDefinition = (CommandDefinition) object;
		int compareTo = id.compareTo(commandDefinition.id);
		
		if (compareTo == 0) {		
			compareTo = name.compareTo(commandDefinition.name);			
		
			if (compareTo == 0) {
				compareTo = Util.compare(categoryId, commandDefinition.categoryId);
				
				if (compareTo == 0) {
					compareTo = Util.compare(description, commandDefinition.description);

					if (compareTo == 0)
						compareTo = Util.compare(pluginId, commandDefinition.pluginId);								
				}							
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof CommandDefinition))
			return false;

		CommandDefinition commandDefinition = (CommandDefinition) object;	
		return Util.equals(categoryId, commandDefinition.categoryId) && Util.equals(description, commandDefinition.description) && id.equals(commandDefinition.id) && name.equals(commandDefinition.name) && Util.equals(pluginId, commandDefinition.pluginId);
	}

	public String getCategoryId() {
		return categoryId;
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
		result = result * HASH_FACTOR + Util.hashCode(categoryId);
		result = result * HASH_FACTOR + Util.hashCode(description);
		result = result * HASH_FACTOR + id.hashCode();
		result = result * HASH_FACTOR + name.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(pluginId);
		return result;
	}

	public String toString() {
		return '[' + id + ',' + name + ',' + categoryId + ',' + description + ',' + pluginId + ']';
	}
}
