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
 * This label provider is used by AbstractTableRendering 
 * if clients decide to provide customized decorations in the rendering.  Otherwise
 * the table rendering uses TableRenderingLabelProvider.
 *
 */
public class TableRenderingLabelProviderEx extends TableRenderingLabelProvider
	implements ITableColorProvider, ITableFontProvider {
	
	public TableRenderingLabelProviderEx(AbstractTableRendering rendering){
		super(rendering);
	}
	
	public Color getBackground(Object element, int columnIndex) {
		
		if (columnIndex == 0)
			return super.getBackground(element);
		
		if (element instanceof TableRenderingLine)
		{
			TableRenderingLine line = (TableRenderingLine)element;
			
			BigInteger lineAddress = new BigInteger(line.getAddress(), 16);
			int offset = (columnIndex-1)*fRendering.getBytesPerColumn();
			
			if (offset < fRendering.getBytesPerLine() &&
				(offset+fRendering.getBytesPerColumn()) <= fRendering.getBytesPerLine())
			{
								
				IColorProvider colorProvider = (IColorProvider)fRendering.getAdapter(IColorProvider.class);
				if (colorProvider != null)
				{
					MemoryRenderingElement renderingElement = getMemoryRenderingElement(line, lineAddress, offset);
					Color color = colorProvider.getBackground(renderingElement);
					
					if (color != null)
						return color;
				}
			}
		}
		return super.getBackground(element);
	}
	
	public Color getForeground(Object element, int columnIndex) {
		if (element instanceof TableRenderingLine)
		{
			TableRenderingLine line = (TableRenderingLine)element;
			
			if (columnIndex > 0)
			{
				BigInteger lineAddress = new BigInteger(line.getAddress(), 16);
				int offset = (columnIndex-1)*fRendering.getBytesPerColumn();
				
				if (offset < fRendering.getBytesPerLine() &&
					(offset+fRendering.getBytesPerColumn()) <= fRendering.getBytesPerLine())
				{				
					IColorProvider colorProvider = (IColorProvider)fRendering.getAdapter(IColorProvider.class);					
					if (colorProvider != null)
					{
						MemoryRenderingElement renderingElement = getMemoryRenderingElement(line, lineAddress, offset);
						Color color = colorProvider.getForeground(renderingElement);
						
						if (color != null)
							return color;
					}
				}
			}
		}
		return super.getForeground(element);
	}
	
	public Image getColumnImage(Object element, int columnIndex) 
	{
		if (element instanceof TableRenderingLine)
		{
			if (columnIndex > 0)
			{
				TableRenderingLine line = (TableRenderingLine)element;
				BigInteger lineAddress = new BigInteger(line.getAddress(), 16);
				int offset = (columnIndex-1)*fRendering.getBytesPerColumn();
				
				if (offset < fRendering.getBytesPerLine() &&
					(offset+fRendering.getBytesPerColumn()) <= fRendering.getBytesPerLine())
				{
					ILabelProvider labelProvider = (ILabelProvider)fRendering.getAdapter(ILabelProvider.class);
					if (labelProvider != null)
					{
						MemoryRenderingElement renderingElement = getMemoryRenderingElement(line, lineAddress, offset);
						Image image = labelProvider.getImage(renderingElement);
						if (image != null)
							return image;
					}
				}
			}
		}
	
		return super.getColumnImage(element, columnIndex);
	}
	
	public Font getFont(Object element, int columnIndex) {
		if (element instanceof TableRenderingLine)
		{
			if (columnIndex > 0)
			{
				TableRenderingLine line = (TableRenderingLine)element;
				BigInteger lineAddress = new BigInteger(line.getAddress(), 16);
				int offset = (columnIndex-1)*fRendering.getBytesPerColumn();
				
				if (offset < fRendering.getBytesPerLine() &&
					(offset+fRendering.getBytesPerColumn()) <= fRendering.getBytesPerLine())
				{
					IFontProvider fontProvider = (IFontProvider)fRendering.getAdapter(IFontProvider.class);
					if (fontProvider != null)
					{
						MemoryRenderingElement renderingElement = getMemoryRenderingElement(line, lineAddress, offset);
						Font font = fontProvider.getFont(renderingElement);
						if (font != null)
							return font;
					}
				}
			}
		}
		return null;
	}
	
	private MemoryRenderingElement getMemoryRenderingElement(TableRenderingLine line, BigInteger lineAddress, int offset) {
		BigInteger cellAddress = lineAddress.add(BigInteger.valueOf(offset));
		MemoryByte[] bytes = line.getBytes(offset, offset+fRendering.getBytesPerColumn());
		// make a copy to ensure that the memory bytes are not overwritten
		// by clients
		MemoryByte[] copy = new MemoryByte[bytes.length];
		System.arraycopy(bytes, 0, copy, 0, bytes.length);
		MemoryRenderingElement renderingElement = new MemoryRenderingElement(fRendering, cellAddress, copy);
		return renderingElement;
	}
}
