/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.ui.texteditor;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

/**
 * An incremental find target. Replace is always disabled.
 */
class IncrementalFindTarget implements IFindReplaceTarget, IFindReplaceTargetExtension, VerifyKeyListener, MouseListener, FocusListener, ITextListener {

	/** The text viewer to operate on */
	private final ITextViewer fTextViewer;
	/** The status line manager for output */
	private final IStatusLineManager fStatusLine;
	/** The find replace target to delegate find requests */
	private final IFindReplaceTarget fTarget;

	/** The current find string */
	private StringBuffer fFindString= new StringBuffer();
	/** The position to start the find from */
	private int fBasePosition;
	/** The position of the first upper case character, -1 if none */
	private int fCasePosition;
	/** The position of the last successful find */
	private int fCurrentIndex;	
	/** A flag indicating if last find was successful */
	private boolean fFound;	
	/** A flag indicating listeners are installed. */
	private boolean fInstalled;

	/**
	 * Creates an instance of an incremental find target.
	 * 
	 * @param viewer the text viewer to operate on
	 * @param manager the status line manager for output
	 */
	public IncrementalFindTarget(ITextViewer viewer, IStatusLineManager manager) {
		fTextViewer= viewer;
		fStatusLine= manager;
		fTarget= viewer.getFindReplaceTarget();
	}

	/*
	 * @see IFindReplaceTarget#canPerformFind()
	 */
	public boolean canPerformFind() {
		return fTarget.canPerformFind();
	}

	/*
	 * @see IFindReplaceTarget#findAndSelect(int, String, boolean, boolean, boolean)
	 */
	public int findAndSelect(int offset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord) {
		return fTarget.findAndSelect(offset, findString, searchForward, caseSensitive, wholeWord);
	}

	/*
	 * @see IFindReplaceTarget#getSelection()
	 */
	public Point getSelection() {
		return fTarget.getSelection();
	}

	/*
	 * @see IFindReplaceTarget#getSelectionText()
	 */
	public String getSelectionText() {
		return fTarget.getSelectionText();
	}

	/*
	 * @see IFindReplaceTarget#isEditable()
	 */
	public boolean isEditable() {
		return false;
	}

	/*
	 * @see IFindReplaceTarget#replaceSelection(String)
	 */
	public void replaceSelection(String text) {
	}

	/*
	 * @see IFindReplaceTargetExtension#beginSession()
	 */
	public void beginSession() {
		fFindString.setLength(0);
		fCasePosition= -1;		
		fBasePosition= fTarget.getSelection().x;
		fCurrentIndex= fBasePosition;
		fFound= true;
		
		install();
		performFindNext(true, fBasePosition);
		
		if (fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).beginSession();			
	}

	/*
	 * @see IFindReplaceTargetExtension#endSession()
	 */
	public void endSession() {
		if (fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).endSession();

		// will uninstall itself
	}

	/*
	 * @see IFindReplaceTargetExtension#getScope()
	 */
	public IRegion getScope() {
		return null;
	}

	/*
	 * @see IFindReplaceTargetExtension#setGlobal(boolean)
	 */
	public void setGlobal(boolean global) {
	}

	/*
	 * @see IFindReplaceTargetExtension#setScope(IRegion)
	 */
	public void setScope(IRegion scope) {
	}

	
	private void install() {
		if (fInstalled)
			return;

		fTextViewer.addTextListener(this);
					
		StyledText text= fTextViewer.getTextWidget();
		text.addVerifyKeyListener(this);
		text.addMouseListener(this);
		
		fInstalled= true;
	}
	
	private void uninstall() {
		fTextViewer.removeTextListener(this);

		StyledText text= fTextViewer.getTextWidget();
		text.removeVerifyKeyListener(this);
		text.removeMouseListener(this);			

		fInstalled= false;
	}
	
	private void performFindNext(boolean forward, int position) {
		String string= fFindString.toString();

		int index;
		if (string.length() == 0) {

			// workaround for empty selection in target
			fTextViewer.setSelectedRange(fBasePosition + fTextViewer.getVisibleRegion().getOffset(), 0);
			index= fBasePosition;

		} else {

			if (!forward)
				position--;

			index= findIndex(string, position, forward, fCasePosition != -1, true);
		}

		if (index == -1) {
			if (fFound) {
				// beep once
				fFound= false;
				fTextViewer.getTextWidget().getDisplay().beep();
			}
			
			statusError("Incremental Find: " + string + " not found");

		} else {
			fFound= true;
			fCurrentIndex= index;
			statusMessage("Incremental Find: " + string);
		}
	}

