/*******************************************************************************
 * Copyright (c) 2015, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.contentassist;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.TextStyle;

import org.eclipse.jface.viewers.StyledString.Styler;


/**
 * Provides {@link Styler} that applies bold style on the given font.
 *
 * @since 3.11
 */
public final class BoldStylerProvider {

	private Font fFont;

	private Font fBoldFont;

	private Styler fBoldStyler;

	/**
	 * Creates a {@link BoldStylerProvider} instance which uses the given <code>font</code>.
	 *
	 * @param font the font to use for creating the bold font
	 */
	public BoldStylerProvider(Font font) {
		fFont= font;
	}

	/**
	 * Disposes the bold font created for the styler.
	 */
	public void dispose() {
		if (fBoldFont != null) {
			fBoldFont.dispose();
			fBoldFont= null;
		}
	}

	/**
	 * Returns a {@link Styler} that applies bold style to the given {@link TextStyle}'s font.
	 *
	 * @return a bold styler
	 */
	public Styler getBoldStyler() {
		if (fBoldStyler == null) {
			fBoldStyler= new Styler() {
				@Override
				public void applyStyles(TextStyle textStyle) {
					textStyle.font= getBoldFont();
				}
			};
		}
		return fBoldStyler;
	}

	/**
	 * Creates (if not already done) and returns the bold font used by the styler to apply the bold
	 * style.
	 *
	 * <p>
	 * <strong>Note:</strong> The callers must not cache and re-use the returned font outside the
	 * current call.
	 * </p>
	 *
	 * @return the bold font used by the styler
	 */
	public Font getBoldFont() {
		if (fBoldFont == null) {
			FontData[] data= fFont.getFontData();
			for (FontData element : data) {
				element.setStyle(SWT.BOLD);
			}
			fBoldFont= new Font(fFont.getDevice(), data);
		}
		return fBoldFont;
	}

	/**
	 * Returns the font used by the styler to create the bold font.
	 *
	 * @return the font used for creating the bold font
	 */
	public Font getFont() {
		return fFont;
	}

}
