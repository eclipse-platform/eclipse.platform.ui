package org.eclipse.update.internal.ui.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.UpdateManagerPlugin;
import org.eclipse.update.internal.security.JarVerificationResult;
import org.eclipse.update.internal.security.JarVerifier;
import org.eclipse.update.internal.core.Policy;
/**
 *
 * Call example:
 * JarVerificationService verifier = new JarVerificationService();
 * JarVerificationResult result = verifier.verify(IFeature,ContentReferences[], InstallMonitor monitor);
 * throws an exception if user canceled or error occured
 */
public class JarVerificationService implements IFeatureVerification {

	/**
	 * The result object
	 */
	private JarVerificationResult result;

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
	 */
	public JarVerificationResult getResult() {
		if (result == null) {
			result = new JarVerificationResult();
		}

		return result;
	}

	/**
	 * returns an JarVerificationResult
	 * The monitor can be null.
	 */
	private JarVerificationResult okToInstall(final File jarFile, final String id, final String featureName, final String providerName, InstallMonitor monitor) {

		jarVerifier.setMonitor(monitor);
		result= jarVerifier.verify(jarFile);

		switch (getResult().getResultCode()) {
			case JarVerifier.UNKNOWN_ERROR :
				{
					getResult().setResultCode(JarVerificationResult.ERROR_INSTALL);					
					break;
				}
			case JarVerifier.VERIFICATION_CANCELLED :
				{
					getResult().setResultCode(JarVerificationResult.CANCEL_INSTALL);							
					break;
				}
			default :
				{
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							getResult().setResultCode(openWizard(jarFile, id, featureName, providerName));
						}
					});
				}
		}

		return getResult();
	}
	/**
	 * 
	 */
	private int openWizard(File jarFile, String id, String componentName, String providerName) {

		int code;

		//	JarVerifierWizard wizard = new JarVerifierWizard(jarFile, componentName, verificationResult);
		//	WizardDialog dialog = new WizardDialog(shell, wizard);

		JarVerificationDialog dialog = new JarVerificationDialog(shell, id, componentName, providerName, jarFile, result);
		dialog.open();

		if (JarVerificationDialog.COMPONENT_TO_INSTALL)
			code = JarVerificationResult.OK_TO_INSTALL;
		else
			code = JarVerificationResult.CANCEL_INSTALL;

		return code;
	
	}
	
	/*
	 * @see IFeatureVerification#verify(IFeature feature,ContentReference[], InstallMonitor)
	 */
	public void verify(IFeature feature,ContentReference[] references, InstallMonitor monitor) throws CoreException {
		if (references==null || references.length==0) return ;
		
		try {
		for (int i = 0; i < references.length; i++) {
			if (references[i] instanceof JarContentReference){
				JarContentReference jarReference = (JarContentReference)references[i];
				result = okToInstall(jarReference.asFile(),feature.getVersionedIdentifier().toString(),feature.getLabel(),feature.getProvider(),monitor);
				if (result.getResultCode()==JarVerificationResult.CANCEL_INSTALL){
					throw newCoreException(Policy.bind("JarVerificationService.CancelInstall"),result.getResultException()); //$NON-NLS-1$
				}
				if (result.getResultCode()==JarVerificationResult.ERROR_INSTALL){
					throw newCoreException(Policy.bind("JarVerificationService.UnsucessfulVerification"),result.getResultException()); //$NON-NLS-1$
				}				
			}
		}
		} catch (IOException e){
					throw newCoreException(Policy.bind("JarVerificationService.ErrorDuringVerification"),e);			 //$NON-NLS-1$
		}
	}

	/**
	 * 
	 */
	private CoreException newCoreException(String s, Throwable e) throws CoreException {
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		return new CoreException(new Status(IStatus.ERROR,id,0,s,e));
	}	

}