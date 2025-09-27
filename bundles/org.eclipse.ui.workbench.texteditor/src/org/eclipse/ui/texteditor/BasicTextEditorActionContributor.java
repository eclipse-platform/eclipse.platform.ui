/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Tom Eicher (Avaloq Evolution AG) - block selection mode
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.EditorActionBarContributor;


/**
 * Manages the installation and removal of global actions for the same type of
 * editors.
 * <p>
 * If instantiated and used as-is, this contributor connects to all of the
 * workbench defined global editor actions the corresponding actions of the
 * current editor. It also adds addition actions for searching and navigation
 * (go to line) as well as a set of status fields.
 * </p>
 * <p>
 * Subclasses may override the following methods:
 * </p>
 * <ul>
 * <li><code>contributeToMenu</code> - extend to contribute to menu</li>
 * <li><code>contributeToToolBar</code> - reimplement to contribute to tool
 * bar</li>
 * <li><code>contributeToStatusLine</code> - reimplement to contribute to status
 * line</li>
 * <li><code>setActiveEditor</code> - extend to react to editor changes</li>
 * </ul>
 *
 * @see org.eclipse.ui.texteditor.ITextEditorActionConstants
 */
public class BasicTextEditorActionContributor extends EditorActionBarContributor {


	/** The global actions to be connected with editor actions */
	private final static String[] ACTIONS= {
		ITextEditorActionConstants.UNDO,
		ITextEditorActionConstants.REDO,
		ITextEditorActionConstants.CUT,
		ITextEditorActionConstants.COPY,
		ITextEditorActionConstants.PASTE,
		ITextEditorActionConstants.DELETE,
		ITextEditorActionConstants.SELECT_ALL,
		ITextEditorActionConstants.FIND,
		ITextEditorActionConstants.PRINT,
		ITextEditorActionConstants.PROPERTIES,
		ITextEditorActionConstants.REVERT
	};

	/**
	 * Status field definition.
	 * @since 3.0
	 */
	private static class StatusFieldDef {

		private final String category;
		private final String actionId;
		private final boolean visible;
		private final int widthInChars;

		private StatusFieldDef(String category, String actionId, boolean visible, int widthInChars) {
			Assert.isNotNull(category);
			this.category= category;
			this.actionId= actionId;
			this.visible= visible;
			this.widthInChars= widthInChars;
		}
	}

	/**
	 * The status fields to be set to the editor
	 * @since 3.0
	 */
	private final static StatusFieldDef[] STATUS_FIELD_DEFS= {
		new StatusFieldDef(ITextEditorActionConstants.STATUS_CATEGORY_FIND_FIELD, null, false, EditorMessages.Editor_FindIncremental_reverse_name.length() + 15),
		new StatusFieldDef(ITextEditorActionConstants.STATUS_CATEGORY_ELEMENT_STATE, null, true, StatusLineContributionItem.DEFAULT_WIDTH_IN_CHARS + 1),
		new StatusFieldDef(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_MODE, ITextEditorActionDefinitionIds.TOGGLE_OVERWRITE, true, StatusLineContributionItem.DEFAULT_WIDTH_IN_CHARS),
		new StatusFieldDef(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION, ITextEditorActionConstants.GOTO_LINE, true, StatusLineContributionItem.DEFAULT_WIDTH_IN_CHARS)
	};

	/**
	 * The active editor part.
	 */
	private IEditorPart fActiveEditorPart;
	/**
	 * The find next action.
	 * @since 2.0
	 */
	private final RetargetTextEditorAction fFindNext;
	/**
	 * The find previous action.
	 * @since 2.0
	 */
	private final RetargetTextEditorAction fFindPrevious;
	/**
	 * The incremental find action.
	 * @since 2.0
	 */
	private final RetargetTextEditorAction fIncrementalFind;
	/**
	 * The reverse incremental find action.
	 * @since 2.1
	 */
	private final RetargetTextEditorAction fIncrementalFindReverse;
	/**
	 * The go to line action.
	 */
	private final RetargetTextEditorAction fGotoLine;
	/**
	 * The word completion action.
	 * @since 3.1
	 */
	private final RetargetTextEditorAction fHippieCompletion;
	/**
	 * The map of status fields.
	 * @since 2.0
	 */
	private final Map<StatusFieldDef, StatusLineContributionItem> fStatusFields;


