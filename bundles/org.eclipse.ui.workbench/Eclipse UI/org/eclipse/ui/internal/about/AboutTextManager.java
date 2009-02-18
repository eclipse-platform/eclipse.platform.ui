/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;

/**
 * Manages links in styled text.
 */

public class AboutTextManager {

    /**
     * Scan the contents of the about text
     * @param s 
     * @return 
     */
    public static AboutItem scan(String s) {
        ArrayList linkRanges = new ArrayList();
        ArrayList links = new ArrayList();
        
        // slightly modified version of jface url detection
        // see org.eclipse.jface.text.hyperlink.URLHyperlinkDetector
        
		int urlSeparatorOffset= s.indexOf("://"); //$NON-NLS-1$
		while(urlSeparatorOffset >= 0) {
	
			boolean startDoubleQuote= false;
	
			// URL protocol (left to "://")
			int urlOffset= urlSeparatorOffset;
			char ch;
			do {
				urlOffset--;
				ch= ' ';
				if (urlOffset > -1)
					ch= s.charAt(urlOffset);
				startDoubleQuote= ch == '"';
			} while (Character.isUnicodeIdentifierStart(ch));
			urlOffset++;
			
	
			// Right to "://"
			StringTokenizer tokenizer= new StringTokenizer(s.substring(urlSeparatorOffset + 3), " \t\n\r\f<>", false); //$NON-NLS-1$
			if (!tokenizer.hasMoreTokens())
				return null;
	
			int urlLength= tokenizer.nextToken().length() + 3 + urlSeparatorOffset - urlOffset;
	
			if (startDoubleQuote) {
				int endOffset= -1;
				int nextDoubleQuote= s.indexOf('"', urlOffset);
				int nextWhitespace= s.indexOf(' ', urlOffset);
				if (nextDoubleQuote != -1 && nextWhitespace != -1)
					endOffset= Math.min(nextDoubleQuote, nextWhitespace);
				else if (nextDoubleQuote != -1)
					endOffset= nextDoubleQuote;
				else if (nextWhitespace != -1)
					endOffset= nextWhitespace;
				if (endOffset != -1)
					urlLength= endOffset - urlOffset;
			}
			
			linkRanges.add(new int[] { urlOffset, urlLength });
			links.add(s.substring(urlOffset, urlOffset+urlLength));
			
			urlSeparatorOffset= s.indexOf("://", urlOffset+urlLength+1); //$NON-NLS-1$
		}
        return new AboutItem(s, (int[][]) linkRanges.toArray(new int[linkRanges
                .size()][2]), (String[]) links
                .toArray(new String[links.size()]));
    }
	private StyledText styledText;
	
    private Cursor handCursor;

    private Cursor busyCursor;

    private boolean mouseDown = false;

    private boolean dragEvent = false;
    
    private AboutItem item;
    
    public AboutTextManager(StyledText text) {
    	this.styledText = text;
    	createCursors();
    	addListeners();
    }
    
    private void createCursors() {
        handCursor = new Cursor(styledText.getDisplay(), SWT.CURSOR_HAND);
        busyCursor = new Cursor(styledText.getDisplay(), SWT.CURSOR_WAIT);
        styledText.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handCursor.dispose();
                handCursor = null;
                busyCursor.dispose();
                busyCursor = null;
            }
        });
    }

	
    /**
     * Adds listeners to the given styled text
     */
    protected void addListeners() {
        styledText.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if (e.button != 1) {
                    return;
                }
                mouseDown = true;
            }

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
                    AboutUtils.openLink(styledText.getShell(), item.getLinkAt(offset));
                    StyleRange selectionRange = getCurrentRange();
                    styledText.setSelectionRange(selectionRange.start,
                            selectionRange.length);
                    styledText.setCursor(null);
                }
            }
        });

        styledText.addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
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
                    offset = text.getOffsetAtLocation(new Point(e.x, e.y));
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
            }
        });

        styledText.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                switch (e.detail) {
                case SWT.TRAVERSE_ESCAPE:
                    e.doit = true;
                    break;
                case SWT.TRAVERSE_TAB_NEXT:
                    //Previously traverse out in the backward direction?
                    Point nextSelection = styledText.getSelection();
                    int charCount = styledText.getCharCount();
                    if ((nextSelection.x == charCount)
                            && (nextSelection.y == charCount)) {
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
                    	styledText.setSelectionRange(nextRange.start,
                                nextRange.length);
                        e.doit = true;
                        e.detail = SWT.TRAVERSE_NONE;
                    }
                    break;
                case SWT.TRAVERSE_TAB_PREVIOUS:
                    //Previously traverse out in the forward direction?
                    Point previousSelection = styledText.getSelection();
                    if ((previousSelection.x == 0)
                            && (previousSelection.y == 0)) {
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
                    	styledText.setSelectionRange(previousRange.start,
                                previousRange.length);
                        e.doit = true;
                        e.detail = SWT.TRAVERSE_NONE;
                    }
                    break;
                default:
                    break;
                }
            }
        });

        //Listen for Tab and Space to allow keyboard navigation
        styledText.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                StyledText text = (StyledText) event.widget;
                if (event.character == ' ' || event.character == SWT.CR) {
                    if (item != null) {
                        //Be sure we are in the selection
                        int offset = text.getSelection().x + 1;

                        if (item.isLinkAt(offset)) {
                            text.setCursor(busyCursor);
                            AboutUtils.openLink(styledText.getShell(), item.getLinkAt(offset));
                            StyleRange selectionRange = getCurrentRange();
                            text.setSelectionRange(selectionRange.start,
                                    selectionRange.length);
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
     * @return the about item
     */
    public AboutItem getItem() {
        return item;
    }

    /**
     * Sets the about item.
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

        for (int i = 0; i < ranges.length; i++) {
            if ((currentSelectionStart >= ranges[i].start)
                    && (currentSelectionEnd <= (ranges[i].start + ranges[i].length))) {
                return ranges[i];
            }
        }
        return null;
    }

    /**
     * Find the next range after the current 
     * selection.
     */
    private StyleRange findNextRange() {
        StyleRange[] ranges = styledText.getStyleRanges();
        int currentSelectionEnd = styledText.getSelection().y;

        for (int i = 0; i < ranges.length; i++) {
            if (ranges[i].start >= currentSelectionEnd) {
				return ranges[i];
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
        Color fg = JFaceColors.getHyperlinkText(styledText.getShell()
                .getDisplay());
        for (int i = 0; i < linkRanges.length; i++) {
            StyleRange r = new StyleRange(linkRanges[i][0], linkRanges[i][1],
                    fg, null);
            styledText.setStyleRange(r);
        }
    }
}
