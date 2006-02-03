/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.team.internal.ui.history.CompareFileRevisionEditorInput;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
import org.eclipse.ui.*;

public class CompareRevisionAction extends TeamAction {

	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					IStructuredSelection structSel = getSelection();
					if (structSel.size() != 2)
						return;

					Object[] objArray = structSel.toArray();

					IFileRevision file1 = (IFileRevision) objArray[0];
					IFileRevision file2 = (IFileRevision) objArray[1];

					FileRevisionTypedElement left = new FileRevisionTypedElement(file1);
					FileRevisionTypedElement right = new FileRevisionTypedElement(file2);
					
				   CompareEditorInput input = new CompareFileRevisionEditorInput(left, right);
				   IWorkbenchPage page = getTargetPage();
				   IEditorPart editor = findReusableCompareEditor(getTargetPage());
				     if(editor != null) {
				     	IEditorInput otherInput = editor.getEditorInput();
				     	if(otherInput.equals(input)) {
				     		// simply provide focus to editor
				     		page.activate(editor);
				     	} else {
				     		// if editor is currently not open on that input either re-use existing
				     		CompareUI.reuseCompareEditor(input, (IReusableEditor)editor);
				     		page.activate(editor);
				     	}
				     } else {
				     	CompareUI.openCompareEditor(input);
				     }
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			}
		}, TeamUIMessages.ConfigureProjectAction_configureProject, PROGRESS_BUSYCURSOR);
	}
	
	protected boolean isEnabled() throws TeamException {
		int sizeofSelection = getSelection().size();

		if (sizeofSelection == 2)
			return true;

		return false;
	}
	
	/**
	 * Returns an editor that can be re-used. An open compare editor that
	 * has un-saved changes cannot be re-used.
	 */
	public static IEditorPart findReusableCompareEditor(IWorkbenchPage page) {
		IEditorReference[] editorRefs = page.getEditorReferences();	
		for (int i = 0; i < editorRefs.length; i++) {
			IEditorPart part = editorRefs[i].getEditor(false);
			if(part != null 
					&& (part.getEditorInput() instanceof CompareFileRevisionEditorInput) 
					&& part instanceof IReusableEditor) {
				if(! part.isDirty()) {	
					return part;	
				}
			}
		}
		return null;
	}

}
