/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.gestures;

import java.util.Arrays;

import org.eclipse.ui.internal.util.Util;

public final class CaptureEvent implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		CaptureEvent.class.getName().hashCode();

	public static CaptureEvent create(int data, int pen, Point[] points) {
		return new CaptureEvent(data, pen, points);
	}

	private int data;
	private int pen;
	private Point[] points;

	private CaptureEvent(int data, int pen, Point[] points) {
		super();
		this.data = data;
		this.pen = pen;

		if (points == null)
			throw new NullPointerException();

		points = (Point[]) points.clone();

		for (int i = 0; i < points.length; i++)
			if (points[i] == null)
				throw new IllegalArgumentException();

		this.points = points;
	}

	public int compareTo(Object object) {
		CaptureEvent castedObject = (CaptureEvent) object;
		int compareTo = data - castedObject.data;

		if (compareTo == 0) {
			compareTo = pen - castedObject.pen;

			if (compareTo == 0)
				compareTo = Util.compare(points, castedObject.points);
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof CaptureEvent))
			return false;

		CaptureEvent castedObject = (CaptureEvent) object;
		return data == castedObject.data
			&& pen == castedObject.pen
			&& Arrays.equals(points, castedObject.points);
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
