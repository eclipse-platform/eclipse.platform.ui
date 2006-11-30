/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.history;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ui.history.IHistoryPageSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPageSite;

public class DialogHistoryPageSite implements IHistoryPageSite {

	private ISelectionProvider selectionProvider;
	private final Shell shell;
	private IToolBarManager toolBarManager;
	
	public DialogHistoryPageSite(Shell shell) {
		this.shell = shell;
	}
	
	public IPageSite getWorkbenchPageSite() {
		return null;
	}

	public IWorkbenchPart getPart() {
		return null;
	}

	public Shell getShell() {
		return shell;
	}

	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	public void setSelectionProvider(ISelectionProvider provider) {
		this.selectionProvider = provider;
	}

	public void setFocus() {
		// Nothing to do
	}

	public void setToolBarManager(IToolBarManager toolBarManager) {
		this.toolBarManager = toolBarManager;
	}

	public boolean isModal() {
		return true;
	}

	public IToolBarManager getToolBarManager() {
		return toolBarManager;
	}

}
