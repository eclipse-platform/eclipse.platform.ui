/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.AboutItem;
import org.eclipse.ui.internal.WorkbenchMessages;
 
/**
 * Abstract superclass of about dialogs 
 */ 
 
public abstract class ProductInfoDialog extends Dialog{
	private static final String ATT_HTTP = "http://"; //$NON-NLS-1$
	private AboutItem item;
	private boolean webBrowserOpened;
	private String webBrowser = null;
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
			StyledText text = (StyledText)e.widget;
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
				text.setSelectionRange(selectionRange.start, selectionRange.length);
				text.setCursor(null);
			}
		}
	});
	
	styledText.addMouseMoveListener(new MouseMoveListener() {
		public void mouseMove(MouseEvent e) {
			// Do not change cursor on drag events
			if (mouseDown) {
				if (!dragEvent) {
					StyledText text = (StyledText)e.widget;
					text.setCursor(null);
				}
				dragEvent = true;
				return;
			}
			StyledText text = (StyledText)e.widget;
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
			StyledText text = (StyledText)e.widget;
			switch (e.detail) {
			case SWT.TRAVERSE_ESCAPE:
				e.doit = true;
				break;
			case SWT.TRAVERSE_TAB_NEXT:
				//Previously traverse out in the backward direction?
				Point nextSelection = text.getSelection();
				int charCount = text.getCharCount();
				if ((nextSelection.x == charCount) && (nextSelection.y == charCount)){
					text.setSelection(0);
				}
				StyleRange nextRange  = findNextRange(text);
				if (nextRange == null) {
					// Next time in start at beginning, also used by 
					// TRAVERSE_TAB_PREVIOUS to indicate we traversed out
					// in the forward direction
					text.setSelection(0);
					e.doit = true;
				} else {
					text.setSelectionRange(nextRange.start, nextRange.length);
					e.doit = true;
					e.detail = SWT.TRAVERSE_NONE;
				}
				break;
			case SWT.TRAVERSE_TAB_PREVIOUS:
				//Previously traverse out in the forward direction?
				Point previousSelection = text.getSelection();
				if ((previousSelection.x == 0) && (previousSelection.y == 0))
					text.setSelection(text.getCharCount());
				StyleRange previousRange = findPreviousRange(text);
				if (previousRange == null) {
					// Next time in start at the end, also used by 
					// TRAVERSE_TAB_NEXT to indicate we traversed out
					// in the backward direction
					text.setSelection(text.getCharCount());
					e.doit = true;
				}
				else {
					text.setSelectionRange(previousRange.start, previousRange.length);
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
		public void keyPressed (KeyEvent event){
			StyledText text = (StyledText)event.widget;
			if(event.character == ' ' || event.character == SWT.CR){
				if(item != null){
					//Be sure we are in the selection
					int offset = text.getSelection().x + 1;

					if (item.isLinkAt(offset)) {	
						text.setCursor(busyCursor);
						openLink(item.getLinkAt(offset));
						StyleRange selectionRange = getCurrentRange(text);
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
protected StyleRange getCurrentRange(StyledText text){
	StyleRange[] ranges = text.getStyleRanges();
	int currentSelectionEnd = text.getSelection().y;
	int currentSelectionStart = text.getSelection().x;
	
	for (int i = 0; i < ranges.length; i++) {
		if((currentSelectionStart >= ranges[i].start) && 
			(currentSelectionEnd <= (ranges[i].start + ranges[i].length))) {
			return ranges[i];
		}
	}
	return null;
}

/**
 * Find the next range after the current 
 * selection.
 */
protected StyleRange findNextRange(StyledText text){
	StyleRange[] ranges = text.getStyleRanges();
	int currentSelectionEnd = text.getSelection().y;

	for (int i = 0; i < ranges.length; i++) {
		if(ranges[i].start >= currentSelectionEnd)
			return ranges[i];
	}
	return null;
}

/**
 * Find the previous range before the current selection.
 */
protected StyleRange findPreviousRange(StyledText text){
	StyleRange[] ranges = text.getStyleRanges();
	int currentSelectionStart = text.getSelection().x;

	for (int i = ranges.length - 1; i > -1; i--) {
		if((ranges[i].start + ranges[i].length - 1) < currentSelectionStart)
			return ranges[i];
	}
	return null;
}

//TODO: Move browser support from Help system, remove this method
private Process openWebBrowser(String href) throws IOException{
	Process p = null;
	if (webBrowser == null) {
		try {
			webBrowser = "netscape"; //$NON-NLS-1$
			p = Runtime.getRuntime().exec(webBrowser + "  " + href); //$NON-NLS-1$;
		} catch (IOException e) {
			p = null;
			webBrowser = "mozilla"; //$NON-NLS-1$
		}
	}
	 
	if (p==null) {
		try {
			p = Runtime.getRuntime().exec(webBrowser + " " + href); //$NON-NLS-1$;
		} catch (IOException e) {
			p = null;
			throw e;
		}
	}
	return p;
}

/**
 * Open a link
 */
protected void openLink(final String href) {
	final Display d = Display.getCurrent();
	if (SWT.getPlatform().equals("win32")) { //$NON-NLS-1$
		Program.launch(href);
	} else {
		Thread launcher = new Thread("About Link Launcher") {//$NON-NLS-1$
			public void run() {
				try {
					if (webBrowserOpened) {
						Runtime.getRuntime().exec(webBrowser + " -remote openURL(" + href + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						Process p = openWebBrowser(href);
						webBrowserOpened = true;
						try {
							if (p != null)
								p.waitFor();
						} catch (InterruptedException e) {
							d.asyncExec(new Runnable() {
								public void run() {
									MessageDialog.openError(getShell(), WorkbenchMessages.getString("ProductInfoDialog.errorTitle"), //$NON-NLS-1$
									WorkbenchMessages.getString("ProductInfoDialog.unableToOpenWebBrowser")); //$NON-NLS-1$
								}
							});
						} finally {
							webBrowserOpened = false;
						}
					}
				} catch (IOException e) {
					d.asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(getShell(), WorkbenchMessages.getString("ProductInfoDialog.errorTitle"), //$NON-NLS-1$
							WorkbenchMessages.getString("ProductInfoDialog.unableToOpenWebBrowser")); //$NON-NLS-1$
						}
					});
				}
			}
		};
		launcher.start();
	}
}

/**
 * Sets the styled text's bold ranges
 */
protected void setBoldRanges(StyledText styledText, int[][] boldRanges) {
	for (int i = 0; i < boldRanges.length; i++) {
		StyleRange r = new StyleRange(boldRanges[i][0], boldRanges[i][1], null, null, SWT.BOLD);
		styledText.setStyleRange(r);
	}
}

/**
 * Sets the styled text's link (blue) ranges
 */
protected void setLinkRanges(StyledText styledText, int[][] linkRanges) {
	Color fg = JFaceColors.getHyperlinkText(styledText.getShell().getDisplay());
	for (int i = 0; i < linkRanges.length; i++) {
		StyleRange r = new StyleRange(linkRanges[i][0], linkRanges[i][1], fg, null);
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
		if (i!=max) i--;
		linkRanges.add(new int[] {start, i - start});
		links.add(s.substring(start, i));
		i = s.indexOf(ATT_HTTP, i);
	}
	return new AboutItem(
			s,
			(int[][])linkRanges.toArray(new int[linkRanges.size()][2]),
			(String[])links.toArray(new String[links.size()]));
}

}
