/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.html;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import org.eclipse.core.runtime.Platform;

/**
 * Provides a set of convenience methods for creating HTML pages.
 * <p>
 * Moved into this package from <code>org.eclipse.jface.internal.text.revisions</code>.</p>
 *
 * @since 3.14
 */
public class HTMLBuilder {

	private static volatile RGB DEFAULT_BG_COLOR_RGB= new RGB(255, 255, 225); // CoreRGB value of info bg color on WindowsXP
	private static volatile RGB DEFAULT_FG_COLOR_RGB= new RGB(0, 0, 0); // CoreRGB value of info fg color on WindowsXP
	private static volatile RGB DEFAULT_LINK_COLOR_RGB= new RGB(0, 0, 255); // dark blue
	private static volatile RGB DEFAULT_ACTIVE_LINK_COLOR_RGB= new RGB(0, 0, 128); // light blue

	private static final String UNIT; // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=155993
	static {
		UNIT= Platform.OS.isMac() ? "px" : "pt";   //$NON-NLS-1$//$NON-NLS-2$
	}


	private RGB bgColor;
	private RGB fgColor;
	private RGB linkColor;
	private RGB alinkColor;
	
	public HTMLBuilder() {
		this(DEFAULT_BG_COLOR_RGB, DEFAULT_FG_COLOR_RGB, DEFAULT_LINK_COLOR_RGB, DEFAULT_ACTIVE_LINK_COLOR_RGB);
	}
	
	public HTMLBuilder(RGB bg, RGB fg, RGB link, RGB alink) {
		setColors(bg, fg, link, alink);
	}
	
	public void setColors(RGB bg, RGB fg, RGB link, RGB alink) {
		this.bgColor = bg;
		this.fgColor = fg;
		this.linkColor = link;
		this.alinkColor = alink;
	}

	private static String replace(String text, char c, String s) {

		int previous= 0;
		int current= text.indexOf(c, previous);

		if (current == -1)
			return text;

		StringBuilder buffer= new StringBuilder();
		while (current > -1) {
			buffer.append(text.substring(previous, current));
			buffer.append(s);
			previous= current + 1;
			current= text.indexOf(c, previous);
		}
		buffer.append(text.substring(previous));

		return buffer.toString();
	}

	/**
	 * Escapes reserved HTML characters in the given string.
	 * <p>
	 * <b>Warning:</b> Does not preserve whitespace.
	 *
	 * @param content the input string
	 * @return the string with escaped characters
	 *
	 * @see #convertToHTMLContentWithWhitespace(String) for use in browsers
	 */
	public static String convertToHTMLContent(String content) {
		content= replace(content, '&', "&amp;"); //$NON-NLS-1$
		content= replace(content, '"', "&quot;"); //$NON-NLS-1$
		content= replace(content, '<', "&lt;"); //$NON-NLS-1$
		return replace(content, '>', "&gt;"); //$NON-NLS-1$
	}

	/**
	 * Escapes reserved HTML characters in the given string and returns them in a way that preserves
	 * whitespace in a browser.
	 *
	 * @param content the input string
	 * @return the processed string
	 *
	 * @see #addPreFormatted(StringBuilder, String)
	 * @see #convertToHTMLContent(String)
	 * @since 3.7
	 */
	public static String convertToHTMLContentWithWhitespace(String content) {
		content= replace(content, '&', "&amp;"); //$NON-NLS-1$
		content= replace(content, '"', "&quot;"); //$NON-NLS-1$
		content= replace(content, '<', "&lt;"); //$NON-NLS-1$
		content= replace(content, '>', "&gt;"); //$NON-NLS-1$
		return "<span style='white-space:pre'>" + content + "</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String read(Reader rd) {
		StringBuilder buffer= new StringBuilder();
		char[] readBuffer= new char[2048];

		try {
			int n= rd.read(readBuffer);
			while (n > 0) {
				buffer.append(readBuffer, 0, n);
				n= rd.read(readBuffer);
			}
			return buffer.toString();
		} catch (IOException x) {
		}
		return null;
	}

	/**
	 *
	 * @param buffer the output StringBuilder
	 * @param position offset where the prolog is placed
	 * @param fgRGB Foreground-Color
	 * @param bgRGB Background-Color
	 * @param styleSheet Stylesheet
	 */
	public void insertPageProlog(StringBuilder buffer, int position, RGB fgRGB, RGB bgRGB, String styleSheet) {
		if (fgRGB == null)
			fgRGB= this.fgColor;
		if (bgRGB == null)
			bgRGB= this.bgColor;

		StringBuilder pageProlog= new StringBuilder(300);

		pageProlog.append("<html>"); //$NON-NLS-1$

		appendStyleSheet(pageProlog, styleSheet, fgRGB, bgRGB);

		appendColors(pageProlog, fgRGB, bgRGB);

		buffer.insert(position,  pageProlog.toString());
	}

