/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
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

public final class Item implements Comparable {
	
	private final static int HASH_INITIAL = 11;
	private final static int HASH_FACTOR = 21;

	private static Comparator nameComparator;
	
	public static Item create(String description, String icon, String id, String name, String parent, String plugin)
		throws IllegalArgumentException {
		return new Item(description, icon, id, name, parent, plugin);
	}

	public static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((Item) left).getName(), ((Item) right).getName());
				}	
			};		
		
		return nameComparator;
	}
	
	private String description;
	private String icon;
	private String id;
	private String name;
	private String parent;
	private String plugin;
	
	private Item(String description, String icon, String id, String name, String parent, String plugin)
		throws IllegalArgumentException {
		super();
		
		if (id == null || name == null)
			throw new IllegalArgumentException();
		
		this.description = description;
		this.icon = icon;
		this.id = id;
		this.name = name;
		this.parent = parent;
		this.plugin = plugin;
	}
	
	public int compareTo(Object object) {
		Item item = (Item) object;
		int compareTo = id.compareTo(item.id);
		
		if (compareTo == 0) {		
			compareTo = name.compareTo(item.name);			
		
			if (compareTo == 0) {
				Util.compare(description, item.description);
				
				if (compareTo == 0) {		
					compareTo = Util.compare(icon, item.icon);

					if (compareTo == 0) {
						compareTo = Util.compare(parent, item.parent);

						if (compareTo == 0)
							compareTo = Util.compare(plugin, item.plugin);
					}										
				}							
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Item))
			return false;

		Item item = (Item) object;	
		return Util.equals(description, item.description) && Util.equals(icon, item.icon) && id.equals(item.id) && name.equals(item.name) && 
			Util.equals(parent, item.parent) && Util.equals(plugin, item.plugin);
	}

	public String getDescription() {
		return description;	
	}
	
	public String getIcon() {
		return icon;
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
		result = result * HASH_FACTOR + Util.hashCode(icon);
		result = result * HASH_FACTOR + id.hashCode();
		result = result * HASH_FACTOR + name.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(parent);
		result = result * HASH_FACTOR + Util.hashCode(plugin);
		return result;
	}
	
	public String toString() {
		return name + '(' + id + ')';	
	}
}
