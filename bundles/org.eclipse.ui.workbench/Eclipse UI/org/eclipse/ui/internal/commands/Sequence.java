/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class Sequence implements Comparable {

	private final static int HASH_FACTOR = 97;
	private final static int HASH_INITIAL = 107;

	public static Sequence create() {
		return new Sequence(Collections.EMPTY_LIST);
	}

	public static Sequence create(Stroke stroke)
		throws IllegalArgumentException {
		return new Sequence(Collections.singletonList(stroke));
	}

	public static Sequence create(Stroke[] strokes)
		throws IllegalArgumentException {
		return new Sequence(Arrays.asList(strokes));
	}
	
	public static Sequence create(List strokes)
		throws IllegalArgumentException {
		return new Sequence(strokes);
	}

	private List strokes;

	private Sequence(List strokes)
		throws IllegalArgumentException {
		super();
		this.strokes = Collections.unmodifiableList(Util.safeCopy(strokes, Stroke.class));
	}

	public int compareTo(Object object) {
		return Util.compare(strokes, ((Sequence) object).strokes);
	}
	
	public boolean equals(Object object) {
		return object instanceof Sequence && strokes.equals(((Sequence) object).strokes);
	}

	public List getStrokes() {
		return strokes;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		Iterator iterator = strokes.iterator();
		
		while (iterator.hasNext())
			result = result * HASH_FACTOR + ((Stroke) iterator.next()).hashCode();

		return result;
	}

	public boolean isChildOf(Sequence sequence, boolean equals) {
		if (sequence == null)
			return false;
		
		return Util.isChildOf(strokes, sequence.strokes, equals);
	}
}
