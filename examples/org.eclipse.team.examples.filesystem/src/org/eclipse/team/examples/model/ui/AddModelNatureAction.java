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
package org.eclipse.team.examples.model.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.examples.model.ModelProject;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionDelegate;

public class AddModelNatureAction extends ActionDelegate implements IObjectActionDelegate {

	private ISelection selection;
	private IWorkbenchPart targetPart;

	public AddModelNatureAction() {
		super();
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
		super.selectionChanged(action, selection);
	}
	
	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection ss = (IStructuredSelection) selection;
			try {
				PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException {
						try {
							makeIntoModProjects(ss, monitor);
						} catch (CoreException e) {
							throw new InvocationTargetException(e);
						}
					}
				});
			} catch (InvocationTargetException e) {
				ErrorDialog.openError(getShell(), null, null, TeamException.asTeamException(e).getStatus());
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}

	void makeIntoModProjects(IStructuredSelection ss, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Adding model nature", IProgressMonitor.UNKNOWN);
		for (Iterator iter = ss.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element instanceof IProject) {
				IProject project = (IProject) element;
				if (!ModelProject.isModProject(project)) {
					ModelProject.makeModProject(project, new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN));
				}
			}
		}
		monitor.done();
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	private Shell getShell() {
		if (targetPart != null)
			return targetPart.getSite().getShell();
		return null;
	}
	
}
