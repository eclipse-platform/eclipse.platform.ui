/*******************************************************************************
 * Copyright (c) 2016 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt.dom.scrollbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;

/**
 * Base implementation of a handler which will draw a themed scroll bar.
 */
/* default */ abstract class AbstractScrollHandler {


	/**
	 * When a drag starts, it should be set to the initial drag pixel position
	 * (otherwise, it must be null).
	 */
	protected int fInitialDragPixel;

	/**
	 * When a drag starts, it should be set to the initial drag mouse position
	 * (otherwise, it must be null).
	 */
	protected Point fInitialDragPosition;

	/**
	 * The scroll bar that we're replacing.
	 */
	protected final ScrollBar fScrollBar;

	/**
	 * Indicates whether the thick version of the scroll should be shown.
	 */
	private boolean fMouseNearScrollScrollBar;

	/**
	 * It's set during the actual painting and reflects the positions which were
	 * used for drawing (and can be later consulted to get the positions for
	 * interacting with it).
	 */
	protected ScrollBarPositions fScrollBarPositions;

	/**
	 * The rect where the handle is being drawn (or null if it's not available).
	 */
	protected Rectangle fHandleDrawnRect;

	/**
	 * The settings to be used to draw the scrollbar.
	 */
	protected IScrollBarSettings fScrollBarSettings;

	/**
	 * Whether the scrollbar should be visible or not. Note that ideally we
	 * should not have a separate property and should honor the same setting
	 * used for the native scrollbar, but unfortunately, it's not possible to
	 * change the ScrollBar instance, so, a separate property was created for
	 * the themed implementation.
	 */
	private boolean fVisible = true;

	private final boolean fInitialVisible;

	/**
	 * @param scrollBar
	 *            The scroll bar for which we'll be drawing the themed
	 *            replacement. Note that if it's null, nothing will be drawn.
	 * @param scrollBarSettings
	 *            The settings for drawing the scrollbar.
	 */
	protected AbstractScrollHandler(ScrollBar scrollBar, IScrollBarSettings scrollBarSettings) {
		fScrollBar = scrollBar;
		boolean initialVisible = true;
		if (scrollBar != null) {
			initialVisible = scrollBar.getVisible();
		}
		this.fInitialVisible = initialVisible;
		this.fScrollBarSettings = scrollBarSettings;
	}

	public void install(AbstractThemedScrollBarAdapter abstractThemedScrollBarAdapter) {
		if (this.fScrollBar != null) {
			fScrollBar.setVisible(false);
			this.fScrollBar.addSelectionListener(abstractThemedScrollBarAdapter);
		}
	}

	public void uninstall(AbstractThemedScrollBarAdapter abstractThemedScrollBarAdapter, boolean disposing) {
		fInitialDragPosition = null;
		fScrollBarPositions = null;
		fHandleDrawnRect = null;
		if (this.fScrollBar != null && !this.fScrollBar.isDisposed() && !disposing) {
			this.fScrollBar.removeSelectionListener(abstractThemedScrollBarAdapter);
			// Restore its initial visibility state.
			// Note: don't do this if we're disposing at this moment as
			// StyledText will throw a NPE.
			this.fScrollBar.setVisible(fInitialVisible);
		}
	}

	public void setVisible(boolean visible) {
		this.fVisible = visible;
	}

	public boolean getVisible() {
		return this.fVisible;
	}


	/**
	 * @return The rect where the handle is being drawn (or null if it's not
	 *         available).
	 */
	public Rectangle getHandleRect() {
		return this.fHandleDrawnRect;
	}

	/**
	 * @param currClientArea
	 *            The current client area on the control.
	 * @return A rectangle which specifies the area under which the thicker
	 *         version of the scrollbar should be used.
	 */
	public abstract Rectangle computeProximityRect(Rectangle currClientArea);

	/**
	 * If the native scrollbar is made visible, asynchronously hides it.
	 * (subclasses must override -- this should not be needed when SWT provides
	 * an API to actually replace the scrollbar).
	 */
	protected void checkScrollbarInvisible() {
	}

	/**
	 *
	 * @param scrollable
	 *            The scrollable to consider.
	 * @param currClientArea
	 *            The currently visible client area.
	 * @param considerMargins
	 *            Whether the margins of the scrollable should be removed from
	 *            the returned rectangle.
	 * @return A rectangle which has the full area covered by the scroll bar.
	 */
	protected abstract Rectangle getFullBackgroundRect(Scrollable scrollable, Rectangle currClientArea,
			boolean considerMargins);

	public boolean startDragOnMouseDown(Scrollable scrollable, Point controlPos, Point currHorizontalAndTopPixel,
			Rectangle currClientArea) {
		if (this.fScrollBar == null || !this.fVisible || !this.fScrollBarSettings.getScrollBarThemed()) {
			return false;
		}
		Rectangle rect = this.getHandleRect();
		if (rect != null) {
			if (rect.contains(controlPos.x, controlPos.y)) {
				fInitialDragPosition = new Point(controlPos.x, controlPos.y);
				fInitialDragPixel = getRelevantPositionFromPos(currHorizontalAndTopPixel);
				return true;
			}
		}
		return false;
	}

	public boolean scrollOnMouseDown(Scrollable scrollable, Point controlPos,
			Rectangle currClientArea) {
		if (this.fScrollBar == null || !this.fVisible || fScrollBarPositions == null
				|| !this.fScrollBarSettings.getScrollBarThemed()) {
			return false;
		}
		Rectangle rect = this.getHandleRect();
		if (rect != null) {
			if (rect.contains(controlPos.x, controlPos.y)) {
				// If the handle is at the click position, we won't scroll.
				return false;
			}
		}
		Rectangle fullRect = this.getFullBackgroundRect(scrollable, currClientArea, true);
		if (fullRect != null) {
			if (fullRect.contains(controlPos.x, controlPos.y)) {
				int pos = this.getRelevantPositionFromPos(controlPos);
				pos = (int) fScrollBarPositions.convertFromScrollBarPosToControlPixel(pos);
				int selection = this.fScrollBar.getSelection();
				int pageIncrement = this.fScrollBar.getPageIncrement();
				if (pos > selection) {
					this.fScrollBar.setSelection(selection + pageIncrement);
					notifyScrollbarSelectionChanged(scrollable, SWT.PAGE_DOWN);
				} else {
					this.fScrollBar.setSelection(selection - pageIncrement);
					notifyScrollbarSelectionChanged(scrollable, SWT.PAGE_UP);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * @return whether the mouse is currently over the scroll bar.
	 */
	public boolean mousePosOverScroll(Scrollable scrollable, Point controlPos) {
		if (this.fScrollBar == null || !this.fVisible || fScrollBarPositions == null
				|| !this.fScrollBarSettings.getScrollBarThemed()) {
			return false;
		}
		Rectangle currClientArea = scrollable.getClientArea();
		Rectangle fullRect = this.getFullBackgroundRect(scrollable, currClientArea, true);
		if (fullRect != null) {
			boolean ret = fullRect.contains(controlPos.x, controlPos.y);
			return ret;
		}
		return false;
	}

	protected abstract int getRelevantPositionFromPos(Point styledTextPos);

	public boolean isDragging() {
		return fInitialDragPosition != null;
	}

	public boolean stopDragOnMouseUp(Scrollable scrollable) {
		if (fInitialDragPosition != null) {
			fInitialDragPosition = null;
			return true;
		}
		return false;
	}

	/**
	 * Sets the current pixel as the first horizontal or vertical pixel to
	 * position the current client area.
	 *
	 * @param scrollable
	 *            The current scrollable control.
	 * @param pixel
	 *            The pixel which should be made the first horizontal or
	 *            vertical pixel to position the current client area.
	 */
	protected abstract void setPixel(Scrollable scrollable, int pixel);

	/**
	 * Makes a drag when a mouse move happens (the initial drag must be already
	 * set at this point and it should drag based on the different from the
	 * initial drag and the mousePos).
	 *
	 * @param scrollable
	 *            The current scrollable control.
	 * @param mousePos
	 *            The current position of the mouse.
	 * @return Whether the drag happened or not.
	 */
	public boolean dragOnMouseMove(Scrollable scrollable, Point mousePos) {
		if (fInitialDragPosition != null && this.fScrollBarPositions != null && this.fScrollBar != null) {
			int currentMousePos = getRelevantPositionFromPos(mousePos);
			int initialMousePos = getRelevantPositionFromPos(fInitialDragPosition);

			int delta = (currentMousePos - initialMousePos);
			delta /= this.fScrollBarPositions.fPercentageOfClientAreaFromTotalArea;
			setPixel(scrollable, fInitialDragPixel + delta);
			notifyScrollbarSelectionChanged(scrollable, SWT.DRAG);
			return true;
		}
		return false;
	}

	/**
	 * Notifies that a scroll event happened.
	 *
	 * @param scrollable
	 *            The current scrollable control.
	 * @param detail
	 *            The detail for the scroll (see
	 *            #org.eclipse.swt.widgets.ScrollBar.wmScrollChild(long, long)).
	 */
	protected void notifyScrollbarSelectionChanged(Scrollable scrollable, int detail) {
		Event e = new Event();
		e.type = SWT.Selection;
		e.x = 0;
		e.y = 0;
		e.button = 0;
		e.stateMask = 0;
		e.count = 1;

		e.display = scrollable.getDisplay();
		e.widget = fScrollBar;
		e.time = 0;
		e.data = null;
		e.character = '\0';
		e.detail = detail;

		e.doit = true;

		fScrollBar.notifyListeners(SWT.Selection, e);
	}

	public void paintControl(GC gc, Rectangle currClientArea, Scrollable scrollable) {
		checkScrollbarInvisible();
		doPaintControl(gc, currClientArea, scrollable);
	}

	public abstract boolean computePositions(Rectangle currClientArea, Scrollable scrollable);

	protected abstract void doPaintControl(GC gc, Rectangle currClientArea, Scrollable scrollable);

	public int getMouseNearScrollScrollBarWidth() {
		return fScrollBarSettings.getMouseNearScrollScrollBarWidth();
	}

	public int getCurrentScrollBarWidth() {
		if (this.fMouseNearScrollScrollBar) {
			return getMouseNearScrollScrollBarWidth();
		}
		return this.fScrollBarSettings.getScrollBarWidth();
	}

	/**
	 * @param cursorNearScroll
	 *            if true we'll show the thicker version of the scroll (i.e.:
	 *            usually, when we have the mouse close to the scroll, a thicker
	 *            version is shown and when it's far-away, a lean version is
	 *            shown.
	 */
	public void setCursorNearScroll(boolean cursorNearScroll) {
		this.fMouseNearScrollScrollBar = cursorNearScroll;
	}

	/**
	 * @return Whether the mouse is near the scroll.
	 */
	public boolean getMouseNearScrollScrollBar() {
		return this.fMouseNearScrollScrollBar;
	}


}
