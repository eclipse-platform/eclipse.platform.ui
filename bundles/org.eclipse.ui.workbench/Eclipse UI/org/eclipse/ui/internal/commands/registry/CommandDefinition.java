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

import java.text.Collator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.ui.internal.util.Util;

public final class CommandDefinition implements ICommandDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = CommandDefinition.class.getName().hashCode();

	private static Comparator nameComparator;
	
	public static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((ICommandDefinition) left).getName(), ((ICommandDefinition) right).getName());
				}	
			};		
		
		return nameComparator;
	}

	public static SortedMap sortedMapById(List commands) {
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

	public static SortedMap sortedMapByName(List commands) {
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

	private boolean allowsContextBindings;
	private boolean allowsImageBindings;
	private boolean allowsKeyBindings;
	private String categoryId;
	private String description;
	private String helpId;
	private String id;
	private String name;
	private String pluginId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	CommandDefinition(boolean allowsContextBindings, boolean allowsImageBindings, boolean allowsKeyBindings, String categoryId, String description, String helpId, String id, String name, String pluginId) {
		if (id == null || name == null)
			throw new NullPointerException();
		
		this.allowsContextBindings = allowsContextBindings;
		this.allowsImageBindings = allowsImageBindings;
		this.allowsKeyBindings = allowsKeyBindings;	
		this.categoryId = categoryId;
		this.description = description;
		this.helpId = helpId;
		this.id = id;
		this.name = name;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		CommandDefinition commandDefinition = (CommandDefinition) object;
		int compareTo =	allowsContextBindings == false ? (commandDefinition.allowsContextBindings == true ? -1 : 0) : 1; 
		
		if (compareTo == 0) {
			compareTo =	allowsImageBindings == false ? (commandDefinition.allowsImageBindings == true ? -1 : 0) : 1;
			
			if (compareTo == 0) {
				compareTo =	allowsKeyBindings == false ? (commandDefinition.allowsKeyBindings == true ? -1 : 0) : 1;				
			
				if (compareTo == 0) {
					compareTo = Util.compare(categoryId, commandDefinition.categoryId);
		
					if (compareTo == 0) {		
						compareTo = Util.compare(description, commandDefinition.description);	

						if (compareTo == 0) {
							compareTo = Util.compare(helpId, commandDefinition.helpId);
		
							if (compareTo == 0) {
								compareTo = id.compareTo(commandDefinition.id);	
					
								if (compareTo == 0) {
									compareTo = name.compareTo(commandDefinition.name);	
	
									if (compareTo == 0)
										compareTo = Util.compare(pluginId, commandDefinition.pluginId);								
								}							
							}
						}
					}
				}			
			}			
		}
			
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof CommandDefinition))
			return false;

		CommandDefinition commandDefinition = (CommandDefinition) object;	
		boolean equals = true;
		equals &= allowsContextBindings == commandDefinition.allowsContextBindings;
		equals &= allowsImageBindings == commandDefinition.allowsImageBindings;
		equals &= allowsKeyBindings == commandDefinition.allowsKeyBindings;
		equals &= Util.equals(categoryId, commandDefinition.categoryId);
		equals &= Util.equals(description, commandDefinition.description);
		equals &= Util.equals(helpId, commandDefinition.helpId);
		equals &= id.equals(commandDefinition.id);
		equals &= name.equals(commandDefinition.name);
		equals &= Util.equals(pluginId, commandDefinition.pluginId);
		return equals;		
	}

	public boolean getAllowsContextBindings() {
		return allowsContextBindings;
	}

	public boolean getAllowsImageBindings() {
		return allowsImageBindings;
	}
	
	public boolean getAllowsKeyBindings() {
		return allowsKeyBindings;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public String getDescription() {
		return description;	
	}

	public String getHelpId() {
		return helpId;	
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
			hashCode = hashCode * HASH_FACTOR + (allowsContextBindings ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());		
			hashCode = hashCode * HASH_FACTOR + (allowsImageBindings ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());		
			hashCode = hashCode * HASH_FACTOR + (allowsKeyBindings ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode());			
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(categoryId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(helpId);
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
			stringBuffer.append(allowsContextBindings);
			stringBuffer.append(',');
			stringBuffer.append(allowsImageBindings);
			stringBuffer.append(',');
			stringBuffer.append(allowsKeyBindings);
			stringBuffer.append(',');
			stringBuffer.append(categoryId);
			stringBuffer.append(',');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(helpId);
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