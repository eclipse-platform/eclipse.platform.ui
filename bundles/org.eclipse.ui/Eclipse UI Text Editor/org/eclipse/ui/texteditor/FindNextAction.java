package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/**
 * An action which finds next/previous occurence of the last search or
 * the current selection if present.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class FindNextAction extends ResourceAction implements IUpdate {

	/** The action's target */
	private IFindReplaceTarget fTarget;
	/** The part the action is bound to */
	private IWorkbenchPart fWorkbenchPart;
	/** The workbench window */
	private IWorkbenchWindow fWorkbenchWindow;

	private IDialogSettings fDialogSettings;
	private String fFindString;
	private boolean fForward;
	private boolean fWrapInit;
	private boolean fCaseInit;
	private boolean fWholeWordInit;
	private List fFindHistory= new ArrayList();
	
	/**
	 * Creates a new find/replace action for the given text editor. 
	 * The action configures its visual representation from the given 
	 * resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @param forward the search direction
	 * @see ResourceAction#ResourceAction
	 */
	public FindNextAction(ResourceBundle bundle, String prefix, IWorkbenchPart workbenchPart, boolean forward) {
		super(bundle, prefix);
		fWorkbenchPart= workbenchPart;
		fForward= forward;
		update();
	}
	
	/**
	 * Creates a new find/replace action for the given text editor. 
	 * The action configures its visual representation from the given 
	 * resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @param workbenchWindow the workbench window
	 * @param forward the search direction
	 * @see ResourceAction#ResourceAction
	 * 
	 * @deprecated use FindReplaceAction(ResourceBundle, String, IWorkbenchPart) instead
	 */
	public FindNextAction(ResourceBundle bundle, String prefix, IWorkbenchWindow workbenchWindow, boolean forward) {
		super(bundle, prefix);
		fWorkbenchWindow= workbenchWindow;
		fForward= forward;
		update();
	}

	private String getFindString() {
		String string= getSelectionString();

		if (string == null && !fFindHistory.isEmpty())
			string= (String) fFindHistory.get(0);
			
		return string;
	}

	private IStatusLineManager getStatusLineManager() {

		IEditorPart editor= fWorkbenchPart.getSite().getPage().getActiveEditor();
		if (editor == null)
			return null;
			
		IEditorActionBarContributor contributor= editor.getEditorSite().getActionBarContributor();
		if (contributor instanceof EditorActionBarContributor) {
			return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
		}
		return null;
	}	
	
	private void statusError() {
		fWorkbenchPart.getSite().getShell().getDisplay().beep();

		IStatusLineManager manager= getStatusLineManager();
		if (manager == null)				
			return;
			
		manager.setErrorMessage(EditorMessages.getString("FindNext.Status.noMatch.label")); //$NON-NLS-1$
		manager.setMessage("");
	}

	private void statusClear() {
		IStatusLineManager manager= getStatusLineManager();
		if (manager == null)				
			return;
			
		manager.setErrorMessage(""); //$NON-NLS-1$
		manager.setMessage(""); //$NON-NLS-1$
	}
	
	/*
	 *	@see IAction#run
	 */
	public void run() {
		if (fTarget != null) {
			readConfiguration();

			fFindString= getFindString();
			if (fFindString == null) {
				statusError();
				return;	
			}
			
			if (!findNext(fFindString, fForward, fCaseInit, fWrapInit, fWholeWordInit)) {
				statusError();
			} else {
				statusClear();
			}
				
			writeConfiguration();
		}
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

	// --- copied from FindReplaceDialog

	/**
	 * Returns the position of the specified search string, or -1 if the string can
	 * not be found when searching using the given options.
	 */
	private int findIndex(String findString, int startPosition, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord) {

		if (forwardSearch) {
			if (wrapSearch) {
				int index= fTarget.findAndSelect(startPosition, findString, true, caseSensitive, wholeWord);
				if (index == -1)
					index= fTarget.findAndSelect(-1, findString, true, caseSensitive, wholeWord);
				return index;
			}
			return fTarget.findAndSelect(startPosition, findString, true, caseSensitive, wholeWord);
		}

		// backward
		if (wrapSearch) {
			int index= fTarget.findAndSelect(startPosition - 1, findString, false, caseSensitive, wholeWord);
			if (index == -1) {
				index= fTarget.findAndSelect(-1, findString, false, caseSensitive, wholeWord);
			}
			return index;
		}
		return fTarget.findAndSelect(startPosition - 1, findString, false, caseSensitive, wholeWord);
	}
	
	/**
	 * Returns whether the specified  search string can be found using the given options.
	 */
	private boolean findNext(String findString, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch, boolean wholeWord) {

		Point r= fTarget.getSelection();
		int findReplacePosition= r.x;
		if (forwardSearch)
			findReplacePosition += r.y;

		int index= findIndex(findString, findReplacePosition, forwardSearch, caseSensitive, wrapSearch, wholeWord);

		if (index != -1)
			return true;
		
		return false;
	}
	
	//--------------- configuration handling --------------
	
	/**
	 * Returns the dialog settings object used to share state 
	 * between several find/replace dialogs.
	 * 
	 * @return the dialog settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		AbstractUIPlugin plugin= (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		IDialogSettings settings= plugin.getDialogSettings();
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
			
		fWrapInit= s.getBoolean("wrap"); //$NON-NLS-1$
		fCaseInit= s.getBoolean("casesensitive"); //$NON-NLS-1$
		fWholeWordInit= s.getBoolean("wholeword"); //$NON-NLS-1$
		
		String[] findHistory= s.getArray("findhistory"); //$NON-NLS-1$
		if (findHistory != null) {
			fFindHistory.clear();
			for (int i= 0; i < findHistory.length; i++)
				fFindHistory.add(findHistory[i]);
		}
	}

	/**
	 * Stores it current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		IDialogSettings s= getDialogSettings();

		if (fFindString == null)
			return;
			
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
	 * Returns the actual selection of the find replace target
	 */
	private String getSelectionString() {
		
		/*
		 * 1GF86V3: ITPUI:WINNT - Internal errors using Find/Replace Dialog
		 * Now uses TextUtilities rather than focussing on '\n'  
		 */
		String selection= fTarget.getSelectionText();
		if (selection != null && selection.length() > 0) {
			int[] info= TextUtilities.indexOf(TextUtilities.fgDelimiters, selection, 0);
			if (info[0] > 0)
				return selection.substring(0, info[0]);
			else if (info[0] == -1)
				return selection;
		}
		return null;
	}	
}
