/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import org.eclipse.ui.IMemento;

public final class Action implements Comparable {
	
	final static String TAG = "action";
	private final static String ATTRIBUTE_VALUE = "value";	
	
	static Action create(String value) {
		return new Action(value);
	}

	static Action read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		return Action.create(memento.getString(ATTRIBUTE_VALUE));
	}

	static void write(IMemento memento, Action action)
		throws IllegalArgumentException {
		if (memento == null || action == null)
			throw new IllegalArgumentException();
			
		memento.putString(ATTRIBUTE_VALUE, action.getValue());
	}
	
	private String value;
	
	private Action(String value) {
		super();
		this.value = value;	
	}
	
	public String getValue() {
		return value;	
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof Action))
			throw new ClassCastException();
			
		return Util.compare(value, ((Action) object).value);			
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Action))
			return false;
		
		String value = ((Action) object).value;		
		return this.value == null ? value == null : this.value.equals(value);
	}
}
