/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ralf Heydenreich - Bug 559694
 *******************************************************************************/
package org.eclipse.e4.ui.internal.dialogs.about;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.program.Program;

/**
 * Manages links in styled text.
 */
public class AboutText {

	private String aboutProperty;
	private StyledText styledText;

	private Cursor handCursor;
	private Cursor busyCursor;

	private boolean mouseDown = false;
	private boolean dragEvent = false;

	private AboutItem item;

	public AboutText(final Supplier<AboutItem> item) {
		this.setItem(item.get());
	}

	public AboutText(StyledText text, final Supplier<AboutItem> item) {
		this.styledText = text;
		this.setItem(item.get());
		createCursors();
		addListeners();
	}

	public AboutText(String aboutProperty) {
		this.aboutProperty = aboutProperty;
	}

	/**
	 * Scan the contents of the about text
	 *
	 * @return
	 */
	private void createAboutItem() {
		if (aboutProperty == null || aboutProperty.isEmpty()) {
			return;
		}

		HyperlinkExtractor hyperlinkExtractor = new HyperlinkExtractor(aboutProperty);
		setItem(new AboutItem(aboutProperty, hyperlinkExtractor.getLinkRanges(), hyperlinkExtractor.getLinks()));
	}

	private void createCursors() {
		handCursor = new Cursor(styledText.getDisplay(), SWT.CURSOR_HAND);
		busyCursor = new Cursor(styledText.getDisplay(), SWT.CURSOR_WAIT);
		styledText.addDisposeListener((DisposeEvent e) -> {
			handCursor.dispose();
			handCursor = null;
			busyCursor.dispose();
			busyCursor = null;
		});
	}

	/**
	 * Adds listeners to the given styled text
	 */
	protected void addListeners() {
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
					item.getLinkAt(offset).ifPresent(l -> Program.launch(l));
					StyleRange selectionRange = getCurrentRange();
					styledText.setSelectionRange(selectionRange.start, selectionRange.length);
					styledText.setCursor(null);
				}
			}
		});

		styledText.addMouseMoveListener((MouseEvent e) -> {
			// Do not change cursor on drag events
			if (mouseDown) {
				if (!dragEvent) {
					StyledText text = (StyledText) e.widget;
					text.setCursor(null);
				}
				dragEvent = true;
				return;
			}
			StyledText text = (StyledText) e.widget;
			int offset = -1;
			try {
				offset = text.getOffsetAtPoint(new Point(e.x, e.y));
			} catch (IllegalArgumentException ex) {
				// leave value as -1
			}
			if (offset == -1) {
				text.setCursor(null);
			} else if (item != null && item.isLinkAt(offset)) {
				text.setCursor(handCursor);
			} else {
				text.setCursor(null);
			}
		});

		styledText.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
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
					} else {
						styledText.setSelectionRange(nextRange.start, nextRange.length);
						e.detail = SWT.TRAVERSE_NONE;
					}
					e.doit = true;
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
					} else {
						styledText.setSelectionRange(previousRange.start, previousRange.length);
						e.detail = SWT.TRAVERSE_NONE;
					}
					e.doit = true;
					break;
				default:
					break;
				}
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
							item.getLinkAt(offset).ifPresent(l -> Program.launch(l));
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
	public Optional<AboutItem> getAboutItem() {
		if (item == null) {
			createAboutItem();
		}
		return Optional.ofNullable(item);
	}

	/**
	 * Sets the about item.
	 *
	 * @param item about item
	 */
	private void setItem(AboutItem item) {
		this.item = item;
		if (item != null && styledText != null) {
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

		return Arrays.stream(ranges).filter(
				range -> (currentSelectionStart >= range.start && currentSelectionEnd <= range.start + range.length))
				.findFirst().orElse(null);
	}

	/**
	 * Find the next range after the current selection.
	 */
	private StyleRange findNextRange() {
		StyleRange[] ranges = styledText.getStyleRanges();
		int currentSelectionEnd = styledText.getSelection().y;

		return Arrays.stream(ranges).filter(range -> (range.start >= currentSelectionEnd)).findFirst().orElse(null);
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
	private void setLinkRanges(final List<HyperlinkRange> linkRanges) {
		Color fg = JFaceColors.getHyperlinkText(styledText.getShell().getDisplay());
		if (fg == null) {
			// TODO use CSS styling!
			// fall back (update JFaceRegistry)
			JFaceResources.getColorRegistry().put(JFacePreferences.HYPERLINK_COLOR, new RGB(0, 0, 128));
			fg = JFaceColors.getHyperlinkText(styledText.getShell().getDisplay());
		}
		for (HyperlinkRange linkRange : linkRanges) {
			StyleRange range = new StyleRange(linkRange.offset(), linkRange.length(), fg, null);
			styledText.setStyleRange(range);
		}
	}
}
