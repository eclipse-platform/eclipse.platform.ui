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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;

/**
 * A custom selectable control that can be used to control areas that can be
 * expanded or collapsed. The control control can be toggled between selected
 * and deselected state with a mouse or by pressing 'Enter' while the control
 * has focus.
 * <p>
 * The control is rendered as a triangle that points to the right in the
 * collapsed and down in the expanded state. Triangle color can be changed.
 */

public class BoxToggle extends ToggleHyperlink {

	/**
	 * Creates a control in a provided composite.
	 * 
	 * @param parent
	 *            the parent
	 * @param style
	 *            the style
	 */

	public BoxToggle(Composite parent, int style) {
		super(parent, style);
		innerWidth = 8;
		innerHeight = 8;
	}

	protected void paintHyperlink(PaintEvent e) {
		GC gc = e.gc;
		Rectangle box = getBoxBounds(gc);
		gc.setForeground(
			getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		gc.drawRectangle(box);
		gc.setForeground(getForeground());
		gc.drawLine(box.x + 2, box.y + 4, box.x + 6, box.y + 4);
		if (!isExpanded()) {
			gc.drawLine(box.x + 4, box.y + 2, box.x + 4, box.y + 6);
		}
	}
	private Rectangle getBoxBounds(GC gc) {
		int x = 0;
		int y = 0;

		gc.setFont(getFont());
		int height = gc.getFontMetrics().getHeight();
		y = height / 2 - 4 + 1;
		y = Math.max(y, 0);
		return new Rectangle(x, y, 8, 8);
	}
}