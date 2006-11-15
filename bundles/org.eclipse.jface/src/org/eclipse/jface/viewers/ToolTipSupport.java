/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Fredy Dobler <fredy@dobler.net> - bug 159600
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.jface.util.Policy;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * The TooltipSupport is the class that provides tooltips for ColumnViewers.
 * 
 * @since 3.3 <strong>EXPERIMENTAL</strong> This class or interface has been
 *        added as part of a work in progress. This API may change at any given
 *        time. Please do not use this API without consulting with the
 *        Platform/UI team.
 * 
 */
class ToolTipSupport extends DefaultToolTip {
	private ColumnViewer viewer;

	private static final String LABEL_PROVIDER_KEY = Policy.JFACE
			+ "_LABEL_PROVIDER"; //$NON-NLS-1$

	private static final String ELEMENT_KEY = Policy.JFACE + "_ELEMENT_KEY"; //$NON-NLS-1$

	private static final int DEFAULT_SHIFT_X = 10;

	private static final int DEFAULT_SHIFT_Y = 0;

	ToolTipSupport(ColumnViewer viewer) {
		super(viewer.getControl());
		this.viewer = viewer;
	}

	protected boolean shouldCreateToolTip(Event event) {
		boolean rv = false;
		ViewerRow row = viewer.getViewerRow(new Point(event.x, event.y));

		viewer.getControl().setToolTipText(""); //$NON-NLS-1$
		Point point = new Point(event.x, event.y);

		if (row != null) {
			Object element = row.getItem().getData();

			ViewerColumn viewPart = viewer.getViewerColumn(row
					.getColumnIndex(point));

			if (viewPart == null) {
				return false;
			}

			CellLabelProvider labelProvider = viewPart.getLabelProvider();

			if (labelProvider.useNativeToolTip(element)) {
				String text = labelProvider.getToolTipText(element);
				if (text != null) {
					viewer.getControl().setToolTipText(text);
				}
				rv = false;
			} else {
				setPopupDelay(labelProvider.getToolTipDisplayDelayTime(element));
				setHideDelay(labelProvider.getToolTipTimeDisplayed(element));

				Point shift = labelProvider.getToolTipShift(element);

				if (shift == null) {
					setShift(new Point(DEFAULT_SHIFT_X, DEFAULT_SHIFT_Y));
				} else {
					setShift(new Point(shift.x, shift.y));
				}
				setData(LABEL_PROVIDER_KEY, labelProvider);
				setData(ELEMENT_KEY, element);
				rv = true;
			}
		}

		return rv;
	}

	private void updateData() {
		CellLabelProvider labelProvider = (CellLabelProvider) getData(LABEL_PROVIDER_KEY);
		Object element = getData(ELEMENT_KEY);

		setText(labelProvider.getToolTipText(element));
		setStyle(labelProvider.getToolTipStyle(element));
		setForegroundColor(labelProvider.getToolTipForegroundColor(element));
		setBackgroundColor(labelProvider.getToolTipBackgroundColor(element));
		setFont(labelProvider.getToolTipFont(element));
	}


	protected Composite createToolTipContentArea(Event event, Composite parent) {
		updateData();
		return super.createToolTipContentArea(event, parent);
	}

	protected void afterHideToolTip(Event event) {
		if (event != null && event.widget != viewer.getControl()) {
			if (event.type == SWT.MouseDown) {
				viewer.setSelection(new StructuredSelection());
			} else {
				viewer.getControl().setFocus();
			}
		}
	}
}
