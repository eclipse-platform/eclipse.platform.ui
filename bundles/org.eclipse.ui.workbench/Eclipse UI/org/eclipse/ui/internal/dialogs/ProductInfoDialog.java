/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.about.AboutItem;

/**
 * Abstract superclass of about dialogs 
 */

public abstract class ProductInfoDialog extends Dialog {
    private static final String ATT_HTTP = "http://"; //$NON-NLS-1$

    private AboutItem item;

    private Cursor handCursor;

    private Cursor busyCursor;

    private boolean mouseDown = false;

    private boolean dragEvent = false;

    /**
     * Create an instance of this Dialog
     */
    public ProductInfoDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Adds listeners to the given styled text
     */
    protected void addListeners(StyledText styledText) {
        styledText.addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if (e.button != 1) {
                    return;
                }
                mouseDown = true;
            }

            public void mouseUp(MouseEvent e) {
                mouseDown = false;
                StyledText text = (StyledText) e.widget;
                int offset = text.getCaretOffset();
                if (dragEvent) {
                    // don't activate a link during a drag/mouse up operation
                    dragEvent = false;
                    if (item != null && item.isLinkAt(offset)) {
                        text.setCursor(handCursor);
                    }
                } else if (item != null && item.isLinkAt(offset)) {
                    text.setCursor(busyCursor);
                    openLink(item.getLinkAt(offset));
                    StyleRange selectionRange = getCurrentRange(text);
                    text.setSelectionRange(selectionRange.start,
                            selectionRange.length);
                    text.setCursor(null);
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
                if (offset == -1)
                    text.setCursor(null);
                else if (item != null && item.isLinkAt(offset))
                    text.setCursor(handCursor);
                else
                    text.setCursor(null);
            }
        });

        styledText.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                StyledText text = (StyledText) e.widget;
                switch (e.detail) {
                case SWT.TRAVERSE_ESCAPE:
                    e.doit = true;
                    break;
                case SWT.TRAVERSE_TAB_NEXT:
                    //Previously traverse out in the backward direction?
                    Point nextSelection = text.getSelection();
                    int charCount = text.getCharCount();
                    if ((nextSelection.x == charCount)
                            && (nextSelection.y == charCount)) {
                        text.setSelection(0);
                    }
                    StyleRange nextRange = findNextRange(text);
                    if (nextRange == null) {
                        // Next time in start at beginning, also used by 
                        // TRAVERSE_TAB_PREVIOUS to indicate we traversed out
                        // in the forward direction
                        text.setSelection(0);
                        e.doit = true;
                    } else {
                        text.setSelectionRange(nextRange.start,
                                nextRange.length);
                        e.doit = true;
                        e.detail = SWT.TRAVERSE_NONE;
                    }
                    break;
                case SWT.TRAVERSE_TAB_PREVIOUS:
                    //Previously traverse out in the forward direction?
                    Point previousSelection = text.getSelection();
                    if ((previousSelection.x == 0)
                            && (previousSelection.y == 0))
                        text.setSelection(text.getCharCount());
                    StyleRange previousRange = findPreviousRange(text);
                    if (previousRange == null) {
                        // Next time in start at the end, also used by 
                        // TRAVERSE_TAB_NEXT to indicate we traversed out
                        // in the backward direction
                        text.setSelection(text.getCharCount());
                        e.doit = true;
                    } else {
                        text.setSelectionRange(previousRange.start,
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
                            openLink(item.getLinkAt(offset));
                            StyleRange selectionRange = getCurrentRange(text);
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
     * Gets the busy cursor.
     * @return the busy cursor
     */
    protected Cursor getBusyCursor() {
        return busyCursor;
    }

    /**
     * Sets the busy cursor.
     * @param busyCursor the busy cursor
     */
    protected void setBusyCursor(Cursor busyCursor) {
        this.busyCursor = busyCursor;
    }

    /**
     * Gets the hand cursor.
     * @return Returns a hand cursor
     */
    protected Cursor getHandCursor() {
        return handCursor;
    }

    /**
     * Sets the hand cursor.
     * @param handCursor The hand cursor to set
     */
    protected void setHandCursor(Cursor handCursor) {
        this.handCursor = handCursor;
    }

    /**
     * Gets the about item.
     * @return the about item
     */
    protected AboutItem getItem() {
        return item;
    }

    /**
     * Sets the about item.
     * @param item about item
     */
    protected void setItem(AboutItem item) {
        this.item = item;
    }

    /**
     * Find the range of the current selection.
     */
    protected StyleRange getCurrentRange(StyledText text) {
        StyleRange[] ranges = text.getStyleRanges();
        int currentSelectionEnd = text.getSelection().y;
        int currentSelectionStart = text.getSelection().x;

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
    protected StyleRange findNextRange(StyledText text) {
        StyleRange[] ranges = text.getStyleRanges();
        int currentSelectionEnd = text.getSelection().y;

        for (int i = 0; i < ranges.length; i++) {
            if (ranges[i].start >= currentSelectionEnd)
                return ranges[i];
        }
        return null;
    }

    /**
     * Find the previous range before the current selection.
     */
    protected StyleRange findPreviousRange(StyledText text) {
        StyleRange[] ranges = text.getStyleRanges();
        int currentSelectionStart = text.getSelection().x;

        for (int i = ranges.length - 1; i > -1; i--) {
            if ((ranges[i].start + ranges[i].length - 1) < currentSelectionStart)
                return ranges[i];
        }
        return null;
    }

    /**
     * display an error message
     */
    private void openWebBrowserError(final String href, final Throwable t) {
        getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
				String title = WorkbenchMessages.ProductInfoDialog_errorTitle;
				String msg = NLS.bind(WorkbenchMessages.ProductInfoDialog_unableToOpenWebBrowser, href);
				IStatus status = WorkbenchPlugin.getStatus(t);
                ErrorDialog.openError(getShell(), title, msg, status);
            }
        });
    }

    /**
     * Open a link
     */
    protected void openLink(String href) {
        // format the href for an html file (file:///<filename.html>
        // required for Mac only.
        if (href.startsWith("file:")) { //$NON-NLS-1$
            href = href.substring(5);
            while (href.startsWith("/")) { //$NON-NLS-1$
                href = href.substring(1);
            }
            href = "file:///" + href; //$NON-NLS-1$
        }
		IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
		try {
			IWebBrowser browser = support.getExternalBrowser();
			browser.openURL(new URL(urlEncodeForSpaces(href.toCharArray())));
		}
		catch (MalformedURLException e) {
			openWebBrowserError(href, e);
		}
		catch (PartInitException e) {
			openWebBrowserError(href, e);
		}
    }

    /**
     * This method encodes the url, removes the spaces from the url and replaces
     * the same with <code>"%20"</code>. This method is required to fix Bug
     * 77840.
     * 
     * @since 3.0.2
     */
    private String urlEncodeForSpaces(char[] input) {
       StringBuffer retu = new StringBuffer(input.length);
       for (int i = 0; i < input.length; i++) {
           if (input[i] == ' ')
               retu.append("%20"); //$NON-NLS-1$
           else
               retu.append(input[i]);
       }
       return retu.toString();
    }

    /**
     * Open a browser with the argument title on the argument url.  If the url refers to a
     * resource within a bundle, then a temp copy of the file will be extracted and opened.
     * @see <code>Platform.asLocalUrl</code>
     * @param url The target url to be displayed, null will be safely ignored
     * @return true if the url was successfully displayed and false otherwise
     */
    protected boolean openBrowser(URL url) {
        if (url != null)
            try {
                url = Platform.asLocalURL(url);
            } catch (IOException e) {
                return false;
            }
        if (url == null)
            return false;
        openLink(url.toString());
        return true;
    }

    /**
     * Sets the styled text's bold ranges
     */
    protected void setBoldRanges(StyledText styledText, int[][] boldRanges) {
        for (int i = 0; i < boldRanges.length; i++) {
            StyleRange r = new StyleRange(boldRanges[i][0], boldRanges[i][1],
                    null, null, SWT.BOLD);
            styledText.setStyleRange(r);
        }
    }

    /**
     * Sets the styled text's link (blue) ranges
     */
    protected void setLinkRanges(StyledText styledText, int[][] linkRanges) {
        Color fg = JFaceColors.getHyperlinkText(styledText.getShell()
                .getDisplay());
        for (int i = 0; i < linkRanges.length; i++) {
            StyleRange r = new StyleRange(linkRanges[i][0], linkRanges[i][1],
                    fg, null);
            styledText.setStyleRange(r);
        }
    }

    /**
     * Scan the contents of the about text
     */
    protected AboutItem scan(String s) {
        int max = s.length();
        int i = s.indexOf(ATT_HTTP);
        ArrayList linkRanges = new ArrayList();
        ArrayList links = new ArrayList();
        while (i != -1) {
            int start = i;
            // look for the first whitespace character
            boolean found = false;
            i += ATT_HTTP.length();
            while (!found && i < max) {
                found = Character.isWhitespace(s.charAt(i++));
            }
            if (i != max)
                i--;
            linkRanges.add(new int[] { start, i - start });
            links.add(s.substring(start, i));
            i = s.indexOf(ATT_HTTP, i);
        }
        return new AboutItem(s, (int[][]) linkRanges.toArray(new int[linkRanges
                .size()][2]), (String[]) links
                .toArray(new String[links.size()]));
    }

}
