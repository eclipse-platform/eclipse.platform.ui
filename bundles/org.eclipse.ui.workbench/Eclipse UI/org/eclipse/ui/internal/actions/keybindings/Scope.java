/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.actions.keybindings;

import org.eclipse.ui.internal.actions.Util;

public final class Scope implements Comparable {

	private final static int HASH_INITIAL = 107;
	private final static int HASH_FACTOR = 117;

	public static Scope create(String id, String name, String description, String parent, String plugin)
		throws IllegalArgumentException {
		return new Scope(id, name, description, parent, plugin);
	}

	private String id;
	private String name;
	private String description;
	private String parent;
	private String plugin;
		
	private Scope(String id, String name, String description, String parent, String plugin)
		throws IllegalArgumentException {
		super();
		
		if (id == null || name == null)
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
			
		Scope scope = (Scope) object;
		int compareTo = id.compareTo(scope.id);
		
		if (compareTo == 0) {
			compareTo = name.compareTo(scope.name);	
		
			if (compareTo == 0) {
				compareTo = Util.compare(description, scope.description);	

				if (compareTo == 0) {
					compareTo = Util.compare(parent, scope.parent);	

					if (compareTo == 0)
						compareTo = Util.compare(plugin, scope.plugin);
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Configuration))
			return false;

		Scope scope = (Scope) object;		
		return id.equals(scope.id) && name.equals(scope.name) && Util.equals(description, scope.description) && Util.equals(parent, scope.parent) && 
			Util.equals(plugin, scope.plugin);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + id.hashCode();
		result = result * HASH_FACTOR + name.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(description);
		result = result * HASH_FACTOR + Util.hashCode(parent);
		result = result * HASH_FACTOR + Util.hashCode(plugin);
		return result;
	}	

	public String toString() {
		return name + '(' + id + ')';	
	}
}
