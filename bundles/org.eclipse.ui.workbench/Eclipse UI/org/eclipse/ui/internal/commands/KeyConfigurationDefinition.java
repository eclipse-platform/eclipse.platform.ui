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

import org.eclipse.ui.commands.IKeyConfigurationDefinition;
import org.eclipse.ui.internal.util.Util;

final class KeyConfigurationDefinition implements Comparable, IKeyConfigurationDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = KeyConfigurationDefinition.class.getName().hashCode();

	private static Comparator nameComparator;
	
	static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((IKeyConfigurationDefinition) left).getName(), ((IKeyConfigurationDefinition) right).getName());
				}	
			};		
		
		return nameComparator;
	}

	static SortedMap sortedMapById(List keyConfigurations) {
		if (keyConfigurations == null)
			throw new NullPointerException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = keyConfigurations.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IKeyConfigurationDefinition.class);
			IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) object;
			sortedMap.put(keyConfigurationDefinition.getId(), keyConfigurationDefinition);									
		}			
		
		return sortedMap;
	}

	static SortedMap sortedMapByName(List keyConfigurations) {
		if (keyConfigurations == null)
			throw new NullPointerException();

		SortedMap sortedMap = new TreeMap();			
		Iterator iterator = keyConfigurations.iterator();
		
		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, IKeyConfigurationDefinition.class);			
			IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) object;
			sortedMap.put(keyConfigurationDefinition.getName(), keyConfigurationDefinition);									
		}			
		
		return sortedMap;
	}

	private String description;
	private String id;
	private String name;
	private String parentId;
	private String pluginId;
	
	KeyConfigurationDefinition(String description, String id, String name, String parentId, String pluginId) {
		super();
		
		if (id == null || name == null)
			throw new NullPointerException();
		
		this.description = description;
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		KeyConfigurationDefinition keyConfigurationDefinition = (KeyConfigurationDefinition) object;
		int compareTo = id.compareTo(keyConfigurationDefinition.id);
		
		if (compareTo == 0) {		
			compareTo = name.compareTo(keyConfigurationDefinition.name);			
		
			if (compareTo == 0) {
				compareTo = Util.compare(description, keyConfigurationDefinition.description);
				
				if (compareTo == 0) {
					compareTo = Util.compare(parentId, keyConfigurationDefinition.parentId);

					if (compareTo == 0)
						compareTo = Util.compare(pluginId, keyConfigurationDefinition.pluginId);								
				}							
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyConfigurationDefinition))
			return false;

		KeyConfigurationDefinition keyConfigurationDefinition = (KeyConfigurationDefinition) object;	
		return Util.equals(description, keyConfigurationDefinition.description) && id.equals(keyConfigurationDefinition.id) && name.equals(keyConfigurationDefinition.name) && Util.equals(parentId, keyConfigurationDefinition.parentId) && Util.equals(pluginId, keyConfigurationDefinition.pluginId);
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

	public String getParentId() {
		return parentId;
	}

	public String getPluginId() {
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
		return '[' + id + ',' + name + ',' + description + ',' + parentId + ',' + pluginId + ']';
	}
}
