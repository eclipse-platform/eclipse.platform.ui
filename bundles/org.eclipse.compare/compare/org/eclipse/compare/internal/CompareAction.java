/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;


/*
 * The "Compare with each other" action
 */
public class CompareAction implements IObjectActionDelegate {

	private ResourceCompareInput fInput;
	private IWorkbenchPage fWorkbenchPage;

	public void run(IAction action) {
		if (fInput != null) {
			fInput.initializeCompareConfiguration();
			CompareUI.openCompareEditorOnPage(fInput, fWorkbenchPage);
			fInput= null;	// don't reuse this input!
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (fInput == null) {
			CompareConfiguration cc= new CompareConfiguration();
			// buffered merge mode: don't ask for confirmation
			// when switching between modified resources
			cc.setProperty(CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean(false));
						
			fInput= new ResourceCompareInput(cc);
		}
		action.setEnabled(fInput.setSelection(selection));
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fWorkbenchPage= targetPart.getSite().getPage();
	}
}
