/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

public final class ActiveKeyConfiguration implements Comparable {

	private final static int HASH_INITIAL = 21;
	private final static int HASH_FACTOR = 31;

	public static ActiveKeyConfiguration create(String plugin, String value)
		throws IllegalArgumentException {
		return new ActiveKeyConfiguration(plugin, value);
	}

	private String plugin;
	private String value;
	
	private ActiveKeyConfiguration(String plugin, String value)
		throws IllegalArgumentException {
		super();
		
		if (value == null)
			throw new IllegalArgumentException();
		
		this.plugin = plugin;
		this.value = value;
	}
	
	public int compareTo(Object object) {
		ActiveKeyConfiguration activeKeyConfiguration = (ActiveKeyConfiguration) object;
		int compareTo = Util.compare(plugin, activeKeyConfiguration.plugin);
		
		if (compareTo == 0)		
			compareTo = value.compareTo(activeKeyConfiguration.value);			
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ActiveKeyConfiguration))
			return false;

		ActiveKeyConfiguration activeKeyConfiguration = (ActiveKeyConfiguration) object;	
		return Util.equals(plugin, activeKeyConfiguration.plugin) && value.equals(activeKeyConfiguration.value);
	}

	public String getPlugin() {
		return plugin;
	}

	public String getValue() {
		return value;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + Util.hashCode(plugin);
		result = result * HASH_FACTOR + value.hashCode();
		return result;
	}
	
	public String toString() {
		return value;	
	}
}
