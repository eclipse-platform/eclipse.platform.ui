/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.jface.action.Action;
/**
 * Text actions (cut, copy, paste) for the Web browser.
 */
public class TextAction extends Action {
	protected BrowserViewer browser;
	protected byte type;

	public static final byte CUT = 0;
	public static final byte COPY = 1;
	public static final byte PASTE = 2;
	
	/**
	 * TextAction constructor comment.
	 */
	protected TextAction(BrowserViewer browser, byte type) {
		super(type + "!"); //$NON-NLS-1$
		this.browser = browser;
		this.type = type;
	}
	
	/**
	 * Copies the selected text to the clipboard.  The text will be put in the 
	 * clipboard in plain text format.
	 * <p>
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void copy() {
		Point selection = browser.combo.getSelection();
	
		int length = selection.y - selection.x;
		if (length > 0) {
			TextTransfer plainTextTransfer = TextTransfer.getInstance();
			try {
				browser.clipboard.setContents(
					new String[] { browser.combo.getText().substring(selection.x, selection.y) }, 
					new Transfer[] { plainTextTransfer });
			} catch (SWTError error) {
				// Copy to clipboard failed. This happens when another application 
				// is accessing the clipboard while we copy. Ignore the error.
				// Fixes 1GDQAVN
			}
		}
	}
	
	/**
	 * Moves the selected text to the clipboard.  The text will be put in the 
	 * clipboard in plain text format and RTF format.
	 * <p>
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void cut(){
		Point selection = browser.combo.getSelection();
	
		if (selection.y > selection.x) {
			copy();
			delete();
		}
	}
	
	/**
	 * Deletes the character to the right of the caret. Delete the selected text if any.
	 */
	public void delete() {
		Point selection = browser.combo.getSelection();
		String text = browser.combo.getText();
	
		if (selection.x != selection.y) {
			text = text.substring(0, selection.x) + text.substring(selection.y);
			browser.combo.setText(text);
			browser.combo.setSelection(new Point(selection.x, selection.x));
		}
	}
	
	/** 
	 * Replaces the selection with the clipboard text or insert the text at 
	 * the current caret offset if there is no selection. 
	 * If the widget has the SWT.SINGLE style and the clipboard text contains
	 * more than one line, only the first line without line delimiters is 
	 * inserted in the widget.
	 * <p>
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void paste() {
		TextTransfer transfer = TextTransfer.getInstance();
		Point selection = browser.combo.getSelection();
		String text = browser.combo.getText();
		
		String newText = (String) browser.clipboard.getContents(transfer);
		if (newText != null && newText.length() > 0) {
			text = text.substring(0, selection.x) + newText + text.substring(selection.y);
			browser.combo.setText(text);
	
			// set the selection to the end of the paste
			int x = selection.x + newText.length();
			browser.combo.setSelection(new Point(x, x));
		}
	}
	
	/**
	 * Implementation of method defined on <code>IAction</code>.
	 */
	public void run() {
		if (browser == null || browser.combo == null)
			return;
		if (type == CUT)
			cut();
		else if (type == COPY)
			copy();
		else if (type == PASTE)
			paste();
	}
}