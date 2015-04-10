/*******************************************************************************
 * Copyright (c) 2015 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.css.swt.dom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;

/**
 * Abstract class for dealing with an SWT.EraseItem event which customizes the
 * selection color.
 */
public abstract class AbstractControlSelectionEraseListener implements Listener {

	@Override
	public void handleEvent(Event event) {

		Scrollable control = (Scrollable) event.widget;
		int columnCount = getNumberOfColumns(control);

		boolean selected = (event.detail & SWT.SELECTED) != 0;
		boolean hot = (event.detail & SWT.HOT) != 0;

		// If we're painting the selection we also deal with hotness to be
		// consistent (i.e.: hotness is a 'selection preview on hover').
		event.detail &= ~SWT.HOT;

		if (selected || hot) {

			GC gc = event.gc;
			Rectangle area = control.getClientArea();

			// Handling hotness with more than one column doesn't
			// work well because we'd have to change the clipping
			// for all of that (which isn't really possible)...
			boolean handlingOnlyHot = !selected && hot;
			if (handlingOnlyHot) {
				if (columnCount > 1) {
					return;
				}
			}


			String dataBackgroundKey;
			String dataBorderKey;
			if (handlingOnlyHot) {
				dataBackgroundKey = ControlSelectedColorCustomization.HOT_BACKGROUND_COLOR;
				dataBorderKey = ControlSelectedColorCustomization.HOT_BORDER_COLOR;
			} else {
				dataBackgroundKey = ControlSelectedColorCustomization.SELECTION_BACKGROUND_COLOR;
				dataBorderKey = ControlSelectedColorCustomization.SELECTION_BORDER_COLOR;
			}

			Object dataBackground = control.getData(dataBackgroundKey);
			Object dataBorder = control.getData(dataBorderKey);
			Object dataSelectionForeground = control
					.getData(ControlSelectedColorCustomization.SELECTION_FOREGROUND_COLOR);


			Color background = null;
			if ((dataBackground instanceof Color)) {
				background = (Color) dataBackground;
				if (background.isDisposed()) {
					return;
				}
			}

			Color border = null;
			if ((dataBorder instanceof Color)) {
				border = (Color) dataBorder;
				if (border.isDisposed()) {
					return;
				}
			}

			Color selectionForeground = null;

			if ((dataSelectionForeground instanceof Color)) {
				selectionForeground = (Color) dataSelectionForeground;
			} else {
				// Default is the control foreground color.
				selectionForeground = control.getForeground();
			}
			if (selectionForeground.isDisposed()) {
				return;
			}

			if (background == null && border == null) {
				// Nothing to draw
				return;
			}

			// Update clip to cover the whole column.
			int width = area.width;
			if (event.index == columnCount - 1 || columnCount == 0) {
				// i.e.: we only need to fix this for the last column
				// or if the tree/table reports having no columns (which
				// means single column... really weird hum?)
				if (width > 0) {
					Region region = new Region();
					gc.getClipping(region);
					region.add(event.x, event.y, width, event.height);
					gc.setClipping(region);
					region.dispose();
				}
			}

			if (background != null) {
				Color oldbackground = gc.getBackground();
				gc.setBackground(background);
				try {
					gc.fillRectangle(0, area.y, area.width + 2, area.height);
				} finally {
					gc.setBackground(oldbackground);
				}
			}
			if (border != null) {
				gc.setForeground(border);
				gc.drawRectangle(0, event.y, width - 1, event.height - 1);
			}

			// Restore the foreground for proper drawing later on.
			// This should be the color that SWT uses to draw the
			// foreground later on!
			gc.setForeground(selectionForeground);


			// we just painted the background...
			event.detail &= ~SWT.BACKGROUND;

			fixEventDetail(control, event);
		}
	}

	/**
	 * Subclasses should override to fix the event.detail after the selection
	 * event was handled.
	 *
	 * @param control
	 *            the control which had the erase event.
	 * @param event
	 *            the event to have the details fixed.
	 */
	protected abstract void fixEventDetail(Control control, Event event);

	/**
	 * @param control
	 * @return the number of columns from the control
	 */
	protected abstract int getNumberOfColumns(Control control);

}