/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.window;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * This class gives implementors to provide customized tooltips for any control.
 * 
 * @since 3.3 <strong>EXPERIMENTAL</strong> This class or interface has been
 *        added as part of a work in progress. This API may change at any given
 *        time. Please do not use this API without consulting with the
 *        Platform/UI team.
 */
public abstract class ToolTip {
	private Control control;

	private int xShift = 3;

	private int yShift = 0;

	private int popupDelay = 0;

	private int hideDelay = 0;

	private ToolTipOwnerControlListener listener;

	private HashMap data;

	// Ensure that only one tooltip is active in time
	private static Shell CURRENT_TOOLTIP;

	private TooltipHideListener hideListener = new TooltipHideListener();

	private boolean hideOnMouseDown = true;

	/**
	 * Create new instance which add TooltipSupport to the widget
	 * 
	 * @param control
	 *            the control on whose action the tooltip is shown
	 */
	public ToolTip(Control control) {
		this.control = control;
		this.control.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				deactivate();
			}

		});

		this.listener = new ToolTipOwnerControlListener();
		activate();
	}

	/**
	 * Restore arbitary data under the given key
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void setData(String key, Object value) {
		if (data == null) {
			data = new HashMap();
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
	 * display the tooltip. By default the tooltip is shifted 3 pixels to the
	 * left
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
	}

	/**
	 * Should the tooltip displayed because of the given event.
	 * <p>
	 * <b>Subclasses may overwrite this to get custom behaviour</b>
	 * </p>
	 * 
	 * @param event
	 *            the event
	 * @return <code>true</code> if tooltip should be displayed
	 */
	protected boolean shouldCreateToolTip(Event event) {
		return true;
	}

	private Shell toolTipCreate(final Event event) {
		if (shouldCreateToolTip(event)) {
			Shell shell = new Shell(control.getShell(), SWT.ON_TOP | SWT.TOOL
					| SWT.NO_FOCUS);
			shell.setLayout(new FillLayout());
			shell.setLocation(control.toDisplay(event.x + xShift, event.y
					+ yShift));

			toolTipOpen(shell, event);

			return shell;
		}

		return null;
	}

	private void toolTipShow(Shell tip, Event event) {
		if (!tip.isDisposed()) {
			createToolTipContentArea(event, tip);
			if( isHideOnMouseDown() ) {
				toolTipHookBothRecursively(tip);
			} else {
				toolTipHookByTypeRecursively(tip,true,SWT.MouseExit);
			}
			
			tip.pack();
			tip.setVisible(true);
		}
	}

	private void toolTipHide(Shell tip, Event event) {
		if (tip != null && !tip.isDisposed()) {
			tip.dispose();
			CURRENT_TOOLTIP = null;
			afterHideToolTip(event);
		}
	}

	private void toolTipOpen(final Shell shell, final Event event) {
		// Ensure that only one Tooltip is shown in time
		if (CURRENT_TOOLTIP != null) {
			toolTipHide(CURRENT_TOOLTIP, null);
		}

		CURRENT_TOOLTIP = shell;

		if (popupDelay > 0) {
			control.getDisplay().timerExec(popupDelay, new Runnable() {
				public void run() {
					toolTipShow(shell, event);
				}
			});
		} else {
			toolTipShow(CURRENT_TOOLTIP, event);
		}

		if (hideDelay > 0) {
			control.getDisplay().timerExec(popupDelay + hideDelay,
					new Runnable() {

						public void run() {
							toolTipHide(shell, null);
						}
					});
		}
	}

	private void toolTipHookByTypeRecursively(Control c, boolean add, int type) {
		if( add ) {
			c.addListener(type, hideListener);
		} else {
			c.removeListener(type, hideListener);
		}

		if (c instanceof Composite) {
			Control[] children = ((Composite) c).getChildren();
			for (int i = 0; i < children.length; i++) {
				toolTipHookByTypeRecursively(children[i],add,type);
			}
		}
	}
	
	private void toolTipHookBothRecursively(Control c) {
		c.addListener(SWT.MouseDown, hideListener);
		c.addListener(SWT.MouseExit, hideListener);
		
		if (c instanceof Composite) {
			Control[] children = ((Composite) c).getChildren();
			for (int i = 0; i < children.length; i++) {
				toolTipHookBothRecursively(children[i]);
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
	protected abstract Composite createToolTipContentArea(Event event, Composite parent);

	
	/**
	 * This method is called after a Tooltip is hidden.
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
		if( CURRENT_TOOLTIP != null && ! CURRENT_TOOLTIP.isDisposed() ) {
			// Only change if value really changed
			if( hideOnMouseDown != this.hideOnMouseDown ) {
				control.getDisplay().syncExec(new Runnable() {

					public void run() {
						if( CURRENT_TOOLTIP != null && CURRENT_TOOLTIP.isDisposed() ) {
							toolTipHookByTypeRecursively(CURRENT_TOOLTIP, hideOnMouseDown, SWT.MouseDown);
						}
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
		public void handleEvent(Event event) {
			switch (event.type) {
			case SWT.Dispose:
			case SWT.KeyDown:
			case SWT.MouseMove:
			case SWT.MouseDown:
				toolTipHide(CURRENT_TOOLTIP, event);
				break;
			case SWT.MouseHover:
				toolTipCreate(event);
				break;
			}
		}
	}

	private class TooltipHideListener implements Listener {
		public void handleEvent(Event event) {
			if (event.widget instanceof Control) {

				Control c = (Control) event.widget;
				Shell shell = c.getShell();

				switch (event.type) {
				case SWT.MouseDown:
					if (isHideOnMouseDown()) {
						toolTipHide(shell, event);
					}
					break;
				case SWT.MouseExit:
					/*
					 * Give some insets to ensure we get exit informations from
					 * a wider area ;-)
					 */
					Rectangle rect = shell.getBounds();
					rect.x += 5;
					rect.y += 5;
					rect.width -= 10;
					rect.height -= 10;

					if (!rect.contains(c.getDisplay().getCursorLocation())) {
						toolTipHide(shell, event);
					}

					break;
				}
			}
		}
	}
}
