/*
 * Created on Jan 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.widgets;

import org.eclipse.swt.SWT;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ColumnLayoutData {
	public int widthHint=SWT.DEFAULT;
	public int heightHint=SWT.DEFAULT;
	
	public ColumnLayoutData(int wHint, int hHint) {
		this.widthHint = wHint;
		this.heightHint = hHint;
	}
}
