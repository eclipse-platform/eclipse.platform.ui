package org.eclipse.debug.internal.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;

public class PixelConverter {
	
	private FontMetrics fFontMetrics;
	
	public PixelConverter(Control control) {
		GC gc = new GC(control);
		gc.setFont(control.getFont());
		fFontMetrics= gc.getFontMetrics();
		gc.dispose();
	}
	
	private FontMetrics fgFontMetrics;
		
	/**
	 * @see DialogPage#convertHeightInCharsToPixels
	 */
	public int convertHeightInCharsToPixels(int chars) {
		return Dialog.convertHeightInCharsToPixels(fFontMetrics, chars);
	}

	/**
	 * @see DialogPage#convertHorizontalDLUsToPixels
	 */
	public int convertHorizontalDLUsToPixels(int dlus) {
		return Dialog.convertHorizontalDLUsToPixels(fFontMetrics, dlus);
	}

	/**
	 * @see DialogPage#convertVerticalDLUsToPixels
	 */
	public int convertVerticalDLUsToPixels(int dlus) {
		return Dialog.convertVerticalDLUsToPixels(fFontMetrics, dlus);
	}
	
	/**
	 * @see DialogPage#convertWidthInCharsToPixels
	 */
	public int convertWidthInCharsToPixels(int chars) {
		return Dialog.convertWidthInCharsToPixels(fFontMetrics, chars);
	}	
}
