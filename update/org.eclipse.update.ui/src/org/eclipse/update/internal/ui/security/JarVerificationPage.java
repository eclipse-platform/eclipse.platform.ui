/**
 * Created on Apr 25, 2002
 *
 * To change this generated comment edit the template variable "filecomment":
 * Workbench>Preferences>Java>Templates.
 */
package org.eclipse.update.internal.ui.security;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.update.core.IVerificationResult;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.UpdateUIPluginImages;
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
	private boolean okToInstall = false;
	private String componentVerified;	

	
	/**
	 * Constructor for JarVerificationDialog.
	 * @param parentShell
	 * @param newWizard
	 */
	public JarVerificationPage(IVerificationResult verificationResult) {
		super(UpdateUIPlugin.getResourceString("JarVerificationDialog.Verification"));
		_fileName = verificationResult.getContentReference().getIdentifier();
		_VerificationResult = verificationResult;
		_strId = verificationResult.getFeature().getVersionedIdentifier().toString();
		_strFeatureName = verificationResult.getFeature().getLabel();
		_strProviderName = verificationResult.getFeature().getProvider();
		componentVerified =
			(verificationResult.isFeatureVerification()) ? "feature" : "feature file";
		okToInstall = false;
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createContents(Composite compositeParent) {

		// Composite: Client
		//------------------
		Composite compositeClient = new Composite(compositeParent, SWT.NULL);
		GridLayout grid = new GridLayout();
		compositeClient.setLayout(grid);
		compositeClient.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Text Information
		//------------------		
		createTextArea(compositeClient);

		// Certificate Area
		//------------------
		if (_VerificationResult.getVerificationCode()
			== IVerificationResult.TYPE_ENTRY_SIGNED_UNRECOGNIZED
			|| _VerificationResult.getVerificationCode()
				== IVerificationResult.TYPE_ENTRY_SIGNED_RECOGNIZED) {
			createCertificateArea(compositeClient);
		}

		// File and Feature Information
		//-----------------------------		
		createInformationArea(compositeClient);

		// Choice Area
		//------------		
		if (_VerificationResult.getVerificationCode()
			!= IVerificationResult.TYPE_ENTRY_CORRUPTED) {

			// Text: Instruction
			//------------------
			Text textInstruction = new Text(compositeClient, SWT.MULTI | SWT.READ_ONLY);
			textInstruction.setLayoutData(
				new GridData(
					GridData.VERTICAL_ALIGN_BEGINNING
						| GridData.GRAB_VERTICAL
						| GridData.FILL_HORIZONTAL));
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

	/*
	 * Creates the Information text
	 */
	private void createTextArea(Composite compositeClient) {
		//
		// Text: Information
		//------------------
		Text textInformation =
			new Text(compositeClient, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
		textInformation.setLayoutData(
			new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
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
		layout.verticalSpacing = 0;
		compositeInformation.setLayout(layout);
		compositeInformation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Feature name
		//---------------
		Label label = null;
		if (_strFeatureName != null && _strFeatureName.length() > 0) {
			label = new Label(compositeInformation, SWT.NULL);
			label.setText(
				UpdateUIPlugin.getResourceString("JarVerificationDialog.FeatureName"));
			//$NON-NLS-1$
			label = new Label(compositeInformation, SWT.NULL);
			label.setFont(JFaceResources.getBannerFont());
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
			label.setFont(JFaceResources.getBannerFont());				
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
			label.setFont(JFaceResources.getBannerFont());							
			label.setText(_strProviderName);
		}
		// Label: File name
		//-----------------
		label = new Label(compositeInformation, SWT.NULL);
		label.setText(
			UpdateUIPlugin.getResourceString("JarVerificationDialog.FileName"));
		//$NON-NLS-1$
		label = new Label(compositeInformation, SWT.NULL);
		label.setFont(JFaceResources.getBannerFont());						
		label.setText(_fileName);
	}

	/*
	 * Show certificate information
	 */
	private void createCertificateArea(Composite compositeClient) {

		// Group box
		//----------
		Group group = new Group(compositeClient, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// Certificate Text
		//-------------------
		Text textInformation = new Text(group, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
		textInformation.setLayoutData(new GridData(GridData.FILL_BOTH));
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