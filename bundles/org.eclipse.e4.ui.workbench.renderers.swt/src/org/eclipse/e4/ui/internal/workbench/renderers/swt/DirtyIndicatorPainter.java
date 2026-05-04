/*******************************************************************************
 * Copyright (c) 2026 Vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.renderers.swt;

import java.util.function.Predicate;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.workbench.renderers.swt.CTabRendering;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Paints a filled circle in a right-aligned column for dirty rows in editor
 * list popups (Ctrl+E quick switch, chevron drop-down, ...) so they match the
 * close-button overlay drawn by {@code CTabFolderRenderer.drawDirtyIndicator}
 * for tabs. Right-aligning makes it easy to scan the list for dirty editors.
 */
public final class DirtyIndicatorPainter {

	// Diameter and color match CTabFolderRenderer.drawDirtyIndicator so the
	// indicator looks identical to the close-button overlay used on tabs.
	private static final int DIAMETER = 8;

	// Padding on either side of the dot so it does not crowd the text or the
	// cell's right edge.
	private static final int PADDING = DIAMETER;

	private DirtyIndicatorPainter() {
	}

	/**
	 * @return whether the {@link CTabRendering#SHOW_DIRTY_INDICATOR_ON_TABS new
	 *         dirty indicator style} is enabled
	 */
	public static boolean isEnabled() {
		return Platform.getPreferencesService().getBoolean(
				CTabRendering.PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT,
				CTabRendering.SHOW_DIRTY_INDICATOR_ON_TABS,
				CTabRendering.SHOW_DIRTY_INDICATOR_ON_TABS_DEFAULT, null);
	}

	/**
	 * Adds {@link SWT#MeasureItem} and {@link SWT#PaintItem} listeners to
	 * {@code table}. The measure listener reserves space at the right of every
	 * row so the dots line up in a column and do not crowd the text. The paint
	 * listener draws a filled circle right-aligned in that reserved column for
	 * rows whose data passes {@code isDirty}. Both listeners are no-ops while
	 * {@link #isEnabled()} returns {@code false}, so callers can install them
	 * unconditionally.
	 */
	public static void install(Table table, Predicate<Object> isDirty) {
		Listener listener = event -> {
			if (!isEnabled()) {
				return;
			}
			if (!(event.item instanceof TableItem item)) {
				return;
			}
			if (event.type == SWT.MeasureItem) {
				// Reserve space for the dot column on every row so the column
				// width packs wide enough and all dots align.
				event.width += PADDING + DIAMETER + PADDING;
				return;
			}
			if (!isDirty.test(item.getData())) {
				return;
			}
			GC gc = event.gc;
			Rectangle cellBounds = item.getBounds(event.index);
			int x = cellBounds.x + cellBounds.width - DIAMETER - PADDING;
			int y = cellBounds.y + (cellBounds.height - DIAMETER) / 2;
			Color originalBackground = gc.getBackground();
			int originalAntialias = gc.getAntialias();
			gc.setBackground(gc.getForeground());
			gc.setAntialias(SWT.ON);
			gc.fillOval(x, y, DIAMETER, DIAMETER);
			gc.setBackground(originalBackground);
			gc.setAntialias(originalAntialias);
		};
		table.addListener(SWT.MeasureItem, listener);
		table.addListener(SWT.PaintItem, listener);
	}
}
