/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
package org.eclipse.jface.internal.text.html;

import java.io.Reader;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import org.eclipse.text.html.HTMLBuilder;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.jface.text.DefaultInformationControl;

/**
 * Provides a set of convenience methods for creating HTML pages.
 * <p>
 * Moved into this package from <code>org.eclipse.jface.internal.text.revisions</code>.</p>
 */
public class HTMLPrinter {
	private static final HTMLBuilder CORE= new HTMLBuilder();

	static {
		final Display display= Display.getDefault();
		if (display != null && !display.isDisposed()) {
			try {
				display.asyncExec(() -> {
					cacheColors(display);
					installColorUpdater(display);
				});
			} catch (SWTError err) {
				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=45294
				if (err.code != SWT.ERROR_DEVICE_DISPOSED)
					throw err;
			}
		}
	}

	private HTMLPrinter() {
	}

	private static void installColorUpdater(final Display display) {
		display.addListener(SWT.Settings, event -> cacheColors(display));
		JFaceResources.getColorRegistry().addListener(event -> cacheColors(display));
	}

	private static org.eclipse.text.html.RGB fromRGB(RGB val) {
		// Preserve	null RGB as HTMLBuilder contains the default colors and sets them accordingly
		// in case of null parameter passed
		if (val == null) {
			return null;
		}
		return new org.eclipse.text.html.RGB(val.red, val.green, val.blue);
	}
	private static void cacheColors(Display display) {
		org.eclipse.text.html.RGB bg= fromRGB(JFaceColors.getInformationViewerBackgroundColor(display).getRGB());
		org.eclipse.text.html.RGB fg= fromRGB(JFaceColors.getInformationViewerForegroundColor(display).getRGB());
		Color hyperlinkText= JFaceColors.getHyperlinkText(display);
		Color activeHyperlinkText= JFaceColors.getActiveHyperlinkText(display);
		org.eclipse.text.html.RGB link= hyperlinkText == null ? null : fromRGB(hyperlinkText.getRGB());
		org.eclipse.text.html.RGB alink= activeHyperlinkText == null ? null : fromRGB(activeHyperlinkText.getRGB());
		CORE.setColors(bg, fg, link, alink);
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
	 * @see #addPreFormatted(StringBuilder, String) for rendering with an HTML2TextReader
	 */
	public static String convertToHTMLContent(String content) {
		return HTMLBuilder.convertToHTMLContent(content);
	}

	/**
	 * Escapes reserved HTML characters in the given string and returns them in a way that preserves
	 * whitespace in a browser.
	 * <p>
	 * <b>Warning:</b> Whitespace will not be preserved when rendered with an
	 * {@link HTML2TextReader} (e.g. in a {@link DefaultInformationControl} that renders simple
	 * HTML).
	 *
	 * @param content the input string
	 * @return the processed string
	 *
	 * @see #addPreFormatted(StringBuilder, String)
	 * @see #convertToHTMLContent(String)
	 * @since 3.7
	 */
	public static String convertToHTMLContentWithWhitespace(String content) {
		return HTMLBuilder.convertToHTMLContentWithWhitespace(content);
	}

	public static String read(Reader rd) {
		return HTMLBuilder.read(rd);
	}

	/**
	 *
	 * @deprecated migrate to new StringBuilder API
	 *
	 * @param buffer the output StringBuilder
	 * @param position offset where the prolog is placed
	 * @param fgRGB Foreground-Color
	 * @param bgRGB Background-Color
	 * @param styleSheet Stylesheet
	 */
	@Deprecated
	public static void insertPageProlog(StringBuffer buffer, int position, RGB fgRGB, RGB bgRGB, String styleSheet) {
		runOp(buffer, (sb) -> CORE.insertPageProlog(sb, position, fromRGB(fgRGB), fromRGB(bgRGB), styleSheet));
	}

	/**
	 *
	 * @param buffer the output StringBuilder
	 * @param position offset where the prolog is placed
	 * @param fgRGB Foreground-Color
	 * @param bgRGB Background-Color
	 * @param styleSheet Stylesheet
	 */
	public static void insertPageProlog(StringBuilder buffer, int position, RGB fgRGB, RGB bgRGB, String styleSheet) {
		CORE.insertPageProlog(buffer, position, fromRGB(fgRGB), fromRGB(bgRGB), styleSheet);
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param styles array with styles to be appended
	 *
	 */
	public static void insertStyles(StringBuilder buffer, String[] styles) {
		CORE.insertStyles(buffer, styles);
	}


	/**
	 *
	 * @param buffer the output buffer
	 * @param styles array with styles to be appended
	 *
	 * @deprecated As of 3.13, replaced by {@link #insertStyles(StringBuilder, String[])}
	 */
	@Deprecated
	public static void insertStyles(StringBuffer buffer, String[] styles) {
		runOp(buffer, (sb) -> CORE.insertStyles(sb, styles));
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param position the offset
	 *
	 */
	public static void insertPageProlog(StringBuilder buffer, int position) {
		CORE.insertPageProlog(buffer, position);
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param position the offset
	 *
	 * @deprecated migrate to new StringBuilder API
	 */
	@Deprecated
	public static void insertPageProlog(StringBuffer buffer, int position) {
		runOp(buffer, (sb) -> CORE.insertPageProlog(sb, position));
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param position the offset
	 * @param styleSheetURL URL to the Stylesheet
	 *
	 * @deprecated migrate to new StringBuilder API
	 */
	@Deprecated
	public static void insertPageProlog(StringBuffer buffer, int position, URL styleSheetURL) {
		runOp(buffer, (sb) -> CORE.insertPageProlog(sb, position, styleSheetURL));
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param position the offset
	 * @param styleSheetURL URL to the Stylesheet
	 *
	 */
	public static void insertPageProlog(StringBuilder buffer, int position, URL styleSheetURL) {
		CORE.insertPageProlog(buffer, position, styleSheetURL);
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param position the offset
	 * @param styleSheet Stylesheet
	 *
	 */
	public static void insertPageProlog(StringBuilder buffer, int position, String styleSheet) {
		CORE.insertPageProlog(buffer, position, styleSheet);
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param position the offset
	 * @param styleSheet Stylesheet
	 *
	 * @deprecated migrate to new StringBuilder API
	 */
	@Deprecated
	public static void insertPageProlog(StringBuffer buffer, int position, String styleSheet) {
		runOp(buffer, (sb) -> CORE.insertPageProlog(sb, position, styleSheet));
	}

	/**
	 *
	 * @param buffer the output buffer
	 *
	 */
	public static void addPageProlog(StringBuilder buffer) {
		CORE.addPageProlog(buffer);
	}

	/**
	 *
	 * @param buffer the output buffer
	 *
	 * @deprecated migrate to new StringBuilder API
	 */
	@Deprecated
	public static void addPageProlog(StringBuffer buffer) {
		runOp(buffer, CORE::addPageProlog);
	}

	public static void addPageEpilog(StringBuilder buffer) {
		CORE.addPageEpilog(buffer);
	}

	/**
	 *
	 * @param buffer the output buffer
	 *
	 * @deprecated migrate to new StringBuilder API
	 */
	@Deprecated
	public static void addPageEpilog(StringBuffer buffer) {
		runOp(buffer, CORE::addPageEpilog);
	}

	/**
	 *
	 * @param buffer the output buffer
	 *
	 */
	public static void startBulletList(StringBuilder buffer) {
		CORE.startBulletList(buffer);
	}

	/**
	 *
	 * @param buffer the output buffer
	 *
	 * @deprecated migrate to new StringBuilder API
	 */
	@Deprecated
	public static void startBulletList(StringBuffer buffer) {
		runOp(buffer, CORE::startBulletList);
	}

	/**
	 * ends the bulletpointlist
	 *
	 * @param buffer the output buffer
	 *
	 */
	public static void endBulletList(StringBuilder buffer) {
		CORE.endBulletList(buffer);
	}

	/**
	 * ends the bulletpointlist
	 *
	 * @param buffer the output buffer
	 *
	 * @deprecated migrate to new StringBuilder API
	 */
	@Deprecated
	public static void endBulletList(StringBuffer buffer) {
		runOp(buffer, CORE::endBulletList);
	}

	/**
	 * Adds bulletpoint
	 *
	 * @param buffer the output buffer
	 * @param bullet the bulletpoint
	 *
	 */
	public static void addBullet(StringBuilder buffer, String bullet) {
		CORE.addBullet(buffer, bullet);
	}

	/**
	 * Adds bulletpoint
	 *
	 * @param buffer the output buffer
	 * @param bullet the bulletpoint
	 *
	 * @deprecated migrate to new StringBuilder API
	 */
	@Deprecated
	public static void addBullet(StringBuffer buffer, String bullet) {
		runOp(buffer, (sb) -> CORE.addBullet(sb, bullet));
	}

	/**
	 *
	 * Adds h5 headline
	 *
	 * @param buffer the output buffer
	 * @param header of h5 headline
	 *
	 */
	public static void addSmallHeader(StringBuilder buffer, String header) {
		CORE.addSmallHeader(buffer, header);
	}

	/**
	 *
	 * Adds h5 headline
	 *
	 * @param buffer the output buffer
	 * @param header of h5 headline
	 *
	 * @deprecated migrate to new StringBuilder API
	 */
	@Deprecated
	public static void addSmallHeader(StringBuffer buffer, String header) {
		runOp(buffer, (sb) -> CORE.addSmallHeader(sb, header));
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param paragraph the content of the paragraph
	 *
	 */
	public static void addParagraph(StringBuilder buffer, String paragraph) {
		CORE.addParagraph(buffer, paragraph);
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param paragraph the content of the paragraph
	 *
	 * @deprecated migrate to new StringBuilder API
	 */
	@Deprecated
	public static void addParagraph(StringBuffer buffer, String paragraph) {
		runOp(buffer, (sb) -> CORE.addParagraph(sb, paragraph));
	}

	/**
	 * Appends a string and keeps its whitespace and newlines.
	 * <p>
	 * <b>Warning:</b> This starts a new paragraph when rendered in a browser, but
	 * it doesn't starts a new paragraph when rendered with a {@link HTML2TextReader}
	 * (e.g. in a {@link DefaultInformationControl} that renders simple HTML).
	 *
	 * @param buffer the output StringBuilder
	 * @param preFormatted the string that should be rendered with whitespace preserved
	 *
	 * @see #convertToHTMLContent(String)
	 * @see #convertToHTMLContentWithWhitespace(String)
	 * @since 3.7
	 */
	public static void addPreFormatted(StringBuilder buffer, String preFormatted) {
		CORE.addPreFormatted(buffer, preFormatted);
	}

	/**
	 * Appends a string and keeps its whitespace and newlines.
	 * <p>
	 * <b>Warning:</b> This starts a new paragraph when rendered in a browser, but
	 * it doesn't starts a new paragraph when rendered with a {@link HTML2TextReader}
	 * (e.g. in a {@link DefaultInformationControl} that renders simple HTML).
	 *
	 * @param buffer the output buffer
	 * @param preFormatted the string that should be rendered with whitespace preserved
	 *
	 * @deprecated migrate to new StringBuilder API
	 *
	 * @see #convertToHTMLContent(String)
	 * @see #convertToHTMLContentWithWhitespace(String)
	 * @since 3.7
	 */
	@Deprecated
	public static void addPreFormatted(StringBuffer buffer, String preFormatted) {
		runOp(buffer, (sb) -> CORE.addPreFormatted(sb, preFormatted));
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param paragraphReader The content of the Read will be added to output buffer
	 *
	 */
	public static void addParagraph(StringBuilder buffer, Reader paragraphReader) {
		CORE.addParagraph(buffer, paragraphReader);
	}

	/**
	 *
	 * @param buffer the output buffer
	 * @param paragraphReader The content of the Read will be added to output buffer
	 *
	 * @deprecated migrate to new StringBuilder API
	 */
	@Deprecated
	public static void addParagraph(StringBuffer buffer, Reader paragraphReader) {
		runOp(buffer, (sb) -> CORE.addParagraph(sb, paragraphReader));
	}


	private static interface BuilderBuffer {
		void run(StringBuilder sb);
	}

	private static void runOp(StringBuffer buffer, BuilderBuffer bf) {
		StringBuilder sb = new StringBuilder();
		bf.run(sb);
		buffer.append(sb.toString());
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
	 * @param fontData the font information to use
	 * @return the modified style definitions
	 * @since 3.3
	 */
	public static String convertTopLevelFont(String styles, FontData fontData) {
		boolean bold= (fontData.getStyle() & SWT.BOLD) != 0;
		boolean italic= (fontData.getStyle() & SWT.ITALIC) != 0;
		return HTMLBuilder.convertTopLevelFont(styles, bold, italic, fontData.getHeight(), fontData.getName());
	}
}
