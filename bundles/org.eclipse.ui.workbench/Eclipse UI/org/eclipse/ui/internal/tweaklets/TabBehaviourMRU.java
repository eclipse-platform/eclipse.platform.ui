/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
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
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 490700
 ******************************************************************************/

package org.eclipse.ui.internal.tweaklets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @since 3.3
 */
public class TabBehaviourMRU extends TabBehaviour {

	@Override
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
			List<IEditorReference> refs = new ArrayList<>();
			List<IEditorReference> keep = new ArrayList<>(Arrays.asList(editors));
			int extra = length - page.getEditorReuseThreshold();
			// look for extra editors that should be closed
			for (IEditorReference editor : editors) {
				if (extra == 0) {
					break;
				}

				if (editor.isPinned() || editor.isDirty()) {
					continue;
				}

				refs.add(editor);
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

		return null;
	}
}
