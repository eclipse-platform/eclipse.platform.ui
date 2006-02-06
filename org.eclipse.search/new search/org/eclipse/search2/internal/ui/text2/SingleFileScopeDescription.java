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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.dialogs.IDialogSettings;

import org.eclipse.ui.IWorkbenchPage;

public class SingleFileScopeDescription implements IScopeDescription {

	private static final String KEY_FULL_PATH= "full-path"; //$NON-NLS-1$
	private IPath fPath;

	public SingleFileScopeDescription() {
	}

	public SingleFileScopeDescription(IFile file) {
		fPath= file.getFullPath();
	}

	public IResource[] getRoots(IWorkbenchPage page) {
		return getFiles(page);
	}

	public void store(IDialogSettings section) {
		if (fPath != null) {
			section.put(KEY_FULL_PATH, fPath.toPortableString());
		}
	}
	
	public void restore(IDialogSettings section) {
		fPath= null;
		String portable= section.get(KEY_FULL_PATH);
		if (portable != null) {
			fPath= Path.fromPortableString(portable);
		}
	}

	public void store(Properties props, String prefix) {
		if (fPath != null) {
			props.put(prefix+ KEY_FULL_PATH, fPath.toPortableString());
		}
	}
	public void restore(Properties props, String prefix) {
		fPath= null;
		Object portable= props.get(prefix + KEY_FULL_PATH);
		if (portable != null) {
			fPath= Path.fromPortableString(portable.toString());
		}
	}

	public String getLabelForCombo() {
		return fPath == null ? "null" : RetrieverLabelProvider.getFileLableWithContainer(fPath); //$NON-NLS-1$
	}

	public String getNameForDescription() {
		return getLabelForCombo();
	}

	public IFile[] getFiles(IWorkbenchPage page) {
		if (fPath != null) {
			IResource r= ResourcesPlugin.getWorkspace().getRoot().findMember(fPath);
			if (r instanceof IFile) {
				return new IFile[] {(IFile) r};
			}
		}
		return new IFile[0];
	}
}
