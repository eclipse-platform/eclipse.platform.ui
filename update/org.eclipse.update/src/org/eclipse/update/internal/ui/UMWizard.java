package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Abstract base class for new and updatable products and component wizards.
 */

import java.net.URL;import org.eclipse.core.internal.boot.update.IManifestDescriptor;import org.eclipse.jface.dialogs.MessageDialog;import org.eclipse.jface.resource.JFaceResources;import org.eclipse.jface.viewers.IStructuredSelection;import org.eclipse.jface.wizard.Wizard;import org.eclipse.swt.SWT;import org.eclipse.swt.layout.GridData;import org.eclipse.swt.widgets.Composite;import org.eclipse.swt.widgets.Label;import org.eclipse.ui.INewWizard;import org.eclipse.ui.IWorkbench;import org.eclipse.update.internal.core.UpdateManager;import org.eclipse.update.internal.core.UpdateManagerException;import org.eclipse.update.internal.core.UpdateManagerStrings;

public abstract class UMWizard extends Wizard implements INewWizard {

	protected UpdateManager _updateManager = null;
	protected UMDialog _dialog = null;
	protected IManifestDescriptor[] _manifestDescriptors = null;
	protected URL _urlInstall = null;
	protected String _strInstallId = null;

	protected final static String _strEmpty = new String();
	/**
	 */
	public UMWizard() {

		// Progress Monitor
		//-----------------
		setNeedsProgressMonitor(true);

		// Update Manager
		//---------------
		_updateManager = new UpdateManager(getShell());

		try {
			_updateManager.initialize();
		}

		catch (UpdateManagerException ex) {
			// Unable to find logs
			//--------------------
			displayMessage("error", UpdateManagerStrings.getString("S_Software_Updates"), UpdateManagerStrings.getString("S_Unable_to_open_error_logs"), null);
		}
	}
	/**
	 */
	public UMWizard(UMDialog dialog, IManifestDescriptor[] manifestDescriptors) {

		_dialog = dialog;
		_manifestDescriptors = manifestDescriptors;

		// Progress Monitor
		//-----------------
		setNeedsProgressMonitor(true);

		// Update Manager
		//---------------
		_updateManager = dialog.getUpdateManager();
	}
	/**
	 * Creates a new label with a bold font.
	 */
	protected static Label createBoldLabel(Composite parent, String text, int iHorizontalSpan) {
		Label label = new Label(parent, SWT.NONE);
		label.setFont(JFaceResources.getBannerFont());
		label.setText(text);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.horizontalSpan = iHorizontalSpan;
		label.setLayoutData(data);
		return label;
	}
	/**
	 * Displays error message of type:
	 * <ul>
	 * <li>error</li>
	 * <li>warning</li>
	 * </ul>
	 * The title and message1 are translated before displaying
	 */
	private void displayMessage(String strMessageType, String strTitle, String strMessage1, String strMessage2) {
		MessageDialog.openError(getShell(), strTitle, strMessage1 + "\n" + strMessage2);
	}
	/**
	 * Initializes this creation wizard using the passed workbench and
	 * object selection.
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		return;
	}
	/**
	 */
	public boolean performCancel() {
		// Update preference page content
		//-------------------------------
		if (_dialog != null)
			_dialog.initializeContent();

		return true;
	}
	/**
	 * Subclasses must implement this <code>IWizard</code> method 
	 * to perform any special finish processing for their wizard.
	 */
	public boolean performFinish() {

		// Update preference page content
		//-------------------------------
		if (_dialog != null)
			_dialog.initializeContent();

		return true;
	}
	/**
	 */
	public void setRestartMessageRequired(boolean bRestart) {
		if (_dialog != null)
			_dialog.setRestartMessageRequired(bRestart);
	}
}