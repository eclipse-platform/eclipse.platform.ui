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

public final class Sequence implements Comparable {

	private final static int HASH_FACTOR = 107;
	private final static int HASH_INITIAL = 117;
	private final static String KEY_STROKE_SEPARATOR = " "; //$NON-NLS-1$

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

	public static Sequence parseKeySequence(String string)
		throws IllegalArgumentException {
		if (string == null)
			throw new IllegalArgumentException();

		List strokes = new ArrayList();
		StringTokenizer stringTokenizer = new StringTokenizer(string);
				
		while (stringTokenizer.hasMoreTokens())
			strokes.add(Stroke.parseKeyStroke(stringTokenizer.nextToken()));
			
		return create(strokes);
	}

	public static Sequence recognizeGestureSequence(Point[] points, int sensitivity) {
		Stroke stroke = null;
		List strokes = new ArrayList();
		int x0 = 0;
		int y0 = 0;

		for (int i = 0; i < points.length; i++) {
			Point point = points[i];

			if (i == 0) {
				x0 = point.getX();
				y0 = point.getY();
				continue;
			}

			int x1 = point.getX();
			int y1 = point.getY();
			int dx = (x1 - x0) / sensitivity;
			int dy = (y1 - y0) / sensitivity;

			if ((dx != 0) || (dy != 0)) {
				if (dx > 0 && !Stroke.EAST.equals(stroke)) {
					strokes.add(stroke = Stroke.EAST);
				} else if (dx < 0 && !Stroke.WEST.equals(stroke)) {
					strokes.add(stroke = Stroke.WEST);
				} else if (dy > 0 && !Stroke.SOUTH.equals(stroke)) {
					strokes.add(stroke = Stroke.SOUTH);
				} else if (dy < 0 && !Stroke.NORTH.equals(stroke)) {
					strokes.add(stroke = Stroke.NORTH);
				}

				x0 = x1;
				y0 = y1;
			}
		}

		return Sequence.create(strokes);
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

	public String formatKeySequence() {
		StringBuffer stringBuffer = new StringBuffer();
		Iterator iterator = strokes.iterator();
		int i = 0;
		
		while (iterator.hasNext()) {
			if (i != 0)
				stringBuffer.append(KEY_STROKE_SEPARATOR);

			stringBuffer.append(((Stroke) iterator.next()).formatKeyStroke());
			i++;
		}

		return stringBuffer.toString();
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
