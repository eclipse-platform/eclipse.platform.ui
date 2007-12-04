/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.widgets;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class FormFonts {
	private static FormFonts instance;

	public static FormFonts getInstance() {
		if (instance == null)
			instance = new FormFonts();
		return instance;
	}
	
	private HashMap fonts;
	private HashMap ids;
	
	private FormFonts() {
	}
	
	private class FontIdentifier {
		private Display fDisplay;
		private Font fFont;
		
		FontIdentifier (Display display, Font font) {
			fDisplay = display;
			fFont = font;
		}
		
		public boolean equals(Object obj) {
			if (obj instanceof FontIdentifier) {
				FontIdentifier id = (FontIdentifier)obj;
				return id.fDisplay.equals(fDisplay) && id.fFont.equals(fFont);
			}
			return false;
		}
		
		public int hashCode() {
			return fDisplay.hashCode() * 7 + fFont.hashCode();
		}
	}
	
	private class FontReference {
		private Font fFont;
		private int fCount;
		
		public FontReference(Font font) {
			fFont = font;
			fCount = 1;
		}

		public Font getFont() {
			return fFont;
		}
		// returns a boolean indicating if all clients of this font are finished
		// a true result indicates the underlying image should be disposed
		public boolean decCount() {
			return --fCount == 0;
		}
		public void incCount() {
			fCount++;
		}
	}
	
	public Font getBoldFont(Display display, Font font) {
		checkHashMaps();
		FontIdentifier fid = new FontIdentifier(display, font);
		FontReference result = (FontReference) fonts.get(fid);
		if (result != null && !result.getFont().isDisposed()) {
			result.incCount();
			return result.getFont();
		}
		Font boldFont = createBoldFont(display, font);
		fonts.put(fid, new FontReference(boldFont));
		ids.put(boldFont, fid);
		return boldFont;
	}
	
	public boolean markFinished(Font boldFont) {
		checkHashMaps();
		FontIdentifier id = (FontIdentifier)ids.get(boldFont);
		if (id != null) {
			FontReference ref = (FontReference) fonts.get(id);
			if (ref != null) {
				if (ref.decCount()) {
					fonts.remove(id);
					ids.remove(ref.getFont());
					ref.getFont().dispose();
					validateHashMaps();
				}
				return true;
			}
		}
		// if the image was not found, dispose of it for the caller
		boldFont.dispose();
		return false;
	}

	private Font createBoldFont(Display display, Font regularFont) {
		FontData[] fontDatas = regularFont.getFontData();
		for (int i = 0; i < fontDatas.length; i++) {
			fontDatas[i].setStyle(fontDatas[i].getStyle() | SWT.BOLD);
		}
		return new Font(display, fontDatas);
	}

	private void checkHashMaps() {
		if (fonts == null)
			fonts = new HashMap();
		if (ids == null)
			ids = new HashMap();
	}
	
	private void validateHashMaps() {
		if (fonts.size() == 0)
			fonts = null;
		if (ids.size() == 0)
			ids = null;
	}
}
