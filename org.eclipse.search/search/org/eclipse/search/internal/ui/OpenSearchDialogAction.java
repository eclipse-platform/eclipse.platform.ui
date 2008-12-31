/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Opens the Search Dialog.
 */
public class OpenSearchDialogAction extends Action implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow fWindow;
	private String fPageId;

	public OpenSearchDialogAction() {
		super(SearchMessages.OpenSearchDialogAction_label);
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_TOOL, SearchPluginImages.IMG_TOOL_SEARCH);
		setToolTipText(SearchMessages.OpenSearchDialogAction_tooltip);
	}

	public OpenSearchDialogAction(IWorkbenchWindow window, String pageId) {
		this();
		fPageId= pageId;
		fWindow= window;
	}

	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	public void run(IAction action) {
		run();
	}

	public void run() {
		if (getWorkbenchWindow().getActivePage() == null) {
			SearchPlugin.beep();
			return;
		}
		SearchDialog dialog= new SearchDialog(getWorkbenchWindow(), fPageId);
		dialog.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing since the action isn't selection dependent.
	}

	private IWorkbenchWindow getWorkbenchWindow() {
		if (fWindow == null)
			fWindow= SearchPlugin.getActiveWorkbenchWindow();
		return fWindow;
	}

	public void dispose() {
		fWindow= null;
	}
}
