/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;

public final class KeySequence implements Comparable {

	public final static String ELEMENT = "keysequence"; //$NON-NLS-1$

	public static KeySequence create() {
		return new KeySequence(Collections.EMPTY_LIST);
	}

	public static KeySequence create(KeyStroke keyStroke)
		throws IllegalArgumentException {
		return new KeySequence(Collections.singletonList(keyStroke));
	}

	public static KeySequence create(KeyStroke[] keyStrokes)
		throws IllegalArgumentException {
		return new KeySequence(Arrays.asList(keyStrokes));
	}
	
	public static KeySequence create(List keyStrokes)
		throws IllegalArgumentException {
		return new KeySequence(keyStrokes);
	}
	
	public static KeySequence read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		IMemento[] mementos = memento.getChildren(KeyStroke.ELEMENT);
		
		if (mementos == null)
			throw new IllegalArgumentException();
		
		List keyStrokes = new ArrayList(mementos.length);
		
		for (int i = 0; i < mementos.length; i++)
			keyStrokes.add(KeyStroke.read(mementos[i]));
		
		return KeySequence.create(keyStrokes);
	}

	private List keyStrokes;

	private KeySequence(List keyStrokes)
		throws IllegalArgumentException {
		super();
		
		if (keyStrokes == null)
			throw new IllegalArgumentException();
			
		this.keyStrokes = Collections.unmodifiableList(new ArrayList(keyStrokes));
		Iterator iterator = this.keyStrokes.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof KeyStroke))
				throw new IllegalArgumentException();
	}

	public List getKeyStrokes() {
		return keyStrokes;
	}

	public int compareTo(Object object) {
		if (!(object instanceof KeySequence))
			throw new ClassCastException();

		return Util.compare(keyStrokes.iterator(), ((KeySequence) object).keyStrokes.iterator());
	}
	
	public boolean equals(Object object) {
		return object instanceof KeySequence && keyStrokes.equals(((KeySequence) object).keyStrokes);
	}

	public int hashCode() {
		final int i0 = 52;
		final int i1 = 27;
		int result = i0;		
		Iterator iterator = keyStrokes.iterator();
		
		while (iterator.hasNext())
			result = result * i1 + ((KeyStroke) iterator.next()).hashCode();

		return result;
	}

	public boolean equalsOrIsChildOf(KeySequence keySequence) {
		return keyStrokes.size() >= keySequence.keyStrokes.size() && 
			keyStrokes.subList(0, keySequence.keyStrokes.size()).equals(keySequence.keyStrokes);
	}
			
	public boolean isChildOf(KeySequence keySequence) {
		return keyStrokes.size() > keySequence.keyStrokes.size() &&
			keyStrokes.subList(0, keySequence.keyStrokes.size()).equals(keySequence.keyStrokes);
	}

	public void write(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		Iterator iterator = keyStrokes.iterator();
		
		while (iterator.hasNext())
			((KeyStroke) iterator.next()).write(memento.createChild(KeyStroke.ELEMENT));
	}
}
