package org.eclipse.update.internal.ui.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.Policy;
import org.eclipse.update.internal.core.UpdateManagerPlugin;
import org.eclipse.update.internal.security.*;
/**
 *
 * Call example:
 * JarVerificationService verifier = new JarVerificationService();
 * JarVerificationResult result = verifier.verify(IFeature,ContentReferences[], InstallMonitor monitor);
 * throws an exception if user canceled or error occured
 */
public class JarVerificationService implements IVerificationListener {

	/**
	 * The JarVerifier is a instance variable
	 * bacause we want to reuse it upon multiple calls
	 */
	private JarVerifier jarVerifier;

	/**
	 * the Shell
	 */
	private Shell shell;

	/**
	 * teh result
	 */
	private IVerificationResult result;


	/**
	 * If no shell, create a new shell 
	 */
	public JarVerificationService() {
		this(null);
	}
	/**
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

	/**
	 * returns an JarVerificationResult
	 * The monitor can be null.
	 */
	private IVerificationResult okToInstall(){

		switch (result.getVerificationCode()) {
			case IVerificationResult.UNKNOWN_ERROR :
				{
					result.setResultCode(CHOICE_ERROR);
					break;
				}			
			case IVerificationResult.VERIFICATION_CANCELLED:
				{
					result.setResultCode(CHOICE_ABORT);
					break;
				}

			case IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED_DO_NOT_PROMPT : {
				break;
			}
			
			default :
				{
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							result.setResultCode(openWizard());
						}
					});
				}
		}

		return result;
	}
	/**
	 * 
	 */
	private int openWizard() {

		int code;

		JarVerificationDialog dialog = new JarVerificationDialog(shell, result);
		dialog.open();

		if (dialog.okToInstall())
			code = CHOICE_INSTALL_TRUST_ONCE;
		else
			code = CHOICE_ABORT;

		return code;

	}

	/*
	 * @see IVerificationListener#prompt(IVerificationResult)
	 */
	public int prompt(IVerificationResult verificationResult){
		result = verificationResult;
		return(okToInstall().getResultCode());
	}

	



}