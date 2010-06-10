/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

public class TrimBarLayout extends Layout {

	public TrimBarLayout() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite
	 * , int, int, boolean)
	 */
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		System.out.println("computeSize"); //$NON-NLS-1$
		int totalSpace = wHint;
		int curY = 0;
		int maxY = 0;
		int spaceLeft = totalSpace;

		Control[] kids = composite.getChildren();
		for (Control ctrl : kids) {
			if (isSpacer(ctrl))
				continue;

			ctrl.pack(true);
			Point ctrlSize = ctrl.getSize();
			if (ctrlSize.x <= spaceLeft) {
				if (ctrlSize.y > maxY)
					maxY = ctrlSize.y;
				spaceLeft -= ctrlSize.x;
			} else {
				curY += maxY;
				maxY = ctrlSize.y;
				spaceLeft = totalSpace - ctrlSize.x;
			}
		}

		curY += maxY;
		return new Point(wHint, curY); // MaxY-1 ?
	}

	protected void layout(Composite composite, boolean flushCache) {
		composite.setRedraw(false);
		try {
			System.out.println("layout"); //$NON-NLS-1$
			Rectangle bounds = composite.getBounds();
			int totalSpace = bounds.width;

			List<Control> curLine = new ArrayList<Control>();
			List<Control> spacers = new ArrayList<Control>();

			int curY = 0;
			int maxY = 0;
			int spaceLeft = totalSpace;

			Control[] kids = composite.getChildren();
			Control curSpacer = null;
			for (Control ctrl : kids) {
				if (isSpacer(ctrl)) {
					curSpacer = ctrl;
					continue;
				}

				Rectangle ctrlBounds = ctrl.getBounds();
				if (ctrlBounds.height > maxY)
					maxY = ctrlBounds.height;

				int length = ctrlBounds.width;

				if (spaceLeft < length) {
					tileLine(curLine, spacers, curY, maxY, spaceLeft);

					// reset the tiling parameters
					spaceLeft = totalSpace;
					curY += maxY;
					maxY = 0;

					spacers.clear();
					curLine.clear();

					if (curSpacer != null) {
						spacers.add(curSpacer);
						curLine.add(curSpacer);
					}
					curLine.add(ctrl);
					spaceLeft -= length;
				} else {
					spaceLeft -= length;

					if (curSpacer != null) {
						spacers.add(curSpacer);
						curLine.add(curSpacer);
					}
					curLine.add(ctrl);
				}
				curSpacer = null;
			}
			tileLine(curLine, spacers, curY, maxY, spaceLeft);
		} finally {
			composite.setRedraw(true);
		}
	}

	private void tileLine(List<Control> curLine, List<Control> spacers,
			int curY, int maxY, int spaceLeft) {
		// Process the elements in the current 'line'
		// First, size the spacers to occupy all the space
		int[] spacerWidths = new int[spacers.size()];
		if (spacers.size() > 0) {
			int spacerWidth = spaceLeft / spacers.size();
			for (int count = 0; count < spacers.size(); count++) {
				if (spaceLeft < spacerWidth)
					spacerWidth = spaceLeft;
				spaceLeft -= spacerWidth;
				spacerWidths[count] = spacerWidth;
			}
		}

		// Now just tile the controls
		int curX = 0;
		int spacerCount = 0;
		for (Control toTile : curLine) {
			toTile.setLocation(curX, curY);
			if (isSpacer(toTile)) {
				curX += spacerWidths[spacerCount++];
			} else {
				curX += toTile.getSize().x;
			}
		}
	}

	private boolean isSpacer(Control ctrl) {
		MUIElement element = (MUIElement) ctrl
				.getData(AbstractPartRenderer.OWNING_ME);
		if (element != null && element.getTags().contains("stretch")) //$NON-NLS-1$
			return true;

		return false;
	}
}
