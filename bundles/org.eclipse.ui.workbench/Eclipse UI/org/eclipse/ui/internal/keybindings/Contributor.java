package org.eclipse.ui.internal.keybindings;

/**
Copyright (c) 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

import org.eclipse.ui.IMemento;

final class Contributor implements Comparable {

	final static String TAG = "contributor";
	private final static String ATTRIBUTE_VALUE = "value";
	
	static Contributor create(String value) {
		return new Contributor(value);
	}

	static Contributor read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		return Contributor.create(memento.getString(ATTRIBUTE_VALUE));
	}

	static void write(IMemento memento, Contributor contributor)
		throws IllegalArgumentException {
		if (memento == null || contributor == null)
			throw new IllegalArgumentException();
			
		memento.putString(ATTRIBUTE_VALUE, contributor.getValue());
	}	
	
	private String value;
	
	private Contributor(String value) {
		super();
		this.value = value;	
	}
	
	String getValue() {
		return value;	
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof Contributor))
			throw new ClassCastException();
			
		return Util.compare(value, ((Contributor) object).value);			
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Contributor))
			return false;
		
		String value = ((Contributor) object).value;		
		return this.value == null ? value == null : this.value.equals(value);
	}
}
