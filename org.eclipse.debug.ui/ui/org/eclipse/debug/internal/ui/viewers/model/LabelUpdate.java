/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.debug.internal.core.commands.Request;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @since 3.3
 */
class LabelUpdate extends Request implements ILabelUpdate {
	
	private TreePath fElementPath;
	private String[] fColumnIds;
	private RGB[] fBackgrounds;
	private RGB[] fForegrounds;
	private ImageDescriptor[] fImageDescriptors;
	private String[] fLabels;
	private FontData[] fFontDatas;
	private TreeModelLabelProvider fProvider;
	private TreeItem fItem;
	private int fNumColumns; 
	private IPresentationContext fContext;
	
	/**
	 * Label data cache keys
	 * TODO: workaround for bug 159461
	 */
	static String PREV_LABEL_KEY = "PREV_LABEL_KEY"; //$NON-NLS-1$
	static String PREV_IMAGE_KEY = "PREV_IMAGE_KEY"; //$NON-NLS-1$
	static String PREV_FONT_KEY = "PREV_FONT_KEY"; //$NON-NLS-1$
	static String PREV_FOREGROUND_KEY = "PREV_FOREGROUND_KEY"; //$NON-NLS-1$
	static String PREV_BACKGROUND_KEY = "PREV_BACKGROUND_KEY"; //$NON-NLS-1$
	
	/**
	 * @param elementPath element the label is for
	 * @param item item the label is for
	 * @param provider label provider to callback to 
	 * @param columnIds column identifiers or <code>null</code>
	 * @param context presentation context
	 */
	public LabelUpdate(TreePath elementPath, TreeItem item, TreeModelLabelProvider provider, String[] columnIds, IPresentationContext context) {
		fContext = context;
		fElementPath = elementPath;
		fProvider = provider;
		fColumnIds = columnIds;
		fItem = item;
		fNumColumns = 1;
		if (columnIds != null) {
			fNumColumns = columnIds.length;
		}
		fLabels = new String[fNumColumns];
		fProvider.updateStarted(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#getColumnIds()
	 */
	public String[] getColumnIds() {
		return fColumnIds;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate#getElement()
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
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		fProvider.complete(this);
	}

	/**
	 * Applies settings to viewer cell
	 */
	public void update() {
		// label data is stored to prevent flickering of asynchronous view, see bug 159461
		if (!fItem.isDisposed()) {
			
			for (int i=0; i<fNumColumns; i++){
				// text might be null if the launch has been terminated
				fItem.setText(i,(fLabels[i] == null ? "" : fLabels[i])); //$NON-NLS-1$
			}
			fItem.setData(PREV_LABEL_KEY, fLabels);
			
			if (fImageDescriptors == null) {
				for (int i=0; i<fNumColumns; i++){
					fItem.setImage(i,null);
				}
				fItem.setData(PREV_IMAGE_KEY, null);
			} else {
				Image[] images = new Image[fImageDescriptors.length];
				for (int i = 0; i < fImageDescriptors.length; i++) {
					images[i] = fProvider.getImage(fImageDescriptors[i]);
				}
				if (fColumnIds == null) {
					fItem.setImage(images[0]);
				} else {
					fItem.setImage(images);
				}
				fItem.setData(PREV_IMAGE_KEY, images);
			}
			
			if (fForegrounds == null) {	
				for (int i=0; i<fNumColumns; i++){
					fItem.setForeground(i,null);
				}
				fItem.setData(PREV_FOREGROUND_KEY, null);
			} else {
				Color[] foregrounds = new Color[fForegrounds.length];
				for (int i = 0; i< foregrounds.length; i++) {
					foregrounds[i] = fProvider.getColor(fForegrounds[i]);
				}
				if (fColumnIds == null) {
					fItem.setForeground(0,foregrounds[0]);
				} else {
					for (int i = 0; i< foregrounds.length; i++) {
						fItem.setForeground(i, foregrounds[i]);
					}
				}
				fItem.setData(PREV_FOREGROUND_KEY, foregrounds);
			}
			
			if (fBackgrounds == null) {
				for (int i=0; i<fNumColumns; i++){
					fItem.setBackground(i,null);
				}
				fItem.setData(PREV_BACKGROUND_KEY, null);
			} else {
				Color[] backgrounds = new Color[fBackgrounds.length];
				for (int i = 0; i< backgrounds.length; i++) {
					backgrounds[i] = fProvider.getColor(fBackgrounds[i]);
				}
				if (fColumnIds == null) {
					fItem.setBackground(0,backgrounds[0]);
				} else {
					for (int i = 0; i< backgrounds.length; i++) {
						fItem.setBackground(i, backgrounds[i]);
					}
				}
				fItem.setData(PREV_BACKGROUND_KEY, backgrounds);
			}
			
			if (fFontDatas == null) {
				for (int i=0; i<fNumColumns; i++){
					fItem.setFont(i,null);
				}
				fItem.setData(PREV_FONT_KEY, null);
			} else {
				Font[] fonts = new Font[fFontDatas.length];
				for (int i = 0; i < fFontDatas.length; i++) {
					fonts[i] = fProvider.getFont(fFontDatas[i]);
				}
				if (fColumnIds == null) {
					fItem.setFont(0,fonts[0]);
				} else {
					for (int i = 0; i < fonts.length; i++) {
						fItem.setFont(i, fonts[i]);
					}
				}
				fItem.setData(PREV_FONT_KEY, fonts);
			}
		}
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
}
