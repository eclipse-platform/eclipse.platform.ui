/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.actions.keybindings;

import java.util.Comparator;

import org.eclipse.ui.internal.actions.Label;
import org.eclipse.ui.internal.actions.Util;

public final class Scope implements Comparable {

	private final static int HASH_INITIAL = 217;
	private final static int HASH_FACTOR = 237;

	private static Comparator nameComparator;

	public static Scope create(Label label, String parent, String plugin)
		throws IllegalArgumentException {
		return new Scope(label, parent, plugin);
	}

	public static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Label.nameComparator().compare(((Scope) left).getLabel(), ((Scope) right).getLabel());
				}	
			};		
		
		return nameComparator;		
	}
	
	private Label label;
	private String parent;
	private String plugin;
	
	private Scope(Label label, String parent, String plugin)
		throws IllegalArgumentException {
		super();
		
		if (label == null)
			throw new IllegalArgumentException();

		this.label = label;		
		this.parent = parent;
		this.plugin = plugin;
	}

	public Label getLabel() {
		return label;	
	}

	public String getParent() {
		return parent;
	}
	
	public String getPlugin() {
		return plugin;
	}

	public int compareTo(Object object) {
		Scope scope = (Scope) object;		
		int compareTo = label.compareTo(scope.label);

		if (compareTo == 0) {			
			compareTo = Util.compare(parent, scope.parent);
			
			if (compareTo == 0)
				compareTo = Util.compare(plugin, scope.plugin);
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Scope))
			return false;

		Scope scope = (Scope) object;		
		return label.equals(scope.label) && Util.equals(parent, scope.parent) && Util.equals(plugin, scope.plugin);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + label.hashCode();		
		result = result * HASH_FACTOR + Util.hashCode(parent);
		result = result * HASH_FACTOR + Util.hashCode(plugin);
		return result;
	}
}
