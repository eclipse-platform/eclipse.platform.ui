/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.AbstractTableRendering;
import org.eclipse.debug.ui.memory.IMemoryBlockTablePresentation;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.graphics.Color;

/**
 * @since 3.0
 */
public class TableRenderingLabelProvider extends AbstractTableRenderingLabelProvider implements IColorProvider {
	
	private IMemoryBlockTablePresentation fTablePresentation;

	/**
	 * Constructor for MemoryViewLabelProvider
	 */
	public TableRenderingLabelProvider() {
		super();
	}
	
	public TableRenderingLabelProvider(AbstractTableRendering rendering){
		super(rendering);
		fTablePresentation = (IMemoryBlockTablePresentation)rendering.getAdapter(IMemoryBlockTablePresentation.class);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		if (fTablePresentation != null) {
			fTablePresentation.dispose();
			fTablePresentation = null;
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if (element instanceof TableRenderingLine)
		{
			TableRenderingLine line = (TableRenderingLine)element;
			
			if (line.isMonitored) {
				return DebugUIPlugin.getPreferenceColor(IDebugUIConstants.PREF_MEMORY_HISTORY_KNOWN_COLOR);
			}
			return DebugUIPlugin.getPreferenceColor(IDebugUIConstants.PREF_MEMORY_HISTORY_UNKNOWN_COLOR);
		}
		return DebugUIPlugin.getPreferenceColor(IDebugUIConstants.PREF_MEMORY_HISTORY_KNOWN_COLOR);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		
		String label = super.getColumnText(element, columnIndex);
		
		// consult model presentation for address presentation
		if (columnIndex == 0)
		{	
			if (fTablePresentation != null)
			{	
				String address = ((TableRenderingLine)element).getAddress();
				
				// get address presentation
				String tempLabel = fTablePresentation.getRowLabel(fRendering.getMemoryBlock(), new BigInteger(address, 16));
				
				if (tempLabel != null)
					return tempLabel;
			}
			return label;
		}
		return label;
	}
}
