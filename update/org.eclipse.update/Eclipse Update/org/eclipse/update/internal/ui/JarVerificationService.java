package org.eclipse.update.internal.ui;

import org.eclipse.jface.dialogs.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.widgets.*;
import java.io.File;
import org.eclipse.update.internal.core.*;
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
public class JarVerificationService {

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
 * 
 * @return JarVerificationResult
 */
public JarVerificationResult getResult() {
	if (result==null){
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
	MessageDialog.openError(shell, UpdateManagerStrings.getString("S_Verification"), errorMessage + "\n"+ UpdateManagerStrings.getString("S_The_installation_process_will_be_aborted"));
	getResult().setResultCode(JarVerificationResult.CANCEL_INSTALL);
	getResult().setResultException(jarVerifier.getResultException());
}
/**
 * returns an JarVerificationResult
 * The monitor can be null.
 */
public JarVerificationResult okToInstall(final File jarFile, final String id, final String componentName, final String providerName, IProgressMonitor monitor) {

	jarVerifier.setMonitor(monitor);
	final int verificationResult = jarVerifier.verify(jarFile);

	switch (verificationResult) {
		case JarVerifier.UNKNOWN_ERROR :
			{
				jarVerifierError(UpdateManagerStrings.getString("S_Error_occurred_during_verification"));
				break;
			}
		case JarVerifier.VERIFICATION_CANCELLED :
			{
				jarVerifierError(UpdateManagerStrings.getString("S_Verification_cancelled"));
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

	JarVerificationDialog dialog = new JarVerificationDialog( shell, id, componentName, providerName, jarFile, verificationResult);
	dialog.open();

	if (JarVerificationDialog.COMPONENT_TO_INSTALL)
		code = JarVerificationResult.OK_TO_INSTALL;
	else
		code = JarVerificationResult.CANCEL_INSTALL;

	return code;
}
}
