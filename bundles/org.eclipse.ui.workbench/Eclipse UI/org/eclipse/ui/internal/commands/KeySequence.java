/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public final class KeySequence implements Comparable {

	private final static int HASH_INITIAL = 47;
	private final static int HASH_FACTOR = 57;
	private final static String KEY_STROKE_SEPARATOR = " "; //$NON-NLS-1$

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

	public static KeySequence parse(String string)
		throws IllegalArgumentException {
		if (string == null)
			throw new IllegalArgumentException();

		List keyStrokes = new ArrayList();
		StringTokenizer stringTokenizer = new StringTokenizer(string);
				
		while (stringTokenizer.hasMoreTokens())
			keyStrokes.add(KeyStroke.parse(stringTokenizer.nextToken()));
			
		return create(keyStrokes);
	}

	private List keyStrokes;

	private KeySequence(List keyStrokes)
		throws IllegalArgumentException {
		super();
		this.keyStrokes = Collections.unmodifiableList(Util.safeCopy(keyStrokes, KeyStroke.class));
	}

	public int compareTo(Object object) {
		return Util.compare(keyStrokes, ((KeySequence) object).keyStrokes);
	}
	
	public boolean equals(Object object) {
		return object instanceof KeySequence && keyStrokes.equals(((KeySequence) object).keyStrokes);
	}
	
	public List getKeyStrokes() {
		return keyStrokes;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		Iterator iterator = keyStrokes.iterator();
		
		while (iterator.hasNext())
			result = result * HASH_FACTOR + ((KeyStroke) iterator.next()).hashCode();

		return result;
	}

	public boolean isChildOf(KeySequence keySequence, boolean equals) {
		if (keySequence == null)
			return false;
		
		return Util.isChildOf(keyStrokes, keySequence.keyStrokes, equals);
	}

	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		Iterator iterator = keyStrokes.iterator();
		int i = 0;
		
		while (iterator.hasNext()) {
			if (i != 0)
				stringBuffer.append(KEY_STROKE_SEPARATOR);

			stringBuffer.append(iterator.next());
			i++;
		}

		return stringBuffer.toString();
	}
}
