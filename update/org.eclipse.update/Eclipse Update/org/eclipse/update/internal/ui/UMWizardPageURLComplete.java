package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Presents status of just completed product and component installations
 */
import org.eclipse.jface.wizard.WizardPage;import org.eclipse.swt.SWT;import org.eclipse.swt.layout.GridData;import org.eclipse.swt.layout.GridLayout;import org.eclipse.swt.widgets.Composite;import org.eclipse.swt.widgets.Text;import org.eclipse.update.internal.core.UMSessionManagerSession;import org.eclipse.update.internal.core.UpdateManagerStrings;

public class UMWizardPageURLComplete extends WizardPage {
	protected UMWizard _wizard = null;
	protected Text _textArea = null;
	/**
	 *
	 */
	public UMWizardPageURLComplete(UMWizard wizard, String strName) {
		super(strName);
		_wizard = wizard;

		this.setTitle(UpdateManagerStrings.getString("S_Install_Components"));
		this.setDescription(UpdateManagerStrings.getString("S_Installation_completed"));

		setPageComplete(false);
	}
	/**
	 *
	 */
	public void createControl(Composite compositeParent) {
		// Content
		//--------
		Composite compositeContent = new Composite(compositeParent, SWT.NULL);

		GridLayout layout = new GridLayout();
		compositeContent.setLayout(layout);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		compositeContent.setLayoutData(gridData);

		_textArea = new Text(compositeContent, SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		_textArea.setLayoutData(gridData);

		setControl(compositeContent);
	}
	/**
	 *
	 */
	public void initializeContent() {

		// Do the update/install operations
		//---------------------------------
		UMWizardPageURLInstalling pageInstalling = (UMWizardPageURLInstalling) _wizard.getPage("installing");

		if (pageInstalling != null) {

			pageInstalling.doInstalls();

			// Display the status
			//-------------------
			UMSessionManagerSession session = pageInstalling.getSession();

			if (_textArea != null && session != null)
				_textArea.setText(session.getStatusString());

			// Do not allow going back to a previous page
			// All previous information may be out of date
			//--------------------------------------------
			this.setPreviousPage(this);
		}
	}
	/**
	 * 
	 */
	public void setVisible(boolean bVisible) {

		// Do installations before setting this page visible
		//--------------------------------------------------
		if (bVisible == true) {
			initializeContent();
		}

		setPageComplete(bVisible);

		super.setVisible(bVisible);
	}
}