/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.patch;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.*;

import org.eclipse.ui.IActionDelegate;

import org.eclipse.compare.internal.CompareUIPlugin;


public class CompareWithPatchAction implements IActionDelegate {

	static class PatchWizardDialog extends WizardDialog {
	
		PatchWizardDialog(Shell parent, IWizard wizard) {
			super(parent, wizard);
			
			setShellStyle(getShellStyle() | SWT.RESIZE);
			setMinimumPageSize(600, 400);
		}
	}
	
	private ISelection fSelection;

	public void run(IAction action) {
		
		PatchWizard wizard= new PatchWizard(fSelection);
		PatchWizardDialog wd= new PatchWizardDialog(CompareUIPlugin.getShell(), wizard);
		wd.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		fSelection= selection;
		action.setEnabled(fSelection != null && !fSelection.isEmpty());
	}
}
