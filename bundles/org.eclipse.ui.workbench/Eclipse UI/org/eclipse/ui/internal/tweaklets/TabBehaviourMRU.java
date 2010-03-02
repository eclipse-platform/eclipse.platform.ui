/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.tweaklets;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.internal.IPreferenceConstants;
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
		return null;
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
