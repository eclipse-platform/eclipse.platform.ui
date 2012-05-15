/*******************************************************************************
 * Copyright (c) 2006, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pawel Piech (Wind River) - added support for a virtual tree model viewer (Bug 242489)
 *     Patrick Chuong (Texas Instruments) - added support for checkbox (Bug 286310)
 *     John Cortell (Freescale) - updated javadoc tags (Bug 292301)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.debug.internal.core.commands.Request;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * @since 3.3
 */
class LabelUpdate extends Request implements ILabelUpdate, ICheckUpdate {
	
	private TreePath fElementPath;
	private String[] fColumnIds;
	private RGB[] fBackgrounds;
	private RGB[] fForegrounds;
	private ImageDescriptor[] fImageDescriptors;
	private String[] fLabels;
	private FontData[] fFontDatas;
	private TreeModelLabelProvider fProvider;
	private int fNumColumns; 
	private IPresentationContext fContext;
	private Object fViewerInput;
	private boolean fChecked;
	private boolean fGrayed;
	
	/**
	 * @param viewerInput input at the time the request was made
	 * @param elementPath element the label is for
	 * @param provider label provider to callback to 
	 * @param columnIds column identifiers or <code>null</code>
	 * @param context presentation context
	 */
	public LabelUpdate(Object viewerInput, TreePath elementPath, TreeModelLabelProvider provider, String[] columnIds, IPresentationContext context) {
		fContext = context;
		fViewerInput = viewerInput;
		fElementPath = elementPath;
		fProvider = provider;
		fColumnIds = columnIds;
		fNumColumns = 1;
		if (columnIds != null) {
			fNumColumns = columnIds.length;
		}
		fLabels = new String[fNumColumns];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#getColumnIds()
	 */
	public String[] getColumnIds() {
		return fColumnIds;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getElementPath()
	 */
	public TreePath getElementPath() {
		return fElementPath;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#setBackground(org.eclipse.swt.graphics.RGB, int)
	 */
	public void setBackground(RGB background, int columnIndex) {
		if (background == null) {
			return;
		}
		if (fBackgrounds == null) {
			fBackgrounds = new RGB[fNumColumns];
		}
		fBackgrounds[columnIndex] = background;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#setFontData(org.eclipse.swt.graphics.FontData, int)
	 */
	public void setFontData(FontData fontData, int columnIndex) {
		if (fontData == null) {
			return;
		}
		if (fFontDatas == null) {
			fFontDatas = new FontData[fNumColumns];
		}
		fFontDatas[columnIndex] = fontData;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#setForeground(org.eclipse.swt.graphics.RGB, int)
	 */
	public void setForeground(RGB foreground, int columnIndex) {
		if (foreground == null) {
			return;
		}
		if (fForegrounds == null) {
			fForegrounds = new RGB[fNumColumns];
		}
		fForegrounds[columnIndex] = foreground;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor, int)
	 */
	public void setImageDescriptor(ImageDescriptor image, int columnIndex) {
		if (image == null) {
			return;
		}
		if (fImageDescriptors == null) {
			fImageDescriptors = new ImageDescriptor[fNumColumns];
		}
		fImageDescriptors[columnIndex] = image;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#setLabel(java.lang.String, int)
	 */
	public void setLabel(String text, int columnIndex) {
		fLabels[columnIndex] = text;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getPresentationContext()
	 */
	public IPresentationContext getPresentationContext() {
		return fContext;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.commands.Request#done()
	 */
	public void done() {
		fProvider.complete(this);
	}

	/**
	 * Applies settings to viewer cell
	 */
	public void performUpdate() {
	    fProvider.setElementData(fElementPath, fNumColumns, fLabels, fImageDescriptors, fFontDatas, fForegrounds, fBackgrounds, fChecked, fGrayed);

		fProvider.updateComplete(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getElement()
	 */
	public Object getElement() {
		return getElementPath().getLastSegment();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("ILabelUpdate: "); //$NON-NLS-1$
		buf.append(getElement());
		return buf.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getViewerInput()
	 */
	public Object getViewerInput() {
		return fViewerInput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ICheckUpdate#setChecked(boolean, boolean)
	 */
	public void setChecked(boolean checked, boolean grayed) {
		fChecked = checked;
		fGrayed = grayed;
	}
}