	/**
	 *
	 *
	 * @param pageProlog The Pageprolog where the color has to be set
	 * @param fgRGB Foreground-Color
	 * @param bgRGB Background-Color
	 *
	 */
	private void appendColors(StringBuilder pageProlog, RGB fgRGB, RGB bgRGB) {
		pageProlog.append("<body text=\""); //$NON-NLS-1$
		appendColor(pageProlog, fgRGB);
		pageProlog.append("\" bgcolor=\""); //$NON-NLS-1$
		appendColor(pageProlog, bgRGB);
		pageProlog.append("\">"); //$NON-NLS-1$
	}

	/**
	 *
	 *
	 * @param buffer The Output buffer
	 * @param rgb RGB-Value
	 *
	 */
	private void appendColor(StringBuilder buffer, RGB rgb) {
		buffer.append('#');
		appendAsHexString(buffer, rgb.red);
		appendAsHexString(buffer, rgb.green);
		appendAsHexString(buffer, rgb.blue);
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param intValue the intValue will be converted to hex and appended
	 *
	 */
	private void appendAsHexString(StringBuilder buffer, int intValue) {
		String hexValue= Integer.toHexString(intValue);
		if (hexValue.length() == 1)
			buffer.append('0');
		buffer.append(hexValue);
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param styles array with styles to be appended
	 *
	 */
	public void insertStyles(StringBuilder buffer, String[] styles) {
		if (styles == null || styles.length == 0)
			return;

		StringBuilder styleBuf= new StringBuilder(10 * styles.length);
		for (String style : styles) {
			styleBuf.append(" style=\""); //$NON-NLS-1$
			styleBuf.append(style);
			styleBuf.append('"');
		}

		// Find insertion index
		// a) within existing body tag with trailing space
		int index= buffer.indexOf("<body "); //$NON-NLS-1$
		if (index != -1) {
			buffer.insert(index+5, styleBuf);
			return;
		}

		// b) within existing body tag without attributes
		index= buffer.indexOf("<body>"); //$NON-NLS-1$
		if (index != -1) {
			buffer.insert(index+5, ' ');
			buffer.insert(index+6, styleBuf);
			return;
		}
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param styleSheet the stylesheet
	 * @param fgRGB Foreground-Color
	 * @param bgRGB Background-Color
	 *
	 */
	private void appendStyleSheet(StringBuilder buffer, String styleSheet, RGB fgRGB, RGB bgRGB) {
		if (styleSheet == null)
			return;

		// workaround for https://bugs.eclipse.org/318243
		StringBuilder fg= new StringBuilder();
		appendColor(fg, fgRGB);
		styleSheet= styleSheet.replaceAll("InfoText", fg.toString()); //$NON-NLS-1$
		StringBuilder bg= new StringBuilder();
		appendColor(bg, bgRGB);
		styleSheet= styleSheet.replaceAll("InfoBackground", bg.toString()); //$NON-NLS-1$

		// replace link colors with colors from preferences
		StringBuilder lColor= new StringBuilder();
		appendColor(lColor, this.linkColor);
		styleSheet= styleSheet.replaceAll("hyperlinkColor", lColor.toString()); //$NON-NLS-1$
		StringBuilder activeLinkColor= new StringBuilder();
		appendColor(activeLinkColor, this.alinkColor);
		styleSheet= styleSheet.replaceAll("activeHyperlinkColor", activeLinkColor.toString()); //$NON-NLS-1$

		buffer.append("<head><style CHARSET=\"ISO-8859-1\" TYPE=\"text/css\">"); //$NON-NLS-1$
		buffer.append(styleSheet);
		buffer.append("</style></head>"); //$NON-NLS-1$
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param styleSheetURL the URL to the Stylesheet
	 *
	 */
	private static void appendStyleSheetURL(StringBuilder buffer, URL styleSheetURL) {
		if (styleSheetURL == null)
			return;

		buffer.append("<head>"); //$NON-NLS-1$

		buffer.append("<LINK REL=\"stylesheet\" HREF= \""); //$NON-NLS-1$
		buffer.append(styleSheetURL);
		buffer.append("\" CHARSET=\"ISO-8859-1\" TYPE=\"text/css\">"); //$NON-NLS-1$

		buffer.append("</head>"); //$NON-NLS-1$
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param position the offset
	 *
	 */
	public void insertPageProlog(StringBuilder buffer, int position) {
		StringBuilder pageProlog= new StringBuilder(60);
		pageProlog.append("<html>"); //$NON-NLS-1$
		appendColors(pageProlog, this.fgColor, this.bgColor);
		buffer.insert(position,  pageProlog.toString());
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param position the offset
	 * @param styleSheetURL URL to the Stylesheet
	 *
	 */
	public void insertPageProlog(StringBuilder buffer, int position, URL styleSheetURL) {
		StringBuilder pageProlog= new StringBuilder(300);
		pageProlog.append("<html>"); //$NON-NLS-1$
		appendStyleSheetURL(pageProlog, styleSheetURL);
		appendColors(pageProlog, this.fgColor, this.bgColor);
		buffer.insert(position,  pageProlog.toString());
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param position the offset
	 * @param styleSheet Stylesheet
	 *
	 */
	public void insertPageProlog(StringBuilder buffer, int position, String styleSheet) {
		insertPageProlog(buffer, position, null, null, styleSheet);
	}

	/**
	 *
	 * @param buffer the output buffer
	 *
	 */
	public void addPageProlog(StringBuilder buffer) {
		insertPageProlog(buffer, buffer.length());
	}

	public void addPageEpilog(StringBuilder buffer) {
		buffer.append("</body></html>"); //$NON-NLS-1$
	}

	/**
	 *
	 * @param buffer the output buffer
	 *
	 */
	public void startBulletList(StringBuilder buffer) {
		buffer.append("<ul>"); //$NON-NLS-1$
	}

	/**
	 * ends the bulletpointlist
	 *
	 * @param buffer the output buffer
	 *
	 */
	public void endBulletList(StringBuilder buffer) {
		buffer.append("</ul>"); //$NON-NLS-1$
	}

	/**
	 * Adds bulletpoint
	 *
	 * @param buffer the output buffer
	 * @param bullet the bulletpoint
	 *
	 */
	public void addBullet(StringBuilder buffer, String bullet) {
		if (bullet != null) {
			buffer.append("<li>"); //$NON-NLS-1$
			buffer.append(bullet);
			buffer.append("</li>"); //$NON-NLS-1$
		}
	}

	/**
	 *
	 * Adds h5 headline
	 *
	 * @param buffer the output buffer
	 * @param header of h5 headline
	 *
	 */
	public void addSmallHeader(StringBuilder buffer, String header) {
		if (header != null) {
			buffer.append("<h5>"); //$NON-NLS-1$
			buffer.append(header);
			buffer.append("</h5>"); //$NON-NLS-1$
		}
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param paragraph the content of the paragraph
	 *
	 */
	public void addParagraph(StringBuilder buffer, String paragraph) {
		if (paragraph != null) {
			buffer.append("<p>"); //$NON-NLS-1$
			buffer.append(paragraph);
		}
	}

	/**
	 * Appends a string and keeps its whitespace and newlines.
	 *
	 * @param buffer the output StringBuilder
	 * @param preFormatted the string that should be rendered with whitespace preserved
	 *
	 * @see #convertToHTMLContent(String)
	 * @see #convertToHTMLContentWithWhitespace(String)
	 * @since 3.7
	 */
	public void addPreFormatted(StringBuilder buffer, String preFormatted) {
		if (preFormatted != null) {
			buffer.append("<pre>"); //$NON-NLS-1$
			buffer.append(preFormatted);
			buffer.append("</pre>"); //$NON-NLS-1$
		}
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param paragraphReader The content of the Read will be added to output buffer
	 *
	 */
	public void addParagraph(StringBuilder buffer, Reader paragraphReader) {
		if (paragraphReader != null)
			addParagraph(buffer, read(paragraphReader));
	}

	/**
	 * Replaces the following style attributes of the font definition of the <code>html</code>
	 * element:
	 * <ul>
	 * <li>font-size</li>
	 * <li>font-weight</li>
	 * <li>font-style</li>
	 * <li>font-family</li>
	 * </ul>
	 * The font's name is used as font family, a <code>sans-serif</code> default font family is
	 * appended for the case that the given font name is not available.
	 * <p>
	 * If the listed font attributes are not contained in the passed style list, nothing happens.
	 * </p>
	 *
	 * @param styles CSS style definitions
	 * @param bold Should the text be bold
	 * @param italic Should the text be italic
	 * @param height Height of the text
	 * @param fontName font name
	 * @return the modified style definitions
	 * @since 3.3
	 */
	public static String convertTopLevelFont(String styles, boolean bold, boolean italic, int height, String fontName) {
		String size= Integer.toString(height) + UNIT;
		String family= "'" + fontName + "',sans-serif"; //$NON-NLS-1$ //$NON-NLS-2$

		styles= styles.replaceFirst("(html\\s*\\{.*(?:\\s|;)font-size:\\s*)\\d+pt(\\;?.*\\})", "$1" + size + "$2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		styles= styles.replaceFirst("(html\\s*\\{.*(?:\\s|;)font-weight:\\s*)\\w+(\\;?.*\\})", "$1" + (bold ? "bold" : "normal") + "$2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		styles= styles.replaceFirst("(html\\s*\\{.*(?:\\s|;)font-style:\\s*)\\w+(\\;?.*\\})", "$1" + (italic ? "italic" : "normal") + "$2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		styles= styles.replaceFirst("(html\\s*\\{.*(?:\\s|;)font-family:\\s*).+?(;.*\\})", "$1" + family + "$2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return styles;
	}
}
