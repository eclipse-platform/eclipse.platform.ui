/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A launch configuration tab that displays and edits source lookup
 * path launch configuration attributes.
 * 
 * This tab may be instantiated. This class is not intended to be subclassed.
 * 
 * @since 3.0	 
 */

public class SourceContainerLookupTab extends AbstractLaunchConfigurationTab {
	//the panel displaying the containers
	protected SourceContainerLookupPanel fSourceLookupPanel;
		
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		comp.setLayout(topLayout);
		comp.setFont(parent.getFont());
		
		createVerticalSpacer(comp, 1);
		
		fSourceLookupPanel = new SourceContainerLookupPanel();
		fSourceLookupPanel.setLaunchConfigurationDialog(
				getLaunchConfigurationDialog());
		fSourceLookupPanel.createControl(comp);
		GridData gd = (GridData) fSourceLookupPanel.getControl().getLayoutData();
		gd.heightHint = 200;
		gd.widthHint = 250;
		Dialog.applyDialogFont(comp);
		WorkbenchHelp.setHelp(comp,IDebugHelpContextIds.SOURCELOOKUP_TAB);
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		fSourceLookupPanel.initializeFrom(configuration);
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		fSourceLookupPanel.performApply(configuration);
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return SourceLookupUIMessages.getString("sourceTab.tabTitle"); //$NON-NLS-1$
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return DebugPluginImages.getImage(IDebugUIConstants.IMG_SRC_LOOKUP_TAB);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		fSourceLookupPanel.activated(workingCopy);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		if(fSourceLookupPanel!= null && fSourceLookupPanel.fLocator!=null)
			fSourceLookupPanel.fLocator.dispose();
		fSourceLookupPanel = null;		
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getErrorMessage()
	 */
	public String getErrorMessage() {
		if (fSourceLookupPanel != null) {
			return fSourceLookupPanel.getErrorMessage();
		}
		return super.getErrorMessage();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getMessage()
	 */
	public String getMessage() {
		if (fSourceLookupPanel != null) {
			return fSourceLookupPanel.getMessage();
		}
		return super.getMessage();
	}
}
