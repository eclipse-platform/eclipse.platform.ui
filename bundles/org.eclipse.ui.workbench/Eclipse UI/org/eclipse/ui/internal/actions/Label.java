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

import java.text.Collator;
import java.util.Comparator;

public final class Label implements Comparable {

	private final static int HASH_INITIAL = 21;
	private final static int HASH_FACTOR = 31;

	private static Comparator nameComparator;
	
	public static Label create(String description, String icon, String id, String name)
		throws IllegalArgumentException {
		return new Label(description, icon, id, name);
	}

	public static Comparator nameComparator() {
		if (nameComparator == null)
			nameComparator = new Comparator() {
				public int compare(Object left, Object right) {
					return Collator.getInstance().compare(((Label) left).getName(), ((Label) right).getName());
				}	
			};		
		
		return nameComparator;		
	}
	
	private String description;
	private String icon;
	private String id;
	private String name;
	
	private Label(String description, String icon, String id, String name)
		throws IllegalArgumentException {
		super();
		
		if (id == null || name == null)
			throw new IllegalArgumentException();
		
		this.description = description;
		this.icon = icon;
		this.id = id;
		this.name = name;
	}
	
	public int compareTo(Object object) {
		Label label = (Label) object;
		int compareTo = id.compareTo(label.id);
		
		if (compareTo == 0) {		
			compareTo = name.compareTo(label.name);			
		
			if (compareTo == 0) {
				Util.compare(description, label.description);
				
				if (compareTo == 0)		
					compareTo = Util.compare(icon, label.icon);
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Label))
			return false;

		Label label = (Label) object;		
		return Util.equals(description, label.description) && Util.equals(icon, label.icon) && id.equals(label.id) && name.equals(label.name);
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

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + Util.hashCode(description);
		result = result * HASH_FACTOR + Util.hashCode(icon);
		result = result * HASH_FACTOR + id.hashCode();
		result = result * HASH_FACTOR + name.hashCode();
		return result;
	}
	
	public String toString() {
		return name + '(' + id + ')';	
	}
}
