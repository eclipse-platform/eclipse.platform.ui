package org.eclipse.update.internal.ui;

/**
 * The new product and component updates wizard
 */
import org.eclipse.update.internal.core.*;
import org.eclipse.jface.resource.ImageDescriptor;

public class UMWizardProductComponentRevert extends UMWizard
{
	protected UMWizardPageLaunchHistory         _pageLaunchHistory       = null;
	protected UMWizardPageLaunchHistoryComplete _pageComplete            = null;
/**
 * ScriptNewWizard constructor comment.
 */
public UMWizardProductComponentRevert( UMDialog dialog )
{
	super( dialog, null );

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
	_pageLaunchHistory = new UMWizardPageLaunchHistory( this, "history" );
	_pageComplete = new UMWizardPageLaunchHistoryComplete( this, "complete" );

	addPage(_pageLaunchHistory);
	addPage(_pageComplete);
}
/**
 * 
 */
public boolean performFinish() {

	_pageLaunchHistory.doRevert();
	
	return super.performFinish();
}
}
