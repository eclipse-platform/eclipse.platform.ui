/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

public class SelectionData {
	public Display display;
	public Color bg;
	public Color fg;
	private Point start;
	private Point stop;
	private ArrayList segments;
	private boolean newLineNeeded;

	public SelectionData(MouseEvent e) {
		display = e.display;
		segments = new ArrayList();
		start = new Point(e.x, e.y);
		stop = new Point(e.x, e.y);
		bg = e.display.getSystemColor(SWT.COLOR_LIST_SELECTION);
		fg = e.display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
	}

	public void markNewLine() {
		newLineNeeded=true;
	}
	public void addSegment(String text) {
		if (newLineNeeded) {
			segments.add(System.getProperty("line.separator")); //$NON-NLS-1$
			newLineNeeded=false;
		}
		segments.add(text);
	}

	public void update(MouseEvent e) {
		//Control c = (Control)e.widget;
		stop.x = e.x;
		stop.y = e.y;
	}
	public void reset() {
		segments.clear();
	}
	public String getSelectionText() {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<segments.size(); i++) {
			buf.append((String)segments.get(i));
		}
		return buf.toString();
	}
	public boolean canCopy() {
		return segments.size()>0;
	}

	private int getTopOffset() {
		return start.y<stop.y?start.y:stop.y;
	}
	private int getBottomOffset() {
		return start.y>stop.y?start.y:stop.y;
	}
	public int getLeftOffset(Locator locator) {
		return isInverted(locator)? stop.x:start.x;
	}
	public int getLeftOffset(int rowHeight) {
		return isInverted(rowHeight) ? stop.x:start.x;
	}
	public int getRightOffset(Locator locator) {
		return isInverted(locator)? start.x: stop.x;
	}
	public int getRightOffset(int rowHeight) {
		return isInverted(rowHeight) ? start.x:stop.x;
	}
	private boolean isInverted(Locator locator) {
		int rowHeight = ((int [])locator.heights.get(locator.rowCounter))[0];
		return isInverted(rowHeight);
	}
	private boolean isInverted(int rowHeight) {
		int deltaY = start.y - stop.y;
		if (Math.abs(deltaY) > rowHeight) {
			// inter-row selection
			return deltaY>0;
		}
		// intra-row selection
		return start.x > stop.x;
	}
	public boolean isEnclosed() {
		return !start.equals(stop);
	}

	public boolean isSelectedRow(Locator locator) {
		if (!isEnclosed())
			return false;
		int rowHeight = ((int [])locator.heights.get(locator.rowCounter))[0];
		return isSelectedRow(locator.y, rowHeight);
	}
	public boolean isSelectedRow(int y, int rowHeight) {
		if (!isEnclosed())
			return false;
		return (y + rowHeight >= getTopOffset() &&
				y <= getBottomOffset());
	}
	public boolean isFirstSelectionRow(Locator locator) {
		if (!isEnclosed())
			return false;
		int rowHeight = ((int [])locator.heights.get(locator.rowCounter))[0];
		return (locator.y + rowHeight >= getTopOffset() &&
				locator.y <= getTopOffset());
	}
	public boolean isFirstSelectionRow(int y, int rowHeight) {
		if (!isEnclosed())
			return false;
		return (y + rowHeight >= getTopOffset() &&
				y <= getTopOffset());
	}
	public boolean isLastSelectionRow(Locator locator) {
		if (!isEnclosed())
			return false;
		int rowHeight = ((int [])locator.heights.get(locator.rowCounter))[0];
		return (locator.y + rowHeight >=getBottomOffset() &&
				locator.y <= getBottomOffset());
	}
	public boolean isLastSelectionRow(int y, int rowHeight) {
		if (!isEnclosed())
			return false;
		return (y + rowHeight >=getBottomOffset() &&
				y <= getBottomOffset());
	}
}
