/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.update.internal.ui.security;

import java.security.Certificate;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.internal.security.JarVerificationResult;

/**
 * @version 	1.0
 * @author
 */
public class CertificateDialog extends Dialog {

	protected JarVerificationResult _VerificationResult = null;
	
	/**
	 * Constructor for CertificateDialog.
	 * @param parentShell
	 */
	protected CertificateDialog(Shell parentShell, JarVerificationResult VerificationResult) {
		super(parentShell);
		_VerificationResult = VerificationResult;
	}

	


	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		return super.createDialogArea(parent);
	}

}
