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
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.graphics.Color;

/**
 * @since 3.0
 */
public class MemoryViewTabLabelProvider
	extends AbstractTableViewTabLabelProvider implements IColorProvider{

	private static final String UNBUFFERED_LINE_COLOR = IDebugUIConstants.PLUGIN_ID + ".MemoryViewLineColor"; //$NON-NLS-1$
	
	/**
	 * Constructor for MemoryViewLabelProvider
	 */
	public MemoryViewTabLabelProvider() {
		super();
	}
	
	public MemoryViewTabLabelProvider(ITableMemoryViewTab viewTab, AbstractMemoryRenderer renderer){
		super(viewTab, renderer);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		
		String label = super.getColumnText(element, columnIndex);
		
		// consult model presentation for address presentation
		if (columnIndex == 0)
		{	
			if (fViewTab instanceof MemoryViewTab)
			{	
				// get model presentation
				IDebugModelPresentation presentation = ((MemoryViewTab)fViewTab).getMemoryBlockPresentation();
				
				if (presentation instanceof IMemoryBlockModelPresentation)
				{	
					IMemoryBlockModelPresentation memPresentation = (IMemoryBlockModelPresentation)presentation;
					String address = ((MemoryViewLine)element).getAddress();
					
					// get address presentation
					String tempLabel = memPresentation.getAddressPresentation(fViewTab.getMemoryBlock(), new BigInteger(address, 16));
					
					if (tempLabel != null)
						return tempLabel;
				}
			}
			return label;
		}
		else
		{
			return label;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if (element instanceof MemoryViewLine)
		{
			MemoryViewLine line = (MemoryViewLine)element;
			
			if (line.isMonitored)
				return null;
			else
				return JFaceResources.getColorRegistry().get(UNBUFFERED_LINE_COLOR);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		
		return null;
	}
}
