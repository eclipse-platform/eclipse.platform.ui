/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

	@Override
	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	@Override
	public void run(IAction action) {
		run();
	}

	@Override
	public void run() {
		if (getWorkbenchWindow().getActivePage() == null) {
			SearchPlugin.beep();
			return;
		}
		SearchDialog dialog= new SearchDialog(getWorkbenchWindow(), fPageId);
		dialog.open();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing since the action isn't selection dependent.
	}

	private IWorkbenchWindow getWorkbenchWindow() {
		if (fWindow == null)
			fWindow= SearchPlugin.getActiveWorkbenchWindow();
		return fWindow;
	}

	@Override
	public void dispose() {
		fWindow= null;
	}
}
