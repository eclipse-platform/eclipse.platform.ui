package org.eclipse.update.internal.ui.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.core.IVerificationListener;
import org.eclipse.update.core.IVerificationResult;
import org.eclipse.update.internal.security.JarVerifier;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.wizards.InstallWizardDialog;
/**
 *
 */
public class JarVerificationService implements IVerificationListener {

	/*
	 * The JarVerifier is a instance variable
	 * bacause we want to reuse it upon multiple calls
	 */
	private JarVerifier jarVerifier;

	/*
	 * the Shell
	 */
	private Shell shell;

	/*
	 * If no shell, create a new shell 
	 */
	public JarVerificationService() {
		this(null);
	}
	
	/*
	 * 
	 */
	public JarVerificationService(Shell aShell) {
		jarVerifier = new JarVerifier();
		shell = aShell;

		// find the default display and get the active shell
		if (shell == null) {
			final Display disp = Display.getDefault();
			if (disp == null) {
				shell = new Shell(new Display());
			} else {
				disp.syncExec(new Runnable() {
					public void run() {
						shell = disp.getActiveShell();
					}
				});
			}
		}
	}

	/*
	 * 
	 */
	private int openWizard(IVerificationResult result) {
		int code;
		IDialogPage page = new JarVerificationPage(result);
		Dialog dialog =
				new JarVerificationDialog(shell,page,result);
		dialog.create();
		dialog.getShell().setSize(600, 500);
		dialog.open();
		if (dialog.getReturnCode() == dialog.OK)
			code = CHOICE_INSTALL_TRUST_ALWAYS;
		else
			code = CHOICE_ABORT;

		return code;

	}

	/*
	 * 
	 */
	public int prompt(final IVerificationResult verificationResult){

		if (verificationResult.alreadySeen()) return CHOICE_INSTALL_TRUST_ALWAYS;

		switch (verificationResult.getVerificationCode()) {
			case IVerificationResult.UNKNOWN_ERROR :
					return CHOICE_ERROR;

			case IVerificationResult.VERIFICATION_CANCELLED:
					return CHOICE_ABORT;

			// cannot verify it: do not prompt user.
			case IVerificationResult.TYPE_ENTRY_UNRECOGNIZED: 
				return CHOICE_INSTALL_TRUST_ALWAYS;				
			
			default :
				{
					final int[] wizardResult = new int[1];					
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							wizardResult[0] = openWizard(verificationResult);
						}
					});
					return wizardResult[0];
				}
		}
	}
}