/*******************************************************************************
 * Copyright (c) 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.sourcelookup;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupPanel;
import org.eclipse.debug.internal.ui.sourcelookup.SourceLookupUIMessages;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * A launch configuration tab that displays and edits the source
 * lookup path for a launch configuration. This tab works with the
 * debug platform source lookup facilities - a source lookup director
 * with associated participants and source containers.
 * <p>
 * Clients may call {@link #setHelpContextId(String)} on this tab prior to control
 * creation to alter the default context help associated with this tab. 
 * </p>
 * <p>
 * This tab may be instantiated. 
 * </p>
 * @since 3.0	 
 * @noextend This class is not intended to be subclassed by clients.
 */

public class SourceLookupTab extends AbstractLaunchConfigurationTab {
	//the panel displaying the containers
	private SourceLookupPanel fSourceLookupPanel;
		
	/**
	 * Constructs a new tab with default context help.
	 */
	public SourceLookupTab() {
		setHelpContextId(IDebugHelpContextIds.SOURCELOOKUP_TAB);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), getHelpContextId());
		GridLayout topLayout = new GridLayout();
		topLayout.marginWidth = 0;
		topLayout.marginHeight = 0;
		topLayout.numColumns = 1;
		comp.setLayout(topLayout);
		comp.setFont(parent.getFont());
		
		fSourceLookupPanel = new SourceLookupPanel();
		fSourceLookupPanel.setLaunchConfigurationDialog(
				getLaunchConfigurationDialog());
		fSourceLookupPanel.createControl(comp);
		GridData gd = (GridData) fSourceLookupPanel.getControl().getLayoutData();
		gd.heightHint = 200;
		gd.widthHint = 250;
		Dialog.applyDialogFont(comp);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		fSourceLookupPanel.initializeFrom(configuration);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		fSourceLookupPanel.performApply(configuration);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	public String getId() {
	    return "org.eclipse.debug.ui.sourceLookupTab"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return SourceLookupUIMessages.sourceTab_tabTitle; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return DebugPluginImages.getImage(IInternalDebugUIConstants.IMG_SRC_LOOKUP_TAB);
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
		if (fSourceLookupPanel != null) {
			if (fSourceLookupPanel.getDirector() != null) {
				fSourceLookupPanel.getDirector().dispose();
			}
			fSourceLookupPanel.dispose();
		}
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