	private int findIndex(String findString, int startPosition, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch) {

		if (forwardSearch) {
			if (wrapSearch) {
				int index= fTarget.findAndSelect(startPosition, findString, true, caseSensitive, false);
				if (index == -1)
					index= fTarget.findAndSelect(-1, findString, true, caseSensitive, false);
				return index;
			}
			return fTarget.findAndSelect(startPosition, findString, true, caseSensitive, false);
		}

		// backward
		if (wrapSearch) {
			int index= fTarget.findAndSelect(startPosition - 1, findString, false, caseSensitive, false);
			if (index == -1) {
				index= fTarget.findAndSelect(-1, findString, false, caseSensitive, false);
			}
			return index;
		}
		return fTarget.findAndSelect(startPosition - 1, findString, false, caseSensitive, false);
	}

	/*
	 * @see VerifyKeyListener#verifyKey(VerifyEvent)
	 */
	public void verifyKey(VerifyEvent event) {
		if (!event.doit)
			return;

		if (event.character == 0) {
	
			switch (event.keyCode) {
			
			// ALT, CTRL == leave	
			case SWT.ALT:
			case SWT.CTRL:
				leave();
				break;
			
			// find next
			case SWT.ARROW_DOWN:
				performFindNext(true, fTarget.getSelection().x + fTarget.getSelection().y);
				event.doit= false;
				break;
	
			// find previous
			case SWT.ARROW_UP:
				performFindNext(false, fTarget.getSelection().x);

				if (fCurrentIndex != -1 && fCurrentIndex < fBasePosition)
					fBasePosition= fCurrentIndex;				

				event.doit= false;
				break;			
			}
	
		// event.character != 0
		} else {
			
			switch (event.character) {
			
			// ESC, CR = quit
			case 0x1B:
			case 0x0D:
				leave();
				event.doit= false;
				break;
				
			// backspace	
			case 0x08:
				{
					int length= fFindString.length();
					if (length == 0) {
						fTextViewer.getTextWidget().getDisplay().beep();
						event.doit= false;
						break;

					} else {
						fFindString.replace(length - 1, length, "");
					}
	
					if (fCasePosition == fFindString.length())
						fCasePosition= -1;
	
					performFindNext(true, fBasePosition);
	
					event.doit= false;
				}
				break;		
			
			default:
				if (event.stateMask == 0 || event.stateMask == SWT.SHIFT) {

					char character= event.character;
					if (fCasePosition == -1 && Character.isUpperCase(character) && Character.toLowerCase(character) != character)
						fCasePosition= fFindString.length();
					
					fFindString.append(character);
					performFindNext(true, fBasePosition);
					event.doit= false;
				}
				break;
			}		
		}
	}

	private void leave() {
		statusClear();
		uninstall();				
	}

	/*
	 * @see ITextListener#textChanged(TextEvent)
	 */
	public void textChanged(TextEvent event) {
		leave();
	}

	/*
	 * @see MouseListener#mouseDoubleClick(MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) {
		leave();		
	}

	/*
	 * @see MouseListener#mouseDown(MouseEvent)
	 */
	public void mouseDown(MouseEvent e) {
		leave();
	}

	/*
	 * @see MouseListener#mouseUp(MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {
		leave();
	}

	/*
	 * @see FocusListener#focusGained(FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
		leave();
	}

	/*
	 * @see FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent e) {
		leave();
	}

	private void statusMessage(String string) {
		fStatusLine.setErrorMessage("");
		fStatusLine.setMessage(string);
	}

	private void statusError(String string) {
		fStatusLine.setErrorMessage(string);
		fStatusLine.setMessage("");
	}

	private void statusClear() {
		fStatusLine.setErrorMessage(""); //$NON-NLS-1$
		fStatusLine.setMessage(""); //$NON-NLS-1$
	}
	
	/*
	 * @see IFindReplaceTargetExtension#getLineSelection()
	 */
	public Point getLineSelection() {
		if (fTarget instanceof IFindReplaceTargetExtension)
			return ((IFindReplaceTargetExtension) fTarget).getLineSelection();
		
		return null; // XXX should not return null
	}

	/*
	 * @see IFindReplaceTargetExtension#setSelection(int, int)
	 */
	public void setSelection(int offset, int length) {
		if (fTarget instanceof IFindReplaceTargetExtension)
			((IFindReplaceTargetExtension) fTarget).setSelection(offset, length);
	}

	/*
	 * @see IFindReplaceTargetExtension#setScopeHighlightColor(Color)
	 */
	public void setScopeHighlightColor(Color color) {
	}

}
