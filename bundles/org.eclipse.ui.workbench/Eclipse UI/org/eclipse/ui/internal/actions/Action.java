/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.actions;

import java.util.Comparator;

public final class Action implements Comparable {
	
	private final static int HASH_INITIAL = 11;
	private final static int HASH_FACTOR = 21;

	private static Comparator nameComparator;

	public static Action create(Label label, String plugin)
		throws IllegalArgumentException {
		return new Action(label, plugin);
	}

	public static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Label.nameComparator().compare(((Action) left).getLabel(), ((Action) right).getLabel());
				}	
			};		
		
		return nameComparator;		
	}
	
	private Label label;
	private String plugin;
	
	private Action(Label label, String plugin)
		throws IllegalArgumentException {
		super();
		
		if (label == null)
			throw new IllegalArgumentException();
		
		this.label = label; 
		this.plugin = plugin;
	}
	
	public Label getLabel() {
		return label;	
	}
	
	public String getPlugin() {
		return plugin;
	}
	
	public int compareTo(Object object) {
		Action action = (Action) object;		
		int compareTo = label.compareTo(action.label);

		if (compareTo == 0)
			compareTo = Util.compare(plugin, action.plugin);
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Action))
			return false;

		Action action = (Action) object;		
		return label.equals(action.label) && Util.equals(plugin, action.plugin);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + label.hashCode();		
		result = result * HASH_FACTOR + Util.hashCode(plugin);
		return result;
	}
}
