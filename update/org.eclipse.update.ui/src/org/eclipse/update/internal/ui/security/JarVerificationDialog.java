package org.eclipse.update.internal.ui.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.IVerificationResult;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

/**
 * 
 */
public class JarVerificationDialog extends Dialog {
	private IVerificationResult _VerificationResult = null;
	private String _fileName = null;
	private String _strFeatureName = null;
	private String _strId = null;
	private String _strProviderName = null;
	private boolean okToInstall = false;

	private String componentVerified;
	/**
	 *
	 */
	public JarVerificationDialog(
		Shell shell,
		IVerificationResult verificationResult) {
		super(shell);
		_fileName = verificationResult.getContentReference().getIdentifier();
		_VerificationResult = verificationResult;
		_strId = verificationResult.getFeature().getVersionedIdentifier().toString();
		_strFeatureName = verificationResult.getFeature().getLabel();
		_strProviderName = verificationResult.getFeature().getProvider();
		componentVerified =
			(verificationResult.isFeatureVerification()) ? "feature" : "feature file";
		okToInstall = false;
	}

	/**
	 * Add buttons to the dialog's button bar.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		if (_VerificationResult.getVerificationCode()
			!= IVerificationResult.TYPE_ENTRY_CORRUPTED) {

			if (_VerificationResult.isFeatureVerification()) {
				createButton(
					parent,
					IDialogConstants.OK_ID,
					UpdateUIPlugin.getResourceString("JarVerificationDialog.Install"),
					false);
				//$NON-NLS-1$
			} else {
				createButton(
					parent,
					IDialogConstants.OK_ID,
					UpdateUIPlugin.getResourceString("JarVerificationDialog.Continue"),
					false);
				//$NON-NLS-1$				
			}

			// Radio button: Cancel installation
			//----------------------------------
			createButton(
				parent,
				IDialogConstants.CANCEL_ID,
				UpdateUIPlugin.getResourceString("JarVerificationDialog.Cancel"),
				true);
			//$NON-NLS-1$							

		} else {
			createButton(
				parent,
				IDialogConstants.CANCEL_ID,
				UpdateUIPlugin.getResourceString("JarVerificationDialog.Cancel"),
				true);
		}
	}
	/**
	 * Creates and returns the contents of the upper part 
	 * of the dialog (above the button bar).
	 */
	protected Control createDialogArea(Composite compositeParent) {
		getShell().setText(
			UpdateUIPlugin.getResourceString("JarVerificationDialog.Verification"));
		//$NON-NLS-1$
		// Composite: Client
		//------------------
		Composite compositeClient = new Composite(compositeParent, SWT.NULL);
		GridLayout grid = new GridLayout();
		compositeClient.setLayout(grid);
		compositeClient.setLayoutData(new GridData(GridData.FILL_BOTH));
		// Text: Information
		//------------------
		Text textInformation =
			new Text(compositeClient, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
		textInformation.setLayoutData(
			new GridData(GridData.GRAB_VERTICAL | GridData.FILL_HORIZONTAL));
		StringBuffer strb = new StringBuffer();
		switch (_VerificationResult.getVerificationCode()) {
			case IVerificationResult.TYPE_ENTRY_NOT_SIGNED :
				strb.append(
					UpdateUIPlugin.getFormattedMessage(
						"JarVerificationDialog.AboutToInstall",
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n\r\n"); //$NON-NLS-1$
				strb.append(
					UpdateUIPlugin.getFormattedMessage(
						"JarVerificationDialog.NotDigitallySigned",
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(
					UpdateUIPlugin.getFormattedMessage(
						"JarVerificationDialog.CannotVerifyProvider",
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				if (_VerificationResult.isFeatureVerification()) {
					strb.append(
						UpdateUIPlugin.getResourceString("JarVerificationDialog.InstallMayCorrupt"));
					//$NON-NLS-1$
				} else {
					strb.append(
						UpdateUIPlugin.getResourceString("JarVerificationDialog.ContinueMayCorrupt"));
					//$NON-NLS-1$ 					
				}
				textInformation.setText(strb.toString());
				break;
			case IVerificationResult.TYPE_ENTRY_CORRUPTED :
				strb.append(
					UpdateUIPlugin.getFormattedMessage(
						"JarVerificationDialog.CorruptedContent",
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n\r\n"); //$NON-NLS-1$
				strb.append(
					UpdateUIPlugin.getResourceString(
						"JarVerificationDialog.ComponentNotInstalled"));
				//$NON-NLS-1$
				textInformation.setText(strb.toString());
				break;
			case IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED :
				strb.append(
					UpdateUIPlugin.getFormattedMessage(
						"JarVerificationDialog.SignedComponent",
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n\r\n"); //$NON-NLS-1$
				strb.append(
					UpdateUIPlugin.getFormattedMessage(
						"JarVerificationDialog.UnknownCertificate",
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(
					UpdateUIPlugin.getFormattedMessage(
						"JarVerificationDialog.UnableToVerifyProvider",
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				if (_VerificationResult.isFeatureVerification()) {
					strb.append(
						UpdateUIPlugin.getResourceString("JarVerificationDialog.InstallMayCorrupt"));
					//$NON-NLS-1$
				} else {
					strb.append(
						UpdateUIPlugin.getResourceString("JarVerificationDialog.ContinueMayCorrupt"));
					//$NON-NLS-1$ 					
				}
				textInformation.setText(strb.toString());
				break;
			case IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED :
				strb.append(
					UpdateUIPlugin.getFormattedMessage(
						"JarVerificationDialog.SignedComponent",
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n\r\n"); //$NON-NLS-1$
				strb.append(
					UpdateUIPlugin.getFormattedMessage(
						"JarVerificationDialog.KnownCertificate",
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(
					UpdateUIPlugin.getFormattedMessage(
						"JarVerificationDialog.ProviderKnown",
						componentVerified));
				//$NON-NLS-1$
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(
					UpdateUIPlugin.getFormattedMessage(
						"JarVerificationDialog.Caution",
						_strProviderName));
				//$NON-NLS-1$
				textInformation.setText(strb.toString());
				break;
		}
		if (_VerificationResult.getVerificationCode()
			== IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED
			|| _VerificationResult.getVerificationCode()
				== IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED) {
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
		// Feature name
		//---------------
		Label label = null;
		if (_strFeatureName != null && _strFeatureName.length() > 0) {
			label = new Label(compositeInformation, SWT.NULL);
			label.setText(
				UpdateUIPlugin.getResourceString("JarVerificationDialog.FeatureName"));
			//$NON-NLS-1$
			label = new Label(compositeInformation, SWT.NULL);
			label.setText(_strFeatureName);
		}
		// Feature identifier
		//---------------------
		if (_strId != null && _strId.length() > 0) {
			label = new Label(compositeInformation, SWT.NULL);
			label.setText(
				UpdateUIPlugin.getResourceString("JarVerificationDialog.FeatureIdentifier"));
			//$NON-NLS-1$
			label = new Label(compositeInformation, SWT.NULL);
			label.setText(_strId);
		}
		// Provider name
		//--------------
		if (_strProviderName != null && _strProviderName.length() > 0) {
			label = new Label(compositeInformation, SWT.NULL);
			label.setText(
				UpdateUIPlugin.getResourceString("JarVerificationDialog.Provider"));
			//$NON-NLS-1$
			label = new Label(compositeInformation, SWT.NULL);
			label.setText(_strProviderName);
		}
		// Label: File name
		//-----------------
		label = new Label(compositeInformation, SWT.NULL);
		label.setText(
			UpdateUIPlugin.getResourceString("JarVerificationDialog.FileName"));
		//$NON-NLS-1$
		label = new Label(compositeInformation, SWT.NULL);
		label.setText(_fileName);

		if (_VerificationResult.getVerificationCode()
			!= IVerificationResult.TYPE_ENTRY_CORRUPTED) {
			// Group box
			//----------
			Group group = new Group(compositeClient, SWT.NONE);
			group.setLayout(new GridLayout());
			group.setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL));
			// Text: Instruction
			//------------------
			Text textInstruction = new Text(group, SWT.MULTI | SWT.READ_ONLY);
			if (_VerificationResult.isFeatureVerification()) {
				textInstruction.setText(
					UpdateUIPlugin.getResourceString("JarVerificationDialog.MayChooseToInstall"));
				//$NON-NLS-1$
			} else {
				textInstruction.setText(
					UpdateUIPlugin.getResourceString("JarVerificationDialog.MayChooseToContinue"));
				//$NON-NLS-1$ 					
			}
			//$NON-NLS-1$
		}

		return compositeClient;
	}

	private void addCertificateView(Composite compositeClient) {

		// Group box
		//----------
		Group group = new Group(compositeClient, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(
			new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL));
		// Text: Information
		//------------------
		Text textInformation = new Text(group, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
		textInformation.setLayoutData(
			new GridData(GridData.GRAB_VERTICAL | GridData.FILL_HORIZONTAL));
		StringBuffer strb = new StringBuffer();
		if (_VerificationResult.getSignerInfo() != null) {
			strb.append(_VerificationResult.getSignerInfo());
		}
		if (_VerificationResult.getVerifierInfo() != null) {
			strb.append("\r\n\r\n"); //$NON-NLS-1$
			strb.append(_VerificationResult.getVerifierInfo());
		}
		textInformation.setText(strb.toString());
	}
}