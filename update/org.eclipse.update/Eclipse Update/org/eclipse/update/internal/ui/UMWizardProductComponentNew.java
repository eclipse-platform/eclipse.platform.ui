package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * The new products and components wizard
 */
import org.eclipse.jface.resource.ImageDescriptor;import org.eclipse.update.internal.core.UpdateManagerPlugin;import org.eclipse.update.internal.core.UpdateManagerStrings;

public class UMWizardProductComponentNew extends UMWizard
{
	protected UMWizardPageLocations   _pageUpdateLocationsSearch = null;
	protected UMWizardPageInstallable _pageInstallable           = null;
	protected UMWizardPageInstalling  _pageInstalling            = null;
	protected UMWizardPageComplete    _pageComplete              = null;

/**
 */
public UMWizardProductComponentNew(UMDialog dialog) {
	
	super(dialog, null);

	setWindowTitle(UpdateManagerStrings.getString("S_Installation"));

	ImageDescriptor imageDescriptor = UpdateManagerPlugin.getImageDescriptor("icons/basic/wizban/new_wiz.gif");
	setDefaultPageImageDescriptor(imageDescriptor);
}
/**
 * 
 */
public void addPages() {

	// Create the wizard pages
	//------------------------
	_pageUpdateLocationsSearch = new UMWizardPageLocations  ( this, "locations", false );
	_pageInstallable           = new UMWizardPageInstallable( this, "installable", false);
	_pageInstalling            = new UMWizardPageInstalling ( this, "installing",  false);
	_pageComplete              = new UMWizardPageComplete   ( this, "complete",    false);
	
	addPage(_pageUpdateLocationsSearch);
	addPage(_pageInstallable);
	addPage(_pageInstalling);
	addPage(_pageComplete);
}
/**
 */
public boolean performCancel() {
/*
	if (_pageInstallable.getControl().isVisible() == true) {
		if (_pageInstallable.performCancel())
			== false) return false;
	}
*/
	return super.performCancel();
}
}
