package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Subclassed to provide access to button presses
 */
public class LaunchWizardDialog extends WizardDialog {

	protected LaunchWizard fWizard;
	
	/**
	 * Constructs a wizard dialog
	 */
	public LaunchWizardDialog(Shell shell, LaunchWizard w) {
		super(shell, w);
		fWizard= w;
	}

	protected void cancelPressed() {
		fWizard.performCancel();
		super.cancelPressed();
	}

}

