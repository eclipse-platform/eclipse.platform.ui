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

public final class Configuration implements Comparable {
	
	private final static int HASH_INITIAL = 217;
	private final static int HASH_FACTOR = 237;

	private static Comparator nameComparator;

	public static Configuration create(Label label, String parent, String plugin)
		throws IllegalArgumentException {
		return new Configuration(label, parent, plugin);
	}

	public static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Label.nameComparator().compare(((Configuration) left).getLabel(), ((Configuration) right).getLabel());
				}	
			};		
		
		return nameComparator;		
	}
	
	private Label label;
	private String parent;
	private String plugin;
	
	private Configuration(Label label, String parent, String plugin)
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
		Configuration configuration = (Configuration) object;		
		int compareTo = label.compareTo(configuration.label);

		if (compareTo == 0) {			
			compareTo = Util.compare(parent, configuration.parent);
			
			if (compareTo == 0)
				compareTo = Util.compare(plugin, configuration.plugin);
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Configuration))
			return false;

		Configuration configuration = (Configuration) object;		
		return label.equals(configuration.label) && Util.equals(parent, configuration.parent) && Util.equals(plugin, configuration.plugin);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + label.hashCode();		
		result = result * HASH_FACTOR + Util.hashCode(parent);
		result = result * HASH_FACTOR + Util.hashCode(plugin);
		return result;
	}
}
