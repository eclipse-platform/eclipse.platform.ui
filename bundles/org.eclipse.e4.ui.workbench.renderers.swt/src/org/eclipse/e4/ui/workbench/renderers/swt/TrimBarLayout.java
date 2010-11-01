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
	public static String SPACER = "stretch"; //$NON-NLS-1$
	public static String GLUE = "glue"; //$NON-NLS-1$

	private boolean horizontal;

	public int marginLeft = 0;
	public int marginRight = 0;
	public int marginTop = 0;
	public int marginBottom = 0;
	public int wrapSpacing = 0;

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
		int totalSpace = horizontal ? wHint - (marginLeft + marginRight)
				: hHint - (marginTop + marginBottom);
		int totalMinor = horizontal ? (marginTop + marginBottom)
				: (marginLeft + marginRight);
		int maxMinor = 0;
		int spaceLeft = totalSpace;
		int extraLines = 0;

		Control[] kids = composite.getChildren();
		for (int i = 0; i < kids.length; i++) {
			Control ctrl = kids[i];

			if (isSpacer(ctrl))
				continue;

			ctrl.pack(true);
			if (ctrl instanceof Composite
					&& ((Composite) ctrl).getChildren().length == 0) {
				ctrl.setSize(new Point(0, 0));
			}

			Point ctrlSize = ctrl.getSize();
			int major = horizontal ? ctrlSize.x : ctrlSize.y;
			int minor = horizontal ? ctrlSize.y : ctrlSize.x;

			List<Control> segment = new ArrayList<Control>();
			segment.add(ctrl);
			while (i < (kids.length - 2) && isGlue(kids[i + 1])) {
				ctrl = kids[i + 2];
				ctrl.pack(true);
				segment.add(ctrl);
				ctrlSize = ctrl.getSize();
				major += horizontal ? ctrlSize.x : ctrlSize.y;
				int innerMinor = horizontal ? ctrlSize.y : ctrlSize.x;
				if (innerMinor > minor)
					minor = innerMinor;
				i += 2;
			}

			if (major <= spaceLeft) {
				if (minor > maxMinor)
					maxMinor = minor;
				spaceLeft -= major;
			} else {
				extraLines++;
				totalMinor += maxMinor;
				maxMinor = minor;
				spaceLeft = totalSpace - major;
			}
		}

		// Ad the space for the last line
		totalMinor += maxMinor;

		// and the inter-line spacing
		totalMinor += (extraLines * wrapSpacing);

		if (totalMinor == 0)
			totalMinor = 1; // Hack! returning '0' causes the parent's layout to
							// be 64,64
		return horizontal ? new Point(wHint, totalMinor) : new Point(
				totalMinor, hHint);
	}

	protected void layout(Composite composite, boolean flushCache) {
		Rectangle bounds = composite.getBounds();
		int totalSpace = horizontal ? bounds.width - (marginLeft + marginRight)
				: bounds.height - (marginTop + marginBottom);

		List<Control> curLine = new ArrayList<Control>();
		List<Control> spacers = new ArrayList<Control>();

		int curMinor = horizontal ? marginTop : marginLeft;
		int maxMinor = 0;
		int spaceLeft = totalSpace;

		Control[] kids = composite.getChildren();
		Control curSpacer = null;
		for (int i = 0; i < kids.length; i++) {
			Control ctrl = kids[i];

			if (isSpacer(ctrl)) {
				curSpacer = ctrl;
				continue;
			}

			Point ctrlSize = ctrl.getSize();
			int major = horizontal ? ctrlSize.x : ctrlSize.y;
			int minor = horizontal ? ctrlSize.y : ctrlSize.x;

			List<Control> segment = new ArrayList<Control>();
			segment.add(ctrl);
			while (i < (kids.length - 2) && isGlue(kids[i + 1])) {
				ctrl = kids[i + 2];
				segment.add(ctrl);
				ctrlSize = ctrl.getSize();
				major += horizontal ? ctrlSize.x : ctrlSize.y;
				int innerMinor = horizontal ? ctrlSize.y : ctrlSize.x;
				if (innerMinor > minor)
					minor = innerMinor;
				i += 2;
			}

			if (major <= spaceLeft) {
				if (minor > maxMinor)
					maxMinor = minor;

				spaceLeft -= major;

				if (curSpacer != null) {
					spacers.add(curSpacer);
					curLine.add(curSpacer);
				}
				curLine.addAll(segment);
			} else {
				tileLine(curLine, spacers, curMinor, maxMinor, spaceLeft);

				// reset the tiling parameters
				spaceLeft = totalSpace;
				curMinor += maxMinor + wrapSpacing;
				maxMinor = minor;

				spacers.clear();
				curLine.clear();

				if (curSpacer != null) {
					spacers.add(curSpacer);
					curLine.add(curSpacer);
				}
				curLine.addAll(segment);
				spaceLeft -= major;
			}
			curSpacer = null;
		}
		tileLine(curLine, spacers, curMinor, maxMinor, spaceLeft);
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
		int majorPos = horizontal ? marginLeft : marginTop;
		int spacerCount = 0;
		for (Control toTile : curLine) {
			Point ctrlSize = toTile.getSize();
			int ctrlMinor = horizontal ? ctrlSize.y : ctrlSize.x;

			int minorOffset = 0;
			if (ctrlMinor < maxMinor) {
				minorOffset = (maxMinor - ctrlMinor) / 2;
			}

			if (horizontal)
				toTile.setLocation(majorPos, curMinor + minorOffset);
			else
				toTile.setLocation(curMinor + minorOffset, majorPos);

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
		if (element != null && element.getTags().contains(SPACER))
			return true;

		return false;
	}

	private boolean isGlue(Control ctrl) {
		MUIElement element = (MUIElement) ctrl
				.getData(AbstractPartRenderer.OWNING_ME);
		if (element != null && element.getTags().contains(GLUE))
			return true;

		return false;
	}
}
