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
package org.eclipse.ui.forms.parts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

/**
 * Layout data used in conjunction with <code>HTMLTableLayout</code>.
 * Children in a composite that uses this layout should call
 * <samp>setLayoutData</samp> and pass an instance of this class
 * to control physical placement in the parent.
 * 
 * @see HTMLTableLayout
 * @since 3.0
 */

public class TableData {
/**
 * The control will be left-justified.
 */
	public static final int LEFT = 1;
/**
 * The control will be centered horizontally.
 */
	public static final int CENTER = 2;
/**
 * The control will be right-justified.
 */
	public static final int RIGHT = 3;
/**
 * The control will be aligned with the top of the cell.
 */
	public static final int TOP = 4;
/**
 * The control will be centered vertically.
 */
	public static final int MIDDLE = 5;
/**
 * The control will be aligned with the bottom of the cell.
 */
	public static final int BOTTOM = 6;
/**
 * The control will have the same width as the column it occupies.
 */
	public static final int FILL = 7;
/**
 * Number of columns to span (defualt is 1).
 */
	public int colspan=1;
/**
 * Number of rows to span (default is 1).
 */
	public int rowspan=1;
/**
 * Horizontal alignment (default is LEFT).
 */
	public int align = LEFT;
/**
 * Vertical alignment (default is TOP).
 */
	public int valign = TOP;
/**
 * Horizontal indent (default is 0).
 */
	public int indent = 0;
/**
 * Maximum width of the control (default is SWT.DEFAULT).
 */
	public int maxWidth = SWT.DEFAULT;
/**
 * Maximum height of the control (default is SWT.DEFAULT).
 */
	public int maxHeight = SWT.DEFAULT;
/**
 * Height hint of the control (default is SWT.DEFAULT).
 */
	public int heightHint = SWT.DEFAULT;
/**
 * If <code>true</code>, will grab any excess horizontal space
 * (default is <code>false</code>).
 */
	public boolean grabHorizontal=false;
	
	//private
	int childIndex;
	boolean isItemData=true; 
	int compWidth;
	Point compSize;
		
/**
 * The default constructor.
 *
 */
	public TableData() {
	}
	
/**
 * The convinience constructor - allows passing the alignment styles.
 * @param align horizontal alignment
 * @param valign vertical alignment
 */
	public TableData(int align, int valign) {
		this(align, valign, 1, 1);
	}
	
/**
 * The convinience constructor - allows passing the alignment styles,
 * column and row spans.
 * @param align horizontal alignment
 * @param valign vertical alignment
 * @param rowspan row span
 * @param colspan column span
 */
	public TableData(int align, int valign, int rowspan, int colspan) {
		this.align = align;
		this.valign = valign;
		this.rowspan = rowspan;
		this.colspan = colspan;
	}
}
