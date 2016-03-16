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
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;

public abstract class AbstractThemedScrollBarAdapter
implements ControlListener, Listener, DisposeListener, KeyListener, MouseWheelListener, SelectionListener {

	public static class ScrollBarSettings implements IScrollBarSettings {
		private Color fScrollForegroundColor;
		private Color fScrollBackgroundColor;
		private int fScrollBarWidth = 6;
		private int fMouseNearScrollScrollBarWidth = 15;
		private int fScrollBarBorderRadius;
		private boolean fScrollBarThemed;

		@Override
		public void setScrollBarThemed(boolean themed) {
			fScrollBarThemed = themed;
		}

		@Override
		public boolean getScrollBarThemed() {
			return fScrollBarThemed;
		}

		@Override
		public void setBackgroundColor(Color newColor) {
			this.fScrollBackgroundColor = newColor;
		}

		@Override
		public void setForegroundColor(Color newColor) {
			this.fScrollForegroundColor = newColor;
		}

		@Override
		public Color getForegroundColor() {
			if (this.fScrollForegroundColor != null && this.fScrollForegroundColor.isDisposed()) {
				this.fScrollForegroundColor = null;
			}
			return this.fScrollForegroundColor;
		}

		@Override
		public Color getBackgroundColor() {
			if (this.fScrollBackgroundColor != null && this.fScrollBackgroundColor.isDisposed()) {
				this.fScrollBackgroundColor = null;
			}
			return this.fScrollBackgroundColor;
		}

		@Override
		public void setScrollBarWidth(int width) {
			if (width < 1) {
				width = 1;
			}
			this.fScrollBarWidth = width;
		}

		@Override
		public int getScrollBarWidth() {
			return fScrollBarWidth;
		}

		@Override
		public void setMouseNearScrollScrollBarWidth(int width) {
			if (width < 1) {
				width = 1;
			}
			this.fMouseNearScrollScrollBarWidth = width;
		}

		@Override
		public int getMouseNearScrollScrollBarWidth() {
			return fMouseNearScrollScrollBarWidth;
		}

		@Override
		public void setScrollBarBorderRadius(int radius) {
			if (radius < 0) {
				radius = 0;
			}
			fScrollBarBorderRadius = radius;
		}

		@Override
		public int getScrollBarBorderRadius() {
			return fScrollBarBorderRadius;
		}

	}

	/**
	 * Interface for the scrollbar painter.
	 */
	public static interface IScrollBarPainter extends PaintListener {

		/**
		 * Makes sure that scrollbars are redrawn.
		 */
		void redrawScrollBars();

		void install(Scrollable scrollable);

		void uninstall();
	}

	protected final IScrollBarSettings fScrollBarSettings;

	protected final Scrollable fScrollable;

	protected final IScrollBarPainter fPainter;

	protected final AbstractScrollHandler fHorizontalScrollHandler;

	protected final AbstractScrollHandler fVerticalScrollHandler;

	private final Display fDisplay;

	protected Point fLastHorizontalAndTopPixel = null;

	private Cursor fOldCursor;

	private final ScrollOnMouseDownRunnable fScrollOnMouseDownTimer;

	protected boolean installed = false;

	public AbstractThemedScrollBarAdapter(Scrollable scrollable, AbstractScrollHandler horizontalScrollHandler,
			AbstractScrollHandler verticalScrollHandler, IScrollBarSettings scrollBarSettings) {
		this.fScrollable = scrollable;
		this.fScrollBarSettings = scrollBarSettings;
		fHorizontalScrollHandler = horizontalScrollHandler;
		fVerticalScrollHandler = verticalScrollHandler;
		fPainter = createPaintListener();

		// We need to add ourselves to the display because these events
		// shouldn't get to the actual StyledText (i.e.: we have to handle
		// the click on the drag position without letting the event reach the
		// StyledText). Also, we want to get mouse positions out of the
		// StyledText (to detect if the thicker version of the scroll bar
		// should be used).
		fDisplay = Display.getCurrent();

		fScrollOnMouseDownTimer = new ScrollOnMouseDownRunnable(fScrollable, fDisplay);
	}

	protected void install() {
		if (fScrollable.isDisposed() || fDisplay.isDisposed() || installed) {
			return;
		}
		installed = true;
		fHorizontalScrollHandler.install(this);
		fVerticalScrollHandler.install(this);
		fDisplay.addFilter(SWT.MouseDown, this);
		fDisplay.addFilter(SWT.MouseUp, this);
		fDisplay.addFilter(SWT.MouseMove, this);

		fScrollable.addControlListener(this);
		fScrollable.addKeyListener(this);
		fScrollable.addMouseWheelListener(this);
		fScrollable.addDisposeListener(this);
		fScrollable.addPaintListener(fPainter);

		fPainter.install(fScrollable);
	}

	protected void uninstall(boolean disposing) {
		if (!installed) {
			return;
		}
		installed = false;
		this.fPainter.uninstall();

		if (!fScrollable.isDisposed()) {
			fScrollable.removeControlListener(this);
			fScrollable.removeKeyListener(this);
			fScrollable.removeMouseWheelListener(this);
			fScrollable.removeDisposeListener(this);
			fScrollable.removePaintListener(fPainter);
		}

		if (!fDisplay.isDisposed()) {
			fDisplay.removeFilter(SWT.MouseDown, this);
			fDisplay.removeFilter(SWT.MouseUp, this);
			fDisplay.removeFilter(SWT.MouseMove, this);
		}

		fHorizontalScrollHandler.uninstall(this, disposing);
		fVerticalScrollHandler.uninstall(this, disposing);
	}

	protected abstract IScrollBarPainter createPaintListener();

	private void setArrowCursor() {
		if (fOldCursor == null) {
			Display display = Display.getCurrent();
			fOldCursor = fScrollable.getCursor();
			Cursor arrowCursor = display.getSystemCursor(SWT.CURSOR_ARROW);
			fScrollable.setCursor(arrowCursor);
		}
	}

	private void restoreCursor() {
		if (fOldCursor != null) {
			fOldCursor = null;
			fScrollable.setCursor(null);
		}
	}

	/**
	 * Helper class to keep scrolling when the mouse is down.
	 */
	private static class ScrollOnMouseDownRunnable implements Runnable {

		private final Scrollable fScrollable;
		private final Display fDisplay;
		private AbstractScrollHandler fTargetScrollHandler;
		private Point fControlPos;

		public ScrollOnMouseDownRunnable(Scrollable scrollable, Display display) {
			this.fScrollable = scrollable;
			this.fDisplay = display;
		}

		@Override
		public void run() {
			if (fControlPos == null || fScrollable.isDisposed() || this.fDisplay.isDisposed()) {
				return;
			}
			// Note: we re-click based on the current position.
			Point cursorLocation = fDisplay.getCursorLocation();
			Point point = fScrollable.toControl(cursorLocation);
			Rectangle currClientArea = fScrollable.getClientArea();

			fTargetScrollHandler.scrollOnMouseDown(fScrollable, point, currClientArea);

			// After the first, the interval is only 50 millis
			fDisplay.timerExec(50, this);
		}

		public void start(Point controlPos, AbstractScrollHandler targetScrollHandler) {
			this.fControlPos = controlPos;
			this.fTargetScrollHandler = targetScrollHandler;
			if (controlPos != null && targetScrollHandler != null) {
				// First one is 200 millis
				fDisplay.timerExec(200, this);
			}
		}

		public void stop() {
			this.fControlPos = null;
		}
	}

	@Override
	public void controlMoved(ControlEvent e) {
		setMouseNearScrollScrollBar(false, false);
	}

	@Override
	public void controlResized(ControlEvent e) {
		setMouseNearScrollScrollBar(false, false);
		fPainter.redrawScrollBars();
	}

	@Override
	public void handleEvent(Event event) {
		// pos is always relative to widget
		if (!(event.widget instanceof Control)) {
			return;
		}
		Control control = (Control) event.widget;
		Point displayPos = control.toDisplay(event.x, event.y);
		Point controlPos = fScrollable.toControl(displayPos);

		if (event.type == SWT.MouseDown) {
			fLastHorizontalAndTopPixel = computeHorizontalAndTopPixel();

			if (event.widget == fScrollable) {
				Rectangle currClientArea = fScrollable.getClientArea();
				if (this.fHorizontalScrollHandler.startDragOnMouseDown(fScrollable, controlPos,
						fLastHorizontalAndTopPixel, currClientArea)
						|| this.fVerticalScrollHandler.startDragOnMouseDown(fScrollable, controlPos,
								fLastHorizontalAndTopPixel, currClientArea)) {

					this.stopEventPropagation(event);
				} else if (this.fHorizontalScrollHandler.scrollOnMouseDown(fScrollable, controlPos, currClientArea)) {
					this.fScrollOnMouseDownTimer.start(controlPos, this.fHorizontalScrollHandler);
					this.stopEventPropagation(event);

				} else if (this.fVerticalScrollHandler.scrollOnMouseDown(fScrollable, controlPos, currClientArea)) {
					this.fScrollOnMouseDownTimer.start(controlPos, this.fVerticalScrollHandler);
					this.stopEventPropagation(event);
				}

			}
			checkChangedHorizontalAndTopPixel();

		} else if (event.type == SWT.MouseUp) {
			this.fScrollOnMouseDownTimer.stop();
			this.fHorizontalScrollHandler.stopDragOnMouseUp(fScrollable);
			this.fVerticalScrollHandler.stopDragOnMouseUp(fScrollable);
			checkChangedHorizontalAndTopPixel();

		} else if (event.type == SWT.MouseMove) {
			if (!fHorizontalScrollHandler.isDragging() && !fVerticalScrollHandler.isDragging()) {
				Rectangle currClientArea = fScrollable.getClientArea();

				Rectangle proximityRectHorizontal = fHorizontalScrollHandler.computeProximityRect(currClientArea);
				Rectangle proximityRectVertical = fVerticalScrollHandler.computeProximityRect(currClientArea);

				boolean showHorizontal = ((proximityRectHorizontal != null
						&& proximityRectHorizontal.contains(controlPos.x, controlPos.y))
						&& currClientArea.width < (fHorizontalScrollHandler.fScrollBar.getMaximum()
								- fHorizontalScrollHandler.fScrollBar.getMinimum()));

				boolean showVertical = ((proximityRectVertical != null
						&& proximityRectVertical.contains(controlPos.x, controlPos.y))
						&& currClientArea.height < (fVerticalScrollHandler.fScrollBar.getMaximum()
								- fVerticalScrollHandler.fScrollBar.getMinimum()));

				if (showVertical || showHorizontal) {
					setMouseNearScrollScrollBar(showVertical, showHorizontal);
					setArrowCursor();
				} else {
					setMouseNearScrollScrollBar(false, false);
					restoreCursor();
				}
			}

			if (fHorizontalScrollHandler.dragOnMouseMove(fScrollable, controlPos)
					|| fVerticalScrollHandler.dragOnMouseMove(fScrollable, controlPos)) {
				this.stopEventPropagation(event);
				return;
			}
			checkChangedHorizontalAndTopPixel();
		}
	}

	/**
	 * Makes sure that the passed event is no longer propagated.
	 */
	private void stopEventPropagation(Event event) {
		// Note: just setting event.doit does not work (the display
		// keeps on going with it, so, we set the event type to None
		// -- which then stops it).
		event.type = SWT.None;
		event.doit = false;
	}

	protected boolean setMouseNearScrollScrollBar(boolean mouseNearVerticalScroll, boolean mouseNearHorizontalScroll) {
		if (mouseNearVerticalScroll != fVerticalScrollHandler.getMouseNearScrollScrollBar()
				|| mouseNearHorizontalScroll != fHorizontalScrollHandler.getMouseNearScrollScrollBar()) {
			fHorizontalScrollHandler.setCursorNearScroll(mouseNearHorizontalScroll);
			fVerticalScrollHandler.setCursorNearScroll(mouseNearVerticalScroll);
			fPainter.redrawScrollBars();
			return true;
		}
		return false;
	}

	protected void checkChangedHorizontalAndTopPixel() {
		Point newHorizontalAndTopPixel = computeHorizontalAndTopPixel();
		if (!newHorizontalAndTopPixel.equals(fLastHorizontalAndTopPixel)) {
			fLastHorizontalAndTopPixel = newHorizontalAndTopPixel;
			fPainter.redrawScrollBars();
		}
	}

	protected abstract Point computeHorizontalAndTopPixel();

	// API to customize scroll bar.
	public void setScrollBarThemed(boolean themed) {
		if (themed != getScrollBarThemed()) {
			fScrollBarSettings.setScrollBarThemed(themed);
			if (themed) {
				install();
			} else {
				uninstall(false);
			}
		}
	}

	public boolean getScrollBarThemed() {
		return fScrollBarSettings.getScrollBarThemed();
	}

	public void setScrollBarBackgroundColor(Color newColor) {
		fScrollBarSettings.setBackgroundColor(newColor);
		this.fPainter.redrawScrollBars();
	}

	public Color getScrollBarBackgroundColor() {
		return fScrollBarSettings.getBackgroundColor();
	}

	public void setScrollBarForegroundColor(Color newColor) {
		fScrollBarSettings.setForegroundColor(newColor);
		this.fPainter.redrawScrollBars();
	}

	public Color getScrollBarForegroundColor() {
		return fScrollBarSettings.getForegroundColor();
	}

	public void setScrollBarWidth(int width) {
		fScrollBarSettings.setScrollBarWidth(width);
		this.fPainter.redrawScrollBars();
	}

	public int getScrollBarWidth() {
		return fScrollBarSettings.getScrollBarWidth();
	}

	public void setMouseNearScrollScrollBarWidth(int width) {
		fScrollBarSettings.setMouseNearScrollScrollBarWidth(width);
		this.fPainter.redrawScrollBars();
	}

	public int getMouseNearScrollScrollBarWidth() {
		return fScrollBarSettings.getMouseNearScrollScrollBarWidth();
	}

	public void setVerticalScrollBarVisible(boolean visible) {
		this.fVerticalScrollHandler.setVisible(visible);
	}

	public void setHorizontalScrollBarVisible(boolean visible) {
		this.fHorizontalScrollHandler.setVisible(visible);
	}

	public boolean getVerticalScrollBarVisible() {
		return this.fVerticalScrollHandler.getVisible();
	}

	public boolean getHorizontalScrollBarVisible() {
		return this.fHorizontalScrollHandler.getVisible();
	}

	public void setScrollBarBorderRadius(int radius) {
		fScrollBarSettings.setScrollBarBorderRadius(radius);
		this.fPainter.redrawScrollBars();
	}

	public int getScrollBarBorderRadius() {
		return fScrollBarSettings.getScrollBarBorderRadius();
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		uninstall(true);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		checkChangedHorizontalAndTopPixel();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		checkChangedHorizontalAndTopPixel();
	}

	@Override
	public void mouseScrolled(MouseEvent e) {
		checkChangedHorizontalAndTopPixel();
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		checkChangedHorizontalAndTopPixel();
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {

	}
}
