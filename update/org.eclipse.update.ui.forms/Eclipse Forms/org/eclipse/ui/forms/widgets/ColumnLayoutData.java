/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;
import org.eclipse.swt.SWT;
/**
 * This class is used to store layout data for the 
 * <code>ColumnLayout</code> class.
 * 
 * @see ColumnLayout
 * @since 3.0
 */
public class ColumnLayoutData {
	/**
	 * Width hint that will be used instead of the computed control width when
	 * used in conjunction with <code>ColumnLayout</code> class (default is SWT.DEFAULT).
	 */
	public int widthHint = SWT.DEFAULT;
	/**
	 * Height hint that will be used instead of the computed control height
	 * when used in conjunction with <code>ColumnLayout</code> class (default is
	 * SWT.DEFAULT).
	 */
	public int heightHint = SWT.DEFAULT;
	public static final int LEFT = 1;
	public static final int CENTER = 2;
	public static final int RIGHT = 3;
	public static final int FILL = 4;
	
	public int horizontalAlignment = FILL;
	
	/**
	 * Creates the new instance of the class.
	 * 
	 * @param wHint
	 *            width hint value
	 * @param hHint
	 *            height hint value
	 */
	public ColumnLayoutData(int wHint, int hHint) {
		this.widthHint = wHint;
		this.heightHint = hHint;
	}
	public ColumnLayoutData() {}
}
