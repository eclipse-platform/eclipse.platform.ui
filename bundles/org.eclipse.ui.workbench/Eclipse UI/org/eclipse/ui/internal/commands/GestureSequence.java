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

public final class GestureSequence implements Comparable {

	private final static int HASH_INITIAL = 47;
	private final static int HASH_FACTOR = 57;

	public static GestureSequence create() {
		return new GestureSequence(Collections.EMPTY_LIST);
	}

	public static GestureSequence create(GestureStroke gestureStroke)
		throws IllegalArgumentException {
		return new GestureSequence(Collections.singletonList(gestureStroke));
	}

	public static GestureSequence create(GestureStroke[] gestureStrokes)
		throws IllegalArgumentException {
		return new GestureSequence(Arrays.asList(gestureStrokes));
	}
	
	public static GestureSequence create(List gestureStrokes)
		throws IllegalArgumentException {
		return new GestureSequence(gestureStrokes);
	}

	public static GestureSequence parse(String gestureString)
		throws IllegalArgumentException {
		// TODO
		return create();
	}

	public static GestureSequence recognize(Point[] points, int sensitivity) {
		GestureStroke gestureStroke = null;
		List gestureStrokes = new ArrayList();
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
				if (dx > 0 && !GestureStroke.EAST.equals(gestureStroke)) {
					gestureStrokes.add(gestureStroke = GestureStroke.EAST);
				} else if (dx < 0 && !GestureStroke.WEST.equals(gestureStroke)) {
					gestureStrokes.add(gestureStroke = GestureStroke.WEST);
				} else if (dy > 0 && !GestureStroke.SOUTH.equals(gestureStroke)) {
					gestureStrokes.add(gestureStroke = GestureStroke.SOUTH);
				} else if (dy < 0 && !GestureStroke.NORTH.equals(gestureStroke)) {
					gestureStrokes.add(gestureStroke = GestureStroke.NORTH);
				}

				x0 = x1;
				y0 = y1;
			}
		}

		return GestureSequence.create(gestureStrokes);
	}

	private List gestureStrokes;

	private GestureSequence(List gestureStrokes)
		throws IllegalArgumentException {
		super();
		
		if (gestureStrokes == null)
			throw new IllegalArgumentException();
			
		this.gestureStrokes = Collections.unmodifiableList(new ArrayList(gestureStrokes));
		Iterator iterator = this.gestureStrokes.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof GestureStroke))
				throw new IllegalArgumentException();
	}

	public int compareTo(Object object) {
		return Util.compare(gestureStrokes, ((GestureSequence) object).gestureStrokes);
	}
	
	public boolean equals(Object object) {
		return object instanceof GestureSequence && gestureStrokes.equals(((GestureSequence) object).gestureStrokes);
	}
	
	public List getGestureStrokes() {
		return gestureStrokes;
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		Iterator iterator = gestureStrokes.iterator();
		
		while (iterator.hasNext())
			result = result * HASH_FACTOR + ((GestureStroke) iterator.next()).hashCode();

		return result;
	}

	public boolean isChildOf(GestureSequence gestureSequence, boolean equals) {
		if (gestureSequence == null)
			return false;
		
		return Util.isChildOf(gestureStrokes, gestureSequence.gestureStrokes, equals);
	}
}
