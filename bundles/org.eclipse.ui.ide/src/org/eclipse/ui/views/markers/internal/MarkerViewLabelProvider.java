/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * The MarkerViewLabelProvider is a label provider for an individual field.
 * 
 * @since 3.3
 * 
 */
public class MarkerViewLabelProvider extends ColumnLabelProvider {

	IField field;

	/**
	 * Create a MarkerViewLabelProvider on a field
	 * 
	 * @param field
	 */
	MarkerViewLabelProvider(IField field) {
		this.field = field;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		return field.getValue(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		return field.getImage(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getFont(java.lang.Object)
	 */
	public Font getFont(Object element) {
		if (element instanceof ProblemMarker)
			return null;
		return JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#erase(org.eclipse.swt.widgets.Event,
	 *      java.lang.Object)
	 */
	// protected void erase(Event event, Object element) {
	//
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#measure(org.eclipse.swt.widgets.Event,
	 *      java.lang.Object)
	 */
	// protected void measure(Event event, Object element) {
	// // Do nothing by default
	//
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#paint(org.eclipse.swt.widgets.Event,
	 *      java.lang.Object)
	 */
	// protected void paint(Event event, Object element) {
	//
	// Image image = field.getImage(element);
	// int textStart = event.x;
	// if (image != null) {
	// event.gc.drawImage(image, event.x, event.y);
	// textStart += image.getBounds().width
	// + IDialogConstants.HORIZONTAL_SPACING;
	// }
	// if (!(element instanceof ProblemMarker))
	// event.gc.setFont(JFaceResources.getFontRegistry().getBold(
	// JFaceResources.DEFAULT_FONT));
	//
	// checkBackground((TreeItem) event.item);
	//
	// event.gc.drawText(field.getValue(element), textStart, event.y, true);
	// }
	/**
	 * @param event
	 */
	// private void checkBackground(TreeItem item) {
	//
	// if (item.getParentItem() == null) {// top level items
	// int index = item.getParent().indexOf(item);
	// setBackgroundColor(item, index % 2 == 0);
	// } else {
	// TreeItem parent = item.getParentItem();
	// boolean parentBusy = parent.getParent().indexOf(parent) % 2 == 0;
	// int index = parent.indexOf(item);
	// if (parentBusy)
	// setBackgroundColor(item, index % 2 > 0);
	// else
	// setBackgroundColor(item, index % 2 == 0);
	// }
	//
	// }
	/**
	 * Set the background color if showColor is true.
	 * 
	 * @param item
	 * @param showColor
	 */
	// private void setBackgroundColor(TreeItem item, boolean showColor) {
	// if (showColor)
	// item.setBackground(item.getParent().getDisplay().getSystemColor(
	// SWT.COLOR_WIDGET_LIGHT_SHADOW));
	//
	// }
}
