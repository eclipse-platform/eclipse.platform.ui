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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public final class GestureSupport {

	public final static Stroke EAST = Stroke.create(6);  
	public final static Stroke NORTH = Stroke.create(8);
	public final static Stroke SOUTH = Stroke.create(2);
	public final static Stroke WEST = Stroke.create(4);

	private final static String STROKE_SEPARATOR = " "; //$NON-NLS-1$
	
	public static String formatSequence(Sequence sequence)
		throws IllegalArgumentException {
		if (sequence == null)
			throw new IllegalArgumentException();
			
		int i = 0;
		Iterator iterator = sequence.getStrokes().iterator();
		StringBuffer stringBuffer = new StringBuffer();
		
		while (iterator.hasNext()) {
			if (i != 0)
				stringBuffer.append(STROKE_SEPARATOR);

			stringBuffer.append(formatStroke((Stroke) iterator.next()));
			i++;
		}

		return stringBuffer.toString();
	}

	public static String formatStroke(Stroke stroke) {
		return Util.ZERO_LENGTH_STRING; //TODO
	}
	
	public static Sequence parseSequence(String string)
		throws IllegalArgumentException {
		if (string == null)
			throw new IllegalArgumentException();

		List strokes = new ArrayList();
		StringTokenizer stringTokenizer = new StringTokenizer(string);
				
		while (stringTokenizer.hasMoreTokens())
			strokes.add(parseStroke(stringTokenizer.nextToken()));
			
		return Sequence.create(strokes);
	}	
	
	public static Stroke parseStroke(String string)
		throws IllegalArgumentException {
		if (string == null)
			throw new IllegalArgumentException();
		
		int value = 0; // TODO
		return Stroke.create(value);
	}

	public static Sequence recognize(Point[] points, int sensitivity) {
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
				if (dx > 0 && !EAST.equals(stroke)) {
					strokes.add(stroke = EAST);
				} else if (dx < 0 && !WEST.equals(stroke)) {
					strokes.add(stroke = WEST);
				} else if (dy > 0 && !SOUTH.equals(stroke)) {
					strokes.add(stroke = SOUTH);
				} else if (dy < 0 && !NORTH.equals(stroke)) {
					strokes.add(stroke = NORTH);
				}

				x0 = x1;
				y0 = y1;
			}
		}

		return Sequence.create(strokes);
	}

	private GestureSupport() {
		super();
	}
}
