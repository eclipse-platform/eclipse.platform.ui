/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Fredy Dobler <fredy@dobler.net> - bug 159600
 *     Brock Janiczak <brockj@tpg.com.au> - bug 182443
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.jface.util.Policy;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;

/**
 * The ColumnViewerTooltipSupport is the class that provides tooltips for ColumnViewers.
 * 
 * @since 3.3
 * 
 */
public class ColumnViewerToolTipSupport extends DefaultToolTip {
	private ColumnViewer viewer;

	private static final String LABEL_PROVIDER_KEY = Policy.JFACE
			+ "_LABEL_PROVIDER"; //$NON-NLS-1$

	private static final String ELEMENT_KEY = Policy.JFACE + "_ELEMENT_KEY"; //$NON-NLS-1$

	private static final int DEFAULT_SHIFT_X = 10;

	private static final int DEFAULT_SHIFT_Y = 0;

	/**
	 * Enable ToolTip support for the viewer by creating an instance from this
	 * class. To get all necessary informations this support class consults the
	 * {@link CellLabelProvider}.
	 * 
	 * @param viewer
	 *            the viewer the support is attached to
	 * @param style style passed to control tooltip behaviour
	 * 
	 * @param manualActivation
	 *            <code>true</code> if the activation is done manually using
	 *            {@link #show(Point)}
	 */
	protected ColumnViewerToolTipSupport(ColumnViewer viewer, int style, boolean manualActivation ) {
		super(viewer.getControl(),style,manualActivation);
		this.viewer = viewer;
	}

	/**
	 * Enable ToolTip support for the viewer by creating an instance from this
	 * class. To get all necessary informations this support class consults the
	 * {@link CellLabelProvider}.
	 * 
	 * @param viewer
	 *            the viewer the support is attached to
	 */
	public static void enableFor(ColumnViewer viewer) {
		new ColumnViewerToolTipSupport(viewer,ToolTip.NO_RECREATE,false);
	}
	
	/**
	 * Enable ToolTip support for the viewer by creating an instance from this
	 * class. To get all necessary informations this support class consults the
	 * {@link CellLabelProvider}.
	 * 
	 * @param viewer
	 *            the viewer the support is attached to
	 * @param style style passed to control tooltip behaviour
	 * 
	 * @see ToolTip#RECREATE
	 * @see ToolTip#NO_RECREATE
	 */
	public static void enableFor(ColumnViewer viewer, int style) {
		new ColumnViewerToolTipSupport(viewer,style,false);
	}
	
	protected Object getToolTipArea(Event event) {
		return viewer.getCell(new Point(event.x,event.y));
	}

	protected final boolean shouldCreateToolTip(Event event) {
		if( ! super.shouldCreateToolTip(event) ) {
			return false;
		}
		
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
			boolean useNative = labelProvider.useNativeToolTip(element);
			
			String text = labelProvider.getToolTipText(element);
			Image img = null;
			
			if( ! useNative ) {
				img = labelProvider.getToolTipImage(element);
			}
			
			if( useNative || (text == null && img == null ) ) {
				viewer.getControl().setToolTipText(text);
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

				setText(text);
				setImage(img);
				setStyle(labelProvider.getToolTipStyle(element));
				setForegroundColor(labelProvider.getToolTipForegroundColor(element));
				setBackgroundColor(labelProvider.getToolTipBackgroundColor(element));
				setFont(labelProvider.getToolTipFont(element));
				
				// Check if at least one of the values is set
				rv = getText(event) != null || getImage(event) != null;
			}
		}

		return rv;
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
