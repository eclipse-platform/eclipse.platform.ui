/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pierre-Yves B., pyvesdev@gmail.com - Bug 121634: [find/replace] status bar must show the string being searched when "String Not Found"
 *******************************************************************************/

package org.eclipse.ui.texteditor;

import java.util.ResourceBundle;

import org.osgi.framework.FrameworkUtil;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.IDialogSettings;

import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.findandreplace.FindReplaceLogic;
import org.eclipse.ui.internal.findandreplace.HistoryStore;
import org.eclipse.ui.internal.findandreplace.SearchOptions;
import org.eclipse.ui.internal.findandreplace.overlay.FindReplaceOverlay;
import org.eclipse.ui.internal.texteditor.NLSUtility;


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
	@Deprecated
	public FindNextAction(ResourceBundle bundle, String prefix, IWorkbenchWindow workbenchWindow, boolean forward) {
		super(bundle, prefix);
		fWorkbenchWindow= workbenchWindow;
		fForward= forward;
		update();
	}

	private HistoryStore getSearchHistory() {
		return new HistoryStore(getDialogSettings(), HistoryStore.SEARCH_HISTORY_KEY, 15);
	}

	/**
	 * Returns the find string based on the selection or the find history.
	 * @return the find string
	 */
	private String getFindString() {
		String fullSelection= fTarget.getSelectionText();
		String firstLine= getFirstLine(fullSelection);
		if ((firstLine.isEmpty() || fRegExSearch && fullSelection.equals(fSelection)) && !getSearchHistory().isEmpty())
			return getSearchHistory().get(0);
		else if (fRegExSearch && !fullSelection.isEmpty())
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

		String msg= NLSUtility.format(EditorMessages.FindNext_Status_noMatch_label, fFindString);
		manager.setMessage(msg);
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

	@Override
	public void run() {
		fFindString= getFindString();
		if (fFindString == null) {
			statusNotFound();
			return;
		}
		if (fTarget == null) {
			return;
		}
		statusClear();

		FindReplaceLogic findReplaceLogic = createFindReplaceLogic(fTarget);

		findReplaceLogic.setFindString(fFindString);
		findReplaceLogic.performSearch();
		if (!findReplaceLogic.getStatus().wasSuccessful()) {
			statusNotFound();
		}
		writeConfiguration();
	}

	private FindReplaceLogic createFindReplaceLogic(IFindReplaceTarget target) {
		FindReplaceLogic findReplaceLogic = new FindReplaceLogic();
		boolean isTargetEditable = false;
		if (target != null) {
			isTargetEditable = target.isEditable();
		}
		findReplaceLogic.updateTarget(target, isTargetEditable);
		if (fForward) {
			findReplaceLogic.activate(SearchOptions.FORWARD);
		}

		if (shouldUseOverlay()) {
			initializeFindReplaceLogicForOverlay(findReplaceLogic);
		} else {
			initializeFindReplaceLogicForDialog(findReplaceLogic);
		}

		return findReplaceLogic;
	}

	private void initializeFindReplaceLogicForOverlay(FindReplaceLogic findReplaceLogic) {
		findReplaceLogic.activate(SearchOptions.WRAP);
	}

	private void initializeFindReplaceLogicForDialog(FindReplaceLogic findReplaceLogic) {
		IDialogSettings s = getDialogSettings();

		fWrapInit = s.get("wrap") == null || s.getBoolean("wrap"); //$NON-NLS-1$ //$NON-NLS-2$
		fCaseInit = s.getBoolean("casesensitive"); //$NON-NLS-1$
		fWholeWordInit = s.getBoolean("wholeword"); //$NON-NLS-1$
		fRegExSearch = s.getBoolean("isRegEx"); //$NON-NLS-1$

		if (fCaseInit) {
			findReplaceLogic.activate(SearchOptions.CASE_SENSITIVE);
		}
		if (fWrapInit) {
			findReplaceLogic.activate(SearchOptions.WRAP);
		}
		if (fRegExSearch) {
			findReplaceLogic.activate(SearchOptions.REGEX);
		}
		if (fWholeWordInit && findReplaceLogic.isAvailable(SearchOptions.WHOLE_WORD)) {
			findReplaceLogic.activate(SearchOptions.WHOLE_WORD);
		}
	}

	@Override
	public void update() {

		if (fWorkbenchPart == null && fWorkbenchWindow != null)
			fWorkbenchPart= fWorkbenchWindow.getPartService().getActivePart();

		if (fWorkbenchPart != null)
			fTarget= fWorkbenchPart.getAdapter(IFindReplaceTarget.class);
		else
			fTarget= null;

		setEnabled(fTarget != null && fTarget.canPerformFind());
	}

	//--------------- configuration handling --------------

	private static final String INSTANCE_SCOPE_NODE_NAME = "org.eclipse.ui.editors"; //$NON-NLS-1$

	private static final String USE_FIND_REPLACE_OVERLAY = "useFindReplaceOverlay"; //$NON-NLS-1$

	private boolean shouldUseOverlay() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(INSTANCE_SCOPE_NODE_NAME);
		boolean overlayPreference = preferences.getBoolean(USE_FIND_REPLACE_OVERLAY, true);
		return overlayPreference && fWorkbenchPart instanceof StatusTextEditor;
	}

	/**
	 * Returns the dialog settings object used to share state
	 * between several find/replace dialogs.
	 *
	 * @return the dialog settings to be used
	 */
	private IDialogSettings getDialogSettings() {
		if (shouldUseOverlay()) {
			return PlatformUI.getDialogSettingsProvider(FrameworkUtil.getBundle(FindReplaceOverlay.class))
					.getDialogSettings();
		}
		return PlatformUI.getDialogSettingsProvider(FrameworkUtil.getBundle(FindReplaceDialog.class))
				.getDialogSettings();
	}

	/**
	 * Stores its current configuration in the dialog store.
	 */
	private void writeConfiguration() {
		if (fFindString == null)
			return;

		getSearchHistory().addOrPushToTop(fFindString);
	}

	/**
	 * Returns the first line of the given selection.
	 *
	 * @param selection the selection
	 * @return the first line of the selection
	 */
	private String getFirstLine(String selection) {
		if (!selection.isEmpty()) {
			int delimiterOffset = TextUtilities.nextDelimiter(selection, 0).delimiterIndex;
			if (delimiterOffset > 0)
				return selection.substring(0, delimiterOffset);
		}
		return selection;
	}
}
