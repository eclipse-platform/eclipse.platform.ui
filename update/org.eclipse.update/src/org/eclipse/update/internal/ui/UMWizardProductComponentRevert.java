package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * 
 */
import org.eclipse.jface.resource.ImageDescriptor;import org.eclipse.update.internal.core.UpdateManagerPlugin;import org.eclipse.update.internal.core.UpdateManagerStrings;

public class UMWizardProductComponentRevert extends UMWizard {
	protected UMWizardPageLaunchHistory _pageLaunchHistory = null;
	protected UMWizardPageLaunchHistoryComplete _pageComplete = null;
	
	/**
	 */
	public UMWizardProductComponentRevert(UMDialog dialog) {
		super(dialog, null);

		setWindowTitle(UpdateManagerStrings.getString("S_Installation"));

		ImageDescriptor imageDescriptor = UpdateManagerPlugin.getImageDescriptor("icons/full/wizban/revert.gif");
		setDefaultPageImageDescriptor(imageDescriptor);
	}
	/**
	 * 
	 */
	public void addPages() {

		// Create the wizard pages
		//------------------------
		_pageLaunchHistory = new UMWizardPageLaunchHistory(this, "history");
		_pageComplete = new UMWizardPageLaunchHistoryComplete(this, "complete");

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