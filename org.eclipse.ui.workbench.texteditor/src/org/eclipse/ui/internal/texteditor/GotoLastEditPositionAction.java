/*
 * Copyright (c) 2000, 2003 IBM Corp. and others..
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 */
package org.eclipse.ui.internal.texteditor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * Goes to last edit position.
 * 
 * @see org.eclipse.ui.texteditor.EditPosition
 * @since 2.1
 */
public class GotoLastEditPositionAction extends Action implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow fWindow;
	private IAction fAction;

	public GotoLastEditPositionAction() {
		WorkbenchHelp.setHelp(this, IAbstractTextEditorHelpContextIds.GOTO_LAST_EDIT_POSITION_ACTION);
		setId(ITextEditorActionDefinitionIds.GOTO_LAST_EDIT_POSITION);
		setActionDefinitionId(ITextEditorActionDefinitionIds.GOTO_LAST_EDIT_POSITION);
		setEnabled(false);
	}

	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	public void run(IAction action) {
		run();
	}

	public void run() {
		EditPosition editPosition= TextEditorPlugin.getDefault().getLastEditPosition();
		if (editPosition == null)
			return;

		final Position pos= editPosition.getPosition();
		if (pos == null || pos.isDeleted)
			return;

		IWorkbenchWindow window= getWindow();
		if (window == null)
			return;
		
		IWorkbenchPage page= window.getActivePage();
			
		IEditorPart editor;
		try {
			editor= page.openEditor(editPosition.getEditorInput(), editPosition.getEditorId());
		} catch (PartInitException ex) {
			editor= null;
		}

		// Optimization - could also use else branch
		if (editor instanceof ITextEditor) {
			ITextEditor textEditor= (ITextEditor)editor;
			textEditor.selectAndReveal(pos.offset, pos.length);

		} else
		 if (editor != null) {
			final IEditorInput input= editor.getEditorInput();
			final IEditorPart finalEditor= editor;
			if (input instanceof IFileEditorInput) {

				WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
					protected void execute(IProgressMonitor monitor) throws CoreException {
						IMarker marker= null;
						try {
							marker= ((IFileEditorInput)input).getFile().createMarker(IMarker.TEXT);
							marker.setAttribute(IMarker.CHAR_START, pos.offset); 
							marker.setAttribute(IMarker.CHAR_END, pos.offset + pos.length);

							finalEditor.gotoMarker(marker);
							
						} finally {
							if (marker != null)
								marker.delete();
						}
					}
				};

				try {
					op.run(null);
				} catch (InvocationTargetException ex) {
					String message= EditorMessages.getString("Editor.error.gotoLastEditPosition.message"); //$NON-NLS-1$
					if (fWindow != null) {
						Shell shell= fWindow.getShell();
						String title= EditorMessages.getString("Editor.error.gotoLastEditPosition.title"); //$NON-NLS-1$
						MessageDialog.openError(shell, title, message);
					} else {
						Throwable t= ex.getTargetException();
						IStatus status= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, message, t);
						TextEditorPlugin.getDefault().getLog().log(status);
					}
				} catch (InterruptedException e) {
					Assert.isTrue(false, "this operation can not be cancelled"); //$NON-NLS-1$
				}
			}
			editor.setFocus();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		boolean enabled= TextEditorPlugin.getDefault().getLastEditPosition() != null;
		setEnabled(enabled);
		action.setEnabled(enabled);

		// This is no longer needed once the action is enabled.
		if (!enabled) {
			 // adding the same action twice has no effect.
			TextEditorPlugin.getDefault().addLastEditPositionDependentAction(action);
			// this is always the same action for this instance
			fAction= action;
		}
	}

	private IWorkbenchWindow getWindow() {
		if (fWindow == null)
			fWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		return fWindow;
	}

	public void dispose() {
		fWindow= null;
		TextEditorPlugin.getDefault().removeLastEditPositionDependentAction(fAction);
	}
}
