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
package org.eclipse.jface.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * Contains static methods for performing simple geometric operations
 * on the SWT geometry classes.
 *
 * @since 3.0
 */
public class Geometry {

	/**
	 * Prevent this class from being instantiated.
	 * 
	 * @since 3.0
	 */
	private Geometry() {
	}

	/**
	 * Returns the square of the distance between two points. 
	 * <p>This is preferred over the real distance when searching
	 * for the closest point, since it avoids square roots.</p>
	 * 
	 * @param p1 first endpoint
	 * @param p2 second endpoint
	 * @return the square of the distance between the two points
	 * 
	 * @since 3.0
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
	 * @return the magnitude of the given 2d vector
	 * @since 3.0
	 */
	public static double magnitude(Point p) {
		return Math.sqrt(magnitudeSquared(p));
	}

	/**
	 * Returns the square of the magnitude of the given 2-space vector (represented
	 * using a point)
	 * 
	 * @param p the point whose magnitude is being computed
	 * @return the square of the magnitude of the given vector
	 * @since 3.0
	 */
	public static int magnitudeSquared(Point p) {
		return p.x * p.x + p.y * p.y;
	}

	/**
	 * Returns the size of the rectangle, as a Point
	 * 
	 * @param rectangle rectangle whose size is being computed
	 * @return the size of the given rectangle
	 * @since 3.0
	 */
	public static Point getSize(Rectangle rectangle) {
		return new Point(rectangle.width, rectangle.height);
	}

