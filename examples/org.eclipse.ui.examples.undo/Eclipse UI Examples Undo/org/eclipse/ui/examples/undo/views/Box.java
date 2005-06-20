/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.undo.views;


import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

/**
 * 2D Rectangle object
 */
public class Box  {
	private int x1, y1, x2, y2;
	/**
	 * Constructs a box, defined by any two diametrically 
	 * opposing corners.
	 * 
	 * @param x1 the virtual X coordinate of the first corner
	 * @param y1 the virtual Y coordinate of the first corner
	 * @param x2 the virtual X coordinate of the second corner
	 * @param y2 the virtual Y coordinate of the second corner
	 */
	public Box(Point origin, Point corner) {
		x1 = origin.x; 
		y1 = origin.y; 
		x2 = corner.x; 
		y2 = corner.y;
	}
	public void draw(GC gc) {
		gc.setLineStyle(SWT.LINE_SOLID);
		gc.drawRectangle(x1, y1, x2-x1, y2-y1);
	}
	
	public boolean contains(int x, int y) {
		return x >= x1 && 
			x <= x2 && 
			y >= y1 && 
			y <= y2;
	}
}
