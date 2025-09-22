/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *                                                 bugfix in: 195137, 198089, 225190
 *******************************************************************************/

package org.eclipse.jface.window;

import java.util.HashMap;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * This class gives implementors to provide customized tooltips for any control.
 *
 * @since 3.3
 */
public abstract class ToolTip {
	private final Control control;

	private int xShift = 3;

	private int yShift = 0;

	private int popupDelay = 0;

	private int hideDelay = 0;

	private final ToolTipOwnerControlListener listener;

	private HashMap<String, Object> data;

	// Ensure that only one tooltip is active in time
	private static Shell CURRENT_TOOLTIP;

	/**
	 * Recreate the tooltip on every mouse move
	 */
	public static final int RECREATE = 1;

	/**
	 * Don't recreate the tooltip as long the mouse doesn't leave the area
	 * triggering the tooltip creation
	 */
	public static final int NO_RECREATE = 1 << 1;

	private final TooltipHideListener hideListener = new TooltipHideListener();

	private final Listener shellListener;

	private boolean hideOnMouseDown = true;

	private boolean respectDisplayBounds = true;

	private boolean respectMonitorBounds = true;

	private final int style;

	private Object currentArea;

	/**
	 * Create new instance which add TooltipSupport to the widget
	 *
	 * @param control
	 *            the control on whose action the tooltip is shown
	 */
	public ToolTip(Control control) {
		this(control, RECREATE, false);
	}

	/**
	 * @param control
	 *            the control to which the tooltip is bound
	 * @param style
	 *            style passed to control tooltip behavior
	 *
	 * @param manualActivation
	 *            <code>true</code> if the activation is done manually using
	 *            {@link #show(Point)}
	 * @see #RECREATE
	 * @see #NO_RECREATE
	 */
	public ToolTip(Control control, int style, boolean manualActivation) {
		this.control = control;
		this.style = style;
		this.listener = new ToolTipOwnerControlListener();
		this.shellListener = event -> {
			if (ToolTip.this.control != null
					&& !ToolTip.this.control.isDisposed()) {
				ToolTip.this.control.getDisplay().asyncExec(() -> {
					// Check if the new active shell is the tooltip
					// itself
					if (ToolTip.this.control != null && !ToolTip.this.control.isDisposed()
							&& ToolTip.this.control.getDisplay().getActiveShell() != CURRENT_TOOLTIP) {
						toolTipHide(CURRENT_TOOLTIP, event);
					}
				});
			}
		};

		if (!manualActivation) {
			activate();
		}
	}

	/**
	 * Restore arbitrary data under the given key
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void setData(String key, Object value) {
		if (data == null) {
			data = new HashMap<>();
		}
		data.put(key, value);
	}

	/**
	 * Get the data restored under the key
	 *
	 * @param key
	 *            the key
	 * @return data or <code>null</code> if no entry is restored under the key
	 */
	public Object getData(String key) {
		if (data != null) {
			return data.get(key);
		}
		return null;
	}

	/**
	 * Set the shift (from the mouse position triggered the event) used to
	 * display the tooltip.
	 * <p>
	 * By default the tooltip is shifted 3 pixels to the right.
	 * </p>
	 *
	 * @param p
	 *            the new shift
	 */
	public void setShift(Point p) {
		xShift = p.x;
		yShift = p.y;
	}

	/**
	 * Activate tooltip support for this control
	 */
	public void activate() {
		deactivate();
		control.addListener(SWT.Dispose, listener);
		control.addListener(SWT.MouseHover, listener);
		control.addListener(SWT.MouseMove, listener);
		control.addListener(SWT.MouseExit, listener);
		control.addListener(SWT.MouseDown, listener);
		control.addListener(SWT.MouseWheel, listener);
	}

	/**
	 * Deactivate tooltip support for the underlying control
	 */
	public void deactivate() {
		control.removeListener(SWT.Dispose, listener);
		control.removeListener(SWT.MouseHover, listener);
		control.removeListener(SWT.MouseMove, listener);
		control.removeListener(SWT.MouseExit, listener);
		control.removeListener(SWT.MouseDown, listener);
		control.removeListener(SWT.MouseWheel, listener);
	}

	/**
	 * Return whether the tooltip respects bounds of the display.
	 *
	 * @return <code>true</code> if the tooltip respects bounds of the display
	 */
	public boolean isRespectDisplayBounds() {
		return respectDisplayBounds;
	}

