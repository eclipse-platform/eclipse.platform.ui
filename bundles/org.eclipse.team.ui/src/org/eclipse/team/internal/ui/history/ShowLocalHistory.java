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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.history.IHistoryPage;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionDelegate;

public class ShowLocalHistory extends ActionDelegate implements IObjectActionDelegate {

	private IStructuredSelection fSelection;
	
	public void run(IAction action) {
		final Shell shell= Display.getDefault().getActiveShell();
		try {
			new ProgressMonitorDialog(shell).run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					final IResource resource = (IResource) fSelection.getFirstElement();
					Runnable r = new Runnable() {
						public void run() {
							IHistoryPage page = TeamUI.getHistoryView().showHistoryFor(resource);
							if (page instanceof LocalHistoryPage){
								LocalHistoryPage historyPage = (LocalHistoryPage) page;
								historyPage.setClickAction(isCompare());
							}
						}
					};
					TeamUIPlugin.getStandardDisplay().asyncExec(r);				
				}
			});
		} catch (InvocationTargetException exception) {
			ErrorDialog.openError(shell, null, null, new Status(IStatus.ERROR, TeamUIPlugin.PLUGIN_ID, IStatus.ERROR, TeamUIMessages.ShowLocalHistory_1, exception.getTargetException()));
		} catch (InterruptedException exception) {
		}
	}
	
	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
		}
	}
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// do nothing
	}

	protected boolean isCompare() {
		return false;
	}

}
