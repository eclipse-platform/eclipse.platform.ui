/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import org.eclipse.ui.IMemento;

final class Identifier implements Comparable {
	
	final static String ELEMENT = "identifier"; //$NON-NLS-1$
	private final static String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$	
	
	static Identifier create(String value) {
		return new Identifier(value);
	}

	static Identifier read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		return create(memento.getString(ATTRIBUTE_VALUE));
	}

	private String value;
	
	private Identifier(String value) {
		super();
		this.value = value;	
	}
	
	String getValue() {
		return value;	
	}

	public int compareTo(Object object) {
		if (!(object instanceof Identifier))
			throw new ClassCastException();
			
		return Util.compare(value, ((Identifier) object).value);			
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Identifier))
			return false;
		
		return Util.equals(value, ((Identifier) object).value);
	}

	public int hashCode() {
		return Util.hashCode(value);
	}

	void write(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		memento.putString(ATTRIBUTE_VALUE, value);
	}
}
