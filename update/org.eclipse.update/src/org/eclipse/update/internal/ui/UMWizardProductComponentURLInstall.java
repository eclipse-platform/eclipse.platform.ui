package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * The new product and component updates wizard
 */
import java.net.URL;import org.eclipse.update.internal.core.UpdateManagerStrings;

public class UMWizardProductComponentURLInstall extends UMWizard {
	protected UMWizardPageURLInstall _pageInstall = null;
	protected UMWizardPageURLInstallable _pageInstallable = null;
	protected UMWizardPageURLInstalling _pageInstalling = null;
	protected UMWizardPageURLComplete _pageComplete = null;

	protected UMApplicationUserInterface _application = null;
	/**
	 *
	 */
	public UMWizardProductComponentURLInstall(UMApplicationUserInterface application) {
		super();
		_application = application;

		setWindowTitle(UpdateManagerStrings.getString("S_Installing"));
	}
	/**
	 * 
	 */
	public void addPages() {

		// Create the wizard pages
		//------------------------
		_pageInstall = new UMWizardPageURLInstall(this, "install");
		_pageInstallable = new UMWizardPageURLInstallable(this, "installable");
		_pageInstalling = new UMWizardPageURLInstalling(this, "installing");
		_pageComplete = new UMWizardPageURLComplete(this, "complete");

		addPage(_pageInstall);
		addPage(_pageInstallable);
		addPage(_pageInstalling);
		addPage(_pageComplete);
	}
	/**
	 */
	public boolean performCancel() {
		if (_application != null)
			_application.stopEventLoop();

		return true;
	}
	/**
	 */
	public boolean performFinish() {
		if (_application != null)
			_application.stopEventLoop();

		return true;
	}
	/**
	 */
	public void setInstallID(String strInstallId) {
		_strInstallId = strInstallId;
	}
	/**
	 */
	public void setInstallURL(URL urlInstall) {
		_urlInstall = urlInstall;
	}
}