/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000, 2001
 */
package org.eclipse.compare.internal;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IActionDelegate;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;


public class CompareAction implements IActionDelegate {

	private ResourceCompareInput fInput;

	public void run(IAction action) {
		if (fInput != null)
			CompareUI.openCompareEditor(fInput);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (fInput == null)
			fInput= new ResourceCompareInput(new CompareConfiguration());
		action.setEnabled(fInput.setSelection(selection));
	}
}
