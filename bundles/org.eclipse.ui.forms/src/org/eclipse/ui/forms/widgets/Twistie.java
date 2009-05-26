/*******************************************************************************
 *  Copyright (c) 2000, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * A custom selectable control that can be used to control areas that can be
 * expanded or collapsed. The control control can be toggled between selected
 * and deselected state with a mouse or by pressing 'Enter' while the control
 * has focus.
 * <p>
 * The control is rendered as a triangle that points to the right in the
 * collapsed and down in the expanded state. Triangle color can be changed.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>None</dd>
 * </dl>
 * 
 * @see TreeNode
 * @since 3.0
 */
public class Twistie extends ToggleHyperlink {
	private static final int[] onPoints = { 0, 2, 8, 2, 4, 6 };

	private static final int[] offPoints = { 2, -1, 2, 8, 6, 4 };

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
		innerWidth = 9;
		innerHeight = 9;
	}

	/*
	 * @see SelectableControl#paint(GC)
	 */
	protected void paintHyperlink(GC gc) {
		Color bg;
		if (!isEnabled())
			bg = getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		else if (hover && getHoverDecorationColor() != null)
			bg = getHoverDecorationColor();
		else if (getDecorationColor() != null)
			bg = getDecorationColor();
		else
			bg = getForeground();
		gc.setBackground(bg);
		int[] data;
		Point size = getSize();
		int x = (size.x - 9) / 2;
		int y = (size.y - 9) / 2;
		if (isExpanded())
			data = translate(onPoints, x, y);
		else
			data = translate(offPoints, x, y);
		gc.fillPolygon(data);
		gc.setBackground(getBackground());
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		redraw();
	}
}
