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
	private boolean horizontal;

	public TrimBarLayout(boolean horizontal) {
		this.horizontal = horizontal;
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
		int totalSpace = horizontal ? wHint : hHint;
		int curMinor = 0;
		int maxMinor = 0;
		int spaceLeft = totalSpace;

		Control[] kids = composite.getChildren();
		for (Control ctrl : kids) {
			if (isSpacer(ctrl))
				continue;

			ctrl.pack(true);
			Point ctrlSize = ctrl.getSize();
			int major = horizontal ? ctrlSize.x : ctrlSize.y;
			int minor = horizontal ? ctrlSize.y : ctrlSize.x;
			if (major <= spaceLeft) {
				if (minor > maxMinor)
					maxMinor = minor;
				spaceLeft -= major;
			} else {
				curMinor += maxMinor;
				maxMinor = minor;
				spaceLeft = totalSpace - major;
			}
		}

		curMinor += maxMinor;
		if (curMinor == 0)
			curMinor = 1; // Hack! returning '0' causes the parent's layout to
							// be 64,64
		return horizontal ? new Point(wHint, curMinor) : new Point(curMinor,
				hHint);
	}

	protected void layout(Composite composite, boolean flushCache) {
		composite.setRedraw(false);
		try {
			Rectangle bounds = composite.getBounds();
			int totalSpace = horizontal ? bounds.width : bounds.height;

			List<Control> curLine = new ArrayList<Control>();
			List<Control> spacers = new ArrayList<Control>();

			int curMinor = 0;
			int maxMinor = 0;
			int spaceLeft = totalSpace;

			Control[] kids = composite.getChildren();
			Control curSpacer = null;
			for (Control ctrl : kids) {
				if (isSpacer(ctrl)) {
					curSpacer = ctrl;
					continue;
				}

				Point ctrlSize = ctrl.getSize();
				int major = horizontal ? ctrlSize.x : ctrlSize.y;
				int minor = horizontal ? ctrlSize.y : ctrlSize.x;
				if (minor > maxMinor)
					maxMinor = minor;

				if (spaceLeft < major) {
					tileLine(curLine, spacers, curMinor, maxMinor, spaceLeft);

					// reset the tiling parameters
					spaceLeft = totalSpace;
					curMinor += maxMinor;
					maxMinor = 0;

					spacers.clear();
					curLine.clear();

					if (curSpacer != null) {
						spacers.add(curSpacer);
						curLine.add(curSpacer);
					}
					curLine.add(ctrl);
					spaceLeft -= major;
				} else {
					spaceLeft -= major;

					if (curSpacer != null) {
						spacers.add(curSpacer);
						curLine.add(curSpacer);
					}
					curLine.add(ctrl);
				}
				curSpacer = null;
			}
			tileLine(curLine, spacers, curMinor, maxMinor, spaceLeft);
		} finally {
			composite.setRedraw(true);
		}
	}

	private void tileLine(List<Control> curLine, List<Control> spacers,
			int curMinor, int maxMinor, int spaceLeft) {
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
		int majorPos = 0;
		int spacerCount = 0;
		for (Control toTile : curLine) {
			if (horizontal)
				toTile.setLocation(majorPos, curMinor);
			else
				toTile.setLocation(curMinor, majorPos);

			if (isSpacer(toTile)) {
				majorPos += spacerWidths[spacerCount++];
			} else {
				int major = horizontal ? toTile.getSize().x
						: toTile.getSize().y;
				majorPos += major;
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
