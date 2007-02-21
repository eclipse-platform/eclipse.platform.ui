/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.tweaklets;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.internal.EditorAreaHelper;
import org.eclipse.ui.internal.EditorManager;
import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.internal.EditorSite;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.eclipse.ui.internal.registry.EditorDescriptor;

/**
 * @since 3.3
 * 
 */
public class TabBehaviourMRU extends TabBehaviour {

	public boolean alwaysShowPinAction() {
		return false;
	}

	public IEditorReference findReusableEditor(WorkbenchPage page) {
		boolean reuse = WorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
		if (!reuse) {
			return null;
		}

		IEditorReference editors[] = page.getSortedEditors();
		if (editors.length < page.getEditorReuseThreshold()) {
			return null;
		}

		IEditorReference dirtyEditor = null;

		// Find a editor to be reused
		for (int i = 0; i < editors.length; i++) {
			IEditorReference editor = editors[i];
			// if(editor == activePart)
			// continue;
			if (editor.isPinned()) {
				continue;
			}
			if (editor.isDirty()) {
				if (dirtyEditor == null) {
					dirtyEditor = editor;
				}
				continue;
			}
			return editor;
		}
		if (dirtyEditor == null) {
			return null;
		}

		/* fix for 11122 */
		boolean reuseDirty = WorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IPreferenceConstants.REUSE_DIRTY_EDITORS);
		if (!reuseDirty) {
			return null;
		}

		MessageDialog dialog = new MessageDialog(page.getWorkbenchWindow()
				.getShell(),
				WorkbenchMessages.EditorManager_reuseEditorDialogTitle,
				null, // accept the default window icon
				NLS.bind(WorkbenchMessages.EditorManager_saveChangesQuestion,
						dirtyEditor.getName()), MessageDialog.QUESTION,
				new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.NO_LABEL,
						WorkbenchMessages.EditorManager_openNewEditorLabel }, 0);
		int result = dialog.open();
		if (result == 0) { // YES
			ProgressMonitorDialog pmd = new ProgressMonitorJobsDialog(dialog
					.getShell());
			pmd.open();
			dirtyEditor.getEditor(true).doSave(pmd.getProgressMonitor());
			pmd.close();
		} else if ((result == 2) || (result == -1)) {
			return null;
		}
		return dirtyEditor;
	}

	public IEditorReference reuseInternalEditor(WorkbenchPage page,
			EditorManager manager, EditorAreaHelper editorPresentation,
			EditorDescriptor desc, IEditorInput input,
			IEditorReference reusableEditorRef) {
		IEditorPart reusableEditor = reusableEditorRef.getEditor(false);
		if (reusableEditor == null) {
			IEditorReference result = new EditorReference(manager, input, desc);
			page.closeEditor(reusableEditorRef, false);
			return result;
		}

		EditorSite site = (EditorSite) reusableEditor.getEditorSite();
		EditorDescriptor oldDesc = site.getEditorDescriptor();
		if ((desc.getId().equals(oldDesc.getId()))
				&& (reusableEditor instanceof IReusableEditor)) {
			Workbench wb = (Workbench) page.getWorkbenchWindow().getWorkbench();
			editorPresentation.moveEditor(reusableEditor, -1);
			wb.getEditorHistory().add(reusableEditor.getEditorInput(),
					site.getEditorDescriptor());
			page.reuseEditor((IReusableEditor) reusableEditor, input);
			return reusableEditorRef;
		}
		// findReusableEditor(...) checks pinned and saves editor if
		// necessary, so it's OK to close "reusableEditor"
		IEditorReference ref = new EditorReference(manager, input, desc);
		reusableEditor.getEditorSite().getPage().closeEditor(reusableEditor,
				false);
		return ref;
	}

}