	/**
	 * Returns a new point whose coordinates are the minimum of the coordinates of the
	 * given points
	 * 
	 * @param p1 a Point
	 * @param p2 a Point
	 * @return a new point whose coordinates are the minimum of the coordinates of the
	 * given points
	 * @since 3.0
	 */
	public static Point min(Point p1, Point p2) {
		return new Point(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y));
	}
	
	/**
	 * Returns a new point whose coordinates are the maximum of the coordinates
	 * of the given points
	 * @param p1 a Point
	 * @param p2 a Point
	 * @return point a new point whose coordinates are the maximum of the coordinates
	 * @since 3.0
	 */
	public static Point max(Point p1, Point p2) {
		return new Point(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y));
	}

	/**
	 * Returns a vector in the given direction with the given
	 * magnitude. Directions are given using SWT direction constants, and
	 * the resulting vector is in the screen's coordinate system. That is,
	 * the vector (0, 1) is down and the vector (1, 0) is right. 
	 * 
	 * @param distance magnitude of the vector
	 * @param direction one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, or SWT.RIGHT
	 * @return a point representing a vector in the given direction with the given magnitude
	 * @since 3.0
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

	/**
	 * Returns the point in the center of the given rectangle.
	 * 
	 * @param rect rectangle being computed
	 * @return a Point at the center of the given rectangle.
	 * @since 3.0
	 */
	public static Point centerPoint(Rectangle rect) {
		return new Point(rect.x + rect.width / 2, rect.y + rect.height / 2);
	}

	/**
	 * Returns the height or width of the given rectangle.
	 * 
	 * @param toMeasure rectangle to measure
	 * @param width returns the width if true, and the height if false
	 * @return the width or height of the given rectangle
	 * @since 3.0
	 */
	public static int getDimension(Rectangle toMeasure, boolean width) {
		if (width) {
			return toMeasure.width;
		} else {
			return toMeasure.height;
		}
	}

	/**
	 * Returns the distance of the given point from a particular side of the given rectangle.
	 * Returns negative values for points outside the rectangle.
	 * 
	 * @param rectangle a bounding rectangle
	 * @param testPoint a point to test
	 * @param edgeOfInterest side of the rectangle to test against
	 * @return the distance of the given point from the given edge of the rectangle
	 * @since 3.0
	 */
	public static int getDistanceFromEdge(Rectangle rectangle, Point testPoint, int edgeOfInterest) {
		switch(edgeOfInterest) {
			case SWT.TOP: return testPoint.y - rectangle.y ;
			case SWT.BOTTOM: return rectangle.y + rectangle.height - testPoint.y;
			case SWT.LEFT: return testPoint.x - rectangle.x;
			case SWT.RIGHT: return rectangle.x + rectangle.width - testPoint.x;
		}
		
		return 0;
	}

	/**
	 * Extrudes the given edge inward by the given distance. That is, if one side of the rectangle
	 * was sliced off with a given thickness, this returns the rectangle that forms the slice. Note
	 * that the returned rectangle will be inside the given rectangle if size > 0.
	 * 
	 * @param toExtrude the rectangle to extrude. The resulting rectangle will share three sides
	 * with this rectangle.
	 * @param size distance to extrude. A negative size will extrude outwards (that is, the resulting
	 * rectangle will overlap the original iff this is positive). 
	 * @param orientation the side to extrude.  One of SWT.LEFT, SWT.RIGHT, SWT.TOP, or SWT.BOTTOM. The 
	 * resulting rectangle will always share this side with the original rectangle.
	 * @return a rectangle formed by extruding the given side of the rectangle by the given distance.
	 * @since 3.0
	 */
	public static Rectangle getExtrudedEdge(Rectangle toExtrude, int size, int orientation) {
		Rectangle bounds = new Rectangle(toExtrude.x, toExtrude.y, toExtrude.width, toExtrude.height);
		
		if (!isHorizontal(orientation)) {
			bounds.width = size;
		} else {
			bounds.height = size;
		}
		
		switch(orientation) {
		case SWT.RIGHT:
			bounds.x = toExtrude.x + toExtrude.width - bounds.width;
			break;
		case SWT.BOTTOM:
			bounds.y = toExtrude.y + toExtrude.height - bounds.height;
			break;
		}
	
		normalize(bounds);
		
		return bounds;
	}

	/**
	 * Returns the opposite of the given direction. That is, returns SWT.LEFT if
	 * given SWT.RIGHT and visa-versa.
	 * 
	 * @param swtDirectionConstant one of SWT.LEFT, SWT.RIGHT, SWT.TOP, or SWT.BOTTOM
	 * @return one of SWT.LEFT, SWT.RIGHT, SWT.TOP, or SWT.BOTTOM
	 * @since 3.0
	 */
	public static int getOppositeSide(int swtDirectionConstant) {
		switch(swtDirectionConstant) {
			case SWT.TOP: return SWT.BOTTOM;
			case SWT.BOTTOM: return SWT.TOP;
			case SWT.LEFT: return SWT.RIGHT;
			case SWT.RIGHT: return SWT.LEFT;
		}
		
		return swtDirectionConstant;
	}

	/**
	 * Converts the given boolean into an SWT orientation constant.
	 * 
	 * @param horizontal if true, returns SWT.HORIZONTAL. If false, returns SWT.VERTICAL 
	 * @return SWT.HORIZONTAL or SWT.VERTICAL.
	 * @since 3.0
	 */
	public static int getSwtHorizontalOrVerticalConstant(boolean horizontal) {
		if (horizontal) {
			return SWT.HORIZONTAL;
		} else {
			return SWT.VERTICAL;
		}
	}

	/**
	 * Returns true iff the given SWT side constant corresponds to a horizontal side
	 * of a rectangle. That is, returns true for the top and bottom but false for the
	 * left and right.
	 * 
	 * @param swtSideConstant one of SWT.TOP, SWT.BOTTOM, SWT.LEFT, or SWT.RIGHT
	 * @return true iff the given side is horizontal.
	 * @since 3.0
	 */
	public static boolean isHorizontal(int swtSideConstant) {
		return !(swtSideConstant == SWT.LEFT || swtSideConstant == SWT.RIGHT);
	}

	/**
	 * Moves the given rectangle by the given delta.
	 * 
	 * @param rect rectangle to move (will be modified)
	 * @param delta direction vector to move the rectangle by
	 * @since 3.0
	 */
	public static void moveRectangle(Rectangle rect, Point delta) {
		rect.x += delta.x;
		rect.y += delta.y;
	}

	/**
	 * Normalizes the given rectangle. That is, any rectangle with
	 * negative width or height becomes a rectangle with positive
	 * width or height that extends to the upper-left of the original
	 * rectangle. 
	 * 
	 * @param rect rectangle to modify
	 * @since 3.0
	 */
	public static void normalize(Rectangle rect) {
		if (rect.width < 0) {
			rect.width = -rect.width;
			rect.x -= rect.width;
		}
		
		if (rect.height < 0) {
			rect.height = -rect.height;
			rect.y -= rect.height;
		}
	}

	/**
	 * Converts the given rectangle from the local coordinate system of the given object
	 * into display coordinates.
	 * 
	 * @param coordinateSystem local coordinate system being converted from
	 * @param toConvert rectangle to convert
	 * @return a rectangle in display coordinates
	 * @since 3.0
	 */
	public static Rectangle toDisplay(Control coordinateSystem, Rectangle toConvert) {
		Point start = coordinateSystem.toDisplay(toConvert.x, toConvert.y);
		return new Rectangle(start.x, start.y, toConvert.width, toConvert.height);
	}
	
}
