/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.*;

public class RepositoriesFilterAction extends Action {
	private TreeViewer viewer;
	private RepositoriesFilter filter;
	private RepositoriesView view;

	public RepositoriesFilterAction(RepositoriesView view) {
		this.view = view;
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(TreeViewer viewer) {
		this.viewer = viewer;
	}
	
	public void setFilter(RepositoriesFilter filter) {
		this.filter = filter;
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run() {
		RepositoriesFilterDialog historyDialog = new RepositoriesFilterDialog(viewer.getControl().getShell());
		if (filter != null) {
			historyDialog.setFilter(filter);
		}
		if (historyDialog.open() == Window.CANCEL) {
			return;
		}
		
		filter = historyDialog.getFilter();
		view.showFilter(filter);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
}
