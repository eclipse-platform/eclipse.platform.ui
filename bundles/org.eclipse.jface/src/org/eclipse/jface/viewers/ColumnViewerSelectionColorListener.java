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
package org.eclipse.jface.viewers;

import java.util.Arrays;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;

/**
 * EraseItem event listener that provides custom selection coloring for JFace
 * viewers. This listener only activates when no custom owner draw label
 * provider is registered, ensuring it doesn't conflict with existing custom
 * drawing implementations.
 * <p>
 * The listener provides different colors for:
 * <ul>
 * <li>Selected items when the control has focus</li>
 * <li>Selected items when the control doesn't have focus</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider
 * @see org.eclipse.jface.viewers.StyledCellLabelProvider
 * @see org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter
 * @since 3.39
 */
public class ColumnViewerSelectionColorListener implements Listener {

	private static final String LISTENER_KEY = "org.eclipse.jface.viewers.selection_color_listener"; //$NON-NLS-1$
	private static final String OWNER_DRAW_LISTENER_KEY = "owner_draw_label_provider_listener"; //$NON-NLS-1$

	private static final String COLOR_SELECTION_BG_FOCUS = "org.eclipse.jface.SELECTION_BACKGROUND_FOCUSED"; //$NON-NLS-1$
	private static final String COLOR_SELECTION_FG_FOCUS = "org.eclipse.jface.SELECTION_FOREGROUND_FOCUSED"; //$NON-NLS-1$
	private static final String COLOR_SELECTION_BG_NO_FOCUS = "org.eclipse.jface.SELECTION_BACKGROUND_NO_FOCUS"; //$NON-NLS-1$
	private static final String COLOR_SELECTION_FG_NO_FOCUS = "org.eclipse.jface.SELECTION_FOREGROUND_NO_FOCUS"; //$NON-NLS-1$

	/**
	 * Registers the selection color listener on the given viewer.
	 * <p>
	 * This method is idempotent - calling it multiple times on the same viewer has
	 * no additional effect.
	 * </p>
	 *
	 * @param viewer the viewer to which the listener should be added
	 */
	public static void addListenerToViewer(StructuredViewer viewer) {
		Control control = viewer.getControl();
		if (control.isDisposed() || isListenerRegistered(control)) {
			return; // Already registered or disposed
		}

		ColumnViewerSelectionColorListener listener = new ColumnViewerSelectionColorListener();
		control.setData(LISTENER_KEY, listener);
		control.addListener(SWT.EraseItem, listener);
	}

	private static boolean isListenerRegistered(Control control) {
		return control.getData(LISTENER_KEY) != null;
	}

	@Override
	public void handleEvent(Event event) {
		if ((event.detail & SWT.SELECTED) == 0) {
			return; // Not selected
		}

		if (event.widget instanceof Control control && !control.isEnabled()) {
			return; // Disabled control
		}

		if (hasAdditionalEraseItemListeners(event)) {
			return; // Let other listeners handle selection
		}

		drawSelection(event);
	}

	/**
	 * Checks if additional EraseItem listeners were registered after this listener
	 * that are NOT the OwnerDrawListener. This allows user code to override the
	 * selection coloring by adding their own EraseItem listener, while still
	 * allowing StyledCellLabelProvider to work (which uses OwnerDrawListener but
	 * doesn't draw selection).
	 *
	 * @param event the erase event
	 * @return <code>true</code> if other custom listeners are present that should
	 *         handle selection, <code>false</code> otherwise
	 */
	private boolean hasAdditionalEraseItemListeners(Event event) {
		if (!(event.widget instanceof Control control)) {
			return false;
		}

		Listener[] listeners = control.getListeners(SWT.EraseItem);
		Object ownerDrawListener = control.getData(OWNER_DRAW_LISTENER_KEY);
		return Arrays.stream(listeners).anyMatch(l -> l != this && l != ownerDrawListener);
	}

	/**
	 * Draws custom selection coloring for the given event.
	 * <p>
	 * This method provides consistent selection rendering across different viewers
	 * and owner draw implementations. It handles both focused and unfocused
	 * selection states using themed colors from the ColorRegistry with appropriate
	 * fallbacks.
	 * </p>
	 *
	 * @param event the erase event containing the widget, GC, and coordinates
	 * @since 3.32
	 */
	public static void drawSelection(Event event) {
		Control control = (Control) event.widget;
		GC gc = event.gc;

		final Color backgroundColor = getSelectionColor(
				control.isFocusControl() ? COLOR_SELECTION_BG_FOCUS : COLOR_SELECTION_BG_NO_FOCUS, event.display);
		final Color foregroundColor = getSelectionColor(
				control.isFocusControl() ? COLOR_SELECTION_FG_FOCUS : COLOR_SELECTION_FG_NO_FOCUS, event.display);

		gc.setBackground(backgroundColor);
		gc.setForeground(foregroundColor);

		int width = event.width;
		if (event.widget instanceof Scrollable scrollable) {
			width = scrollable.getClientArea().width;
		}

		gc.fillRectangle(0, event.y, width, event.height);

		// Remove SELECTED and BACKGROUND flags to prevent native drawing from
		// overwriting our custom colors
		event.detail &= ~(SWT.SELECTED | SWT.BACKGROUND);
	}

	private static Color getSelectionColor(String key, org.eclipse.swt.graphics.Device device) {
		ColorRegistry registry = JFaceResources.getColorRegistry();

		if (!registry.hasValueFor(key)) {
			RGB systemColor = device.getSystemColor(idToColor(key)).getRGB();
			registry.put(key, systemColor);
		}

		return registry.get(key);
	}

	private static int idToColor(String id) {
		return switch (id) {
		case COLOR_SELECTION_BG_FOCUS -> SWT.COLOR_TITLE_BACKGROUND;
		case COLOR_SELECTION_FG_FOCUS -> SWT.COLOR_WHITE;
		case COLOR_SELECTION_BG_NO_FOCUS -> SWT.COLOR_TITLE_INACTIVE_BACKGROUND;
		case COLOR_SELECTION_FG_NO_FOCUS -> SWT.COLOR_TITLE_INACTIVE_FOREGROUND;
		default -> SWT.COLOR_LIST_SELECTION;
		};
	}

}
