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
package org.eclipse.update.ui.forms.internal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

public class TableData {
	public static final int LEFT = 1;
	public static final int CENTER = 2;
	public static final int RIGHT = 3;
	public static final int TOP = 4;
	public static final int MIDDLE = 5;
	public static final int BOTTOM = 6;
	public static final int FILL = 7;
	public int colspan=1;
	public int rowspan=1;
	public int align = LEFT;
	public int valign = TOP;
	public int indent = 0;
	public int maxWidth = SWT.DEFAULT;
	public int maxHeight = SWT.DEFAULT;
	public int heightHint = SWT.DEFAULT;
	public boolean grabHorizontal=false;
	
	//private
	int childIndex;
	boolean isItemData=true; 
	int compWidth;
	Point compSize;
		
	public TableData() {
	}
	
	public TableData(int align, int valign) {
		this(align, valign, 1, 1);
	}
	
	public TableData(int align, int valign, int rowspan, int colspan) {
		this.align = align;
		this.valign = valign;
		this.rowspan = rowspan;
		this.colspan = colspan;
	}
}
