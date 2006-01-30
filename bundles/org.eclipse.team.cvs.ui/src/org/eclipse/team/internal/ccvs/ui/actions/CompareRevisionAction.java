/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.team.internal.ui.history.CompareFileRevisionEditorInput;
import org.eclipse.team.internal.ui.history.FileRevisionTypedElement;

public class CompareRevisionAction extends TeamAction {

	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					IStructuredSelection structSel = getSelection();
					if (structSel.size() != 2)
						return;

					Object[] objArray = structSel.toArray();

					IFileRevision file1 = (IFileRevision) objArray[0];
					IFileRevision file2 = (IFileRevision) objArray[1];

					FileRevisionTypedElement left = new FileRevisionTypedElement(file1);
					FileRevisionTypedElement right = new FileRevisionTypedElement(file2);
					
					CompareUI.openCompareEditorOnPage(new CompareFileRevisionEditorInput(left, right), getTargetPage());

				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			}
		}, TeamUIMessages.ConfigureProjectAction_configureProject, PROGRESS_BUSYCURSOR);
	}

	protected boolean isEnabled() throws TeamException {
		int sizeofSelection = getSelection().size();

		if (sizeofSelection == 2)
			return true;

		return false;
	}

}
