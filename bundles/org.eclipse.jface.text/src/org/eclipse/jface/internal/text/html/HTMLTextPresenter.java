/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;


/**
 * <p>
 * Moved into this package from <code>org.eclipse.jface.internal.text.revisions</code>.</p>
 */
public class HTMLTextPresenter implements DefaultInformationControl.IInformationPresenter, DefaultInformationControl.IInformationPresenterExtension {

	private static final String LINE_DELIM= System.lineSeparator();

	private boolean fEnforceUpperLineLimit;

	public HTMLTextPresenter(boolean enforceUpperLineLimit) {
		fEnforceUpperLineLimit= enforceUpperLineLimit;
	}

	public HTMLTextPresenter() {
		this(true);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.eclipse.jface.text.DefaultInformationControl.IInformationPresenter#updatePresentation(org.eclipse.swt.widgets.Display,
	 *      java.lang.String, org.eclipse.jface.text.TextPresentation, int, int)
	 * @deprecated Use {@link #updatePresentation(Drawable, String, TextPresentation, int, int)}
	 *             instead
	 */
	@Deprecated
	@Override
	public String updatePresentation(Display display, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight) {
		return updatePresentation((Drawable)display, hoverInfo, presentation, maxWidth, maxHeight);
	}

	@Override
	public String updatePresentation(Drawable drawable, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight) {

		if (hoverInfo == null)
			return null;

		if (!fEnforceUpperLineLimit) {
			maxHeight= Integer.MAX_VALUE;
		}

		presentation.clear();

		try (HTML2TextReader reader= new HTML2TextReader(new StringReader(hoverInfo), presentation)){
			hoverInfo= reader.getString().trim();
		} catch (IOException e) {
			return null;
		}

		Font[] fonts= new Font[(SWT.BOLD | SWT.ITALIC)];
		GC gc= new GC(drawable);
		Font baseFont= gc.getFont(); // the html reader does not set font info, so ranges will based on the default font of the drawable
		fonts[0]= baseFont;
		try {
			TextLayout layout= new TextLayout(gc.getDevice());
			try {
				layout.setWidth(maxWidth);
				layout.setText(hoverInfo);
				layout.setFont(baseFont);

				for (Iterator<StyleRange> iterator= presentation.getAllStyleRangeIterator(); iterator.hasNext();) {
					StyleRange range= iterator.next();
					// text layouts don't use <code>fontStyles</code>, so use a bold/italic font instead
					fonts[range.fontStyle]= maybeAllocateFont(baseFont, range.fontStyle, fonts[range.fontStyle]);
					range.font= fonts[range.fontStyle];
					layout.setStyle(range, range.start, range.start + range.length - 1);
				}

				if (layout.getLineCount() == 0) {
					return ""; //$NON-NLS-1$
				}

				int[] lineOffsets= layout.getLineOffsets();

				int textHeight= 0;
				StringBuilder buffer= new StringBuilder();
				Rectangle currentLineBounds= layout.getLineBounds(0);
				int currentLineIndex= 0;
				List<Integer> positionOfInsertedLineBreaks= new ArrayList<>();
				boolean addMoreLines= true;

				// append lines to the buffer until we run out of vertical space
				while (currentLineIndex < layout.getLineCount() - 1) {
					textHeight+= currentLineBounds.height;

					Rectangle nextLineBounds= layout.getLineBounds(currentLineIndex + 1);
					if (textHeight + nextLineBounds.height <= maxHeight) {
						// we have room for at least the current line and the next one
						String line= hoverInfo.substring(lineOffsets[currentLineIndex], lineOffsets[currentLineIndex + 1]);
						buffer.append(line);
						if (!line.endsWith(LINE_DELIM)) {
							// new line was started by the layout wrapping the text, not by a line delimiter in the text, need to add it
							positionOfInsertedLineBreaks.add(lineOffsets[currentLineIndex + 1]);
							buffer.append(LINE_DELIM);
						}
					} else {
						// we only have room for one more line;
						addMoreLines= false;
						if (currentLineIndex == 0) {
							// never make the first line an ellipsis, even if we don't have enough space
							buffer.append(hoverInfo.substring(lineOffsets[currentLineIndex], lineOffsets[currentLineIndex + 1]));
						} else {
							buffer.append(HTMLMessages.getString("HTMLTextPresenter.ellipse")); //$NON-NLS-1$
						}
						break;
					}
					currentLineBounds= nextLineBounds;
					currentLineIndex++;
				}
				if (addMoreLines && currentLineIndex < layout.getLineCount()) {
					// either there is only a single line, or we have enough space for all lines
					buffer.append(hoverInfo.substring(lineOffsets[currentLineIndex], lineOffsets[currentLineIndex + 1]));
				}

				presentation.clear();
				int insertedBreaksCount= 0; // we need to adjust all positions by the space taken by inserted line breaks
				int[] ranges= layout.getRanges();
				int[] adjustedRanges= new int[ranges.length];
				for (int i= 0; i < ranges.length; i++) {
					while (insertedBreaksCount < positionOfInsertedLineBreaks.size() &&
							positionOfInsertedLineBreaks.get(insertedBreaksCount) <= ranges[i]) {
						// advance in the list of positions until the current position is greater or equal than where the range offset is
						insertedBreaksCount++;
					}
					adjustedRanges[i]= ranges[i] + insertedBreaksCount * System.lineSeparator().length();
				}

				for (int i= 0; i < ranges.length; i+= 2) {
					// re-apply the styles, adjusting for inserted line breaks
					TextStyle style= layout.getStyle(ranges[i]);

					StyleRange styleRange= new StyleRange(style);
					styleRange.fontStyle= indexOf(fonts, style.font);
					styleRange.font= null; // need to remove the bold/italic font: it's going to be disposed
					styleRange.start= adjustedRanges[i];
					styleRange.length= adjustedRanges[i + 1] - adjustedRanges[i] + 1;
					presentation.addStyleRange(styleRange);
				}

				presentation.setResultWindow(new Region(0, buffer.length()));

				return buffer.toString();
			} finally {
				layout.dispose();
			}
		} finally {
			gc.dispose();
			// dispose all fonts except the first font, which is the base font
			for (int i= 1; i < fonts.length; i++) {
				if (fonts[i] != null) {
					fonts[i].dispose();
				}
			}
		}
	}

	private int indexOf(Font[] fonts, Font font) {
		for (int i= 0; i < fonts.length; i++) {
			if (fonts[i] == font) {
				return i;
			}
		}
		throw new IllegalArgumentException("Unexpected font found"); //$NON-NLS-1$
	}

	private Font maybeAllocateFont(Font baseFont, int fontStyle, Font font) {
		if (font != null) {
			return font;
		}

		return new Font(baseFont.getDevice(), getFontData(baseFont, fontStyle));
	}

	private FontData[] getFontData(Font baseFont, int style) {
		FontData[] fontDatas= baseFont.getFontData();
		for (FontData fontData : fontDatas) {
			fontData.setStyle(style);
		}
		return fontDatas;
	}
}

