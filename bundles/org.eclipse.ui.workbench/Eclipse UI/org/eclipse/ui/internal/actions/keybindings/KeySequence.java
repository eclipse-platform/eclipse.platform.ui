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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.actions.Util;

public final class KeySequence implements Comparable {

	public final static String ELEMENT = "keysequence"; //$NON-NLS-1$
	private final static int HASH_INITIAL = 47;
	private final static int HASH_FACTOR = 57;

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

	public boolean isChildOf(KeySequence keySequence, boolean equals) {
		if (keySequence == null)
			return false;
		
		return Util.isChildOf(keyStrokes, keySequence.keyStrokes, equals);
	}

	public void write(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		Iterator iterator = keyStrokes.iterator();
		
		while (iterator.hasNext())
			((KeyStroke) iterator.next()).write(memento.createChild(KeyStroke.ELEMENT));
	}

	public int compareTo(Object object) {
		return Util.compare(keyStrokes, ((KeySequence) object).keyStrokes);
	}
	
	public boolean equals(Object object) {
		return object instanceof KeySequence && keyStrokes.equals(((KeySequence) object).keyStrokes);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		Iterator iterator = keyStrokes.iterator();
		
		while (iterator.hasNext())
			result = result * HASH_FACTOR + ((KeyStroke) iterator.next()).hashCode();

		return result;
	}
}
