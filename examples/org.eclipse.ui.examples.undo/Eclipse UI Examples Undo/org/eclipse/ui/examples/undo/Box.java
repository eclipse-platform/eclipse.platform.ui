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
package org.eclipse.ui.examples.undo;


import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * 2D Box that can move itself, hit-test, and draw.
 */
public class Box  {

	/*
	 * The location of the box.
	 */
	public int x1, y1, x2, y2;

	/*
	 * Constructs a box, defined by any two diametrically
	 * opposing corners.
	 */
	public Box(int x1, int y1, int x2, int y2) {
		super();
		set(x1, y1, x2, y2);
	}

	/*
	 * Move the box to a new origin.
	 */
	public void move(Point origin) {
		set(origin.x, origin.y, origin.x + getWidth(), origin.y + getHeight());
	}

	/*
	 * Draw the box with the specified gc.
	 */
	public void draw(GC gc) {
		gc.drawRectangle(x1, y1, x2-x1, y2-y1);
	}

	/*
	 * Set the position of the box
	 */
	private void set(int x1, int y1, int x2, int y2) {
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
	}

	/*
	 * Return true if this box contains the point specified by
	 * the x and y.
	 */
	public boolean contains(int x, int y) {
		return x >= x1 &&
			x <= x2 &&
			y >= y1 &&
			y <= y2;
	}

	/*
	 * Answer the width of the box
	 */
	public int getWidth() {
		return x2 - x1;
	}

	/*
	 * Answer the height of the box
	 */
	public int getHeight() {
		return y2 - y1;
	}
}
