/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.security;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.ui.*;
/**
 *
 */
public class JarVerificationService implements IVerificationListener {

	/*
	 * The JarVerifier is a instance variable
	 * bacause we want to reuse it upon multiple calls
	 */

	private Shell shell;
	// keep track of the last verify code.
	private int lastVerifyCode = -1;
	
	/**
	 * Processed ContentRefernces.  They will be skipped if prompted
	 * to verify the same reference again.
	 */
	private Map processed=new HashMap();

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
		JarVerificationDialog dialog =
				new JarVerificationDialog(shell,page,result);
		dialog.create();
		dialog.getShell().setSize(600, 500);
		dialog.getShell().setText(UpdateUIMessages.JarVerificationDialog_wtitle); 
		dialog.open();
		if (dialog.getReturnCode() == JarVerificationDialog.OK) {
			code = CHOICE_INSTALL_TRUST_ONCE;
		} else if (dialog.getReturnCode() == JarVerificationDialog.INSTALL_ALL) {
			code = CHOICE_INSTALL_TRUST_ALWAYS;
		} else { 
			code = CHOICE_ABORT;
		}
		return code;

	}

	/*
	 * 
	 */
	public int prompt(final IVerificationResult verificationResult){
		if (!UpdateCore.getPlugin().getPluginPreferences().getBoolean(UpdateCore.P_CHECK_SIGNATURE)) 
			return CHOICE_INSTALL_TRUST_ALWAYS;

		if (verificationResult.alreadySeen()) return CHOICE_INSTALL_TRUST_ALWAYS;
		
		if(see(verificationResult)) return CHOICE_INSTALL_TRUST_ALWAYS;
		
		if (lastVerifyCode == CHOICE_INSTALL_TRUST_ALWAYS) return CHOICE_INSTALL_TRUST_ALWAYS;

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
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							lastVerifyCode = openWizard(verificationResult);
						}
					});
					return lastVerifyCode;
				}
		}
	}

	/**
	 * Checks whether feature archive has been seen already.
	 * Remembers the fact that archive is being seen now.
	 * @param verificationResult
	 * @return true if the archive has been seen before, false if first time
	 */
	private boolean see(final IVerificationResult verificationResult) {
		String key = verificationResult.getFeature().getVersionedIdentifier().toString()
			+"/"+verificationResult.getContentReference().getIdentifier(); //$NON-NLS-1$
		Long value = new Long(verificationResult.getContentReference().getLastModified());
		Long cachedValue = (Long)processed.get(key);
		if(value.equals(cachedValue)){
			return true;
		}else{
			processed.put(key, value);
			return false;
		}
	}
}
