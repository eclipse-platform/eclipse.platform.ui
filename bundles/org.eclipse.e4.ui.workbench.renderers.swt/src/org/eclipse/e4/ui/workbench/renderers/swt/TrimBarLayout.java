/*******************************************************************************
 * Copyright (c) 2011, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christian Walther (Indel AG) - Bug 399458: Fix layout overlap in line-wrapped trim bar
 *     Christian Walther (Indel AG) - Bug 389012: Fix division by zero in TrimBarLayout
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ToolBar;

public class TrimBarLayout extends Layout {
	class TrimLine {
		Map<Control, Point> sizeMap = new HashMap<Control, Point>();
		List<Control> ctrls = new ArrayList<Control>();
		int spacerCount = 0;
		int extraSpace = 0;
		int major = 0;
		int minor = 0;

		public void addControl(Control ctrl) {
			Point ctrlSize = computeSize(ctrl);
			int ctrlMajor = horizontal ? ctrlSize.x : ctrlSize.y;
			int ctrlMinor = horizontal ? ctrlSize.y : ctrlSize.x;

			major += ctrlMajor;
			if (ctrlMinor > minor)
				minor = ctrlMinor;

			sizeMap.put(ctrl, ctrlSize);
			ctrls.add(ctrl);

			if (isSpacer(ctrl))
				spacerCount++;
		}

		public void mergeSegment(TrimLine segment) {
			sizeMap.putAll(segment.sizeMap);
			ctrls.addAll(segment.ctrls);

			major += segment.major;
			if (segment.minor > minor)
				minor = segment.minor;

			spacerCount += segment.spacerCount;
		}
	}

	private List<TrimLine> lines = new ArrayList<TrimLine>();

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
		// Clear the current cache
		lines.clear();

		// First, hide any empty toolbars
		MTrimBar bar = (MTrimBar) composite
				.getData(AbstractPartRenderer.OWNING_ME);
		for (MTrimElement te : bar.getChildren()) {
			hideManagedTB(te);
		}

		int totalMajor = horizontal ? wHint - (marginLeft + marginRight)
				: hHint - (marginTop + marginBottom);
		int totalMinor = 0;
		int spaceLeft = totalMajor;

		TrimLine curLine = new TrimLine();
		Control[] kids = composite.getChildren();
		for (int i = 0; i < kids.length; i++) {
			Control ctrl = kids[i];

			// GLUE Handling; gather any glued controls up into a 'segment'
			TrimLine segment = new TrimLine();
			segment.addControl(ctrl);
			while (i < (kids.length - 2) && isGlue(kids[i + 1])) {
				segment.addControl(kids[i + 1]);
				segment.addControl(kids[i + 2]);
				i += 2;
			}

			// Do we have enough space ?
			if (segment.major <= spaceLeft) {
				// Yes, add the segment to the current line
				curLine.mergeSegment(segment);
				spaceLeft -= segment.major;
			} else {
				// No, cache the current line and start a new one
				curLine.extraSpace = spaceLeft;
				lines.add(curLine);
				totalMinor += curLine.minor;

				curLine = segment;
				spaceLeft = totalMajor - segment.major;
			}
		}

		if (curLine.ctrls.size() > 0) {
			curLine.extraSpace = spaceLeft;
			lines.add(curLine);
			totalMinor += curLine.minor;
		}

		// Adjust the 'totalMinor' to account for the margins
		int totalWrapSpacing = (lines.size() - 1) * wrapSpacing;
		totalMinor += horizontal ? (marginTop + marginBottom)
				+ totalWrapSpacing : (marginLeft + marginRight)
				+ totalWrapSpacing;
		Point calcSize = horizontal ? new Point(wHint, totalMinor) : new Point(
				totalMinor, hHint);
		return calcSize;
	}

	private Point computeSize(Control ctrl) {
		Point ctrlSize = ctrl.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		// Hack! the StatusLine doesn't compute a useable size
		if (isStatusLine(ctrl)) {
			ctrlSize.x = 375;
			ctrlSize.y = 26;
		}

		return ctrlSize;
	}

	/**
	 * This is a HACK ! Due to compatibility restrictions we have the case where
	 * we <b>must</b> leave 'empty' toolbars in the trim. This code detects this
	 * particular scenario and hides any TB's of this type...
	 * 
	 * @param te
	 *            The proposed trim element
	 * @return <code>true</code> iff this element represents an empty managed
	 *         TB.
	 */
	private boolean hideManagedTB(MTrimElement te) {
		if (!(te instanceof MToolBar)
				|| !(te.getRenderer() instanceof ToolBarManagerRenderer))
			return false;

		if (!(te.getWidget() instanceof Composite))
			return false;

		Composite teComp = (Composite) te.getWidget();
		Control[] kids = teComp.getChildren();
		if (kids.length != 1 || !(kids[0] instanceof ToolBar))
			return false;

		boolean barVisible = ((ToolBar) kids[0]).getItemCount() > 0;

		// HACK! The trim dragging code uses the visible attribute as well
		// this is a local 'lock' to prevent the layout from messing with it
		if (!te.getTags().contains("LockVisibility")) { //$NON-NLS-1$
			te.setVisible(barVisible);
		}

		return !barVisible;
	}

	protected void layout(Composite composite, boolean flushCache) {
		Rectangle bounds = composite.getBounds();

		// offset the rectangle to allow for the margins
		bounds.x = marginLeft;
		bounds.y = marginTop;
		bounds.width -= (marginLeft + marginRight);
		bounds.height -= (marginTop + marginBottom);

		// If we were called directly we need to fill the caches
		if (lines.size() == 0) {
			if (horizontal)
				computeSize(composite, bounds.width, SWT.DEFAULT, true);
			else
				computeSize(composite, SWT.DEFAULT, bounds.height, true);
		}
		if (lines.size() == 0)
			return;

		for (TrimLine curLine : lines) {
			tileLine(curLine, bounds);
			if (horizontal)
				bounds.y += curLine.minor + wrapSpacing;
			else
				bounds.x += curLine.minor + wrapSpacing;
		}
	}

	/**
	 * @param curLine
	 * @param bounds
	 */
	private void tileLine(TrimLine curLine, Rectangle bounds) {
		int curX = bounds.x;
		int curY = bounds.y;
		int remainingExtraSpace = curLine.extraSpace;
		int remainingSpacerCount = curLine.spacerCount;
		for (Control ctrl : curLine.ctrls) {
			if (ctrl.isDisposed()) {
				continue;
			}
			Point ctrlSize = curLine.sizeMap.get(ctrl);
			int ctrlWidth = ctrlSize.x;
			int ctrlHeight = ctrlSize.y;
			boolean zeroSize = ctrlWidth == 0 && ctrlHeight == 0;

			// If its a 'spacer' then add any available 'extra' space to it
			if (isSpacer(ctrl)) {
				int extra = remainingExtraSpace / remainingSpacerCount;
				if (horizontal) {
					ctrlWidth += extra;
					// leave out 4 pixels at the bottom to avoid overlapping the
					// 1px bottom border of the toolbar (bug 389941)
					ctrl.setBounds(curX, curY, ctrlWidth, curLine.minor - 4);
				} else {
					ctrlHeight += extra;
					ctrl.setBounds(curX, curY, curLine.minor, ctrlHeight);
				}
				zeroSize = false;
				remainingExtraSpace -= extra;
				remainingSpacerCount--;
			}

			if (horizontal) {
				int offset = (curLine.minor - ctrlHeight) / 2;
				if (!isSpacer(ctrl)) {
					if (!zeroSize)
						ctrl.setBounds(curX, curY + offset, ctrlWidth,
								ctrlHeight);
					else
						ctrl.setBounds(curX, curY, 0, 0);
				}
				curX += ctrlWidth;
			} else {
				int offset = (curLine.minor - ctrlWidth) / 2;
				ctrl.setBounds(curX + offset, curY, ctrlWidth, ctrlHeight);
				curY += ctrlHeight;
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

	private boolean isStatusLine(Control ctrl) {
		MUIElement element = (MUIElement) ctrl
				.getData(AbstractPartRenderer.OWNING_ME);
		if (element != null && element.getElementId() != null
				&& element.getElementId().equals("org.eclipse.ui.StatusLine")) //$NON-NLS-1$
			return true;

		return false;
	}

	/**
	 * @param trimPos
	 * @return
	 */
	public Control ctrlFromPoint(Composite trimComp, Point trimPos) {
		if (trimComp == null || trimComp.isDisposed() || lines == null
				|| lines.size() == 0)
			return null;

		Control[] kids = trimComp.getChildren();
		for (int i = 0; i < kids.length; i++) {
			if (kids[i].isDisposed())
				continue;
			if (kids[i].getBounds().contains(trimPos))
				return kids[i];
		}

		return null;
	}
}
