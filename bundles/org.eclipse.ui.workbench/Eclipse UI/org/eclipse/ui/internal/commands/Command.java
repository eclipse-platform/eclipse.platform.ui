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

import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.internal.util.Util;

final class Command implements Comparable, ICommand {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = Command.class.getName().hashCode();

	private static Comparator nameComparator;
	
	static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((ICommand) left).getName(), ((ICommand) right).getName());
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
			Util.assertInstance(object, ICommand.class);				
			ICommand command = (ICommand) object;
			sortedMap.put(command.getId(), command);									
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
			Util.assertInstance(object, ICommand.class);
			ICommand command = (ICommand) object;
			sortedMap.put(command.getName(), command);									
		}			
		
		return sortedMap;
	}

	private boolean active;
	private String categoryId;
	private String description;
	private String id;
	private String name;
	private String pluginId;
	
	Command(boolean active, String categoryId, String description, String id, String name, String pluginId) {
		super();
		
		if (id == null || name == null)
			throw new NullPointerException();
		
		this.active = active;
		this.categoryId = categoryId;
		this.description = description;
		this.id = id;
		this.name = name;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		Command command = (Command) object;
		int compareTo = active == false ? (command.active == true ? -1 : 0) : 1;
		
		if (compareTo == 0) {	
			compareTo = id.compareTo(command.id);
		
			if (compareTo == 0) {		
				compareTo = name.compareTo(command.name);			
			
				if (compareTo == 0) {
					compareTo = Util.compare(categoryId, command.categoryId);
					
					if (compareTo == 0) {
						compareTo = Util.compare(description, command.description);
	
						if (compareTo == 0)
							compareTo = Util.compare(pluginId, command.pluginId);								
					}							
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Command))
			return false;

		Command command = (Command) object;	
		return active == command.active && Util.equals(categoryId, command.categoryId) && Util.equals(description, command.description) && id.equals(command.id) && name.equals(command.name) && Util.equals(pluginId, command.pluginId);
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
		result = result * HASH_FACTOR + (active ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());
		result = result * HASH_FACTOR + Util.hashCode(categoryId);
		result = result * HASH_FACTOR + Util.hashCode(description);
		result = result * HASH_FACTOR + id.hashCode();
		result = result * HASH_FACTOR + name.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(pluginId);
		return result;
	}

	public boolean isActive() {
		return active;
	}

	public String toString() {
		final StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append('[');
		stringBuffer.append(active);
		stringBuffer.append(',');
		stringBuffer.append(id);
		stringBuffer.append(',');
		stringBuffer.append(name);
		stringBuffer.append(',');
		stringBuffer.append(categoryId);
		stringBuffer.append(',');
		stringBuffer.append(description);
		stringBuffer.append(',');
		stringBuffer.append(pluginId);
		stringBuffer.append(']');
		return stringBuffer.toString();
	}
}
