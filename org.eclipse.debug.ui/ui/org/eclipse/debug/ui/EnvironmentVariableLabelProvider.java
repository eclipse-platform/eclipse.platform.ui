package org.eclipse.debug.ui;
/*******************************************************************************
 * Copyright (c) 2000, 2003 Keith Seitz and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Keith Seitz (keiths@redhat.com) - initial implementation
 *     IBM Corporation - integration and code cleanup
 *******************************************************************************/
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;


public class EnvironmentVariableLabelProvider
	extends LabelProvider
	implements ITableLabelProvider
{
	public String getColumnText(Object element, int columnIndex)
	{
		String result = null;
		
		if (element != null)
		{
			EnvironmentVariable var = (EnvironmentVariable) element;
			switch (columnIndex)
			{
				case 0:
					// variable
					result = var.getName();
					break;
				
				case 1:
					// value
					result = var.getValue();
					break;
			}
		}
		
		return result;
	}
	
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}
}
