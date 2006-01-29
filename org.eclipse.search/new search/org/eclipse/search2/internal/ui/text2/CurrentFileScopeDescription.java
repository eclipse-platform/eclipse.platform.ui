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

package org.eclipse.search2.internal.ui.text2;

import java.util.Properties;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.dialogs.IDialogSettings;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.search2.internal.ui.SearchMessages;

public class CurrentFileScopeDescription implements IScopeDescription {

	private static final IResource[] EMPTY_ARRAY= new IResource[0];

	public CurrentFileScopeDescription() {
	}

	public IResource[] getRoots(IWorkbenchPage page) {
		IEditorPart editor= page.getActiveEditor();
		if (editor != null) {
			IEditorInput ei= editor.getEditorInput();
			if (ei instanceof IFileEditorInput) {
				IFileEditorInput fi= (IFileEditorInput) ei;
				return new IResource[] {fi.getFile()};
			}
		}
		return EMPTY_ARRAY;
	}

	public void store(IDialogSettings section) {
	}
	public void restore(IDialogSettings section) {
	}

	public void store(Properties props, String prefix) {
	}
	public void restore(Properties props, String prefix) {
	}

	public String getLabel() {
		return SearchMessages.CurrentFileScopeDescription_label;
	}
}