	/**
	 * Creates an empty editor action bar contributor. The action bars are
	 * furnished later via the <code>init</code> method.
	 *
	 * @see org.eclipse.ui.IEditorActionBarContributor#init(org.eclipse.ui.IActionBars, org.eclipse.ui.IWorkbenchPage)
	 */
	public BasicTextEditorActionContributor() {

		fFindNext= new RetargetTextEditorAction(EditorMessages.getBundleForConstructedKeys(), "Editor.FindNext."); //$NON-NLS-1$
		fFindNext.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_NEXT);
		fFindPrevious= new RetargetTextEditorAction(EditorMessages.getBundleForConstructedKeys(), "Editor.FindPrevious."); //$NON-NLS-1$
		fFindPrevious.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_PREVIOUS);
		fIncrementalFind= new RetargetTextEditorAction(EditorMessages.getBundleForConstructedKeys(), "Editor.FindIncremental."); //$NON-NLS-1$
		fIncrementalFind.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_INCREMENTAL);
		fIncrementalFindReverse= new RetargetTextEditorAction(EditorMessages.getBundleForConstructedKeys(), "Editor.FindIncrementalReverse."); //$NON-NLS-1$
		fIncrementalFindReverse.setActionDefinitionId(IWorkbenchActionDefinitionIds.FIND_INCREMENTAL_REVERSE);
		fGotoLine= new RetargetTextEditorAction(EditorMessages.getBundleForConstructedKeys(), "Editor.GotoLine."); //$NON-NLS-1$
		fGotoLine.setActionDefinitionId(ITextEditorActionDefinitionIds.LINE_GOTO);
		fHippieCompletion= new RetargetTextEditorAction(EditorMessages.getBundleForConstructedKeys(), "Editor.HippieCompletion."); //$NON-NLS-1$
		fHippieCompletion.setActionDefinitionId(ITextEditorActionDefinitionIds.HIPPIE_COMPLETION);

		fStatusFields= new HashMap<>(3);
		for (StatusFieldDef fieldDef : STATUS_FIELD_DEFS) {
			fStatusFields.put(fieldDef, new StatusLineContributionItem(fieldDef.category, fieldDef.visible, fieldDef.widthInChars));
		}
	}

	/**
	 * Returns the active editor part.
	 *
	 * @return the active editor part
	 */
	protected final IEditorPart getActiveEditorPart() {
		return fActiveEditorPart;
	}

	/**
	 * Returns the action registered with the given text editor.
	 *
	 * @param editor the editor, or <code>null</code>
	 * @param actionId the action id
	 * @return the action, or <code>null</code> if none
	 */
	protected final IAction getAction(ITextEditor editor, String actionId) {
		return (editor == null || actionId == null ? null : editor.getAction(actionId));
	}

	/**
	 * The method installs the global action handlers for the given text editor.
	 * <p>
	 * This method cannot be overridden by subclasses.</p>
	 *
	 * @param part the active editor part
	 * @since 2.0
	 */
	private void doSetActiveEditor(IEditorPart part) {

		if (fActiveEditorPart == part) {
			return;
		}

		if (fActiveEditorPart instanceof ITextEditorExtension extension) {
			for (StatusFieldDef element : STATUS_FIELD_DEFS) {
				extension.setStatusField(null, element.category);
			}
		}

		fActiveEditorPart= part;
		ITextEditor editor= (part instanceof ITextEditor i) ? i : null;

		IActionBars actionBars= getActionBars();
		for (String element : ACTIONS) {
			actionBars.setGlobalActionHandler(element, getAction(editor, element));
		}
		actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.SHOW_WHITESPACE_CHARACTERS, getAction(editor, ITextEditorActionConstants.SHOW_WHITESPACE_CHARACTERS));
		actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.BLOCK_SELECTION_MODE, getAction(editor, ITextEditorActionConstants.BLOCK_SELECTION_MODE));
		if (editor instanceof AbstractTextEditor && ((AbstractTextEditor)editor).isWordWrapSupported()) {
			actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.WORD_WRAP, getAction(editor, ITextEditorActionConstants.WORD_WRAP));
		}

		fFindNext.setAction(getAction(editor, ITextEditorActionConstants.FIND_NEXT));
		fFindPrevious.setAction(getAction(editor, ITextEditorActionConstants.FIND_PREVIOUS));
		fIncrementalFind.setAction(getAction(editor, ITextEditorActionConstants.FIND_INCREMENTAL));
		fIncrementalFindReverse.setAction(getAction(editor, ITextEditorActionConstants.FIND_INCREMENTAL_REVERSE));
		fGotoLine.setAction(getAction(editor, ITextEditorActionConstants.GOTO_LINE));
		fHippieCompletion.setAction(getAction(editor, ITextEditorActionConstants.HIPPIE_COMPLETION));

		for (StatusFieldDef element : STATUS_FIELD_DEFS) {
			if (fActiveEditorPart instanceof ITextEditorExtension extension) {
				StatusLineContributionItem statusField= fStatusFields.get(element);
				statusField.setActionHandler(getAction(editor, element.actionId));
				extension.setStatusField(statusField, element.category);
			}
		}
	}

	/**
	 * The <code>BasicTextEditorActionContributor</code> implementation of this
	 * <code>IEditorActionBarContributor</code> method installs the global
	 * action handler for the given text editor by calling a private helper
	 * method.
	 * <p>
	 * Subclasses may extend.</p>
	 *
	 * @param part {@inheritDoc}
	 */
	@Override
	public void setActiveEditor(IEditorPart part) {
		doSetActiveEditor(part);
	}

	@Override
	public void contributeToMenu(IMenuManager menu) {

		IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			editMenu.prependToGroup(IWorkbenchActionConstants.FIND_EXT, fIncrementalFindReverse);
			editMenu.prependToGroup(IWorkbenchActionConstants.FIND_EXT, fIncrementalFind);
			editMenu.prependToGroup(IWorkbenchActionConstants.FIND_EXT, fFindPrevious);
			editMenu.prependToGroup(IWorkbenchActionConstants.FIND_EXT, fFindNext);

			addOrInsert(editMenu, new Separator(ITextEditorActionConstants.GROUP_OPEN));
			addOrInsert(editMenu, new Separator(ITextEditorActionConstants.GROUP_INFORMATION));
			addOrInsert(editMenu, new Separator(ITextEditorActionConstants.GROUP_ASSIST));
			addOrInsert(editMenu, new Separator(ITextEditorActionConstants.GROUP_GENERATE));
			addOrInsert(editMenu, new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

			editMenu.appendToGroup(ITextEditorActionConstants.GROUP_ASSIST, fHippieCompletion);
		}

		IMenuManager navigateMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
		if (navigateMenu != null) {
			navigateMenu.appendToGroup(IWorkbenchActionConstants.MB_ADDITIONS, fGotoLine);
		}
	}

	/**
	 * The <code>item</code> is {@link IContributionManager#add(IContributionItem) added} to
	 * <code>menu</code> if no item with the same id currently exists. If there already is an
	 * contribution item with the same id, the new item gets
	 * {@link IContributionManager#insertAfter(String, IContributionItem) inserted after} it.
	 *
	 * @param menu the contribution manager
	 * @param item the contribution item
	 * @since 3.2
	 */
	private void addOrInsert(IContributionManager menu, IContributionItem item) {
		String id= item.getId();
		if (menu.find(id) == null) {
			menu.add(item);
		} else {
			menu.insertAfter(id, item);
		}
	}

	@Override
	public void contributeToStatusLine(IStatusLineManager statusLineManager) {
		super.contributeToStatusLine(statusLineManager);
		for (StatusFieldDef element : STATUS_FIELD_DEFS) {
			statusLineManager.add(fStatusFields.get(element));
		}
	}

	@Override
	public void dispose() {
		doSetActiveEditor(null);
		super.dispose();
	}
}
