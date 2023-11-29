/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
package org.eclipse.ui.internal.about;

import java.util.ArrayList;
import java.util.StringTokenizer;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;

/**
 * Manages links in styled text.
 */

public class AboutTextManager {

	/**
	 * Scan the contents of the about text
	 *
	 * @return AboutItem
	 */
	public static AboutItem scan(String aboutText) {
		ArrayList<int[]> linkRanges = new ArrayList<>();
		ArrayList<String> links = new ArrayList<>();

		// Slightly modified version of JFace URL detection,
		// see org.eclipse.jface.text.hyperlink.URLHyperlinkDetector

		int urlSeparatorOffset = aboutText.indexOf("://"); //$NON-NLS-1$
		while (urlSeparatorOffset >= 0) {

			boolean startDoubleQuote = false;

			// URL protocol (left to "://")
			int urlOffset = urlSeparatorOffset;
			char ch;
			do {
				urlOffset--;
				ch = ' ';
				if (urlOffset > -1)
					ch = aboutText.charAt(urlOffset);
				startDoubleQuote = ch == '"';
			} while (Character.isUnicodeIdentifierStart(ch));
			urlOffset++;

			// Right to "://"
			StringTokenizer tokenizer = new StringTokenizer(aboutText.substring(urlSeparatorOffset + 3), " \t\n\r\f<>", false); //$NON-NLS-1$
			if (!tokenizer.hasMoreTokens())
				return null;

			int urlLength = tokenizer.nextToken().length() + 3 + urlSeparatorOffset - urlOffset;

			if (startDoubleQuote) {
				int endOffset = -1;
				int nextDoubleQuote = aboutText.indexOf('"', urlOffset);
				int nextWhitespace = aboutText.indexOf(' ', urlOffset);
				if (nextDoubleQuote != -1 && nextWhitespace != -1)
					endOffset = Math.min(nextDoubleQuote, nextWhitespace);
				else if (nextDoubleQuote != -1)
					endOffset = nextDoubleQuote;
				else if (nextWhitespace != -1)
					endOffset = nextWhitespace;
				if (endOffset != -1)
					urlLength = endOffset - urlOffset;
			}

			linkRanges.add(new int[] { urlOffset, urlLength });
			links.add(aboutText.substring(urlOffset, urlOffset + urlLength));

			urlSeparatorOffset = aboutText.indexOf("://", urlOffset + urlLength + 1); //$NON-NLS-1$
		}
		return new AboutItem(aboutText, linkRanges.toArray(new int[linkRanges.size()][2]),
				links.toArray(new String[links.size()]));
	}

	private StyledText styledText;

	private boolean mouseDown = false;

	private boolean dragEvent = false;

	private AboutItem item;

	public AboutTextManager(StyledText text) {
		this.styledText = text;
		addListeners();
	}