	/**
	 * Set to <code>false</code> if display bounds should not be respected or to
	 * <code>true</code> if the tooltip is should repositioned to not overlap the
	 * display bounds.
	 * <p>
	 * Default is <code>true</code>
	 * </p>
	 *
	 * @param respectDisplayBounds <code>false</code> if tooltip is allowed to
	 *                             overlap display bounds
	 */
	public void setRespectDisplayBounds(boolean respectDisplayBounds) {
		this.respectDisplayBounds = respectDisplayBounds;
	}

	/**
	 * Return whether the tooltip respects bounds of the monitor.
	 *
	 * @return <code>true</code> if tooltip respects the bounds of the monitor
	 */
	public boolean isRespectMonitorBounds() {
		return respectMonitorBounds;
	}

	/**
	 * Set to <code>false</code> if monitor bounds should not be respected or to
	 * <code>true</code> if the tooltip is should repositioned to not overlap the
	 * monitors bounds. The monitor the tooltip belongs to is the same is control's
	 * monitor the tooltip is shown for.
	 * <p>
	 * Default is <code>true</code>
	 * </p>
	 *
	 * @param respectMonitorBounds <code>false</code> if tooltip is allowed to
	 *                             overlap monitor bounds
	 */
	public void setRespectMonitorBounds(boolean respectMonitorBounds) {
		this.respectMonitorBounds = respectMonitorBounds;
	}

	/**
	 * Should the tooltip displayed because of the given event.
	 * <p>
	 * <b>Subclasses may overwrite this to get custom behavior</b>
	 * </p>
	 *
	 * @param event
	 *            the event
	 * @return <code>true</code> if tooltip should be displayed
	 */
	protected boolean shouldCreateToolTip(Event event) {
		if ((style & NO_RECREATE) != 0) {
			Object tmp = getToolTipArea(event);

			// No new area close the current tooltip
			if (tmp == null) {
				hide();
				return false;
			}

			return !tmp.equals(currentArea);
		}

		return true;
	}

	/**
	 * This method is called before the tooltip is hidden
	 *
	 * @param event
	 *            the event trying to hide the tooltip
	 * @return <code>true</code> if the tooltip should be hidden
	 */
	private boolean shouldHideToolTip(Event event) {
		if (event != null && event.type == SWT.MouseMove
				&& (style & NO_RECREATE) != 0) {
			Object tmp = getToolTipArea(event);

			// No new area close the current tooltip
			if (tmp == null) {
				hide();
				return false;
			}

			return !tmp.equals(currentArea);
		}

		return true;
	}

	/**
	 * This method is called to check for which area the tooltip is
	 * created/hidden for. In case of {@link #NO_RECREATE} this is used to
	 * decide if the tooltip is hidden recreated.
	 *
	 * <code>By the default it is the widget the tooltip is created for but could be any object. To decide if
	 * the area changed the {@link Object#equals(Object)} method is used.</code>
	 *
	 * @param event
	 *            the event
	 * @return the area responsible for the tooltip creation or
	 *         <code>null</code> this could be any object describing the area
	 *         (e.g. the {@link Control} onto which the tooltip is bound to, a
	 *         part of this area e.g. for {@link ColumnViewer} this could be a
	 *         {@link ViewerCell})
	 */
	protected Object getToolTipArea(Event event) {
		return control;
	}

	/**
	 * Start up the tooltip programmatically
	 *
	 * @param location
	 *            the location relative to the control the tooltip is shown
	 */
	public void show(Point location) {
		Event event = new Event();
		event.x = location.x;
		event.y = location.y;
		event.widget = control;
		toolTipCreate(event);
	}

	private Shell toolTipCreate(final Event event) {
		if (shouldCreateToolTip(event)) {
			Shell shell = new Shell(control.getShell(), SWT.ON_TOP | SWT.TOOL
					| SWT.NO_FOCUS);
			shell.setLayout(new FillLayout());

			toolTipOpen(shell, event);

			return shell;
		}

		return null;
	}

