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

import org.eclipse.ui.internal.util.Util;

public final class XCaptureEvent implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = XCaptureEvent.class.getName().hashCode();

	public static XCaptureEvent create(int data, int pen, XPoint[] points)
		throws IllegalArgumentException {
		return new XCaptureEvent(data, pen, points);
	}

	private int data;
	private int pen;
	private XPoint[] points;

	private XCaptureEvent(int data, int pen, XPoint[] points)
		throws IllegalArgumentException {
		super();
		this.data = data;
		this.pen = pen;

		if (points == null)
			throw new IllegalArgumentException();
		
		points = (XPoint[]) points.clone();

		for (int i = 0; i < points.length; i++)
			if (points[i] == null)
				throw new IllegalArgumentException();
	
		this.points = points;
	}

	public int compareTo(Object object) {
		XCaptureEvent captureEvent = (XCaptureEvent) object;
		int compareTo = data - captureEvent.data;

		if (compareTo == 0) {
			compareTo = pen - captureEvent.pen;

			if (compareTo == 0)
				compareTo = Util.compare(points, captureEvent.points);
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof XCaptureEvent))
			return false;

		XCaptureEvent captureEvent = (XCaptureEvent) object;
		return data == captureEvent.data && pen == captureEvent.pen && Arrays.equals(points, captureEvent.points);
	}

	public int getData() {
		return data;
	}

	public int getPen() {
		return pen;
	}

	public XPoint[] getPoints() {
		return (XPoint[]) points.clone();
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
