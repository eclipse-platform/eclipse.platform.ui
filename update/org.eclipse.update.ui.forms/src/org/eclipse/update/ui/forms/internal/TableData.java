package org.eclipse.update.ui.forms.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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