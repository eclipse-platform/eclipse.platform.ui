/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.parts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * A custom selectable control that can be used to control areas that can be
 * expanded or collapsed. The control control can be toggled between selected
 * and deselected state with a mouse or by pressing 'Enter' while the control
 * has focus.
 * <p>
 * The control is rendered as a triangle that points to the right in the
 * collapsed and down in the expanded state. Triangle color can be changed.
 */

public class Twistie extends SelectableControl {
	private boolean selection;
	private Color decorationColor;
	private Color activeColor;
	private Cursor activeCursor;
	private boolean hover = false;
	private static final int marginWidth = 2;
	private static final int marginHeight = 2;
	private static final int WIDTH = 9;
	private static final int HEIGHT = 9;
	private static final int[] offPoints = { 0, 2, 8, 2, 4, 6 };
	private static final int[] onPoints = { 2, -1, 2, 8, 6, 4 };

	/**
	 * Creates a control in a provided composite.
	 * 
	 * @param parent
	 *            the parent
	 * @param style
	 *            the style
	 */

	public Twistie(Composite parent, int style) {
		super(parent, style);
		addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				selection = !selection;
				redraw();
			}
		});

		addListener(SWT.MouseEnter, new Listener() {
			public void handleEvent(Event e) {
				hover = true;
				if (activeCursor != null)
					setCursor(activeCursor);
				redraw();
			}
		});
		addListener(SWT.MouseExit, new Listener() {
			public void handleEvent(Event e) {
				hover = false;
				if (activeCursor != null)
					setCursor(null);
				redraw();
			}
		});
		initAccessible();
	}

	/**
	 * Sets the color of the twistie triangle.
	 * 
	 * @param decorationColor
	 */

	public void setDecorationColor(Color decorationColor) {
		this.decorationColor = decorationColor;
	}

	/**
	 * Returns the color of the twistie triangle.
	 * 
	 * @return
	 */

	public Color getDecorationColor() {
		return decorationColor;
	}

	/**
	 * Sets the active color of the twistie triangle. Active color will be used
	 * when mouse enters the twistie area.
	 * 
	 * @param activeColor
	 *            the active color to use
	 */

	public void setActiveDecorationColor(Color activeColor) {
		this.activeColor = activeColor;
	}

	/**
	 * Returns the active color of the twistie triangle.
	 * 
	 * @return the active twistie color
	 */
	public Color getActiveDecorationColor() {
		return activeColor;
	}

	/**
	 * Sets the active cursor. This cursor will be used when the mouse enters
	 * the twistie area.
	 * 
	 * @param activeCursor
	 */
	public void setActiveCursor(Cursor activeCursor) {
		this.activeCursor = activeCursor;
	}
	/**
	 * Computes the size of the control.
	 * @param wHint width hint
	 * @param hHint height hint
	 * @param changed if true, flush any saved layout state
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		int width, height;

		if (wHint != SWT.DEFAULT)
			width = wHint;
		else
			width = WIDTH + 2 * marginWidth;
		if (hHint != SWT.DEFAULT)
			height = hHint;
		else
			height = HEIGHT + 2 * marginHeight;
		return new Point(width, height);
	}

	/**
	 * Returns the state of the twistie control. When twistie is in the normal
	 * (downward) state, the value is <samp>false</samp>. Collapsed
	 * twistie will return <samp>true</samp>.
	 * @return <samp>true</samp> if collapsed, <samp>false</samp> otherwise.
	 */
	public boolean getSelection() {
		return selection;
	}

	/**
	 * Sets the state of the twistie control
	 * @param selection
	 */
	public void setSelection(boolean selection) {
		this.selection = selection;
		redraw();
	}

	/*
	 * @see SelectableControl#paint(GC)
	 */
	protected void paint(GC gc) {
		if (hover && activeColor != null)
			gc.setBackground(activeColor);
		else if (decorationColor != null)
			gc.setBackground(decorationColor);
		else
			gc.setBackground(getForeground());
		int[] data;
		Point size = getSize();
		int x = (size.x - 9) / 2;
		int y = (size.y - 9) / 2;
		if (selection)
			data = translate(onPoints, x, y);

		else
			data = translate(offPoints, x, y);
		gc.fillPolygon(data);
		gc.setBackground(getBackground());
	}

	private void initAccessible() {
		getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getHelp(AccessibleEvent e) {
				e.result = getToolTipText();
			}
		});

		getAccessible()
			.addAccessibleControlListener(new AccessibleControlAdapter() {
			public void getChildAtPoint(AccessibleControlEvent e) {
				Point testPoint = toControl(new Point(e.x, e.y));
				if (getBounds().contains(testPoint)) {
					e.childID = ACC.CHILDID_SELF;
				}
			}

			public void getLocation(AccessibleControlEvent e) {
				Rectangle location = getBounds();
				Point pt = toDisplay(new Point(location.x, location.y));
				e.x = pt.x;
				e.y = pt.y;
				e.width = location.width;
				e.height = location.height;
			}

			public void getChildCount(AccessibleControlEvent e) {
				e.detail = 0;
			}

			public void getRole(AccessibleControlEvent e) {
				e.detail = ACC.ROLE_TREE;
			}

			public void getState(AccessibleControlEvent e) {
				e.detail =
					Twistie.this.getSelection()
						? ACC.STATE_EXPANDED
						: ACC.STATE_COLLAPSED;
			}

			public void getValue(AccessibleControlEvent e) {
				e.result = Twistie.this.getSelection() ? "1" : "0";
			}
		});
	}

	private int[] translate(int[] data, int x, int y) {
		int[] target = new int[data.length];
		for (int i = 0; i < data.length; i += 2) {
			target[i] = data[i] + x;
		}
		for (int i = 1; i < data.length; i += 2) {
			target[i] = data[i] + y;
		}
		return target;
	}
}
