/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class for html-related functions.
 * 
 * @since 3.2
 */
public final class HTMLPrinter {

	private static RGB BG_COLOR_RGB= null;

	static {
		final Display display= Display.getDefault();
		if (display != null && !display.isDisposed()) {
			try {
				display.asyncExec(new Runnable() {

					public void run() {
						BG_COLOR_RGB= display.getSystemColor(SWT.COLOR_INFO_BACKGROUND).getRGB();
					}
				});
			} catch (SWTError error) {
				if (error.code != SWT.ERROR_DEVICE_DISPOSED)
					throw error;
			}
		}
	}

	public static void addBullet(StringBuffer buffer, String face, int height, String bullet) {
		if (bullet != null) {
			buffer.append("<li>"); //$NON-NLS-1$
			buffer.append("<span style= \"font-size:"); //$NON-NLS-1$
			buffer.append(height);
			buffer.append(".0pt;font-family:"); //$NON-NLS-1$
			buffer.append(face);
			buffer.append("\">"); //$NON-NLS-1$
			buffer.append(bullet);
			buffer.append("</span>"); //$NON-NLS-1$
			buffer.append("</li>"); //$NON-NLS-1$
		}
	}

	public static void addPageEpilog(StringBuffer buffer) {
		buffer.append("</font></body></html>"); //$NON-NLS-1$
	}

	public static void addParagraph(StringBuffer buffer, String face, int height, String paragraph) {
		if (paragraph != null) {
			buffer.append("<p>"); //$NON-NLS-1$
			buffer.append("<span style= \"font-size:"); //$NON-NLS-1$
			buffer.append(height);
			buffer.append(".0pt;font-family:"); //$NON-NLS-1$
			buffer.append(face);
			buffer.append("\">"); //$NON-NLS-1$
			buffer.append(paragraph);
			buffer.append("</span>"); //$NON-NLS-1$
		}
	}

	public static void addSmallHeader(StringBuffer buffer, String face, int height, String header) {
		if (header != null) {
			buffer.append("<h5>"); //$NON-NLS-1$
			buffer.append("<span style= \"font-size:"); //$NON-NLS-1$
			buffer.append(height);
			buffer.append(".0pt;font-family:"); //$NON-NLS-1$
			buffer.append(face);
			buffer.append("\">"); //$NON-NLS-1$
			buffer.append(header);
			buffer.append("</span>"); //$NON-NLS-1$
			buffer.append("</h5>"); //$NON-NLS-1$
		}
	}

	private static void appendColor(StringBuffer buffer, RGB rgb) {
		buffer.append('#');
		buffer.append(Integer.toHexString(rgb.red));
		buffer.append(Integer.toHexString(rgb.green));
		buffer.append(Integer.toHexString(rgb.blue));
	}

	public static String convertToHTMLContent(String content) {
		content= replace(content, '&', "&amp;"); //$NON-NLS-1$
		content= replace(content, '"', "&quot;"); //$NON-NLS-1$
		content= replace(content, '<', "&lt;"); //$NON-NLS-1$
		return replace(content, '>', "&gt;"); //$NON-NLS-1$
	}

	public static void endBulletList(StringBuffer buffer) {
		buffer.append("</ul>"); //$NON-NLS-1$
	}

	private static RGB getBgColor() {
		if (BG_COLOR_RGB != null)
			return BG_COLOR_RGB;
		else
			return new RGB(255, 255, 225);

	}

	public static void insertPageProlog(StringBuffer buffer, String font, int height, int position, RGB bgRGB) {
		if (bgRGB == null)
			insertPageProlog(buffer, font, height, position, getBgColor());
		else {
			StringBuffer pageProlog= new StringBuffer(60);
			pageProlog.append("<html><body text=\"#000000\" bgcolor=\""); //$NON-NLS-1$
			appendColor(pageProlog, bgRGB);
			pageProlog.append("\"><font face=\"" + font + "\" size=\"" + height + ".0pt\">"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buffer.insert(position, pageProlog.toString());
		}
	}

	public static void insertStyles(StringBuffer buffer, String[] styles) {
		if (styles == null || styles.length == 0)
			return;
		StringBuffer styleBuf= new StringBuffer(10 * styles.length);
		for (int i= 0; styles != null && i < styles.length; i++) {
			styleBuf.append(" style=\""); //$NON-NLS-1$
			styleBuf.append(styles[i]);
			styleBuf.append('"');
		}
		int index= buffer.indexOf("<body "); //$NON-NLS-1$
		if (index == -1)
			return;
		buffer.insert(index + 5, styleBuf);
	}

	private static String replace(String text, char c, String s) {
		int previous= 0;
		int current= text.indexOf(c, previous);
		if (current == -1)
			return text;
		StringBuffer buffer= new StringBuffer();
		while (current > -1) {
			buffer.append(text.substring(previous, current));
			buffer.append(s);
			previous= current + 1;
			current= text.indexOf(c, previous);
		}
		buffer.append(text.substring(previous));
		return buffer.toString();
	}

	public static void startBulletList(StringBuffer buffer) {
		buffer.append("<ul>"); //$NON-NLS-1$
	}

	private HTMLPrinter() {
		// Not for instantiation
	}
}