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
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.dialogs.IDialogSettings;

import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.search2.internal.ui.SearchMessages;

public class WorkspaceScopeDescription implements IScopeDescription {
	public static final String LABEL= SearchMessages.WorkspaceScopeDescription_label;

	public WorkspaceScopeDescription() {
	}

	public String getLabel() {
		return LABEL;
	}

	public IResource[] getRoots(IWorkbenchPage page) {
		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}

	public void restore(IDialogSettings section) {
	}

	public void store(IDialogSettings section) {
	}

	public void store(Properties props, String prefix) {
	}

	public void restore(Properties props, String prefix) {
	}
}
