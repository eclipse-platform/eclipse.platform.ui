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
 * The class will verify the Component and return a JarVerifierResult object
 * to the sender. The result contains a returnCode.
 * The return code is one of the VerifyComponent static values.
 *
 * If the component is signed and trusted by one of the keystore
 * the user will not be prompted, and we'll consider the component
 * to be installed.
 *
 * Call example:
 * JarVerificationService verifier = new JarVerificationService();
 * JarVerificationResult result = verifier.okToInstall(File jarFileToVerify,String nameOfTheComponent, IProgressMonitor monitor);
 * if (result.getResultCode()==result.OK_TO_INSTALL){
 *    // install component
 * }
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
	 * 
	 */
	private void jarVerifierError(final String errorMessage) {
		/*
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					MessageDialog diag =
						new MessageDialog(
							shell,
							errorMessage + ": " + UpdateManagerStrings.getString("S_The_installation_process_will_be_aborted"),
							null,
							jarVerifier.getResultException().getMessage(),
							MessageDialog.ERROR,
							new String[] { UpdateManagerStrings.getString("S_OK") },
							0);
		
					diag.open();
		
		
				}
			});
		*/
		MessageDialog.openError(shell, Policy.bind("JarVerificationService.Verification"), errorMessage + Policy.bind("JarVerificationService.InstallationAborted")); //$NON-NLS-1$ //$NON-NLS-2$
		getResult().setResultCode(JarVerificationResult.CANCEL_INSTALL);
		getResult().setResultException(jarVerifier.getResultException());
	}
	/**
	 * returns an JarVerificationResult
	 * The monitor can be null.
	 */
	public JarVerificationResult okToInstall(final File jarFile, final String id, final String componentName, final String providerName, InstallMonitor monitor) {

		jarVerifier.setMonitor(monitor);
		final int verificationResult = jarVerifier.verify(jarFile);

		switch (verificationResult) {
			case JarVerifier.UNKNOWN_ERROR :
				{
					jarVerifierError(Policy.bind("JarVerificationService.ErrorVerification")); //$NON-NLS-1$
					break;
				}
			case JarVerifier.VERIFICATION_CANCELLED :
				{
					jarVerifierError(Policy.bind("JarVerificationService.VerificationCanceled")); //$NON-NLS-1$
					break;
				}
			case JarVerifier.SOURCE_VERIFIED :
				{
					getResult().setResultCode(JarVerificationResult.OK_TO_INSTALL);
					getResult().setResultException(null);
					break;
				}
				// if the JAR is not signed, corrupted or not validated
				// ask the user for confirmation.
			default :
				{
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							getResult().setResultCode(openWizard(jarFile, id, componentName, providerName, verificationResult));
							getResult().setResultException(null);
						}
					});
				}
		}

		return getResult();
	}
	/**
	 * 
	 */
	private int openWizard(File jarFile, String id, String componentName, String providerName, int verificationResult) {

		int code;

		//	JarVerifierWizard wizard = new JarVerifierWizard(jarFile, componentName, verificationResult);
		//	WizardDialog dialog = new WizardDialog(shell, wizard);

		JarVerificationDialog dialog = new JarVerificationDialog(shell, id, componentName, providerName, jarFile, verificationResult);
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
				JarVerificationResult result = okToInstall(jarReference.asFile(),feature.getVersionedIdentifier().toString(),feature.getLabel(),feature.getProvider(),monitor);
				if (result.getResultCode()==JarVerificationResult.CANCEL_INSTALL){
					throw newCoreException(Policy.bind("JarVerificationService.UnsucessfulVerification"),null); //$NON-NLS-1$
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