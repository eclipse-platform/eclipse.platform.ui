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

package org.eclipse.debug.internal.ui.views.memory.renderings;

import java.math.BigInteger;

import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.ui.memory.AbstractTableRendering;
import org.eclipse.debug.ui.memory.MemoryRenderingElement;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * This label provider is used by AbstractTableRendering if clients decide to
 * provide customized decorations in the rendering. Otherwise the table
 * rendering uses TableRenderingLabelProvider.
 * 
 */
public class TableRenderingLabelProviderEx extends TableRenderingLabelProvider implements ITableColorProvider, ITableFontProvider {

	private IFontProvider fFontProvider;
	private ILabelProvider fLabelProvider;
	private IColorProvider fColorProvider;

	public TableRenderingLabelProviderEx(AbstractTableRendering rendering) {
		super(rendering);
		fLabelProvider = (ILabelProvider) rendering.getAdapter(ILabelProvider.class);
		fColorProvider = (IColorProvider) rendering.getAdapter(IColorProvider.class);
		fFontProvider = (IFontProvider) rendering.getAdapter(IFontProvider.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		if (fFontProvider != null) {
			fFontProvider = null;
		}
		if (fColorProvider != null) {
			fColorProvider = null;
		}
		if (fLabelProvider != null) {
			fLabelProvider.dispose();
			fLabelProvider = null;
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
	 */
	public Color getBackground(Object element, int columnIndex) {
		if (fColorProvider != null && columnIndex > 0) {
			MemoryRenderingElement renderingElement = getMemoryRenderingElement(element, columnIndex);
			if (renderingElement != null) {
				Color color = fColorProvider.getBackground(renderingElement);
				if (color != null)
					return color;
			}
		}
		return super.getBackground(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
	 */
	public Color getForeground(Object element, int columnIndex) {
		if (fColorProvider != null && columnIndex > 0) {
			MemoryRenderingElement renderingElement = getMemoryRenderingElement(element, columnIndex);
			if (renderingElement != null) {
				Color color = fColorProvider.getForeground(renderingElement);
				if (color != null)
					return color;
			}			
		}
		return super.getForeground(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		if (fLabelProvider != null && columnIndex > 0) {
			MemoryRenderingElement renderingElement = getMemoryRenderingElement(element, columnIndex);
			if (renderingElement != null) {
				Image image = fLabelProvider.getImage(renderingElement);
				if (image != null)
					return image;
			}
		}
		return super.getColumnImage(element, columnIndex);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
	 */
	public Font getFont(Object element, int columnIndex) {
		if (fFontProvider != null && columnIndex > 0) {
			MemoryRenderingElement renderingElement = getMemoryRenderingElement(element, columnIndex);
			if (renderingElement != null) {
				Font font = fFontProvider.getFont(renderingElement);
				if (font != null)
					return font;
			}
		}
		return null;
	}

	/**
	 * Returns a memory rendering element corresponding to the given element
	 * or <code>null</code> if none.
	 *  
	 * @param element element to be rendered
	 * @param columnIndex column index at which to render
	 * @return memory rendering element or <code>null</code>
	 */
	private MemoryRenderingElement getMemoryRenderingElement(Object element, int columnIndex) {
		if (element instanceof TableRenderingLine) {
			TableRenderingLine line = (TableRenderingLine) element;
			BigInteger lineAddress = new BigInteger(line.getAddress(), 16);
			int offset = (columnIndex - 1) * fRendering.getBytesPerColumn();
			if (offset < fRendering.getBytesPerLine() && (offset + fRendering.getBytesPerColumn()) <= fRendering.getBytesPerLine()) {
				return getMemoryRenderingElement(line, lineAddress, offset);
			}
		}
		return null;
	}
	
	private MemoryRenderingElement getMemoryRenderingElement(TableRenderingLine line, BigInteger lineAddress, int offset) {
		BigInteger cellAddress = lineAddress.add(BigInteger.valueOf(offset));
		MemoryByte[] bytes = line.getBytes(offset, offset
				+ fRendering.getBytesPerColumn());
		// make a copy to ensure that the memory bytes are not overwritten
		// by clients
		MemoryByte[] copy = new MemoryByte[bytes.length];
		System.arraycopy(bytes, 0, copy, 0, bytes.length);
		MemoryRenderingElement renderingElement = new MemoryRenderingElement(
				fRendering, cellAddress, copy);
		return renderingElement;
	}
}
