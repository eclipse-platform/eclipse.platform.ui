/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.actions;

public final class Action implements Comparable {
	
	private final static int HASH_INITIAL = 17;
	private final static int HASH_FACTOR = 27;

	public static Action create(String id, String name, String description, String icon, String plugin)
		throws IllegalArgumentException {
		return new Action(id, name, description, icon, plugin);
	}
	
	private String id;
	private String name;
	private String description;
	private String icon;
	private String plugin;
	
	private Action(String id, String name, String description, String icon, String plugin)
		throws IllegalArgumentException {
		super();
		
		if (id == null)
			throw new IllegalArgumentException();
		
		this.id = id;
		this.name = name;
		this.description = description;
		this.icon = icon;
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
	
	public String getIcon() {
		return icon;
	}
	
	public String getPlugin() {
		return plugin;
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof Action))
			throw new ClassCastException();
			
		Action action = (Action) object;
		int compareTo = id.compareTo(action.id);
		
		if (compareTo == 0) {
			compareTo = Util.compare(name, action.name);	
		
			if (compareTo == 0) {
				compareTo = Util.compare(description, action.description);	

				if (compareTo == 0) {
					compareTo = Util.compare(icon, action.icon);	

					if (compareTo == 0)
						compareTo = Util.compare(plugin, action.plugin);
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Action))
			return false;

		Action action = (Action) object;		
		return id.equals(action.id) && Util.equals(name, action.name) && Util.equals(description, action.description) && 
			Util.equals(icon, action.icon) && Util.equals(plugin, action.plugin);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + id.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(name);
		result = result * HASH_FACTOR + Util.hashCode(description);
		result = result * HASH_FACTOR + Util.hashCode(icon);
		result = result * HASH_FACTOR + Util.hashCode(plugin);
		return result;
	}
	
	public String toString() {
		return name != null ? name : '(' + id + ')';	
	}
}
