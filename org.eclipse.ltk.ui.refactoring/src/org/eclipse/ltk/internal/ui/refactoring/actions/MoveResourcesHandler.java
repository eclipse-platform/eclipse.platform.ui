/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.actions;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.resource.MoveResourcesWizard;

public class MoveResourcesHandler extends AbstractResourcesHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell activeShell= HandlerUtil.getActiveShell(event);
		ISelection sel= HandlerUtil.getCurrentSelection(event);
		if (sel instanceof IStructuredSelection) {
			IResource[] resources= getSelectedResources((IStructuredSelection) sel);
			if (resources.length > 0) {
				MoveResourcesWizard refactoringWizard= new MoveResourcesWizard(resources);
				RefactoringWizardOpenOperation op= new RefactoringWizardOpenOperation(refactoringWizard);
				try {
					op.run(activeShell, RefactoringUIMessages.MoveResourcesHandler_title);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
		return null;
	}
}