	/**
	 * Adds listeners to the given styled text
	 */
	protected void addListeners() {
		Cursor handCursor = styledText.getDisplay().getSystemCursor(SWT.CURSOR_HAND);
		Cursor busyCursor = styledText.getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
		styledText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button != 1) {
					return;
				}
				mouseDown = true;
			}

			@Override
			public void mouseUp(MouseEvent e) {
				mouseDown = false;
				int offset = styledText.getCaretOffset();
				if (dragEvent) {
					// don't activate a link during a drag/mouse up operation
					dragEvent = false;
					if (item != null && item.isLinkAt(offset)) {
						styledText.setCursor(handCursor);
					}
				} else if (item != null && item.isLinkAt(offset)) {
					styledText.setCursor(busyCursor);
					AboutUtils.openLink(item.getLinkAt(offset));
					StyleRange selectionRange = getCurrentRange();
					styledText.setSelectionRange(selectionRange.start, selectionRange.length);
					styledText.setCursor(null);
				}
			}
		});

		styledText.addMouseMoveListener(e -> {
			// Do not change cursor on drag events
			if (mouseDown) {
				if (!dragEvent) {
					StyledText text1 = (StyledText) e.widget;
					text1.setCursor(null);
				}
				dragEvent = true;
				return;
			}
			StyledText text2 = (StyledText) e.widget;
			int offset = text2.getOffsetAtPoint(new Point(e.x, e.y));
			if (offset == -1) {
				text2.setCursor(null);
			} else if (item != null && item.isLinkAt(offset)) {
				text2.setCursor(handCursor);
			} else {
				text2.setCursor(null);
			}
		});

		styledText.addTraverseListener(e -> {
			switch (e.detail) {
			case SWT.TRAVERSE_ESCAPE:
				e.doit = true;
				break;
			case SWT.TRAVERSE_TAB_NEXT:
				// Previously traverse out in the backward direction?
				Point nextSelection = styledText.getSelection();
				int charCount = styledText.getCharCount();
				if ((nextSelection.x == charCount) && (nextSelection.y == charCount)) {
					styledText.setSelection(0);
				}
				StyleRange nextRange = findNextRange();
				if (nextRange == null) {
					// Next time in start at beginning, also used by
					// TRAVERSE_TAB_PREVIOUS to indicate we traversed out
					// in the forward direction
					styledText.setSelection(0);
					e.doit = true;
				} else {
					styledText.setSelectionRange(nextRange.start, nextRange.length);
					e.doit = true;
					e.detail = SWT.TRAVERSE_NONE;
				}
				break;
			case SWT.TRAVERSE_TAB_PREVIOUS:
				// Previously traverse out in the forward direction?
				Point previousSelection = styledText.getSelection();
				if ((previousSelection.x == 0) && (previousSelection.y == 0)) {
					styledText.setSelection(styledText.getCharCount());
				}
				StyleRange previousRange = findPreviousRange();
				if (previousRange == null) {
					// Next time in start at the end, also used by
					// TRAVERSE_TAB_NEXT to indicate we traversed out
					// in the backward direction
					styledText.setSelection(styledText.getCharCount());
					e.doit = true;
				} else {
					styledText.setSelectionRange(previousRange.start, previousRange.length);
					e.doit = true;
					e.detail = SWT.TRAVERSE_NONE;
				}
				break;
			default:
				break;
			}
		});

		// Listen for Tab and Space to allow keyboard navigation
		styledText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				StyledText text = (StyledText) event.widget;
				if (event.character == ' ' || event.character == SWT.CR) {
					if (item != null) {
						// Be sure we are in the selection
						int offset = text.getSelection().x + 1;

						if (item.isLinkAt(offset)) {
							text.setCursor(busyCursor);
							AboutUtils.openLink(item.getLinkAt(offset));
							StyleRange selectionRange = getCurrentRange();
							text.setSelectionRange(selectionRange.start, selectionRange.length);
							text.setCursor(null);
						}
					}
					return;
				}
			}
		});
	}

	/**
	 * Gets the about item.
	 *
	 * @return the about item
	 */
	public AboutItem getItem() {
		return item;
	}

	/**
	 * Sets the about item.
	 *
	 * @param item about item
	 */
	public void setItem(AboutItem item) {
		this.item = item;
		if (item != null) {
			styledText.setText(item.getText());
			setLinkRanges(item.getLinkRanges());
		}
	}

	/**
	 * Find the range of the current selection.
	 */
	private StyleRange getCurrentRange() {
		StyleRange[] ranges = styledText.getStyleRanges();
		int currentSelectionEnd = styledText.getSelection().y;
		int currentSelectionStart = styledText.getSelection().x;

		for (StyleRange range : ranges) {
			if ((currentSelectionStart >= range.start) && (currentSelectionEnd <= (range.start + range.length))) {
				return range;
			}
		}
		return null;
	}

	/**
	 * Find the next range after the current selection.
	 */
	private StyleRange findNextRange() {
		StyleRange[] ranges = styledText.getStyleRanges();
		int currentSelectionEnd = styledText.getSelection().y;

		for (StyleRange range : ranges) {
			if (range.start >= currentSelectionEnd) {
				return range;
			}
		}
		return null;
	}

	/**
	 * Find the previous range before the current selection.
	 */
	private StyleRange findPreviousRange() {
		StyleRange[] ranges = styledText.getStyleRanges();
		int currentSelectionStart = styledText.getSelection().x;

		for (int i = ranges.length - 1; i > -1; i--) {
			if ((ranges[i].start + ranges[i].length - 1) < currentSelectionStart) {
				return ranges[i];
			}
		}
		return null;
	}

	/**
	 * Sets the styled text's link (blue) ranges
	 */
	private void setLinkRanges(int[][] linkRanges) {
		Color fg = JFaceColors.getHyperlinkText(styledText.getShell().getDisplay());
		for (int[] linkRange : linkRanges) {
			StyleRange r = new StyleRange(linkRange[0], linkRange[1], fg, null);
			styledText.setStyleRange(r);
		}
	}
}