	private void toolTipShow(Shell tip, Event event) {
		if (!tip.isDisposed()) {
			currentArea = getToolTipArea(event);
			createToolTipContentArea(event, tip);
			if (isHideOnMouseDown()) {
				toolTipHookBothRecursively(tip);
			} else {
				toolTipHookByTypeRecursively(tip, true, SWT.MouseExit);
			}

			tip.pack();
			Point size = tip.getSize();
			Point location = fixupDisplayBounds(size, getLocation(size, event));

			// Need to adjust a bit more if the mouse cursor.y == tip.y and
			// the cursor.x is inside the tip
			Point cursorLocation = tip.getDisplay().getCursorLocation();

			if (cursorLocation.y == location.y && location.x < cursorLocation.x
					&& location.x + size.x > cursorLocation.x) {
				location.y -= 2;
			}

			tip.setLocation(location);
			tip.setVisible(true);
		}
	}

	private Point fixupDisplayBounds(Point tipSize, Point location) {
		if (respectDisplayBounds || respectMonitorBounds) {
			Rectangle bounds;
			Point rightBounds = new Point(tipSize.x + location.x, tipSize.y
					+ location.y);

			Monitor[] ms = control.getDisplay().getMonitors();

			if (respectMonitorBounds && ms.length > 1) {
				// By default present in the monitor of the control
				bounds = control.getMonitor().getBounds();
				Point p = new Point(location.x, location.y);

				// Search on which monitor the event occurred
				Rectangle tmp;
				for (Monitor element : ms) {
					tmp = element.getBounds();
					if (tmp.contains(p)) {
						bounds = tmp;
						break;
					}
				}

			} else {
				bounds = control.getDisplay().getBounds();
			}

			if (!(bounds.contains(location) && bounds.contains(rightBounds))) {
				if (rightBounds.x > bounds.x + bounds.width) {
					location.x -= rightBounds.x - (bounds.x + bounds.width);
				}

				if (rightBounds.y > bounds.y + bounds.height) {
					location.y -= rightBounds.y - (bounds.y + bounds.height);
				}

				if (location.x < bounds.x) {
					location.x = bounds.x;
				}

				if (location.y < bounds.y) {
					location.y = bounds.y;
				}
			}
		}

		return location;
	}

	/**
	 * Get the display relative location where the tooltip is displayed.
	 * Subclasses may overwrite to implement custom positioning.
	 *
	 * @param tipSize
	 *            the size of the tooltip to be shown
	 * @param event
	 *            the event triggered showing the tooltip
	 * @return the absolute position on the display
	 */
	public Point getLocation(Point tipSize, Event event) {
		return control.toDisplay(event.x + xShift, event.y + yShift);
	}

	private void toolTipHide(Shell tip, Event event) {
		if (tip != null && !tip.isDisposed() && shouldHideToolTip(event)) {
			control.getShell().removeListener(SWT.Deactivate, shellListener);
			currentArea = null;
			passOnEvent(tip, event);
			tip.dispose();
			CURRENT_TOOLTIP = null;
			afterHideToolTip(event);
		}
		if (event != null && event.type == SWT.Dispose) {
			deactivate();
			data = null;
		}
	}

	private void passOnEvent(Shell tip, Event event) {
		if (control != null && !control.isDisposed() && event != null
				&& event.widget != control && event.type == SWT.MouseDown) {
			// the following was left in order to fix bug 298770 with minimal change. In 3.7, the complete method should be removed.
			tip.close();
		}
	}

	private void toolTipOpen(final Shell shell, final Event event) {
		// Ensure that only one Tooltip is shown in time
		if (CURRENT_TOOLTIP != null) {
			toolTipHide(CURRENT_TOOLTIP, null);
		}

		CURRENT_TOOLTIP = shell;

		control.getShell().addListener(SWT.Deactivate, shellListener);

		if (popupDelay > 0) {
			control.getDisplay().timerExec(popupDelay, () -> toolTipShow(shell, event));
		} else {
			toolTipShow(CURRENT_TOOLTIP, event);
		}

		if (hideDelay > 0) {
			control.getDisplay().timerExec(popupDelay + hideDelay,
					() -> toolTipHide(shell, null));
		}
	}

	private void toolTipHookByTypeRecursively(Control c, boolean add, int type) {
		if (add) {
			c.addListener(type, hideListener);
		} else {
			c.removeListener(type, hideListener);
		}

		if (c instanceof Composite) {
			Control[] children = ((Composite) c).getChildren();
			for (Control element : children) {
				toolTipHookByTypeRecursively(element, add, type);
			}
		}
	}

