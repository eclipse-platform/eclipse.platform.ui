/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.security;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.*;
import org.eclipse.update.core.IVerificationResult;
import org.eclipse.update.internal.ui.UpdateUIMessages;
import org.eclipse.update.internal.ui.wizards.BannerPage;

/**
 * 
 */
public class JarVerificationPage extends BannerPage {

	private IVerificationResult _VerificationResult = null;
	private String _fileName = null;
	private String _strFeatureName = null;
	private String _strId = null;
	private String _strProviderName = null;
	private TitleAreaDialog _Dialog;

	/*
	 * Constructor for JarVerificationPage.
	 */
	public JarVerificationPage(IVerificationResult verificationResult) {
		super(UpdateUIMessages.JarVerificationDialog_Verification); 
		_fileName = verificationResult.getContentReference().getIdentifier();
		_VerificationResult = verificationResult;
		_strId = verificationResult.getFeature().getVersionedIdentifier().toString();
		_strFeatureName = verificationResult.getFeature().getLabel();
		_strProviderName = verificationResult.getFeature().getProvider();
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createContents(Composite compositeParent) {
        PlatformUI.getWorkbench().getHelpSystem().setHelp(compositeParent, "org.eclipse.update.ui.JarVerificationPage"); //$NON-NLS-1$
		// Composite: Client
		//------------------
		Composite compositeClient = new Composite(compositeParent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		compositeClient.setLayout(layout);
		compositeClient.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Text Information
		//------------------		
		createTextArea(compositeClient);

		// Certificate Area
		//------------------
		createCertificateArea(compositeClient);

		// File and Feature Information
		//-----------------------------		
		createInformationArea(compositeClient);

		// Choice Area
		//------------		
		//createChoiceArea(compositeClient);

		Dialog.applyDialogFont(compositeParent);
		return compositeClient;

	}

	/*
	 * Creates the Information text
	 */
	private void createTextArea(Composite compositeClient) {

		// Label: Information
		//------------------
		Label labelInformation =
			new Label(compositeClient, SWT.WRAP);
		labelInformation.setLayoutData(
			new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));

        String actionMsg = null;
        if (_VerificationResult.isFeatureVerification()) {
            actionMsg = UpdateUIMessages.JarVerificationDialog_MayChooseToInstall; 
        } else {
            actionMsg = UpdateUIMessages.JarVerificationDialog_MayChooseToContinue; 
        }
        
        StringBuffer strb = new StringBuffer();
		switch (_VerificationResult.getVerificationCode()) {

			case IVerificationResult.TYPE_ENTRY_NOT_SIGNED :
				String msg = (_VerificationResult.isFeatureVerification()?
					UpdateUIMessages.JarVerificationDialog_AboutToInstall_Feature:
						UpdateUIMessages.JarVerificationDialog_AboutToInstall_File)+
                        "\r\n" + actionMsg; //$NON-NLS-1$
				setMessage(msg, WARNING);
				strb.append(_VerificationResult.isFeatureVerification()?
							UpdateUIMessages.JarVerificationDialog_NotDigitallySigned_Feature:
								UpdateUIMessages.JarVerificationDialog_NotDigitallySigned_File);
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(_VerificationResult.isFeatureVerification()?
						UpdateUIMessages.JarVerificationDialog_CannotVerifyProvider_Feature:
							UpdateUIMessages.JarVerificationDialog_CannotVerifyProvider_File);
                
/*				strb.append("\r\n"); //$NON-NLS-1$
				if (_VerificationResult.isFeatureVerification()) {
					strb.append(
						UpdateUI.getResourceString("JarVerificationDialog.InstallMayCorrupt"));//$NON-NLS-1$
				} else {
					strb.append(
						UpdateUI.getResourceString("JarVerificationDialog.ContinueMayCorrupt"));//$NON-NLS-1$ 					
				}
				*/
				labelInformation.setText(strb.toString());
				break;

			case IVerificationResult.TYPE_ENTRY_CORRUPTED :
				msg = _VerificationResult.isFeatureVerification()?
							UpdateUIMessages.JarVerificationDialog_CorruptedContent_Feature:
								UpdateUIMessages.JarVerificationDialog_CorruptedContent_File;
				setMessage(msg, ERROR);
				strb.append(
					UpdateUIMessages.JarVerificationDialog_ComponentNotInstalled); 
				labelInformation.setText(strb.toString());
				break;

			case IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED :
				msg = (_VerificationResult.isFeatureVerification()?
						UpdateUIMessages.JarVerificationDialog_SignedComponent_Feature:
							UpdateUIMessages.JarVerificationDialog_SignedComponent_Feature) +  
                        "\r\n" + actionMsg; //$NON-NLS-1$
                
				setMessage(msg, WARNING);
				strb.append(_VerificationResult.isFeatureVerification()?
					UpdateUIMessages.JarVerificationDialog_UnknownCertificate_Feature:
						UpdateUIMessages.JarVerificationDialog_UnknownCertificate_File);
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(_VerificationResult.isFeatureVerification()?
						UpdateUIMessages.JarVerificationDialog_UnableToVerifyProvider_Feature:
							UpdateUIMessages.JarVerificationDialog_UnableToVerifyProvider_File);
/*				strb.append("\r\n"); //$NON-NLS-1$
				if (_VerificationResult.isFeatureVerification()) {
					strb.append(
						UpdateUI.getResourceString("JarVerificationDialog.InstallMayCorrupt")); //$NON-NLS-1$
				} else {
					strb.append(
						UpdateUI.getResourceString("JarVerificationDialog.ContinueMayCorrupt"));//$NON-NLS-1$ 					
				}
				*/
				labelInformation.setText(strb.toString());
				break;

			case IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED :
				msg = (_VerificationResult.isFeatureVerification()?
						UpdateUIMessages.JarVerificationDialog_SignedComponent_Feature:
							UpdateUIMessages.JarVerificationDialog_SignedComponent_File) +
                        "\r\n" + actionMsg; //$NON-NLS-1$
				setMessage(msg, WARNING);
				strb.append(_VerificationResult.isFeatureVerification()?
					UpdateUIMessages.JarVerificationDialog_KnownCertificate_Feature:
						UpdateUIMessages.JarVerificationDialog_KnownCertificate_File);
				strb.append("\r\n"); //$NON-NLS-1$
				strb.append(_VerificationResult.isFeatureVerification()?
					UpdateUIMessages.JarVerificationDialog_ProviderKnown_Feature:
						UpdateUIMessages.JarVerificationDialog_ProviderKnown_File);
				strb.append("\r\n"); //$NON-NLS-1$

				labelInformation.setText(strb.toString());

//				createCautionArea(compositeClient);
				break;
		}
	}
	

	/*
	 * Presents File & Feature information
	 */
	private void createInformationArea(Composite compositeClient) {

		// Composite: Information labels
		//------------------------------
		Composite compositeInformation = new Composite(compositeClient, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = layout.marginHeight = 0;
		compositeInformation.setLayout(layout);
		compositeInformation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// get bold face
		FontRegistry fregistry = JFaceResources.getFontRegistry();
		Font boldFont = fregistry.getBold(JFaceResources.DIALOG_FONT);
		
		// Feature name
		//---------------
		Label keyLabel = null;
		CLabel valueLabel = null;
		if (_strFeatureName != null && _strFeatureName.length() > 0) {
			keyLabel = new Label(compositeInformation, SWT.NULL);
			keyLabel.setText(
				UpdateUIMessages.JarVerificationDialog_FeatureName); 

			valueLabel = new CLabel(compositeInformation, SWT.NULL);
			valueLabel.setFont(boldFont);
			valueLabel.setText(_strFeatureName);
			valueLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		// Feature identifier
		//---------------------
		if (_strId != null && _strId.length() > 0) {
			keyLabel = new Label(compositeInformation, SWT.NULL);
			keyLabel.setText(
				UpdateUIMessages.JarVerificationDialog_FeatureIdentifier); 

			valueLabel = new CLabel(compositeInformation, SWT.NULL);
			valueLabel.setFont(boldFont);
			valueLabel.setText(_strId);
			valueLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		// Provider name
		//--------------
		if (_strProviderName != null && _strProviderName.length() > 0) {
			keyLabel = new Label(compositeInformation, SWT.NULL);
			keyLabel.setText(
				UpdateUIMessages.JarVerificationDialog_Provider); 

			valueLabel = new CLabel(compositeInformation, SWT.NULL);
			valueLabel.setFont(boldFont);
			valueLabel.setText(_strProviderName);
			valueLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		// Label: File name
		//-----------------
		keyLabel = new Label(compositeInformation, SWT.NULL);
		keyLabel.setText(
			UpdateUIMessages.JarVerificationDialog_FileName); 

		valueLabel = new CLabel(compositeInformation, SWT.NULL);
		valueLabel.setFont(boldFont);
		valueLabel.setText(_fileName);
		valueLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	/*
	 * Show certificate information
	 */
	private void createCertificateArea(Composite compositeClient) {

		if (_VerificationResult.getVerificationCode()
			== IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED
			|| _VerificationResult.getVerificationCode()
				== IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED) {
			// Group box
			//----------
			Group group = new Group(compositeClient, SWT.SHADOW_ETCHED_IN);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginWidth = layout.marginHeight = 0;
			group.setLayout(layout);
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			group.setText(UpdateUIMessages.JarVerificationDialog_CertificateInfo); 

			// Signer
			//-------------------
			Label keyLabel = null;
			Text valueText = null;
			//data = new GridData(GridData.FILL_HORIZONTAL);
			//data.horizontalIndent = 0;
			//textInformation.setLayoutData(data);			
			if (_VerificationResult.getSignerInfo() != null) {
				keyLabel = new Label(group, SWT.NULL);
				keyLabel.setText(UpdateUIMessages.JarVerificationDialog_SubjectCA); 
				keyLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

				valueText = new Text(group, SWT.MULTI|SWT.BORDER|SWT.WRAP|SWT.V_SCROLL);
				valueText.setText(_VerificationResult.getSignerInfo());
				valueText.setEditable(false);
				valueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}
			
			// Authenticator
			//---------------------
			if (_VerificationResult.getVerifierInfo() != null) {
				keyLabel = new Label(group, SWT.NULL);
				keyLabel.setText(UpdateUIMessages.JarVerificationDialog_RootCA); 
				keyLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));				

				valueText = new Text(group, SWT.MULTI|SWT.BORDER|SWT.WRAP|SWT.V_SCROLL);
				valueText.setText(_VerificationResult.getVerifierInfo());
				valueText.setEditable(false);
				valueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}
		}
	}

	/*
	 * Sets the Dialog
	 */
	public void setTitleAreaDialog(TitleAreaDialog dialog) {
		_Dialog = dialog;
	}

	/*
	 * 
	 */
	public void setMessage(String newMessage, int newType) {
		super.setMessage(newMessage, newType);
		if (_Dialog != null) {
			_Dialog.setMessage(newMessage, newType);
		}
	}

}
