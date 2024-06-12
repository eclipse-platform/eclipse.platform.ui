/*******************************************************************************
 * Copyright (c) 2024 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers.internal;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;

/**
 * EraseItem event listener that takes care of coloring the selection color of
 * column viewers. The coloring is only applied if no other erase item listener
 * is registered for the viewer. If other erase item listeners are registers,
 * most probably a other customer coloring is applied and should not be
 * overwritten.
 *
 * @see FocusCellOwnerDrawHighlighter
 */
public class ColumnViewerSelectionColorListener implements Listener {

	/**
	 * Registers an erase item event listener that takes care of coloring the
	 * selection color of the given viewer.
	 *
	 * @param viewer The viewer that should be colored
	 */
	public static void addListenerToViewer(StructuredViewer viewer) {
		viewer.getControl().addListener(SWT.EraseItem, new ColumnViewerSelectionColorListener());
	}

	@Override
	public void handleEvent(Event event) {
		if ((event.detail & SWT.SELECTED) == 0) {
			return; /* item not selected */
		}

		if (event.widget instanceof Control control && !control.isEnabled()) {
			return; /* item is disabled, no coloring required */
		}

		Listener[] eraseItemListeners = event.widget.getListeners(SWT.EraseItem);
		if (eraseItemListeners.length != 1) {
			return; /* other eraseItemListener exists, do not apply coloring */
		}

		GC gc = event.gc;
		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		if (event.widget instanceof Control control && control.isFocusControl()) {
			Color background = colorRegistry.get("org.eclipse.ui.workbench.SELECTED_CELL_BACKGROUND"); //$NON-NLS-1$
			if (background == null) {
				background = event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
			}
			Color foreground = colorRegistry.get("org.eclipse.ui.workbench.SELECTED_CELL_FOREGROUND"); //$NON-NLS-1$
			if (foreground == null) {
				foreground = event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
			}
			event.gc.setBackground(background);
			event.gc.setForeground(foreground);
		} else {
			Color background = colorRegistry.get("org.eclipse.ui.workbench.SELECTED_CELL_BACKGROUND_NO_FOCUS"); //$NON-NLS-1$
			if (background == null) {
				background = event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
			}
			Color foreground = colorRegistry.get("org.eclipse.ui.workbench.SELECTED_CELL_FOREGROUND_NO_FOCUS"); //$NON-NLS-1$
			if (foreground == null) {
				foreground = event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
			}
			event.gc.setBackground(background);
			event.gc.setForeground(foreground);
		}

		int width = event.width;
		if (event.widget instanceof Scrollable scrollable) {
			width = scrollable.getClientArea().width;
		}

		gc.fillRectangle(0, event.y, width, event.height);

		event.detail &= ~SWT.SELECTED;
	}

}
