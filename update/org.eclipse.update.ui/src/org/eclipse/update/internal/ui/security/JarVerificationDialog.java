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
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.Policy;
import org.eclipse.update.internal.security.CertificatePair;
import sun.security.x509.X500Name;

public class JarVerificationDialog extends Dialog {
	private IVerificationResult _VerificationResult = null;
	private String _fileName = null;
	private String _strComponentName = null;
	private String _strId = null;
	private String _strProviderName = null;
	private Button _buttonInstall;
	private Button _buttonCancel;
	private Button _buttonTrustCertificate;
	private boolean okToInstall = false;
	/**
	 *
	 */
	public JarVerificationDialog(Shell shell,  IVerificationResult verificationResult) {
		super(shell);
		_fileName = verificationResult.getContentReference().getIdentifier();
		_VerificationResult = verificationResult;
		_strId = verificationResult.getFeature().getVersionedIdentifier().toString();
		_strComponentName =verificationResult.getFeature().getLabel();
		_strProviderName = verificationResult.getFeature().getProvider();
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
			case IVerificationResult.TYPE_ENTRY_NOT_SIGNED :
				strb.append(Policy.bind("JarVerificationDialog.AboutToInstall")); //$NON-NLS-1$
				strb.append("\r\n\r\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.NotDigitallySigned")); //$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.CannotVerifyProvider")); //$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.MayCorrupt")); //$NON-NLS-1$ 
				textInformation.setText(strb.toString());
				break;
			case IVerificationResult.TYPE_ENTRY_CORRUPTED :
				strb.append(Policy.bind("JarVerificationDialog.CorruptedContent")); //$NON-NLS-1$
				strb.append("\r\n\r\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.ComponentNotInstalled")); //$NON-NLS-1$
				textInformation.setText(strb.toString());
				break;
			case IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED :
				strb.append(Policy.bind("JarVerificationDialog.SignedComponent")); //$NON-NLS-1$
				strb.append("\r\n\r\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.UnknownCertificate")); //$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.UnableToVerifyProvider")); //$NON-NLS-1$ 
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.MayCorrupt")); //$NON-NLS-1$ 
				textInformation.setText(strb.toString());
				break;
			case IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED :
				strb.append(Policy.bind("JarVerificationDialog.SignedComponent")); //$NON-NLS-1$
				strb.append("\r\n\r\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.KnownCertificate")); //$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.ProviderKnown")); //$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(Policy.bind("JarVerificationDialog.Caution",_strProviderName)); //$NON-NLS-1$
				textInformation.setText(strb.toString());
				break;
		}
		if (_VerificationResult.getVerificationCode() == IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED  || _VerificationResult.getVerificationCode() == IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED) {
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
		if (_VerificationResult.getVerificationCode() != IVerificationResult.TYPE_ENTRY_CORRUPTED) {
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
		if (_VerificationResult.getSignerInfo()!=null) {
			strb.append(_VerificationResult.getSignerInfo());
		}
		if (_VerificationResult.getVerifierInfo()!=null) {		
			strb.append("\r\n\r\n"); //$NON-NLS-1$
			strb.append(_VerificationResult.getVerifierInfo());			
		}
		textInformation.setText(strb.toString());
	}
}