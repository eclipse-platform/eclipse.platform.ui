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

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkingSet;

import org.eclipse.search2.internal.ui.SearchMessages;

public class WindowWorkingSetScopeDescription extends WorkingSetScopeDescription {

	public WindowWorkingSetScopeDescription() {
		setLabel(SearchMessages.WindowWorkingSetScopeDescription_label);
	}

	public IResource[] getRoots(IWorkbenchPage page) {
		IWorkingSet ws= page.getAggregateWorkingSet();
		return getRootsFromWorkingSet(ws);
	}

	public void store(IDialogSettings section) {
	}
	public void restore(IDialogSettings section) {
	}

	public void store(Properties props, String prefix) {
	}
	public void restore(Properties props, String prefix) {
	}
}
