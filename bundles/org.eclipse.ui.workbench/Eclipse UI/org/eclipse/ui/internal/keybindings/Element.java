package org.eclipse.ui.internal.keybindings;

/**
Copyright (c) 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

import org.eclipse.ui.IMemento;

final class Element implements Comparable {
	
	final static String TAG = "element";		
	private final static String ATTRIBUTE_VALUE = "value";
	
	static Element create(String value)
		throws IllegalArgumentException {
		return new Element(value);
	}

	static Element[] create(String[] values)
		throws IllegalArgumentException {
		if (values == null)
			throw new IllegalArgumentException();
					
		Element[] elements = new Element[values.length];
			
		for (int i = 0; i < values.length; i++)
			elements[i] = create(values[i]);
		
		return elements;			
	}

	static Element read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		return Element.create(memento.getString(ATTRIBUTE_VALUE));
	}

	static void write(IMemento memento, Element element)
		throws IllegalArgumentException {
		if (memento == null || element == null)
			throw new IllegalArgumentException();
			
		memento.putString(ATTRIBUTE_VALUE, element.getValue());
	}
	
	private String value;
	
	private Element(String value)
		throws IllegalArgumentException {
		super();
		
		if (value == null)
			throw new IllegalArgumentException();
		
		this.value = value;	
	}
	
	String getValue() {
		return value;	
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof Element))
			throw new ClassCastException();
			
		return value.compareTo(((Element) object).value); 
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Element))
			return false;
		
		return value.equals(((Element) object).value);		
	}
}
