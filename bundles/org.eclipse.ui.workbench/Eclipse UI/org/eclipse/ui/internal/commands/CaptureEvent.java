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

public final class CaptureEvent implements Comparable {

	private final static int HASH_FACTOR = 27;
	private final static int HASH_INITIAL = 37;

	public static CaptureEvent create(int data, int pen, Point[] points)
		throws IllegalArgumentException {
		return new CaptureEvent(data, pen, points);
	}

	private int data;
	private int pen;
	private Point[] points;

	private CaptureEvent(int data, int pen, Point[] points)
		throws IllegalArgumentException {
		super();
		this.data = data;
		this.pen = pen;

		if (points == null)
			throw new IllegalArgumentException();
		
		points = (Point[]) points.clone();

		for (int i = 0; i < points.length; i++)
			if (points[i] == null)
				throw new IllegalArgumentException();
	
		this.points = points;
	}

	public int compareTo(Object object) {
		CaptureEvent captureEvent = (CaptureEvent) object;
		int compareTo = data - captureEvent.data;

		if (compareTo == 0) {
			compareTo = pen - captureEvent.pen;

			if (compareTo == 0)
				compareTo = Util.compare(points, captureEvent.points);
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof CaptureEvent))
			return false;

		CaptureEvent captureEvent = (CaptureEvent) object;
		return data == captureEvent.data && pen == captureEvent.pen && Arrays.equals(points, captureEvent.points);
	}

	public int getData() {
		return data;
	}

	public int getPen() {
		return pen;
	}

	public Point[] getPoints() {
		return (Point[]) points.clone();
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + data;
		result = result * HASH_FACTOR + pen;

		for (int i = 0; i < points.length; i++)
			result = result * HASH_FACTOR + points[i].hashCode();
		
		return result;
	}
}
