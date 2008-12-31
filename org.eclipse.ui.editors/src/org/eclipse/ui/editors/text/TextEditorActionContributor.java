/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.editors.text;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.ide.IDEActionFactory;

import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;


/**
 * Manages the installation and de-installation of global actions for the default text editor.
 * <p>
 * If instantiated and used as-is, this contributor connects the following global actions:
 * <ul>
 * 		<li>Add Bookmark</li>
 * 		<li>Add Task</li>
 * 		<li>Change Encoding</li>
 * 		<li>Quick Assist</li>
 * </ul>
 *
 * @since 2.0
 */
public class TextEditorActionContributor extends BasicTextEditorActionContributor {

	/**
	 * Change encoding action.
	 * @since 3.1
	 */
	private RetargetTextEditorAction fChangeEncodingAction;
	/**
	 * Quick assist assistant action.
	 * @since 3.3
	 */
	private RetargetTextEditorAction fQuickAssistAction;
	/**
	 * Quick assist menu contribution item.
	 * @since 3.3
	 */
	private IContributionItem fQuickAssistMenuEntry;

	private RetargetTextEditorAction fRetargetShowInformationAction;

	/**
	 * Creates a new contributor.
	 */
	public TextEditorActionContributor() {
		fChangeEncodingAction= new RetargetTextEditorAction(TextEditorMessages.getBundleForConstructedKeys(), "Editor.ChangeEncodingAction."); //$NON-NLS-1$
		fQuickAssistAction= new RetargetTextEditorAction(TextEditorMessages.getBundleForConstructedKeys(), "Editor.QuickAssist."); //$NON-NLS-1$
		fQuickAssistAction.setActionDefinitionId(ITextEditorActionDefinitionIds.QUICK_ASSIST);
		fQuickAssistMenuEntry= new ActionContributionItem(fQuickAssistAction);

		fRetargetShowInformationAction= new RetargetTextEditorAction(TextEditorMessages.getBundleForConstructedKeys(), "Editor.ShowInformation."); //$NON-NLS-1$
		fRetargetShowInformationAction.setActionDefinitionId(ITextEditorActionDefinitionIds.SHOW_INFORMATION);
	}

	/**
	 * Internally sets the active editor to the actions provided by this contributor.
	 * Cannot be overridden by subclasses.
	 *
	 * @param part the editor
	 */
	private void doSetActiveEditor(final IEditorPart part) {

		ITextEditor textEditor= null;
		if (part instanceof ITextEditor)
			textEditor= (ITextEditor) part;

		/** The global actions to be connected with editor actions */
		IActionBars actionBars= getActionBars();

		actionBars.setGlobalActionHandler(IDEActionFactory.ADD_TASK.getId(), getAction(textEditor, IDEActionFactory.ADD_TASK.getId()));
		actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(), getAction(textEditor, IDEActionFactory.BOOKMARK.getId()));

		IAction action= getAction(textEditor, ITextEditorActionConstants.NEXT);
		actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_NEXT_ANNOTATION, action);
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.NEXT, action);
		action= getAction(textEditor, ITextEditorActionConstants.PREVIOUS);
		actionBars.setGlobalActionHandler(ITextEditorActionDefinitionIds.GOTO_PREVIOUS_ANNOTATION, action);
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.PREVIOUS, action);

		action= getAction(textEditor, ITextEditorActionConstants.REFRESH);
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.REFRESH, action);

		fChangeEncodingAction.setAction(getAction(textEditor, ITextEditorActionConstants.CHANGE_ENCODING));

		IAction quickAssistAction= getAction(textEditor, ITextEditorActionConstants.QUICK_ASSIST);
		fQuickAssistAction.setAction(quickAssistAction);

		if (textEditor == null)
			return;

		// Update Quick Assist menu entry - for now don't show disabled entry
		IMenuManager menuMgr= textEditor.getEditorSite().getActionBars().getMenuManager();
		IMenuManager editMenu= menuMgr.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			boolean isEnabled= quickAssistAction != null && quickAssistAction.isEnabled();
			fQuickAssistMenuEntry.setVisible(isEnabled);
			editMenu.update(true);
		}

		fRetargetShowInformationAction.setAction(getAction(textEditor, ITextEditorActionConstants.SHOW_INFORMATION));
	}

	/*
	 * @see org.eclipse.ui.texteditor.BasicTextEditorActionContributor#contributeToMenu(org.eclipse.jface.action.IMenuManager)
	 * @since 3.3
	 */
	public void contributeToMenu(IMenuManager menu) {
		super.contributeToMenu(menu);

		IMenuManager editMenu= menu.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			editMenu.appendToGroup(ITextEditorActionConstants.GROUP_ASSIST, fQuickAssistMenuEntry);
			fQuickAssistMenuEntry.setVisible(false);
			editMenu.appendToGroup(ITextEditorActionConstants.GROUP_INFORMATION, fRetargetShowInformationAction);
		}
	}

	/*
	 * @see IEditorActionBarContributor#setActiveEditor(org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		doSetActiveEditor(part);
	}

	/*
	 * @see IEditorActionBarContributor#dispose()
	 */
	public void dispose() {
		doSetActiveEditor(null);
		super.dispose();
	}

	/*
	 * @see EditorActionBarContributor#init(org.eclipse.ui.IActionBars)
	 */
	public void init(IActionBars bars) {
		super.init(bars);

		IMenuManager menuManager= bars.getMenuManager();
		IMenuManager editMenu= menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null)
			editMenu.add(fChangeEncodingAction);
	}}
