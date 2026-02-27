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
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

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

	private static final String COLOR_SELECTION_BG_FOCUS = "org.eclipse.jface.SELECTION_BACKGROUND_FOCUSED"; //$NON-NLS-1$
	private static final String COLOR_SELECTION_FG_FOCUS = "org.eclipse.jface.SELECTION_FOREGROUND_FOCUSED"; //$NON-NLS-1$
	private static final String COLOR_SELECTION_BG_NO_FOCUS = "org.eclipse.jface.SELECTION_BACKGROUND_NO_FOCUS"; //$NON-NLS-1$
	private static final String COLOR_SELECTION_FG_NO_FOCUS = "org.eclipse.jface.SELECTION_FOREGROUND_NO_FOCUS"; //$NON-NLS-1$

	/**
	 * This class shouldn't be instantiated directly. Attach this listener to a
	 * viewer via {@link #install(StructuredViewer)} instead.
	 */
	private ColumnViewerSelectionColorListener() {
	}

	public static void install(StructuredViewer viewer) {
		Control control = viewer.getControl();
		if (control.isDisposed() || isListenerRegistered(control)) {
			return;
		}

		ColumnViewerSelectionColorListener listener = new ColumnViewerSelectionColorListener();
		registerListener(control, listener);

		// We need both phases
		control.addListener(SWT.EraseItem, listener);
		control.addListener(SWT.PaintItem, listener);
	}

	private static boolean isListenerRegistered(Control control) {
		return control.getData(LISTENER_KEY) != null;
	}

	private static void registerListener(Control control, ColumnViewerSelectionColorListener listener) {
		control.setData(LISTENER_KEY, listener);
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
		Object ownerDrawListener = control.getData(OwnerDrawLabelProvider.OWNER_DRAW_LABEL_PROVIDER_LISTENER);
		return Arrays.stream(listeners).dropWhile(l -> l != this) // ignore listeners before "this"
				.dropWhile(l -> l == this) // also ignore "this"
				.anyMatch(l -> l != ownerDrawListener);
	}

	/**
	 * Gets the foreground color for a selected cell or item in a viewer.
	 *
	 * @param device  The device to get the system color from (in case there is no
	 *                color set in the preferences)
	 * @param focused Whether or not the control that contains the cell or item is
	 *                currently focused.
	 * @return either a color set via preferences or the default system color.
	 */
	public static Color getSelectionForegroundColor(Device device, boolean focused) {
		return getSelectionColor(device, focused ? COLOR_SELECTION_FG_FOCUS : COLOR_SELECTION_FG_NO_FOCUS);
	}

	/**
	 * Gets the background color for a selected cell or item in a viewer.
	 *
	 * @param device  The device to get the system color from (in case there is no
	 *                color set in the preferences)
	 * @param focused Whether or not the control that contains the cell or item is
	 *                currently focused.
	 * @return either a color set via preferences or the default system color.
	 */
	public static Color getSelectionBackgroundColor(Device device, boolean focused) {
		return getSelectionColor(device, focused ? COLOR_SELECTION_BG_FOCUS : COLOR_SELECTION_BG_NO_FOCUS);
	}

	private static Color getSelectionColor(Device device, String key) {
		ColorRegistry registry = JFaceResources.getColorRegistry();

		if (!registry.hasValueFor(key)) {
			RGB systemColor = getSystemColorForId(device, key);
			registry.put(key, systemColor);
		}

		return registry.get(key);
	}

	public static RGB getSystemColorForId(String id) {
		return getSystemColorForId(Display.getDefault(), id);
	}

	private static RGB getSystemColorForId(Device device, String id) {
		return device.getSystemColor(idToColor(id)).getRGB();
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

	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.EraseItem) {
			handleEraseItem(event);
		} else if (event.type == SWT.PaintItem) {
			handlePaintItem(event);
		}
	}

	private void handleEraseItem(Event event) {
		if ((event.detail & SWT.SELECTED) == 0)
			return;
		if (event.widget instanceof Control c && !c.isEnabled())
			return;

		// No need to process if there are more processors (other than
		// TableOwnerDrawSupport) after this one.
		if (hasAdditionalEraseItemListeners(event))
			return;

		Control control = (Control) event.widget;
		GC gc = event.gc;

		Color bg = getSelectionBackgroundColor(event.display, control.isFocusControl());
		Color fg = getSelectionForegroundColor(event.display, control.isFocusControl());

		gc.setBackground(bg);
		gc.setForeground(fg);

		int rowWidth = (event.widget instanceof Scrollable s) ? s.getClientArea().width : event.width;
		gc.fillRectangle(0, event.y, rowWidth, event.height);

		// Prevent native selection drawing
		event.detail &= ~SWT.SELECTED;

	}

	private void handlePaintItem(Event event) {
		// We must not rely on event.detail & SWT.SELECTED because we cleared it above.
		// Instead compute selection state from the widget.
		if (!(event.widget instanceof Table table))
			return;
		if (!(event.item instanceof TableItem item))
			return;

		boolean isSelected = table.isSelected(table.indexOf(item));
		if (!isSelected)
			return; // only adjust painting for selected rows, otherwise leave default owner draw

		GC gc = event.gc;

		// Set the foreground color for selected text/icon painting, if needed
		Color fg = getSelectionColor(event.display,
				table.isFocusControl() ? COLOR_SELECTION_FG_FOCUS : COLOR_SELECTION_FG_NO_FOCUS);
		gc.setForeground(fg);

		// Paint the image explicitly (like TableOwnerDrawSupport does)
		int column = event.index;
		Image image = item.getImage(column);
		if (image != null) {
			Rectangle imageBounds = item.getImageBounds(column);
			Rectangle src = image.getBounds();

			int x = imageBounds.x + Math.max(0, (imageBounds.width - src.width) / 2);
			int y = imageBounds.y + Math.max(0, (imageBounds.height - src.height) / 2);

			gc.drawImage(image, x, y);
		}

		// Text painting:
		// If TableOwnerDrawSupport is installed, it will paint the text itself.
		// If you find text color is wrong on selected rows, you have two choices:
		// 1) Adjust TableOwnerDrawSupport to respect gc foreground, or
		// 2) Paint the text yourself here (requires computing text bounds and applying
		// styles).
	}
}