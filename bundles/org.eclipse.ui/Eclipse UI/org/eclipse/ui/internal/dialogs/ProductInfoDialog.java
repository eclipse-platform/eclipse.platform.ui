package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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
	private 	boolean webBrowserOpened;
	protected    int ABOUT_TEXT_WIDTH = 70; // chars
	protected    int ABOUT_TEXT_HEIGHT = 15; // chars
	private 	Cursor handCursor;
	private 	Cursor busyCursor;
	
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
		public void mouseUp(MouseEvent e) {
			StyledText text = (StyledText)e.widget;
			int offset = text.getCaretOffset();
			if (item != null && item.isLinkAt(offset)) {	
				text.setCursor(busyCursor);
				openLink(item.getLinkAt(offset));
				text.setCursor(null);
			}
		}
	});
	styledText.addMouseMoveListener(new MouseMoveListener() {
		public void mouseMove(MouseEvent e) {
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
			if (e.detail == SWT.TRAVERSE_ESCAPE) {
				e.doit = true;
			} else if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
				StyleRange range = findNextRange(text);
				if (range == null) {
					text.setSelection(0,0);
					e.doit = true;
				} else {
					e.doit = false;
				}
			} else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
				StyleRange range = findPreviousRange(text);
				if (range == null) {
					text.setSelection(0,0);
					e.doit = true;
				} else {
					e.doit = false;
				}
			}
		}
	});
	
	styledText.addFocusListener(new FocusAdapter() {
		public void focusGained(FocusEvent e) {
			StyledText text = (StyledText)e.widget;
			text.setSelection(0,0);
			StyleRange nextRange = findNextRange(text);
			if(nextRange != null) {
				text.setSelection(nextRange.start,nextRange.start + nextRange.length);
			}
		}
	});
	
	//Listen for Tab and Space to allow keyboard navigation
	styledText.addKeyListener(new KeyAdapter() {
		public void keyPressed (KeyEvent event){
			StyledText text = (StyledText)event.widget;

			if(event.character == '\t') {
				StyleRange range = null;
				if (event.stateMask == SWT.SHIFT) {
					range = findPreviousRange(text);
				} else {
					range = findNextRange(text);
				}
				if(range == null){
					text.setSelection(0,0);
				} else{
					text.setSelection(range.start, range.start + range.length);
				}
				return;
			}
			if(event.character == ' ' || event.character == SWT.CR){
				if(item != null){
					//Be sure we are in the selection
					int offset = text.getSelection().x + 1;

					if (item.isLinkAt(offset)) {	
						text.setCursor(busyCursor);
						openLink(item.getLinkAt(offset));
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
		if((ranges[i].start + ranges[i].length) < currentSelectionStart)
			return ranges[i];
	}
	return null;
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
						Runtime.getRuntime().exec("netscape -remote openURL(" + href + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						Process p = Runtime.getRuntime().exec("netscape " + href); //$NON-NLS-1$
						webBrowserOpened = true;
						try {
							if (p != null)
								p.waitFor();
						} catch (InterruptedException e) {
							d.asyncExec(new Runnable() {
								public void run() {
									MessageDialog.openError(getShell(), WorkbenchMessages.getString("ProductInfoDialog.errorTitle"), //$NON-NLS-1$
									WorkbenchMessages.getString("ProductInfoDialog.unableToOpenWebBrowser"));
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
							WorkbenchMessages.getString("ProductInfoDialog.unableToOpenWebBrowser"));
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
