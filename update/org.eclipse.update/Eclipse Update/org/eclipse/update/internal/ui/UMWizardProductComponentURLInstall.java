package org.eclipse.update.internal.ui;

/**
 * The new product and component updates wizard
 */
import java.net.URL;
import org.eclipse.update.internal.core.*;

public class UMWizardProductComponentURLInstall extends UMWizard
{
	protected UMWizardPageURLInstall          _pageInstall             = null;
	protected UMWizardPageURLInstallable      _pageInstallable         = null;
	protected UMWizardPageURLInstalling       _pageInstalling          = null;
	protected UMWizardPageURLComplete         _pageComplete            = null;

	protected UMApplicationUserInterface _application = null;
/**
 * ScriptNewWizard constructor comment.
 */
public UMWizardProductComponentURLInstall(UMApplicationUserInterface application)
{
	super();
	_application = application;

	setWindowTitle( UpdateManagerStrings.getString("S_Installing"));
}
/**
 * 
 */
public void addPages() {

	// Create the wizard pages
	//------------------------
	_pageInstall     = new UMWizardPageURLInstall    ( this, "install");
	_pageInstallable = new UMWizardPageURLInstallable( this, "installable");
	_pageInstalling  = new UMWizardPageURLInstalling ( this, "installing");
	_pageComplete    = new UMWizardPageURLComplete   ( this, "complete");

	addPage(_pageInstall);
	addPage(_pageInstallable);
	addPage(_pageInstalling);
	addPage(_pageComplete);
}
/**
 * The <code>Wizard</code> implementation of this <code>IWizard</code>
 * method does nothing and returns <code>true</code>.
 * Subclasses should reimplement this method if they need to perform 
 * any special cancel processing for their wizard.
 */
public boolean performCancel() {
	if( _application != null )
		_application.stopEventLoop();
		
	return true;
}
/**
 * Subclasses must implement this <code>IWizard</code> method 
 * to perform any special finish processing for their wizard.
 */
public boolean performFinish()
{
	if( _application != null )
		_application.stopEventLoop();

	return true;
}
/**
 * Insert the method's description here.
 * Creation date: (2001-04-08 11:34:09 AM)
 * @param strManifestID java.lang.String
 */
public void setInstallID(String strInstallId) {
	_strInstallId = strInstallId;
}
/**
 * 
 */
public void setInstallURL(URL urlInstall) {
	_urlInstall = urlInstall;           
}
}
