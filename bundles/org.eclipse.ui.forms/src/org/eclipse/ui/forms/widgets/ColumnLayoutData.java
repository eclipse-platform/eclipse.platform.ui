/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;
import org.eclipse.swt.SWT;
/**
 * This class is used to store layout data for the <code>ColumnLayout</code>
 * class. You can control width and height hints, as well as horizontal
 * alignment using instances of this class. As with other layouts, they are not
 * required to get the default behaviour.
 * 
 * @see ColumnLayout
 * @since 3.0
 */
public final class ColumnLayoutData {
	/**
	 * Width hint that will be used instead of the computed control width when
	 * used in conjunction with <code>ColumnLayout</code> class (default is
	 * SWT.DEFAULT).
	 */
	public int widthHint = SWT.DEFAULT;
	/**
	 * Height hint that will be used instead of the computed control height
	 * when used in conjunction with <code>ColumnLayout</code> class (default
	 * is SWT.DEFAULT).
	 */
	public int heightHint = SWT.DEFAULT;
	/**
	 * Horizontal alignment constant - control will be aligned to the left.
	 */
	public static final int LEFT = 1;
	/**
	 * Horizontal alignment constant - control will be aligned to the right.
	 */
	public static final int CENTER = 2;
	/**
	 * Horizontal alignment constant - control will be centered.
	 */
	public static final int RIGHT = 3;
	/**
	 * Horizontal alignment constant - control will fill the column.
	 */
	public static final int FILL = 4;
	/**
	 * Horizontal alignment variable (default is FILL).
	 */
	public int horizontalAlignment = FILL;
	/**
	 * Convinience constructor for the class.
	 * 
	 * @param wHint
	 *            width hint for the control
	 * @param hHint
	 *            height hint for the control
	 */
	public ColumnLayoutData(int wHint, int hHint) {
		this.widthHint = wHint;
		this.heightHint = hHint;
	}
	/**
	 * Convinience constructor for the class.
	 * 
	 * @param wHint
	 *            width hint for the control
	 */
	public ColumnLayoutData(int wHint) {
		this.widthHint = wHint;
	}
	/**
	 * The default constructor.
	 *  
	 */
	public ColumnLayoutData() {
	}
}