	private void toolTipHookBothRecursively(Control c) {
		c.addListener(SWT.MouseDown, hideListener);
		c.addListener(SWT.MouseExit, hideListener);

		if (c instanceof Composite) {
			Control[] children = ((Composite) c).getChildren();
			for (Control element : children) {
				toolTipHookBothRecursively(element);
			}
		}
	}

	/**
	 * Creates the content area of the the tooltip.
	 *
	 * @param event
	 *            the event that triggered the activation of the tooltip
	 * @param parent
	 *            the parent of the content area
	 * @return the content area created
	 */
	protected abstract Composite createToolTipContentArea(Event event,
			Composite parent);

	/**
	 * This method is called after a tooltip is hidden.
	 * <p>
	 * <b>Subclasses may override to clean up requested system resources</b>
	 * </p>
	 *
	 * @param event
	 *            event triggered the hiding action (may be <code>null</code>
	 *            if event wasn't triggered by user actions directly)
	 */
	protected void afterHideToolTip(Event event) {

	}

	/**
	 * Set the hide delay.
	 *
	 * @param hideDelay
	 *            the delay before the tooltip is hidden. If <code>0</code>
	 *            the tooltip is shown until user moves to other item
	 */
	public void setHideDelay(int hideDelay) {
		this.hideDelay = hideDelay;
	}

	/**
	 * Set the popup delay.
	 *
	 * @param popupDelay
	 *            the delay before the tooltip is shown to the user. If
	 *            <code>0</code> the tooltip is shown immediately
	 */
	public void setPopupDelay(int popupDelay) {
		this.popupDelay = popupDelay;
	}

	/**
	 * Return if hiding on mouse down is set.
	 *
	 * @return <code>true</code> if hiding on mouse down in the tool tip is on
	 */
	public boolean isHideOnMouseDown() {
		return hideOnMouseDown;
	}

	/**
	 * If you don't want the tool tip to be hidden when the user clicks inside
	 * the tool tip set this to <code>false</code>. You maybe also need to
	 * hide the tool tip yourself depending on what you do after clicking in the
	 * tooltip (e.g. if you open a new {@link Shell})
	 *
	 * @param hideOnMouseDown
	 *            flag to indicate of tooltip is hidden automatically on mouse
	 *            down inside the tool tip
	 */
	public void setHideOnMouseDown(final boolean hideOnMouseDown) {
		// Only needed if there's currently a tooltip active
		if (CURRENT_TOOLTIP != null && !CURRENT_TOOLTIP.isDisposed()) {
			// Only change if value really changed
			if (hideOnMouseDown != this.hideOnMouseDown) {
				control.getDisplay().syncExec(() -> {
					if (CURRENT_TOOLTIP != null
							&& CURRENT_TOOLTIP.isDisposed()) {
						toolTipHookByTypeRecursively(CURRENT_TOOLTIP,
								hideOnMouseDown, SWT.MouseDown);
					}
				});
			}
		}

		this.hideOnMouseDown = hideOnMouseDown;
	}

	/**
	 * Hide the currently active tool tip
	 */
	public void hide() {
		toolTipHide(CURRENT_TOOLTIP, null);
	}

	private class ToolTipOwnerControlListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			switch (event.type) {
			case SWT.Dispose:
			case SWT.KeyDown:
			case SWT.MouseDown:
			case SWT.MouseMove:
			case SWT.MouseWheel:
				toolTipHide(CURRENT_TOOLTIP, event);
				break;
			case SWT.MouseHover:
				toolTipCreate(event);
				break;
			case SWT.MouseExit:
				/*
				 * Check if the mouse exit happened because we move over the
				 * tooltip
				 */
				if (CURRENT_TOOLTIP != null && !CURRENT_TOOLTIP.isDisposed()) {
					if (CURRENT_TOOLTIP.getBounds().contains(
							control.toDisplay(event.x, event.y))) {
						break;
					}
				}

				toolTipHide(CURRENT_TOOLTIP, event);
				break;
			}
		}
	}

	private class TooltipHideListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			if (event.widget instanceof Control c) {

				Shell shell = c.getShell();

				switch (event.type) {
				case SWT.MouseDown:
					if (isHideOnMouseDown()) {
						toolTipHide(shell, event);
					}
					break;
				case SWT.MouseExit:
					Rectangle rect = shell.getBounds();
					if (!rect.contains(c.getDisplay().getCursorLocation())) {
						toolTipHide(shell, event);
					}

					break;
				}
			}
		}
	}
}
