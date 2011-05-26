/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.tweaklets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.e4.compatibility.E4Util;
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
		int length = editors.length;
		if (length < page.getEditorReuseThreshold()) {
			return null;
		} else if (length > page.getEditorReuseThreshold()) {
			List<IEditorReference> refs = new ArrayList<IEditorReference>();
			List<IEditorReference> keep = new ArrayList<IEditorReference>(Arrays.asList(editors));
			int extra = length - page.getEditorReuseThreshold();
			// look for extra editors that should be closed
			for (int i = 0; i < editors.length; i++) {
				if (extra == 0) {
					break;
				}

				if (editors[i].isPinned() || editors[i].isDirty()) {
					continue;
				}

				refs.add(editors[i]);
				extra--;
			}

			for (IEditorReference ref : refs) {
				page.closeEditor(ref, false);
				keep.remove(ref);
			}

			editors = keep.toArray(new IEditorReference[keep.size()]);
		}

		IEditorReference dirtyEditor = null;

		// find an editor to reuse, go in reverse due to activation order
		for (int i = editors.length - 1; i > -1; i--) {
			IEditorReference editor = editors[i];
			if (editor.isPinned()) {
				// skip pinned editors
				continue;
			}
			if (editor.isDirty()) {
				// record dirty editors
				if (dirtyEditor == null) {
					dirtyEditor = editor;
				}
				continue;
			}
			// an editor is neither pinned nor dirty, use this one
			return editor;
		}
		// can't find anything, return null
		if (dirtyEditor == null) {
			return null;
		}

		/* fix for 11122 */
		boolean reuseDirty = WorkbenchPlugin.getDefault().getPreferenceStore()
				.getBoolean(IPreferenceConstants.REUSE_DIRTY_EDITORS);
		if (!reuseDirty) {
			return null;
		}

		MessageDialog dialog = new MessageDialog(
				page.getWorkbenchWindow().getShell(),
				WorkbenchMessages.EditorManager_reuseEditorDialogTitle,
				null, // accept the default window icon
				NLS.bind(WorkbenchMessages.EditorManager_saveChangesQuestion, dirtyEditor.getName()),
				MessageDialog.QUESTION, new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.NO_LABEL,
						WorkbenchMessages.EditorManager_openNewEditorLabel }, 0) {
			protected int getShellStyle() {
				return super.getShellStyle() | SWT.SHEET;
			}
		};
		int result = dialog.open();
		if (result == 0) { // YES
			IEditorPart editor = dirtyEditor.getEditor(true);
			if (!page.saveEditor(editor, false)) {
				return null;
			}
		} else if ((result == 2) || (result == -1)) {
			return null;
		}
		return dirtyEditor;
	}

	public IEditorReference reuseInternalEditor(WorkbenchPage page,
 Object manager,
			Object editorPresentation,
			EditorDescriptor desc, IEditorInput input,
			IEditorReference reusableEditorRef) {
		E4Util.unsupported("reuseInternalEditor: we reuse nothing"); //$NON-NLS-1$
		return reusableEditorRef;
	}

}
