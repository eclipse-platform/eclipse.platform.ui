/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

public final class Configuration implements Comparable {
	
	private final static int HASH_INITIAL = 27;
	private final static int HASH_FACTOR = 37;

	public static Configuration create(String id, String name, String description, String parent, String plugin)
		throws IllegalArgumentException {
		return new Configuration(id, name, description, parent, plugin);
	}
	
	private String id;
	private String name;
	private String description;
	private String parent;
	private String plugin;
	
	private Configuration(String id, String name, String description, String parent, String plugin)
		throws IllegalArgumentException {
		super();
		
		if (id == null)
			throw new IllegalArgumentException();
		
		this.id = id;
		this.name = name;
		this.description = description;
		this.parent = parent;
		this.plugin = plugin;
	}
	
	public String getId() {
		return id;	
	}
	
	public String getName() {
		return name;
	}		
	
	public String getDescription() {
		return description;	
	}
	
	public String getParent() {
		return parent;
	}
	
	public String getPlugin() {
		return plugin;
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof Configuration))
			throw new ClassCastException();
			
		Configuration configuration = (Configuration) object;
		int compareTo = id.compareTo(configuration.id);
		
		if (compareTo == 0) {
			compareTo = Util.compare(name, configuration.name);	
		
			if (compareTo == 0) {
				compareTo = Util.compare(description, configuration.description);	

				if (compareTo == 0) {
					compareTo = Util.compare(parent, configuration.parent);	

					if (compareTo == 0)
						compareTo = Util.compare(plugin, configuration.plugin);
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Configuration))
			return false;

		Configuration configuration = (Configuration) object;		
		return id.equals(configuration.id) && Util.equals(name, configuration.name) && Util.equals(description, configuration.description) && 
			Util.equals(parent, configuration.parent) && Util.equals(plugin, configuration.plugin);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + id.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(name);
		result = result * HASH_FACTOR + Util.hashCode(description);
		result = result * HASH_FACTOR + Util.hashCode(parent);
		result = result * HASH_FACTOR + Util.hashCode(plugin);
		return result;
	}

	public String toString() {
		return name != null ? name : '(' + id + ')';		
	}
}
