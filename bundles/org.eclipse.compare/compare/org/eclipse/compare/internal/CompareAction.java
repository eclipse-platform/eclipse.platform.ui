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

	private ISelection fSelection;

	public void run(IAction action) {
		CompareUI.openCompareEditor(new ResourceCompareInput(new CompareConfiguration(), fSelection));
	}

	public void selectionChanged(IAction a, ISelection s) {
		fSelection= s;
		// the following check is disabled because it results in a confusing UI:
		// action might be enabled if plugin is not loaded but
		// it gets disabled after plugin has been loaded...
		//Object[] selection= StructuredSelection.toArray(s);
		//((Action)a).setEnabled(selection.length == 2);
	}
}
