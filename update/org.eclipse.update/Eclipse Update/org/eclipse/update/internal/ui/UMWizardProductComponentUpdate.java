package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * The new product and component updates wizard
 */
import org.eclipse.update.internal.core.*;
import org.eclipse.core.internal.boot.update.*;
import org.eclipse.jface.resource.ImageDescriptor;

public class UMWizardProductComponentUpdate extends UMWizard
{
	protected UMWizardPageLocations           _pageLocations           = null;
	protected UMWizardPageInstallable         _pageInstallable         = null;
	protected UMWizardPageInstalling          _pageInstalling          = null;
	protected UMWizardPageComplete            _pageComplete            = null;
/**
 * 
 */
public UMWizardProductComponentUpdate( UMDialog dialog, IManifestDescriptor[] manifestDescriptors )
{
	super( dialog, manifestDescriptors );

	setWindowTitle( UpdateManagerStrings.getString("S_Installation") );
	
	ImageDescriptor imageDescriptor = UpdateManagerPlugin.getImageDescriptor( "icons/basic/wizban/update_wiz.gif" );
	setDefaultPageImageDescriptor( imageDescriptor );
}
/**
 * 
 */
public void addPages() {

	// Create the wizard pages
	//------------------------
	_pageLocations   = new UMWizardPageLocations  ( this, "locations", true );
	_pageInstallable = new UMWizardPageInstallable( this, "installable", true);
	_pageInstalling  = new UMWizardPageInstalling ( this, "installing",  true);
	_pageComplete    = new UMWizardPageComplete   ( this, "complete",    true);

	addPage(_pageLocations);
	addPage(_pageInstallable);
	addPage(_pageInstalling);
	addPage(_pageComplete);
}
}
