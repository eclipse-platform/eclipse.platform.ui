/*******************************************************************************
 * Copyright (c) 2025 SAP SE.
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
package org.eclipse.jface.internal.text.contentassist;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Table;

import org.eclipse.jface.internal.text.TableOwnerDrawSupport;
import org.eclipse.jface.viewers.ColumnViewerSelectionColorListener;

/**
 * Provides custom drawing support for completion proposals. This class ensures that completion
 * proposals are always rendered with a focused appearance.
 *
 * <p>
 * This drawing behavior addresses the particular situation where the code completion is triggered
 * via keyboard shortcut, leaving the editor focused. In such cases, without this custom drawing
 * support, the completion proposal would appear unfocused, leading to a suboptimal coloring.
 * </p>
 */
public class CompletionProposalDrawSupport implements Listener {

	/**
	 * Delegate
	 */
	private final TableOwnerDrawSupport fTableOwnerDrawSupport;

	private CompletionProposalDrawSupport(Table table) {
		fTableOwnerDrawSupport= new TableOwnerDrawSupport(table);
	}

	public static void install(Table table) {
		CompletionProposalDrawSupport listener= new CompletionProposalDrawSupport(table);

		// Since this listener delegates to TableOwnerDrawSupport right after doing
		// its own stuff, we need to make sure to listen for all kind of events so
		// that the delegation happens even after a No-Op.
		table.addListener(SWT.Dispose, listener);
		table.addListener(SWT.MeasureItem, listener);
		table.addListener(SWT.EraseItem, listener);
		table.addListener(SWT.PaintItem, listener);
	}

	@Override
	public void handleEvent(Event event) {

		if (event.widget instanceof Control && event.gc != null) {

			boolean isSelected= (event.detail & SWT.SELECTED) != 0;

			if (event.type == SWT.EraseItem && isSelected) {

				final Color backgroundColor= ColumnViewerSelectionColorListener.getBackgroundColor(event.display, isSelected);
				event.gc.setBackground(backgroundColor);

				final int width= (event.widget instanceof Scrollable s) ? s.getClientArea().width : event.width;
				event.gc.fillRectangle(0, event.y, width, event.height);

				// Prevent native selection drawing
				event.detail&= ~SWT.SELECTED;

			} else if (event.type == SWT.PaintItem) {
				final Color foregroundColor= ColumnViewerSelectionColorListener.getForegroundColor(event.display, isSelected);
				event.gc.setForeground(foregroundColor);
			}
		}

		fTableOwnerDrawSupport.handleEvent(event);
	}
}
