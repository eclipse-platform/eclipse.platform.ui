/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.search.ui.actions;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;

import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import org.eclipse.search2.internal.ui.SearchMessages;
import org.eclipse.search2.internal.ui.text2.FindInFileActionDelegate;
import org.eclipse.search2.internal.ui.text2.FindInProjectActionDelegate;
import org.eclipse.search2.internal.ui.text2.FindInRecentScopeActionDelegate;
import org.eclipse.search2.internal.ui.text2.FindInWorkingSetActionDelegate;
import org.eclipse.search2.internal.ui.text2.FindInWorkspaceActionDelegate;

/**
 * Action group that adds a sub-menu with text search actions to a context menu.
 *
 * @since 3.2
 */
public class TextSearchGroup extends ActionGroup {

	private static final String CTX_MENU_ID= "org.eclipse.search.text.ctxmenu"; //$NON-NLS-1$

	private String fAppendToGroup= ITextEditorActionConstants.GROUP_FIND;
	private String fMenuText= SearchMessages.TextSearchGroup_submenu_text;
	private FindInRecentScopeActionDelegate[] fActions;

	/**
	 * Constructs a TextSearchGroup for adding actions to the context menu
	 * of the editor provided. The editor will be accessed for the purpose of
	 * determining the search string.
	 *
	 * @param editor the editor
	 */
	public TextSearchGroup(IEditorPart editor) {
		createActions(editor);
	}

	/**
	 * Changes the text that is used for the submenu label. The default is
	 * "Search Text".
	 *
	 * @param text the text for the menu label.
	 */
	public void setMenuText(String text) {
		fMenuText= text;
	}

	/**
	 * Changes the group where the submenu is appended to. The default is
	 * ITextEditorActionConstants.GROUP_FIND.
	 *
	 * @param groupID the group id to append to
	 */
	public void setAppendToGroup(String groupID) {
		fAppendToGroup= groupID;
	}

	private void createActions(IEditorPart editor) {
		fActions= new FindInRecentScopeActionDelegate[] {
				new FindInWorkspaceActionDelegate(), new FindInProjectActionDelegate(), new FindInFileActionDelegate(), new FindInWorkingSetActionDelegate()};
		for (int i= 0; i < fActions.length; i++) {
			FindInRecentScopeActionDelegate action= fActions[i];
			action.setActiveEditor(action, editor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
		MenuManager textSearchMM= new MenuManager(fMenuText, CTX_MENU_ID);
		int i=0;
		for (i= 0; i < fActions.length-1; i++) {
			textSearchMM.add(fActions[i]);
		}
		textSearchMM.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		textSearchMM.add(new Separator());
		textSearchMM.add(fActions[i]);

		menu.appendToGroup(fAppendToGroup, textSearchMM);
	}
}
