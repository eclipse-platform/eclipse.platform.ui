/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands.gestures;

import java.util.Arrays;

import org.eclipse.ui.internal.commands.*;

public final class Gesture implements Comparable {

	private final static int HASH_INITIAL = 27;
	private final static int HASH_FACTOR = 37;

	public static Gesture create(int data, int pen, Point[] points)
		throws IllegalArgumentException {
		return new Gesture(data, pen, points);
	}

	private int data;
	private int pen;
	private Point[] points;

	private Gesture(int data, int pen, Point[] points)
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
		Gesture gesture = (Gesture) object;
		int compareTo = data - gesture.data;

		if (compareTo == 0) {
			compareTo = pen - gesture.pen;

			if (compareTo == 0)
				compareTo = org.eclipse.ui.internal.commands.Util.compare(points, gesture.points);
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof Gesture))
			return false;

		Gesture gesture = (Gesture) object;
		return data == gesture.data && pen == gesture.pen && Arrays.equals(points, gesture.points);
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
