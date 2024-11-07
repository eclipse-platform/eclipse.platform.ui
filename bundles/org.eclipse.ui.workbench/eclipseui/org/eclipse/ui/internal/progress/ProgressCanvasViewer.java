/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import java.util.Collections;
import java.util.List;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.TrimUtil;

/**
 * The ProgressCanvasViewer is the viewer used by progress windows. It displays
 * text on the canvas.
 */
public class ProgressCanvasViewer extends AbstractProgressViewer {
	Canvas canvas;

	Object[] displayedItems = new Object[0];

	/**
	 * Font metrics to use for determining pixel sizes.
	 */
	private FontMetrics fontMetrics;

	private int numShowItems = 1;

	private int maxCharacterWidth;

	private int orientation = SWT.HORIZONTAL;

	/**
	 * Create a new instance of the receiver with the supplied parent and style
	 * bits.
	 *
	 * @param parent      The composite the Canvas is created in
	 * @param style       style bits for the canvas
	 * @param itemsToShow the number of items this will show
	 * @param numChars    The number of characters for the width hint.
	 * @param side        the side to display text, this helps determine horizontal
	 *                    vs vertical
	 */
	ProgressCanvasViewer(Composite parent, int style, int itemsToShow, int numChars, int orientation) {
		super();
		this.orientation = orientation;
		numShowItems = itemsToShow;
		maxCharacterWidth = numChars;
		canvas = new Canvas(parent, style);
		hookControl(canvas);
		// Compute and store a font metric
		GC gc = new GC(canvas);
		gc.setFont(JFaceResources.getDefaultFont());
		fontMetrics = gc.getFontMetrics();
		gc.dispose();
		initializeListeners();
	}

	/**
	 * NE: Copied from ContentViewer. We don't want the OpenStrategy hooked in
	 * StructuredViewer.hookControl otherwise the canvas will take focus since it
	 * has a key listener. We don't want this included in the window's tab traversal
	 * order. Defeating it here is more self-contained then setting the tab list on
	 * the shell or other parent composite.
	 */
	@Override
	protected void hookControl(Control control) {
		control.addDisposeListener(this::handleDispose);
	}

	@Override
	protected Widget doFindInputItem(Object element) {
		return null; // No widgets associated with items
	}

	@Override
	protected Widget doFindItem(Object element) {
		return null; // No widgets associated with items
	}

	@Override
	protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
		canvas.redraw();
	}

	@Override
	protected List getSelectionFromWidget() {
		// No selection on a Canvas
		return Collections.EMPTY_LIST;
	}

	@Override
	protected void internalRefresh(Object element) {
		displayedItems = getSortedChildren(getRoot());
		canvas.redraw();
	}

	@Override
	public void reveal(Object element) {
		// Nothing to do here as we do not scroll
	}

	@Override
	protected void setSelectionToWidget(List l, boolean reveal) {
		// Do nothing as there is no selection
	}

	@Override
	public Control getControl() {
		return canvas;
	}

	private void initializeListeners() {
		canvas.addPaintListener(event -> {

			GC gc = event.gc;
			Transform transform = null;
			if (orientation == SWT.VERTICAL) {
				transform = new Transform(event.display);
				transform.translate(TrimUtil.TRIM_DEFAULT_HEIGHT, 0);
				transform.rotate(90);
			}
			ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();

			int itemCount = Math.min(displayedItems.length, numShowItems);

			int yOffset = 0;
			int xOffset = 0;
			if (numShowItems == 1) {// If there is a single item try to center it
				Rectangle clientArea = canvas.getParent().getClientArea();
				if (orientation == SWT.HORIZONTAL) {
					int size1 = clientArea.height;
					yOffset = size1 - (fontMetrics.getHeight());
					yOffset = yOffset / 2;
				} else {
					int size2 = clientArea.width;
					xOffset = size2 - (fontMetrics.getHeight());
					xOffset = xOffset / 2;
				}
			}

			for (int i = 0; i < itemCount; i++) {
				String string = labelProvider.getText(displayedItems[i]);
				if (string == null) {
					string = "";//$NON-NLS-1$
				}
				if (orientation == SWT.HORIZONTAL) {
					gc.drawString(string, 2, yOffset + (i * fontMetrics.getHeight()), true);
				} else {
					gc.setTransform(transform);
					gc.drawString(string, xOffset + (i * fontMetrics.getHeight()), 2, true);
				}
			}
			if (transform != null)
				transform.dispose();
		});
	}

	@Override
	public void setLabelProvider(IBaseLabelProvider labelProvider) {
		Assert.isTrue(labelProvider instanceof ILabelProvider);
		super.setLabelProvider(labelProvider);
	}

	/**
	 * Get the size hints for the receiver. These are used for layout data.
	 *
	 * @return Point - the preferred x and y coordinates
	 */
	public Point getSizeHints() {

		Display display = canvas.getDisplay();

		GC gc = new GC(canvas);
		FontMetrics fm = gc.getFontMetrics();
		int charWidth = (int) fm.getAverageCharacterWidth();
		int charHeight = fm.getHeight();
		int maxWidth = display.getBounds().width / 2;
		int maxHeight = display.getBounds().height / 6;
		int fontWidth = charWidth * maxCharacterWidth;
		int fontHeight = charHeight * numShowItems;
		if (maxWidth < fontWidth) {
			fontWidth = maxWidth;
		}
		if (maxHeight < fontHeight) {
			fontHeight = maxHeight;
		}
		gc.dispose();
		return new Point(fontWidth, fontHeight);
	}

	@Override
	public void add(JobTreeElement... elements) {
		refresh(true);

	}

	@Override
	public void remove(JobTreeElement... elements) {
		refresh(true);

	}

}
