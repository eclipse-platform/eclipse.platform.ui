/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import org.eclipse.ui.IMemento;

public final class PathItem implements Comparable {
	
	public final static String ELEMENT = "pathitem"; //$NON-NLS-1$
	private final static String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$
	
	public static PathItem create(String value)
		throws IllegalArgumentException {
		return new PathItem(value);
	}

	public static PathItem read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		return PathItem.create(memento.getString(ATTRIBUTE_VALUE));
	}

	private String value;
	
	private PathItem(String value)
		throws IllegalArgumentException {
		super();
		
		if (value == null)
			throw new IllegalArgumentException();
		
		this.value = value;	
	}
	
	public String getValue() {
		return value;	
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof PathItem))
			throw new ClassCastException();
			
		return value.compareTo(((PathItem) object).value); 
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof PathItem))
			return false;
		
		return value.equals(((PathItem) object).value);		
	}

	public int hashCode() {
		return value.hashCode();
	}
	
	public void write(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		memento.putString(ATTRIBUTE_VALUE, value);
	}	
}
