/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
		if (fInput != null) {
			fInput.initializeCompareConfiguration();
			CompareUI.openCompareEditor(fInput);
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
}
