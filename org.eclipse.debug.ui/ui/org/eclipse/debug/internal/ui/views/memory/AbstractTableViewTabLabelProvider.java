/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import java.math.BigInteger;
import org.eclipse.debug.internal.core.memory.MemoryByte;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Abstract label provider for an ITableMemoryViewTab
 * 
 * @since 3.0
 */
abstract public class AbstractTableViewTabLabelProvider extends LabelProvider implements ITableLabelProvider{

	protected ITableMemoryViewTab fViewTab;
	protected AbstractMemoryRenderer fRenderer;
	
	/**
	 * 
	 * Constructor for MemoryViewLabelProvider
	 */
	public AbstractTableViewTabLabelProvider() {
		super();
	}
	
	public AbstractTableViewTabLabelProvider(ITableMemoryViewTab viewTab, AbstractMemoryRenderer renderer){
		fViewTab = viewTab;
		setRenderer(renderer);
	}
	
	public void setViewTab(ITableMemoryViewTab viewTab){
		fViewTab = viewTab;
	}
	
	public void setRenderer(AbstractMemoryRenderer renderer){
		fRenderer = renderer;
		renderer.setViewTab(fViewTab);
	}
	
	public AbstractMemoryRenderer getRenderer()
	{
		return fRenderer;
	}

	/**
	 * @see ITableLabelProvider#getColumnImage(Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		
		if (columnIndex == 0)
		{
			return DebugPluginImages.getImage(IInternalDebugUIConstants.IMG_OBJECT_MEMORY);
		}
		else if (columnIndex > (fViewTab.getBytesPerLine()/fViewTab.getColumnSize()))
		{
			return DebugPluginImages.getImage(IInternalDebugUIConstants.IMG_OBJECT_MEMORY);	
		}
		else
		{	
			// if memory in the range has changed, return delta icon
			int startOffset = (columnIndex-1)*fViewTab.getColumnSize();
			int endOffset = startOffset + fViewTab.getColumnSize() - 1;
			if (((MemoryViewLine)element).isRangeChange(startOffset, endOffset)) {
				return DebugPluginImages.getImage(IInternalDebugUIConstants.IMG_OBJECT_MEMORY_CHANGED);
			}
			return DebugPluginImages.getImage(IInternalDebugUIConstants.IMG_OBJECT_MEMORY);	
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		String columnLabel = null;

		if (columnIndex == 0)
		{
			columnLabel = ((MemoryViewLine)element).getAddress();
			
			// consult model presentation for address presentation
		}
		else if (columnIndex > (fViewTab.getBytesPerLine()/fViewTab.getColumnSize()))
		{
			columnLabel = " "; //$NON-NLS-1$
		}
		else
		{	
			int start = (columnIndex-1)*fViewTab.getColumnSize();
			int end = start + fViewTab.getColumnSize();
			MemoryViewLine line = (MemoryViewLine)element;

			MemoryByte[] bytes = ((MemoryViewLine)element).getBytes(start, end);
			BigInteger address = new BigInteger(((MemoryViewLine)element).getAddress(), 16);
			address = address.add(BigInteger.valueOf(start)); 
			
			columnLabel = fRenderer.getString(fViewTab.getRenderingId(), address, bytes, line.getPaddedString());
		}
		return columnLabel;
	}
}
