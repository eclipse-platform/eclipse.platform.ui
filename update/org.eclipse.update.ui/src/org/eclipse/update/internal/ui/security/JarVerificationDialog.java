package org.eclipse.update.internal.ui.security;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.IVerificationResult;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.UpdateUIPluginImages;

/**
 * 
 */
public class JarVerificationDialog extends TitleAreaDialog {

	private IVerificationResult _VerificationResult = null;
	private IDialogPage _DialogPage;
	private Composite pageContainer;
	private Image defaultImage = null;
	private ImageDescriptor defaultImageDescriptor =
		UpdateUIPluginImages.DESC_INSTALL_WIZ;
	
	/**
	 * Constructor for JarVerificationDialog.
	 * @param parentShell
	 * @param newWizard
	 */
	public JarVerificationDialog(Shell parentShell,IDialogPage dialogPage, IVerificationResult verificationResult) {
		super(parentShell);
		setShellStyle(SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);		
		_VerificationResult = verificationResult;
		_DialogPage = dialogPage;
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
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite compositeParent = (Composite)super.createDialogArea(parent);
		setTitleImage(this.getImage());
		setTitle("Feature Verification");
		
		_DialogPage.createControl(compositeParent);
		pageContainer=(Composite)_DialogPage.getControl();
		GridData gd = new GridData(GridData.FILL_BOTH);
		pageContainer.setLayoutData(gd);
		pageContainer.setFont(parent.getFont());		
		
		// Build the separator line
		Label separator= new Label(compositeParent, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return compositeParent;
	}
	
		/**
	 * @see IDialogPage#getImage()
	 */
	public Image getImage() {
		if (defaultImage == null)
			defaultImage = defaultImageDescriptor.createImage();

		return defaultImage;
	}

	public boolean close() {
		// dispose of image
		if (defaultImage != null) {
			defaultImage.dispose();
			defaultImage = null;
		}
		return super.close();		
	}
}
