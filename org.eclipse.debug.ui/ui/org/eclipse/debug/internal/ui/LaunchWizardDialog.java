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

	/**
	 * Only needed for VAJ support as cannot use <code>LaunchWizard.super.nextPressed()</code>
	 * in the runnable
	 */
	private void nextPressed0() {
		super.nextPressed();
	}

	protected void cancelPressed() {
		fWizard.performCancel();
		super.cancelPressed();
	}

	protected void nextPressed() {
		try {
			run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) {
					nextPressed0();
				}
			});
		} catch (InterruptedException ie) {
		} catch (InvocationTargetException ite) {
		}
	}
}

