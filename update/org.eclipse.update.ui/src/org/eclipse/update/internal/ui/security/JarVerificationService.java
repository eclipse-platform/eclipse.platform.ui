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
	 * 
	 */
	private List /*of CertificatePair*/
	trustedCertificates = null;

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
	private JarVerificationResult okToInstall(final JarContentReference jarReference, final IFeature feature, InstallMonitor monitor) throws CoreException {

		jarVerifier.setMonitor(monitor);
		try {
			result = jarVerifier.verify(jarReference.asFile());
		} catch (IOException e) {
			throw newCoreException(Policy.bind("JarVerificationService.ErrorDuringVerification"), e); //$NON-NLS-1$
		}

		switch (getResult().getVerificationCode()) {
			case JarVerification.UNKNOWN_ERROR :
				{
					getResult().setResultCode(JarVerification.ERROR_INSTALL);
					break;
				}
			case JarVerification.VERIFICATION_CANCELLED :
				{
					getResult().setResultCode(JarVerification.CANCEL_INSTALL);
					break;
				}

			default :
				{

					if (alreadyValidated()) {
						return getResult();
					}

					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							getResult().setResultCode(openWizard(jarReference, feature));
						}
					});
				}
		}

		return getResult();
	}
	/**
	 * 
	 */
	private int openWizard(JarContentReference jarReference,IFeature feature) {

		int code;

		JarVerificationDialog dialog = new JarVerificationDialog(shell, jarReference, feature, result);
		dialog.open();

		addTrustedCertificate(dialog.getCertificate());

		if (dialog.okToInstall())
			code = JarVerification.OK_TO_INSTALL;
		else
			code = JarVerification.CANCEL_INSTALL;

		return code;

	}

	/*
	 * @see IFeatureVerification#verify(IFeature feature,ContentReference[], InstallMonitor)
	 */
	public void verify(IFeature feature, ContentReference[] references, InstallMonitor monitor) throws CoreException {
		if (references == null || references.length == 0)
			return;

			for (int i = 0; i < references.length; i++) {
				if (references[i] instanceof JarContentReference) {
					JarContentReference jarReference = (JarContentReference) references[i];
					result = okToInstall(jarReference, feature, monitor);
					if (result.getResultCode() == JarVerification.CANCEL_INSTALL) {
						throw newCoreException(Policy.bind("JarVerificationService.CancelInstall"), //$NON-NLS-1$
						result.getResultException());
					}
					if (result.getResultCode() == JarVerification.ERROR_INSTALL) {
						throw newCoreException(Policy.bind("JarVerificationService.UnsucessfulVerification"), //$NON-NLS-1$
						result.getResultException());
					}
				}
			}
	}

	/**
	 * 
	 */
	private CoreException newCoreException(String s, Throwable e) throws CoreException {
		String id = UpdateManagerPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
		return new CoreException(new Status(IStatus.ERROR, id, 0, s, e));
	}

	private boolean alreadyValidated() {

		if (trustedCertificates != null) {
			// check if this is not a user accepted certificate for this feature
			Iterator iter = trustedCertificates.iterator();
			while (iter.hasNext()) {
				CertificatePair trustedCertificate = (CertificatePair) iter.next();
				CertificatePair[] pairs = getResult().getRootCertificates();
				for (int i = 0; i < pairs.length; i++) {
					if (trustedCertificate.equals(pairs[i])) {
						getResult().setResultCode(JarVerification.OK_TO_INSTALL);
						return true;
					}
				}
			}
		}
		
		return false;
	}

	private void addTrustedCertificate(CertificatePair pair) {
		if (trustedCertificates == null)
			trustedCertificates = new ArrayList();
		if (pair != null)
			trustedCertificates.add(pair);
	}

	private List getTrustedCertificates() {
		if (trustedCertificates == null)
			trustedCertificates = new ArrayList();
		return trustedCertificates;
	}

}