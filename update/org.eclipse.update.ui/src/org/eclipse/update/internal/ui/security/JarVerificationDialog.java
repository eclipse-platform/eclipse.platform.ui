package org.eclipse.update.internal.ui.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.security.Principal;
import java.security.cert.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.JarContentReference;
import org.eclipse.update.internal.core.Policy;
import org.eclipse.update.internal.security.*;
import sun.security.x509.X500Name;

public class JarVerificationDialog extends Dialog {
	private JarVerificationResult _VerificationResult = null;
	private String _fileName = null;
	private String _strComponentName = null;
	private String _strId = null;
	private String _strProviderName = null;
	private Button _buttonInstall;
	private Button _buttonCancel;
	private Button _buttonTrustCertificate;
	private CertificatePair trustedCertificate = null;
	private boolean okToInstall = false;
	/**
	 *
	 */
	public JarVerificationDialog(Shell shell, JarContentReference jarReference, IFeature feature, JarVerificationResult VerificationResult) {
		super(shell);
		_fileName = jarReference.getIdentifier();
		_VerificationResult = VerificationResult;
		_strId = feature.getVersionedIdentifier().toString();
		_strComponentName = feature.getLabel();
		_strProviderName = feature.getProvider();
		trustedCertificate = null;
		okToInstall = false;
	}
	public boolean close() {
		okToInstall = (_buttonInstall != null ? _buttonInstall.getSelection() : false);
		return super.close();
	}
	/**
	 * Add buttons to the dialog's button bar.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
	/**
	 * Creates and returns the contents of the upper part 
	 * of the dialog (above the button bar).
	 */
	protected Control createDialogArea(Composite compositeParent) {
		getShell().setText(Policy.bind("JarVerificationDialog.Verification")); //$NON-NLS-1$
		// Composite: Client
		//------------------
		Composite compositeClient = new Composite(compositeParent, SWT.NULL);
		GridLayout grid = new GridLayout();
		compositeClient.setLayout(grid);
		compositeClient.setLayoutData(new GridData(GridData.FILL_BOTH));
		// Text: Information
		//------------------
		Text textInformation = new Text(compositeClient, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
		textInformation.setLayoutData(new GridData(GridData.GRAB_VERTICAL | GridData.FILL_HORIZONTAL));
		StringBuffer strb = new StringBuffer();
		switch (_VerificationResult.getVerificationCode()) {
			case JarVerification.JAR_NOT_SIGNED :
				strb.append(Policy.bind("JarVerificationDialog.AboutToInstall")); //$NON-NLS-1$
				strb.append("\n\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.NotDigitallySigned")); //$NON-NLS-1$
				strb.append("\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.CannotVerifyProvider")); //$NON-NLS-1$
				strb.append("\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.MayCorrupt")); //$NON-NLS-1$ 
				textInformation.setText(strb.toString());
				break;
			case JarVerification.JAR_CORRUPTED :
				strb.append(Policy.bind("JarVerificationDialog.CorruptedContent")); //$NON-NLS-1$
				strb.append("\n\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.ComponentNotInstalled")); //$NON-NLS-1$
				textInformation.setText(strb.toString());
				break;
			case JarVerification.JAR_INTEGRITY_VERIFIED :
				strb.append(Policy.bind("JarVerificationDialog.SignedComponent")); //$NON-NLS-1$
				strb.append("\n\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.UnknownCertificate")); //$NON-NLS-1$
				strb.append("\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.UnableToVerifyProvider")); //$NON-NLS-1$ 
				strb.append("\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.MayCorrupt")); //$NON-NLS-1$ 
				textInformation.setText(strb.toString());
				break;
			case JarVerification.JAR_SOURCE_VERIFIED :
				strb.append(Policy.bind("JarVerificationDialog.SignedComponent")); //$NON-NLS-1$
				strb.append("\n\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.KnownCertificate")); //$NON-NLS-1$
				strb.append("\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.ProviderKnown")); //$NON-NLS-1$
				strb.append("\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.Caution")); //$NON-NLS-1$
				textInformation.setText(strb.toString());
				break;
		}
		if (_VerificationResult.getVerificationCode() == JarVerification.JAR_INTEGRITY_VERIFIED || _VerificationResult.getVerificationCode() == JarVerification.JAR_SOURCE_VERIFIED) {
			addCertificateView(compositeClient);
		}
		// Composite: Information labels
		//------------------------------
		Composite compositeInformation = new Composite(compositeClient, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.verticalSpacing = 0;
		compositeInformation.setLayout(layout);
		compositeInformation.setLayoutData(new GridData(GridData.FILL_BOTH));
		// Label: File name
		//-----------------
		Label label = new Label(compositeInformation, SWT.NULL);
		label.setText(Policy.bind("JarVerificationDialog.FileName")); //$NON-NLS-1$ 
		label = new Label(compositeInformation, SWT.NULL);
		label.setText(_fileName);
		// Component name
		//---------------
		if (_strComponentName != null && _strComponentName.length() > 0) {
			label = new Label(compositeInformation, SWT.NULL);
			label.setText(Policy.bind("JarVerificationDialog.FeatureName")); //$NON-NLS-1$ 
			label = new Label(compositeInformation, SWT.NULL);
			label.setText(_strComponentName);
		}
		// Component identifier
		//---------------------
		if (_strId != null && _strId.length() > 0) {
			label = new Label(compositeInformation, SWT.NULL);
			label.setText(Policy.bind("JarVerificationDialog.FeatureIdentifier")); //$NON-NLS-1$
			label = new Label(compositeInformation, SWT.NULL);
			label.setText(_strId);
		}
		// Provider name
		//--------------
		if (_strProviderName != null && _strProviderName.length() > 0) {
			label = new Label(compositeInformation, SWT.NULL);
			label.setText(Policy.bind("JarVerificationDialog.Provider")); //$NON-NLS-1$ 
			label = new Label(compositeInformation, SWT.NULL);
			label.setText(_strProviderName);
		}
		if (_VerificationResult.getVerificationCode() != JarVerification.JAR_CORRUPTED) {
			// Group box
			//----------
			Group group = new Group(compositeClient, SWT.NONE);
			group.setLayout(new GridLayout());
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL));
			// Text: Instruction
			//------------------
			Text textInstruction = new Text(group, SWT.MULTI | SWT.READ_ONLY);
			textInstruction.setText(Policy.bind("JarVerificationDialog.MayChooseToInstall"));
			//$NON-NLS-1$
			// Radio button: Install
			//----------------------
			_buttonInstall = new Button(group, SWT.RADIO);
			_buttonInstall.setText(Policy.bind("JarVerificationDialog.Install")); //$NON-NLS-1$
			_buttonInstall.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			// Radio button: Cancel installation
			//----------------------------------
			_buttonCancel = new Button(group, SWT.RADIO);
			_buttonCancel.setText(Policy.bind("JarVerificationDialog.Cancel")); //$NON-NLS-1$
			_buttonCancel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			_buttonCancel.setSelection(true);
		}
		return compositeClient;
	}
	/**
	 * Returns true if the component is to be installed
	 * called by the Wizard when Finish is executed.
	 */
	public boolean okToInstall() {
		return okToInstall;
	}

	private void addCertificateView(Composite compositeClient) {
		X509Certificate certRoot = null;
		X509Certificate certIssuer = null;
		if (_VerificationResult.getFoundCertificate() == null) {
			CertificatePair[] certs = _VerificationResult.getRootCertificates();
			if (certs.length == 0)
				return;
			trustedCertificate = (CertificatePair) certs[0];
		} else {
			trustedCertificate = (CertificatePair) _VerificationResult.getFoundCertificate();
		}
		certRoot = (X509Certificate) trustedCertificate.getRoot();
		certIssuer = (X509Certificate) trustedCertificate.getIssuer();

		// Group box
		//----------
		Group group = new Group(compositeClient, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL));

		// Text: Information
		//------------------
		Text textInformation = new Text(group, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
		textInformation.setLayoutData(new GridData(GridData.GRAB_VERTICAL | GridData.FILL_HORIZONTAL));
		StringBuffer strb = new StringBuffer();
		strb.append(Policy.bind("JarVerificationDialog.SubjectCA")); //$NON-NLS-1$
		strb.append("\n"); //$NON-NLS-1$
		strb.append(Policy.bind("JarVerificationDialog.CAIssuer", issuerString(certIssuer.getSubjectDN()))); //$NON-NLS-1$
		strb.append("\n"); //$NON-NLS-1$
		strb.append(Policy.bind("JarVerificationDialog.ValidBetween", dateString(certIssuer.getNotBefore()), dateString(certIssuer.getNotAfter()))); //$NON-NLS-1$
		strb.append(checkValidity(certIssuer));
		if (certIssuer != null && !certIssuer.equals(certRoot)) {
			strb.append("\n\n"); //$NON-NLS-1$				
			strb.append(Policy.bind("JarVerificationDialog.RootCA")); //$NON-NLS-1$
			strb.append("\n"); //$NON-NLS-1$
			strb.append(Policy.bind("JarVerificationDialog.CAIssuer", issuerString(certIssuer.getIssuerDN()))); //$NON-NLS-1$
			strb.append("\n"); //$NON-NLS-1$
			strb.append(Policy.bind("JarVerificationDialog.ValidBetween", dateString(certRoot.getNotBefore()), dateString(certRoot.getNotAfter()))); //$NON-NLS-1$ 
			strb.append(checkValidity(certRoot));
			strb.append("\n\n"); //$NON-NLS-1$	
		}
		textInformation.setText(strb.toString());

	}

	private String checkValidity(X509Certificate cert) {

		try {
			cert.checkValidity();
		} catch (CertificateExpiredException e) {
			return ("\n" + Policy.bind("JarVerificationDialog.ExpiredCertificate")); //$NON-NLS-1$ 
		} catch (CertificateNotYetValidException e) {
			return ("\n" + Policy.bind("JarVerificationDialog.CertificateNotYetValid")); //$NON-NLS-1$ 
		}
		return ("\n" + Policy.bind("JarVerificationDialog.CertificateValid")); //$NON-NLS-1$
	}

	/**
	 * Gets the trustedCertificate.
	 * @return Returns a CertificatePair
	 */
	public CertificatePair getCertificate() {
		return trustedCertificate;
	}

	private String issuerString(Principal principal) {
		try {
			if (principal instanceof X500Name) {
				String issuerString = "";
				X500Name name = (X500Name) principal;
				issuerString += (name.getDNQualifier() != null) ? name.getDNQualifier() + ",1" : "";
				issuerString += name.getCommonName();
				issuerString += (name.getOrganizationalUnit() != null) ? "," + name.getOrganizationalUnit() : "";
				issuerString += (name.getOrganization() != null) ? "," + name.getOrganization() : "";
				issuerString += (name.getLocality() != null) ? "," + name.getLocality() : "";
				issuerString += (name.getCountry() != null) ? "," + name.getCountry() : "";
				return issuerString;
			}
		} catch (Exception e) {
			// FIXME should log
		}
		return principal.toString();
	}

	private String dateString(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM d, yyyyy");
		return formatter.format(date);
	}
}