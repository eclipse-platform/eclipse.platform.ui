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

public final class ActiveGestureConfiguration implements Comparable {

	private final static int HASH_INITIAL = 11;
	private final static int HASH_FACTOR = 21;

	public static ActiveGestureConfiguration create(String plugin, String value)
		throws IllegalArgumentException {
		return new ActiveGestureConfiguration(plugin, value);
	}

	private String plugin;
	private String value;
	
	private ActiveGestureConfiguration(String plugin, String value)
		throws IllegalArgumentException {
		super();
		
		if (value == null)
			throw new IllegalArgumentException();
		
		this.plugin = plugin;
		this.value = value;
	}
	
	public int compareTo(Object object) {
		ActiveGestureConfiguration activeGestureConfiguration = (ActiveGestureConfiguration) object;
		int compareTo = Util.compare(plugin, activeGestureConfiguration.plugin);
		
		if (compareTo == 0)		
			compareTo = value.compareTo(activeGestureConfiguration.value);			
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ActiveGestureConfiguration))
			return false;

		ActiveGestureConfiguration activeGestureConfiguration = (ActiveGestureConfiguration) object;	
		return Util.equals(plugin, activeGestureConfiguration.plugin) && value.equals(activeGestureConfiguration.value);
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
