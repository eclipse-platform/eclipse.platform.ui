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


import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;

/**
 * A collection of boxes
 */
public class Boxes  {

	/*
	 * The "model," a list of boxes
	 */
	private List<Box> boxes = new ArrayList<Box>();

	/*
	 * Constructs a box collection
	 */
	public Boxes() {
		super();
	}

	/*
	 * Add the specified box to the group of boxes.
	 */
	public void add(Box box) {
		boxes.add(box);
	}

	/*
	 * Remove the specified box from the group of boxes.
	 */
	public void remove(Box box) {
		boxes.remove(box);
	}

	/*
	 * Clear all the boxes from the list of boxes.
	 */
	public void clear() {
		boxes = new ArrayList<Box>();
	}

	/*
	 * Return true if the group of boxes contains the specified box.
	 */
	public boolean contains(Box box) {
		return boxes.contains(box);
	}

	/*
	 * Draw the boxes with the specified gc.
	 */
	public void draw(GC gc) {
		for (int i = 0; i < boxes.size(); i++) {
			boxes.get(i).draw(gc);
		}
	}

	/*
	 * Return the box containing the specified x and y, or null
	 * if no box contains the point.
	 */
	public Box getBox(int x, int y) {
		for (int i=0; i< boxes.size(); i++) {
			Box box = boxes.get(i);
			if (box.contains(x, y)) {
				return box;
			}
		}
		return null;
	}

	/*
	 * Return the list of boxes known by this group of boxes.
	 */
	public List<Box> getBoxes() {
		return boxes;
	}

	/*
	 * Set the list of boxes known by this group of boxes.
	 */
	public void setBoxes(List<Box> boxes) {
		this.boxes = boxes;
	}

}
