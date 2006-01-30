/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.debug.internal.ui.viewers.provisional.ILabelRequestMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Widget;

/**
 * Implementation of an <code>ILabelRequestMonitor</code>. Collects label
 * attributes from an asynchronous label adapter.
 * <p>
 * Not intended to be subclassed or instantiated by clients. For use speficially
 * with <code>AsynchronousViewer</code>.
 * </p>
 * 
 * @since 3.2
 */
class LabelRequestMonitor extends AsynchronousRequestMonitor implements ILabelRequestMonitor {

	/**
	 * Retrieved label text. Only <code>null</code> if cancelled or failed.
	 */
	private String[] fLabels;

	/**
	 * Retrieved image descriptor or <code>null</code>
	 */
	private ImageDescriptor[] fImageDescriptors;

	/**
	 * Retrieved font data or <code>null</code>
	 */
	private FontData[] fFontDatas;

	/**
	 * Retieved colors or <code>null</code>
	 */
	private RGB[] fForegrounds;
	private RGB[] fBackgrounds;

	/**
	 * Cosntructs a request to upate the label of the given widget in the give
	 * viewer.
	 * 
	 * @param widget widget to update
	 * @param viewer viewer containing the widget
	 */
	LabelRequestMonitor(Widget widget, AsynchronousViewer viewer) {
		super(widget, viewer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor#performUpdate()
	 */
	protected void performUpdate() {
		AsynchronousViewer viewer = getViewer();
		Widget widget = getWidget();
		viewer.setLabels(widget, fLabels, fImageDescriptors);
		viewer.setColors(widget, fForegrounds, fBackgrounds);
		viewer.setFonts(widget, fFontDatas);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor#contains(org.eclipse.debug.ui.viewers.AsynchronousRequestMonitor)
	 */
	protected boolean contains(AsynchronousRequestMonitor update) {
		return update instanceof LabelRequestMonitor && update.getWidget() == getWidget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.ILabelRequestMonitor#setLabel(java.lang.String)
	 */
	public void setLabels(String[] text) {
		fLabels = text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.ILabelRequestMonitor#setFontData(org.eclipse.swt.graphics.FontData)
	 */
	public void setFontDatas(FontData[] fontData) {
		fFontDatas = fontData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.ILabelRequestMonitor#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public void setImageDescriptors(ImageDescriptor[] image) {
		fImageDescriptors = image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.ILabelRequestMonitor#setForeground(org.eclipse.swt.graphics.RGB)
	 */
	public void setForegrounds(RGB[] foreground) {
		fForegrounds = foreground;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.viewers.ILabelRequestMonitor#setBackground(org.eclipse.swt.graphics.RGB)
	 */
	public void setBackgrounds(RGB[] background) {
		fBackgrounds = background;
	}

}
