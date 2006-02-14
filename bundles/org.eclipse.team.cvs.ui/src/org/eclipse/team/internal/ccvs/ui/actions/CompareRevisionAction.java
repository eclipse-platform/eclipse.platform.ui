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

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ccvs.ui.CVSHistoryPage;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ui.history.CompareFileRevisionEditorInput;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public class CompareRevisionAction extends BaseSelectionListenerAction {
	
	public CompareRevisionAction(String text) {
		super(text);
	}

	CVSHistoryPage page;
	IStructuredSelection selection;
	
	public void run() {
		try {
			IStructuredSelection structSel = selection;
			Object[] objArray = structSel.toArray();

			IFileRevision file1 = null;
			IFileRevision file2 = null;
			
			switch (structSel.size()){
				case 1:
					file1 = page.getCurrentFileRevision();
					file2 = (IFileRevision) objArray[0];
				break;
				
				case 2:
					file1 = (IFileRevision) objArray[0];
					file2 = (IFileRevision) objArray[1];
				break;
			}

			FileRevisionTypedElement left = new FileRevisionTypedElement(file1);
			FileRevisionTypedElement right = new FileRevisionTypedElement(file2);
			
		   CompareEditorInput input = new CompareFileRevisionEditorInput(left, right);
		   IWorkbenchPage workBenchPage = page.getSite().getPage();
		   IEditorPart editor = findReusableCompareEditor(workBenchPage);
		     if(editor != null) {
		     	IEditorInput otherInput = editor.getEditorInput();
		     	if(otherInput.equals(input)) {
		     		// simply provide focus to editor
		     		workBenchPage.activate(editor);
		     	} else {
		     		// if editor is currently not open on that input either re-use existing
		     		CompareUI.reuseCompareEditor(input, (IReusableEditor)editor);
		     		workBenchPage.activate(editor);
		     	}
		     } else {
		     	CompareUI.openCompareEditor(input);
		     }
		} catch (Exception e) {
		}
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
	
	protected boolean updateSelection(IStructuredSelection selection) {
		this.selection = selection;
		if (selection.size() == 1){
			this.setText(CVSUIMessages.CompareRevisionAction_CompareWithCurrent);
			return true;
		}
		else if (selection.size() == 2){
			this.setText(CVSUIMessages.CompareRevisionAction_CompareWithOther);	
			return true;
		}

		return false;
	}
	public void setPage(CVSHistoryPage page) {
		this.page = page;
	}

}
