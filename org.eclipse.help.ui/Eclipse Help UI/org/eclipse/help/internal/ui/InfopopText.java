package org.eclipse.help.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.text.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.custom.*;
import org.eclipse.help.internal.ui.util.TString;
import org.eclipse.help.internal.contributors.ContextContributor;

/**
 */
public class InfopopText extends Canvas {
	/** Text to display */
	private String text;

	/** Line breaker */
	private static BreakIterator lineBreaker = BreakIterator.getLineInstance();
	/** Lines after splitting */
	ArrayList lines = new ArrayList(10);
	/** Style ranges, per line */
	ArrayList lineStyleRanges = new ArrayList(10);

	/**
	 * Constructor
	 */
	public InfopopText(Composite parent, int style) {
		super(parent, style);

		//setBackground();

		// cleanup any resources here
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				//white.dispose();
			}
		});
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				InfopopText.this.paintControl(e);
			}
		});

	}
	/**
	 * Adjusts the style ranges from a line that was split
	 */
	private void adjustStyles(int oldLineIndex) {
		ArrayList styles = (ArrayList) lineStyleRanges.get(oldLineIndex);
		if (styles.isEmpty())
			lineStyleRanges.add(oldLineIndex + 1, styles);
		else {
			String line = (String) lines.get(oldLineIndex);
			for (int i = 0; i < styles.size(); i++) {
				StyleRange style = (StyleRange) styles.get(i);
				if (style.start >= line.length()) {
					// move all the remaining styles to the new line
					ArrayList newStyles = new ArrayList(styles.size() - i + 1);
					for (int j = i; j < styles.size();) {
						StyleRange s = (StyleRange) styles.get(j);
						styles.remove(s);
						s.start = s.start - line.length();
						newStyles.add(s);
					}
					lineStyleRanges.add(oldLineIndex + 1, newStyles);
					return;
				} else
					if (style.start + style.length > line.length()) {
						// split this style over two lines and
						// move the remaining styles to the new line as well
						ArrayList newStyles = new ArrayList(styles.size() - i + 1);
						StyleRange splitRange1 =
							new StyleRange(style.start, line.length() - style.start, null, null);
						StyleRange splitRange2 =
							new StyleRange(0, style.start + style.length - line.length(), null, null);
						styles.remove(style);
						styles.add(splitRange1);
						newStyles.add(splitRange2);
						for (int j = i + 1; j < styles.size();) {
							StyleRange s = (StyleRange) styles.get(j);
							styles.remove(s);
							s.start = s.start - line.length();
							newStyles.add(s);
						}
						lineStyleRanges.add(oldLineIndex + 1, newStyles);
						return;
					}
			}
			// all the styles were applied to the remaining of the line
			lineStyleRanges.add(oldLineIndex + 1, new ArrayList(0));
		}
	}
	private Point computeLineSize(int lineIndex, int wHint, int hHint) {
		String line = (String) lines.get(lineIndex);
		ArrayList styles = (ArrayList) lineStyleRanges.get(lineIndex);
		if (styles.isEmpty())
			return computeStringSize(line, wHint, hHint, SWT.NONE);

		// Compute the styled and unstyled string sizes
		int offset = 0;
		StringBuffer unstyled = new StringBuffer();
		StringBuffer styled = new StringBuffer();
		for (Iterator iterator = styles.iterator(); iterator.hasNext();) {
			StyleRange range = (StyleRange) iterator.next();
			unstyled.append(line.substring(offset, range.start));
			styled.append(line.substring(range.start, range.start + range.length));
			offset = range.start + range.length;
		}
		if (offset < line.length())
			unstyled.append(line.substring(offset));

		String unstyledText = unstyled.toString();
		String styledText = styled.toString();
		Point unstyledSize = computeStringSize(unstyledText, wHint, hHint, SWT.NONE);
		Point styledSize = computeStringSize(styledText, wHint, hHint, SWT.BOLD);

		return new Point(
			unstyledSize.x + styledSize.x,
			Math.max(unstyledSize.y, styledSize.y));
	}
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point[] lineSizes = new Point[lines.size()];
		for (int i = 0; i < lineSizes.length; i++)
			lineSizes[i] = computeLineSize(i, wHint, hHint);

		int maxWidth = 0, maxHeight = 0;
		for (int i = 0; i < lineSizes.length; i++) {
			maxWidth = Math.max(maxWidth, lineSizes[i].x);
			maxHeight += lineSizes[i].y;
		}

		return new Point(maxWidth + 2, maxHeight + 2);
	}
	private Point computeStringSize(
		String string,
		int wHint,
		int hHint,
		int style) {
		int width = 0, height = 0;

		GC gc = new GC(this);
		Font defaultFont = gc.getFont();
		FontData fd = gc.getFont().getFontData()[0];
		fd.setStyle(SWT.BOLD);
		Font boldFont = new Font(Display.getCurrent(), fd);

		if (string != null) {
			if (style == SWT.BOLD)
				gc.setFont(boldFont);
			Point extent = gc.stringExtent(string);
			if (style == SWT.BOLD)
				gc.setFont(defaultFont);
			width += extent.x;
			height = Math.max(height, extent.y);
		}
		if (wHint != SWT.DEFAULT)
			width = wHint;
		if (hHint != SWT.DEFAULT)
			height = hHint;

		// cleanup
		boldFont.dispose();
		gc.dispose();

		return new Point(width, height);
	}
	private static int getLineBreak(String line) {
		int max = getMaxLineChars();
		lineBreaker.setText(line);
		int lastGoodIndex = 0;
		int currentIndex = lineBreaker.first();
		while (currentIndex < max && currentIndex != BreakIterator.DONE) {
			lastGoodIndex = currentIndex;
			currentIndex = lineBreaker.next();
		}

		return lastGoodIndex;
	}
	/**
	 * Returns the maximum number of characters on a line
	 */
	private static int getMaxLineChars() {
		return 72;
	}
	/**
	 * Returns the styles ranges from the text.
	 * Ranges are relative to the stripped text.
	 */
	private static ArrayList getStyleRanges(String styledText) {
		ArrayList styles = new ArrayList(4);

		int offset = 0;
		int adjustment = 0;
		int tagLength = ContextContributor.BOLD_TAG.length();
		int tagEndLength = ContextContributor.BOLD_CLOSE_TAG.length();

		while (offset < styledText.length()) {
			int begin = styledText.indexOf(ContextContributor.BOLD_TAG, offset);
			if (begin == -1) {
				int end =
					styledText.indexOf(ContextContributor.BOLD_CLOSE_TAG, offset + tagLength);
				if (end == -1) {
					break; // no tags left in the string    
				} else {
					//.... text ... </b> ....
					StyleRange range =
						new StyleRange(offset - adjustment, end - offset, null, null, SWT.BOLD);
					styles.add(range);
					offset = end + tagEndLength;
					adjustment += tagEndLength;
				}
			} else {
				int end =
					styledText.indexOf(ContextContributor.BOLD_CLOSE_TAG, offset + tagLength);
				if (end == -1) {
					// .... text <b> ... [EOS]
					StyleRange range =
						new StyleRange(
							offset + tagLength - adjustment,
							styledText.length() - offset - tagLength,
							null,
							null,
							SWT.BOLD);
					styles.add(range);
					break; // no tags left in the string    
				} else {
					//.... text ..<b> .. text.. </b> ....
					StyleRange range =
						new StyleRange(
							begin - adjustment,
							end - begin - tagLength,
							null,
							null,
							SWT.BOLD);
					styles.add(range);
					offset = end + tagEndLength;
					adjustment += tagLength + tagEndLength;
				}
			}
		}
		return styles;
	}
	public String getText() {
		return text;
	}
	/**
	 * Returns the text without the style
	 */
	private static String getUnstyledText(String styledText) {
		String s = TString.change(styledText, ContextContributor.BOLD_TAG, "");
		s = TString.change(s, ContextContributor.BOLD_CLOSE_TAG, "");
		s = s.trim();
		return s;
	}
	/**
	 * Paint the control when asked for.
	 */
	void paintControl(PaintEvent e) {
		GC gc = e.gc;
		if (text != null) {
			Font defaultFont = gc.getFont();
			FontData fd = gc.getFont().getFontData()[0];
			fd.setStyle(SWT.BOLD);
			Font boldFont = new Font(Display.getCurrent(), fd);
			int fontHeight = gc.getFontMetrics().getHeight();

			for (int i = 0; i < lines.size(); i++) {
				Point lineSize = computeLineSize(i, e.width, e.height);
				String line = (String) lines.get(i);
				ArrayList styles = (ArrayList) lineStyleRanges.get(i);
				if (styles.isEmpty()) {
					gc.drawString(line, e.x, e.y + fontHeight * i);
					continue;
				}

				// Compute the styled and unstyled string sizes
				int offset = 0;
				int x = 0;
				for (Iterator iterator = styles.iterator(); iterator.hasNext();) {
					StyleRange range = (StyleRange) iterator.next();
					String unstyled = line.substring(offset, range.start);
					String styled = line.substring(range.start, range.start + range.length);
					offset = range.start + range.length;
					gc.drawString(unstyled, e.x + x, e.y + fontHeight * i);
					x += gc.stringExtent(unstyled).x;
					gc.setFont(boldFont);
					gc.drawString(styled, e.x + x, e.y + fontHeight * i);
					x += gc.stringExtent(styled).x;
					gc.setFont(defaultFont);
				}

				if (offset < line.length())
					gc.drawString(line.substring(offset), x, e.y + fontHeight * i);
			}

			boldFont.dispose();
		}
	}
	private void processLineBreaks(String text) {
		// Create the original lines with style stripped
		// and keep track of styles
		StringTokenizer st = new StringTokenizer(text, "\r\n");
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			lines.add(getUnstyledText(line));
			lineStyleRanges.add(getStyleRanges(line));
		}

		// Break long lines
		for (int i = 0; i < lines.size(); i++) {
			String line = (String) lines.get(i);
			while (line.length() > 0) {
				int linebreak = getLineBreak(line);

				if (linebreak == 0 || linebreak == line.length())
					break;

				String newline = line.substring(0, linebreak);
				lines.remove(i);
				lines.add(i, newline);
				line = line.substring(linebreak);
				lines.add(++i, line);
				
				// adjust the styles for the old line
				adjustStyles(i-1);
			}
		}
	}
	/**
	 * Set the text to display
	 */
	public void setText(String text) {
		// Break long lines, and also update the style ranges
		// if new lines have been introduced.
		processLineBreaks(text);

		StringBuffer sb = new StringBuffer();
		Iterator it = lines.iterator();
		if (it.hasNext())
			sb.append((String) it.next());
		while (it.hasNext())
			sb.append('\n').append((String) it.next());
		this.text = sb.toString();

		redraw();
	}
}
