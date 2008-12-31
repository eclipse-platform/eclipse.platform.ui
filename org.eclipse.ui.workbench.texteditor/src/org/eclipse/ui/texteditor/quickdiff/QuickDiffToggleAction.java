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

package org.eclipse.ui.texteditor.quickdiff;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.internal.texteditor.quickdiff.QuickDiffMessages;
import org.eclipse.ui.internal.texteditor.quickdiff.QuickDiffRestoreAction;
import org.eclipse.ui.internal.texteditor.quickdiff.ReferenceSelectionAction;
import org.eclipse.ui.internal.texteditor.quickdiff.RestoreAction;
import org.eclipse.ui.internal.texteditor.quickdiff.RevertBlockAction;
import org.eclipse.ui.internal.texteditor.quickdiff.RevertLineAction;
import org.eclipse.ui.internal.texteditor.quickdiff.RevertSelectionAction;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension;
import org.eclipse.ui.texteditor.ITextEditorExtension3;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action to toggle the line number bar's quick diff display. When turned on, quick diff shows
 * the changes relative to the saved version of the file.
 *
 * @since 3.0
 */
public class QuickDiffToggleAction implements IEditorActionDelegate, IUpdate {

	/** The editor we are working on. */
	ITextEditor fEditor= null;

	/** Our UI proxy action. */
	IAction fProxy;

	/** The restore actions associated with this toggle action. */
	QuickDiffRestoreAction[] fRestoreActions=
		new QuickDiffRestoreAction[] {
			new RevertSelectionAction(fEditor, true),
			new RevertBlockAction(fEditor, true),
			new RevertLineAction(fEditor, true),
			new RestoreAction(fEditor, true),
		};

	/** The menu listener that adds the ruler context menu. */
	private IMenuListener fListener= new IMenuListener() {
		/** Group name for additions, in CompilationUnitEditor... */
		private static final String GROUP_ADD= "add"; //$NON-NLS-1$
		/** Group name for debug contributions */
		private static final String GROUP_DEBUB= "debug"; //$NON-NLS-1$
		private static final String GROUP_QUICKDIFF= "quickdiff"; //$NON-NLS-1$
		private static final String MENU_ID= "quickdiff.menu"; //$NON-NLS-1$
		private static final String GROUP_RESTORE= "restore"; //$NON-NLS-1$

		public void menuAboutToShow(IMenuManager manager) {
			// update the toggle action itself
			update();

			IMenuManager menu= (IMenuManager)manager.find(MENU_ID);
			// only add menu if it isn't there yet
			if (menu == null) {
				/* HACK: pre-install menu groups
				 * This is needed since we get the blank context menu, but want to show up
				 * in the same position as the extension-added QuickDiffToggleAction.
				 * The extension is added at the end (naturally), but other menus (debug, add)
				 * don't add themselves to MB_ADDITIONS or alike, but rather to the end, too. So
				 * we pre-install their respective menu groups here.
				 */
				if (manager.find(GROUP_DEBUB) == null)
					manager.insertBefore(IWorkbenchActionConstants.MB_ADDITIONS, new Separator(GROUP_DEBUB));
				if (manager.find(GROUP_ADD) == null)
					manager.insertAfter(IWorkbenchActionConstants.MB_ADDITIONS, new Separator(GROUP_ADD));
				if (manager.find(GROUP_RESTORE) == null)
					manager.insertAfter(GROUP_ADD, new Separator(GROUP_RESTORE));
				if (manager.find(GROUP_QUICKDIFF) == null)
					manager.insertAfter(GROUP_RESTORE, new Separator(GROUP_QUICKDIFF));

				// create quickdiff menu
				menu= new MenuManager(QuickDiffMessages.quickdiff_menu_label, MENU_ID);
				List descriptors= new QuickDiff().getReferenceProviderDescriptors();
				for (Iterator it= descriptors.iterator(); it.hasNext();) {
					ReferenceProviderDescriptor desc= (ReferenceProviderDescriptor) it.next();
					ReferenceSelectionAction action= new ReferenceSelectionAction(desc, fEditor);
					if (action.isEnabled())
						menu.add(action);
				}
				manager.appendToGroup(GROUP_QUICKDIFF, menu);

				// create restore menu if this action is enabled
				if (isConnected()) {
					for (int i= 0; i < fRestoreActions.length; i++) {
						fRestoreActions[i].update();
					}
					// only add block action if selection action is not enabled
					if (fRestoreActions[0].isEnabled())
						manager.appendToGroup(GROUP_RESTORE, fRestoreActions[0]);
					else if (fRestoreActions[1].isEnabled())
						manager.appendToGroup(GROUP_RESTORE, fRestoreActions[1]);
					if (fRestoreActions[2].isEnabled())
						manager.appendToGroup(GROUP_RESTORE, fRestoreActions[2]);
					if (fRestoreActions[3].isEnabled())
						manager.appendToGroup(GROUP_RESTORE, fRestoreActions[3]);
				}
			}
		}
	};

	/*
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		fProxy= action;
		removePopupMenu();
		if (targetEditor instanceof ITextEditor) {
			fEditor= (ITextEditor)targetEditor;
		} else
			fEditor= null;
		for (int i= 0; i < fRestoreActions.length; i++) {
			fRestoreActions[i].setEditor(fEditor);
		}
		setPopupMenu();
	}

	/**
	 * Removes the ruler context menu listener from the current editor.
	 */
	private void removePopupMenu() {
		if (!(fEditor instanceof ITextEditorExtension))
			return;
		((ITextEditorExtension)fEditor).removeRulerContextMenuListener(fListener);
	}

	/**
	 * Installs a submenu with <code>fEditor</code>'s ruler context menu that contains the choices
	 * for the quick diff reference. This allows the toggle action to lazily install the menu once
	 * quick diff has been enabled.
	 */
	private void setPopupMenu() {
		if (!(fEditor instanceof ITextEditorExtension))
			return;
		((ITextEditorExtension)fEditor).addRulerContextMenuListener(fListener);
	}

	/**
	 * States whether this toggle action has been installed and a incremental differ has been
	 * installed with the line number bar.
	 *
	 * @return <code>true</code> if a differ has been installed on <code>fEditor</code>.
	 */
	boolean isConnected() {
		if (!(fEditor instanceof ITextEditorExtension3))
			return false;
		return ((ITextEditorExtension3)fEditor).isChangeInformationShowing();
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		fProxy= action;
		if (fEditor == null)
			return;

		if (fEditor instanceof ITextEditorExtension3) {
			ITextEditorExtension3 extension= (ITextEditorExtension3)fEditor;
			extension.showChangeInformation(!extension.isChangeInformationShowing());
		}
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fProxy= action;
	}

	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		if (fProxy == null)
			return;
		if (isConnected())
			fProxy.setText(QuickDiffMessages.quickdiff_toggle_disable);
		else
			fProxy.setText(QuickDiffMessages.quickdiff_toggle_enable);
	}

}
