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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

public final class CommandDefinition implements ICommandDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = CommandDefinition.class.getName().hashCode();

	public static Map commandDefinitionsById(List commandDefinitions, boolean allowNullIds) {
		if (commandDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();			
		Iterator iterator = commandDefinitions.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ICommandDefinition.class);				
			ICommandDefinition commandDefinition = (ICommandDefinition) object;
			String id = commandDefinition.getId();
			
			if (allowNullIds || id != null)
				map.put(id, commandDefinition);									
		}			
		
		return map;
	}

	public static Map commandDefinitionsByName(List commandDefinitions, boolean allowNullNames) {
		if (commandDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();			
		Iterator iterator = commandDefinitions.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ICommandDefinition.class);
			ICommandDefinition commandDefinition = (ICommandDefinition) object;
			String name = commandDefinition.getName();
			
			if (allowNullNames || name != null)
				map.put(name, commandDefinition);											
		}			
		
		return map;
	}

	private String categoryId;
	private String description;
	private String helpId;
	private String id;
	private String name;
	private String pluginId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	CommandDefinition(String categoryId, String description, String helpId, String id, String name, String pluginId) {
		this.categoryId = categoryId;
		this.description = description;
		this.helpId = helpId;
		this.id = id;
		this.name = name;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		CommandDefinition commandDefinition = (CommandDefinition) object;
		int compareTo =	Util.compare(categoryId, commandDefinition.categoryId);
		
		if (compareTo == 0) {		
			compareTo = Util.compare(description, commandDefinition.description);	
	
			if (compareTo == 0) {
				compareTo = Util.compare(helpId, commandDefinition.helpId);
	
				if (compareTo == 0) {
					compareTo = Util.compare(id, commandDefinition.id);	
		
					if (compareTo == 0) {
						compareTo = Util.compare(name, commandDefinition.name);	
	
						if (compareTo == 0)
							compareTo = Util.compare(pluginId, commandDefinition.pluginId);								
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
		equals &= Util.equals(categoryId, commandDefinition.categoryId);
		equals &= Util.equals(description, commandDefinition.description);
		equals &= Util.equals(helpId, commandDefinition.helpId);
		equals &= Util.equals(id, commandDefinition.id);
		equals &= Util.equals(name, commandDefinition.name);
		equals &= Util.equals(pluginId, commandDefinition.pluginId);
		return equals;		
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
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(categoryId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(description);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(helpId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(id);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(name);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pluginId);
			hashCodeComputed = true;
		}
			
		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
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