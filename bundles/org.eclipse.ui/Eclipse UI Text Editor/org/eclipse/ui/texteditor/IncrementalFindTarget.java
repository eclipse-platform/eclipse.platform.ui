/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

package org.eclipse.ui.texteditor;

import java.text.MessageFormat;
import java.util.Stack;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
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

	/** The string representing rendered tab */
	private final static String TAB= EditorMessages.getString("Editor.FindIncremental.render.tab"); //$NON-NLS-1$

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
	/** The current find stack */
	private Stack fSessionStack;
	/** A constant representing a find-next operation */
	private static final Object NEXT = new Object();
	/** A constant representing a find-previous operation */
	private static final Object PREVIOUS = new Object();
	/** A constant representing adding a character to the find pattern */
	private static final Object CHAR = new Object();
	/** A constant representing a wrap operation */
	private static final Object WRAPPED = new Object();

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
		fSessionStack = new Stack();
		fCasePosition= -1;		
		fBasePosition= fTarget.getSelection().x;
		fCurrentIndex= fBasePosition;
		fFound= true;
		
		install();
		performFindNext(true, fBasePosition, false, false);
		updateStatus(fFound);
		
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
	
	/*
	 * @see IFindReplaceTargetExtension#setReplaceAllMode(boolean)
	 */
	public void setReplaceAllMode(boolean replaceAll) {
	}
	
	private void install() {
		if (fInstalled)
			return;

		StyledText text= fTextViewer.getTextWidget();
		if (text == null)
			return;
		
		text.addMouseListener(this);
		fTextViewer.addTextListener(this);
					
		if (fTextViewer instanceof ITextViewerExtension) {
			ITextViewerExtension e= (ITextViewerExtension) fTextViewer;
			e.prependVerifyKeyListener(this);
		} else {
			text.addVerifyKeyListener(this);
		}
		
		fInstalled= true;
	}
	
	private void uninstall() {
		fTextViewer.removeTextListener(this);

		StyledText text= fTextViewer.getTextWidget();
		text.removeMouseListener(this);
		
		if (fTextViewer instanceof ITextViewerExtension) {
			ITextViewerExtension e= (ITextViewerExtension) fTextViewer;
			e.removeVerifyKeyListener(this);
		} else {
			text.removeVerifyKeyListener(this);
		}
		
		fInstalled= false;
	}

	private boolean performFindNext(boolean forward, int position, boolean wrapSearch, boolean takeBack) {
		String string= fFindString.toString();

		int index;
		if (string.length() == 0) {

			// workaround for empty selection in target
			fTextViewer.setSelectedRange(fBasePosition + fTextViewer.getVisibleRegion().getOffset(), 0);
			index= fBasePosition;

		} else {

			if (!forward)
				position--;

			index= findIndex(string, position, forward, fCasePosition != -1, wrapSearch, takeBack);
		}

		boolean found = (index != -1);
		if (found) fCurrentIndex = index;

		return found;
	}

	private void updateStatus(boolean found) {
		if (fSessionStack == null) return;

		String string= fFindString.toString();
		String prefix = ""; //$NON-NLS-1$
		if (fSessionStack.search(WRAPPED) != -1) {
			prefix = EditorMessages.getString("Editor.FindIncremental.wrapped"); //$NON-NLS-1$
		}
		if (!found) {
			String pattern= EditorMessages.getString("Editor.FindIncremental.not_found.pattern"); //$NON-NLS-1$
			statusError(MessageFormat.format(pattern, new Object[] { prefix, string }));

		} else {
			String pattern= EditorMessages.getString("Editor.FindIncremental.found.pattern"); //$NON-NLS-1$
			statusMessage(MessageFormat.format(pattern, new Object[] { prefix, string }));
		}
	}

	private int findIndex(String findString, int startPosition, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean takeBack) {

		if (fTarget == null)
			return -1;	

		int startIndex = (forwardSearch) ? startPosition : startPosition - 1;
		int index= fTarget.findAndSelect(startIndex, findString, forwardSearch, caseSensitive, false);

		if (index != -1) {
			fFound = true;
			return index;
		}

		if (fFound) {
			// beep once
			StyledText text= fTextViewer.getTextWidget();
			if (!takeBack && text != null && !text.isDisposed())
				text.getDisplay().beep();
		}

		if (!wrapSearch || (fFound && !takeBack)) {
			fFound = false;
			return index;
		}				

		index = fTarget.findAndSelect(-1, findString, forwardSearch, caseSensitive, false);
		fFound = (index != -1);
		if (!takeBack && fFound) wrap();
		return index;
	}

	private void wrap() {
		fSessionStack.push(WRAPPED);
	}

	/*
	 * @see VerifyKeyListener#verifyKey(VerifyEvent)
	 */
	public void verifyKey(VerifyEvent event) {
		if (!event.doit)
			return;

		boolean found = fFound;
		if (event.character == 0) {
	
			switch (event.keyCode) {
			
			// ALT, CTRL, ARROW_LEFT, ARROW_RIGHT == leave	
			case SWT.ALT:
			case SWT.CTRL:
			case SWT.ARROW_LEFT:
			case SWT.ARROW_RIGHT:
			case SWT.HOME:
			case SWT.END:
			case SWT.PAGE_DOWN:
			case SWT.PAGE_UP:
				leave();
				break;
			
			// find next
			case SWT.ARROW_DOWN:
				if (performFindNext(true, fTarget.getSelection().x + fTarget.getSelection().y, true, false))
					fSessionStack.push(NEXT);
				event.doit= false;
				break;
	
			// find previous
			case SWT.ARROW_UP:
				if (performFindNext(false, fTarget.getSelection().x, true, false))
					fSessionStack.push(PREVIOUS);

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
				
			// backspace	and delete
			case 0x08:
			case 0x7F:
				{
					if (fSessionStack.empty()) {
						StyledText text= fTextViewer.getTextWidget();
						if (text != null && !text.isDisposed())
							text.getDisplay().beep();
						event.doit= false;
						break;
					}
					
					Object last = popSessionStack();
					while (!fSessionStack.empty() && fSessionStack.peek() == WRAPPED)
						popSessionStack();

					// Last event repeated a search						
					if (last == PREVIOUS) {
						performFindNext(true, fTarget.getSelection().x + fTarget.getSelection().y, true, true);
					} else if (last == NEXT) {
						performFindNext(false, fTarget.getSelection().x, true, true);

						if (fCurrentIndex != -1 && fCurrentIndex < fBasePosition)
							fBasePosition= fCurrentIndex;				
					} else if (last == CHAR) {
						// Last event added a character
						performFindNext(true, fBasePosition, true, true);
					}
					event.doit= false;
				}
				break;		
			
			default:
				if (event.stateMask == 0 || event.stateMask == SWT.SHIFT) {

					pushChar(event.character);
					performFindNext(true, fBasePosition, false, false);
					event.doit= false;
				}
				break;
			}		
		}
		updateStatus(fFound);
	}

	private void pushChar(char character) {
		if (fCasePosition == -1 && Character.isUpperCase(character) && Character.toLowerCase(character) != character)
			fCasePosition= fFindString.length();
		fFindString.append(character);
		fSessionStack.push(CHAR);
	}

	private Object popSessionStack() {
		Object o = fSessionStack.pop();
		if (o == CHAR) {
			int newLength = fFindString.length() -1;
			fFindString.deleteCharAt(newLength);
			if (fCasePosition == newLength)
				fCasePosition= -1;
		}
		return o;
	}

	private void leave() {
		statusClear();
		uninstall();				
		fSessionStack = null;
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
		fStatusLine.setErrorMessage(""); //$NON-NLS-1$
		fStatusLine.setMessage(escapeTabs(string));
	}

	private void statusError(String string) {
		fStatusLine.setErrorMessage(escapeTabs(string));
		fStatusLine.setMessage(""); //$NON-NLS-1$
	}

	private void statusClear() {
		fStatusLine.setErrorMessage(""); //$NON-NLS-1$
		fStatusLine.setMessage(""); //$NON-NLS-1$
	}
	
	private String escapeTabs(String string) {
		StringBuffer buffer= new StringBuffer();

		int begin= 0;
		int end= string.indexOf('\t', begin);
		
		while (end >= 0) {
			buffer.append(string.substring(begin, end));
			buffer.append(TAB);
			begin= end + 1;
			end= string.indexOf('\t', begin);
		}
		buffer.append(string.substring(begin));
		
		return buffer.toString();
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
