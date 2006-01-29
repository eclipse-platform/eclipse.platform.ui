/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search.ui.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

import org.eclipse.search.ui.IContextMenuConstants;

import org.eclipse.search2.internal.ui.SearchMessages;
import org.eclipse.search2.internal.ui.text2.FindInFileActionDelegate;
import org.eclipse.search2.internal.ui.text2.FindInProjectActionDelegate;
import org.eclipse.search2.internal.ui.text2.FindInRecentScopeActionDelegate;
import org.eclipse.search2.internal.ui.text2.FindInWorkingSetActionDelegate;
import org.eclipse.search2.internal.ui.text2.FindInWorkspaceActionDelegate;

/**
 * Action group that adds a sub-menu with text search actions to a context menu.
 * to a context menu.
 * 
 * @since 3.2
 */
public class TextSearchGroup extends ActionGroup {
	private String fAppendToGroup= ITextEditorActionConstants.GROUP_FIND;
	private String fMenuText= SearchMessages.TextSearchGroup_submenu_text;

	private FindInRecentScopeActionDelegate[] fActions;

	/**
	 * Constructs a TextSearchGroup for adding actions to the context menu
	 * of the editor provided. The editor will be accessed for the purpose of
	 * determining the search string.
	 */
	public TextSearchGroup(IEditorPart editor) {
		createActions(editor);
	}

	/**
	 * Changes the text that is used for the submenu. The default is 
	 * "Find Text".
	 */
	public void setMenuText(String text) {
		fMenuText= text;
	}

	/**
	 * Changes the group where the submenu is appended to. The default is
	 * ITextEditorActionConstants.GROUP_FIND.
	 */
	public void setAppendToGroup(String groupID) {
		fAppendToGroup= groupID;
	}

	private void createActions(IEditorPart editor) {
		fActions= new FindInRecentScopeActionDelegate[] {new FindInRecentScopeActionDelegate(), new FindInWorkspaceActionDelegate(), new FindInProjectActionDelegate(), new FindInFileActionDelegate(), new FindInWorkingSetActionDelegate()};
		for (int i= 0; i < fActions.length; i++) {
			FindInRecentScopeActionDelegate action= fActions[i];
			action.setActiveEditor(action, editor);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
		MenuManager textSearchMM= new MenuManager(fMenuText, IContextMenuConstants.GROUP_SEARCH);
		textSearchMM.add(fActions[0]);
		textSearchMM.add(new Separator());
		for (int i= 1; i < fActions.length; i++) {
			textSearchMM.add(fActions[i]);
		}

		menu.appendToGroup(fAppendToGroup, textSearchMM);
	}

}
