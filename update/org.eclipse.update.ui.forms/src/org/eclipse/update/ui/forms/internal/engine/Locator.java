/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.update.ui.forms.internal.engine;

/**
 * @version 	1.0
 * @author
 */
public class Locator { 
	public int indent;
	public int x, y;
	public int width, height;
	public int rowHeight;
	public int marginWidth;
	public int marginHeight;
	
	public void newLine() {
		resetCaret();
		y += rowHeight;
		rowHeight = 0;
	}
	
	public void resetCaret() {
		x = marginWidth + indent;
	}
}
