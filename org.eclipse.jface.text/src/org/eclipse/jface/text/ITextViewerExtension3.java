/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


/**
 * Translates between model and presentation coordinates.
 */
public interface ITextViewerExtension3 {
	
	
	public IRegion getModelCoverage();
	
	
	public int modelLine2WidgetLine(int modelLine);

	public int modelOffset2WidgetOffset(int modelOffset);

	public IRegion modelRange2WidgetRange(IRegion modelRange);


	public int widgetOffset2ModelOffset(int widgetOffset);
	
	public IRegion widgetRange2ModelRange(IRegion widgetRange);

	public int widgetlLine2ModelLine(int widgetLine);
	
	public int widgetLineOfWidgetOffset(int widgetOffset);
}
