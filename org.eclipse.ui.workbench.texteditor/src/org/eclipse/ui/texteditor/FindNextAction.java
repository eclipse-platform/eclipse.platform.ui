/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.PatternSyntaxException;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.IDialogSettings;

import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;


/**
 * An action which finds the next/previous occurrence of the last search or the
 * current selection if present.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class FindNextAction extends ResourceAction implements IUpdate {

	/** The action's target */
	private IFindReplaceTarget fTarget;
	/** The part the action is bound to */
	private IWorkbenchPart fWorkbenchPart;
	/** The workbench window */
	private IWorkbenchWindow fWorkbenchWindow;
	/** The dialog settings to retrieve the last search */
	private IDialogSettings fDialogSettings;
	/** The find history as initially given in the dialog settings. */
	private List fFindHistory= new ArrayList();
	/** The find string as initially given in the dialog settings. */
	private String fFindString;
	/** The search direction as initially given in the dialog settings. */
	private boolean fForward;
	/** The wrapping flag as initially given in the dialog settings. */
	private boolean fWrapInit;
	/** The case flag as initially given in the dialog settings. */
	private boolean fCaseInit;
	/** The whole word flag as initially given in the dialog settings. */
	private boolean fWholeWordInit;
	/**
	 * The regExSearch flag as initially given in the dialog settings.
	 *
	 * @since 3.0
	 */
	private boolean fRegExSearch;
	/**
	 * The last selection set by find/replace.
	 *
	 * @since 3.0
	 */
	private String fSelection;

	/**
	 * Creates a new find/replace action for the given workbench part.
	 * The action configures its visual representation from the given
	 * resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param workbenchPart the workbench part
	 * @param forward the search direction
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 */
	public FindNextAction(ResourceBundle bundle, String prefix, IWorkbenchPart workbenchPart, boolean forward) {
		super(bundle, prefix);
		fWorkbenchPart= workbenchPart;
		fForward= forward;
		update();
	}

	/**
	 * Creates a new find/replace action for the given workbench window.
	 * The action configures its visual representation from the given
	 * resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param workbenchWindow the workbench window
	 * @param forward the search direction
	 * @see ResourceAction#ResourceAction(ResourceBundle, String)
	 *
	 * @deprecated use FindReplaceAction(ResourceBundle, String, IWorkbenchPart, boolean) instead
	 */
	public FindNextAction(ResourceBundle bundle, String prefix, IWorkbenchWindow workbenchWindow, boolean forward) {
		super(bundle, prefix);
		fWorkbenchWindow= workbenchWindow;
		fForward= forward;
		update();
	}

	/**
	 * Returns the find string based on the selection or the find history.
	 * @return the find string
	 */
	private String getFindString() {
		String fullSelection= fTarget.getSelectionText();
		String firstLine= getFirstLine(fullSelection);
		if ((firstLine.length() == 0 || fRegExSearch && fullSelection.equals(fSelection)) && !fFindHistory.isEmpty())
			return (String) fFindHistory.get(0);
		else if (fRegExSearch && fullSelection.length() > 0)
			return FindReplaceDocumentAdapter.escapeForRegExPattern(fullSelection);
		else
			return firstLine;
	}

	/**
	 * Returns the status line manager of the active editor.
	 * @return the status line manager of the active editor
	 */
	private IStatusLineManager getStatusLineManager() {
		IEditorPart editor= fWorkbenchPart.getSite().getPage().getActiveEditor();
		if (editor == null)
			return null;

		return editor.getEditorSite().getActionBars().getStatusLineManager();
	}

	/**
	 * Sets the "no matches found" error message to the status line.
	 *
	 * @since 3.0
	 */
	private void statusNotFound() {
		fWorkbenchPart.getSite().getShell().getDisplay().beep();

		IStatusLineManager manager= getStatusLineManager();
		if (manager == null)
			return;

		manager.setMessage(EditorMessages.FindNext_Status_noMatch_label);
	}

	/**
	 * Clears the status line.
	 */
	private void statusClear() {
		IStatusLineManager manager= getStatusLineManager();
		if (manager == null)
			return;

		manager.setErrorMessage(""); //$NON-NLS-1$
		manager.setMessage(""); //$NON-NLS-1$
	}

	/*
	 *	@see IAction#run()
	 */
	public void run() {
		if (fTarget != null) {
			readConfiguration();

			fFindString= getFindString();
			if (fFindString == null) {
				statusNotFound();
				return;
			}

			boolean wholeWord= fWholeWordInit && !fRegExSearch && isWord(fFindString);

			statusClear();
			if (!findNext(fFindString, fForward, fCaseInit, fWrapInit, wholeWord, fRegExSearch))
				statusNotFound();

			writeConfiguration();
		}
	}

	/**
	 * Tests whether each character in the given string is a letter.
	 *
	 * @param str the string to check
	 * @return <code>true</code> if the given string is a word
	 * @since 3.2
	 */
	private boolean isWord(String str) {
		if (str == null || str.length() == 0)
			return false;

		for (int i= 0; i < str.length(); i++) {
			if (!Character.isJavaIdentifierPart(str.charAt(i)))
				return false;
		}
		return true;
	}

	/*
	 * @see IUpdate#update()
	 */
	public void update() {

		if (fWorkbenchPart == null && fWorkbenchWindow != null)
			fWorkbenchPart= fWorkbenchWindow.getPartService().getActivePart();

		if (fWorkbenchPart != null)
			fTarget= (IFindReplaceTarget) fWorkbenchPart.getAdapter(IFindReplaceTarget.class);
		else
			fTarget= null;

		setEnabled(fTarget != null && fTarget.canPerformFind());
	}

	/*
	 * @see FindReplaceDialog#findIndex(String, int, boolean, boolean, boolean, boolean)
	 * @since 3.0
	 */
	private int findIndex(String findString, int startPosition, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord, boolean regExSearch) {

		if (forwardSearch) {
			if (wrapSearch) {
				int index= findAndSelect(startPosition, findString, true, caseSensitive, wholeWord, regExSearch);
				if (index == -1) {
					beep();
					index= findAndSelect(-1, findString, true, caseSensitive, wholeWord, regExSearch);
				}
				return index;
			}
			return findAndSelect(startPosition, findString, true, caseSensitive, wholeWord, regExSearch);
		}

		// backward
		if (wrapSearch) {
			int index= findAndSelect(startPosition - 1, findString, false, caseSensitive, wholeWord, regExSearch);
			if (index == -1) {
				beep();
				index= findAndSelect(-1, findString, false, caseSensitive, wholeWord, regExSearch);
			}
			return index;
		}
		return findAndSelect(startPosition - 1, findString, false, caseSensitive, wholeWord, regExSearch);
	}

	/**
	 * Returns whether the specified  search string can be found using the given options.
	 *
	 * @param findString the string to search for
	 * @param forwardSearch the search direction
	 * @param caseSensitive should the search honor cases
	 * @param wrapSearch	should the search wrap to the start/end if end/start reached
	 * @param wholeWord does the find string represent a complete word
	 * @param regExSearch if <code>true</code> findString represents a regular expression
	 * @return <code>true</code> if the find string can be found using the given options
	 * @since 3.0
	 */
	private boolean findNext(String findString, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord, boolean regExSearch) {

		Point r= fTarget.getSelection();
		int findReplacePosition= r.x;
		if (forwardSearch)
			findReplacePosition += r.y;

		int index= findIndex(findString, findReplacePosition, forwardSearch, caseSensitive, wrapSearch, wholeWord, regExSearch);

		if (index != -1)
			return true;

		return false;
	}

	private void beep() {
		Shell shell= null;
		if (fWorkbenchPart != null)
			shell= fWorkbenchPart.getSite().getShell();
		else if (fWorkbenchWindow != null)
			shell= fWorkbenchWindow.getShell();

		if (shell != null && !shell.isDisposed())
			shell.getDisplay().beep();
	}

	/**
	 * Searches for a string starting at the given offset and using the specified search
	 * directives. If a string has been found it is selected and its start offset is
	 * returned.
	 *
	 * @param offset the offset at which searching starts
	 * @param findString the string which should be found
	 * @param forwardSearch the direction of the search
	 * @param caseSensitive <code>true</code> performs a case sensitive search, <code>false</code> an insensitive search
	 * @param wholeWord if <code>true</code> only occurrences are reported in which the findString stands as a word by itself
	 * @param regExSearch if <code>true</code> findString represents a regular expression
	 * @return the position of the specified string, or -1 if the string has not been found
	 * @since 3.0
	 */
	private int findAndSelect(int offset, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord, boolean regExSearch) {
		if (fTarget instanceof IFindReplaceTargetExtension3) {
			try {
				return ((IFindReplaceTargetExtension3)fTarget).findAndSelect(offset, findString, forwardSearch, caseSensitive, wholeWord, regExSearch);
			} catch (PatternSyntaxException ex) {
				return -1;
			}
		}
		return fTarget.findAndSelect(offset, findString, forwardSearch, caseSensitive, wholeWord);
	}

	//--------------- configuration handling --------------

	/**
	 * Returns the dialog settings object used to share state
	 * between several find/replace dialogs.
	 *
	 * @return the dialog settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		IDialogSettings settings= TextEditorPlugin.getDefault().getDialogSettings();
		fDialogSettings= settings.getSection(FindReplaceDialog.class.getName());
		if (fDialogSettings == null)
			fDialogSettings= settings.addNewSection(FindReplaceDialog.class.getName());
		return fDialogSettings;
	}

	/**
	 * Initializes itself from the dialog settings with the same state
	 * as at the previous invocation.
	 */
	private void readConfiguration() {
		IDialogSettings s= getDialogSettings();

		fWrapInit= s.get("wrap") == null || s.getBoolean("wrap"); //$NON-NLS-1$ //$NON-NLS-2$
		fCaseInit= s.getBoolean("casesensitive"); //$NON-NLS-1$
		fWholeWordInit= s.getBoolean("wholeword"); //$NON-NLS-1$
		fRegExSearch= s.getBoolean("isRegEx"); //$NON-NLS-1$
		fSelection= s.get("selection"); //$NON-NLS-1$

		String[] findHistory= s.getArray("findhistory"); //$NON-NLS-1$
		if (findHistory != null) {
			fFindHistory.clear();
			for (int i= 0; i < findHistory.length; i++)
				fFindHistory.add(findHistory[i]);
		}
	}

	/**
	 * Stores its current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		if (fFindString == null)
			return;

		IDialogSettings s= getDialogSettings();
		s.put("selection", fTarget.getSelectionText()); //$NON-NLS-1$

		if (!fFindHistory.isEmpty() && fFindString.equals(fFindHistory.get(0)))
			return;

		int index= fFindHistory.indexOf(fFindString);
		if (index != -1)
			fFindHistory.remove(index);
		fFindHistory.add(0, fFindString);

		while (fFindHistory.size() > 8)
			fFindHistory.remove(8);
		String[] names= new String[fFindHistory.size()];
		fFindHistory.toArray(names);
		s.put("findhistory", names); //$NON-NLS-1$
	}

	/**
	 * Returns the first line of the given selection.
	 *
	 * @param selection the selection
	 * @return the first line of the selection
	 */
	private String getFirstLine(String selection) {
		if (selection.length() > 0) {
			int[] info= TextUtilities.indexOf(TextUtilities.DELIMITERS, selection, 0);
			if (info[0] > 0)
				return selection.substring(0, info[0]);
			else if (info[0] == -1)
				return selection;
		}
		return selection;
	}
}
