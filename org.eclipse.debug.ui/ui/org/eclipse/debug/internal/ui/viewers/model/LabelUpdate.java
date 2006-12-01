/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.debug.internal.ui.commands.actions.AbstractRequestMonitor;
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
class LabelUpdate extends AbstractRequestMonitor implements ILabelUpdate {
	
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
	
	/**
	 * Label/Image cache keys
	 * TODO: workaround for bug 159461
	 */
	static String PREV_LABEL_KEY = "PREV_LABEL_KEY"; //$NON-NLS-1$
	static String PREV_IMAGE_KEY = "PREV_IMAGE_KEY"; //$NON-NLS-1$
	
	/**
	 * @param element element the label is for
	 * @param provider label provider to callback to 
	 * @param columnId column identifier or <code>null</code>
	 */
	public LabelUpdate(TreePath elementPath, TreeItem item, TreeModelLabelProvider provider, String[] columnIds) {
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
		return fProvider.getPresentationContext();
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
		if (!fItem.isDisposed()) {
			if (fColumnIds == null) {
				fItem.setText(fLabels[0]);
			} else {
				fItem.setText(fLabels);
			}
			fItem.setData(PREV_LABEL_KEY, fLabels);
			if (fImageDescriptors == null) {
				fItem.setImage((Image)null);
				fItem.setData(PREV_IMAGE_KEY, null); // TODO: bug 159461
			} else {
				if (fImageDescriptors == null) {
					fItem.setImage((Image)null);
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
					fItem.setData(PREV_IMAGE_KEY, images); // TODO: bug 159461
				}
			}
			if (fForegrounds == null) {
				fItem.setForeground((Color)null);
			} else {
				if (fColumnIds == null) {
					fItem.setForeground(fProvider.getColor(fForegrounds[0]));
				} else {
					for (int i = 0; i< fForegrounds.length; i++) {
						fItem.setForeground(i, fProvider.getColor(fForegrounds[i]));
					}
				}
			}
			if (fBackgrounds == null) {
				fItem.setBackground((Color)null);
			} else {
				if (fColumnIds == null) {
					fItem.setBackground(fProvider.getColor(fBackgrounds[0]));
				} else {
					for (int i = 0; i< fBackgrounds.length; i++) {
						fItem.setBackground(i, fProvider.getColor(fBackgrounds[i]));
					}
				}
			}
			if (fFontDatas == null) {
				fItem.setFont((Font)null);
			} else {
				if (fColumnIds == null) {
					fItem.setFont(fProvider.getFont(fFontDatas[0]));
				} else {
					for (int i = 0; i < fFontDatas.length; i++) {
						fItem.setFont(i, fProvider.getFont(fFontDatas[i]));
					}
				}
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
