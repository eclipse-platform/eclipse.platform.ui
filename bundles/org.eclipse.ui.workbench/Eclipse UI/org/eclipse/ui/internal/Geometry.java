/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Contains static methods for performing simple geometric operations
 * on the SWT geometry classes.
 */
public class Geometry {

	private Geometry() {
	}

	/**
	 * Returns the square of the distance between two points
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static int distanceSquared(Point p1, Point p2) {
		int term1 = p1.x - p2.x;
		int term2 = p1.y - p2.y;
		return term1 * term1 + term2 * term2;
	}

	/**
	 * Returns the magnitude of the given 2d vector (represented as a Point)
	 *  
	 * @param p point representing the 2d vector whose magnitude is being computed
	 * @return
	 */
	public static double magnitude(Point p) {
		return Math.sqrt(magnitudeSquared(p));
	}

	/**
	 * Returns the square of the magnitude of the given 2-space vector (represented
	 * using a point)
	 * 
	 * @param p
	 * @return the square of the magnitude of the given vector
	 */
	public static int magnitudeSquared(Point p) {
		return p.x * p.x + p.y * p.y;
	}

	/**
	 * Returns the area of the rectangle
	 * 
	 * @param rectangle
	 * @return
	 */
	public static Point getSize(Rectangle rectangle) {
		return new Point(rectangle.width, rectangle.height);
	}

	/**
	 * Returns a new point whose coordinates are the minimum of the coordinates of the
	 * given points
	 * 
	 * @param p1
	 * @param p2
	 * @return a new point whose coordinates are the minimum of the coordinates of the
	 * given points
	 */
	public static Point min(Point p1, Point p2) {
		return new Point(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y));
	}

	/**
	 * Returns a direction vector in the given direction with the given
	 * magnitude.
	 * 
	 * @param distance magnitude of the vector
	 * @param direction one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, or SWT.RIGHT
	 * @return a point representing a vector in the given direction with the given magnitude
	 */
	public static Point getDirectionVector(int distance, int direction) {
		switch (direction) {
			case SWT.TOP: return new Point(0, -distance);
			case SWT.BOTTOM: return new Point(0, distance);
			case SWT.LEFT: return new Point(-distance, 0);
			case SWT.RIGHT: return new Point(distance, 0);
		}
		
		return new Point(0,0);
	}
	
}
