/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;


import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.PixelConverter;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
 

/**
 * A dialog used to edit a single launch configuration.
 */
public class LaunchConfigurationPropertiesDialog extends LaunchConfigurationsDialog implements ILaunchConfigurationListener {
	
	/**
	 * The launch configuration to display
	 */
	private ILaunchConfiguration fLaunchConfiguration;

	/**
	 * Constructs a new launch configuration dialog on the given
	 * parent shell.
	 * 
	 * @param shell the parent shell
	 * @param selection the selection used to initialize this dialog, typically the 
	 *  current workbench selection
	 * @param group launch group
	 */
	public LaunchConfigurationPropertiesDialog(Shell shell, ILaunchConfiguration launchConfiguration, LaunchGroupExtension group) {
		super(shell, group);
		setLaunchConfiguration(launchConfiguration);
		DebugPlugin.getDefault().getLaunchManager().addLaunchConfigurationListener(this);
	}
	
	/**
	 * Sets the launch configration to be displayed.
	 * 
	 * @param configuration
	 */
	private void setLaunchConfiguration(ILaunchConfiguration configuration) {
		fLaunchConfiguration = configuration;
	}
	
	/**
	 * Returns the launch configuration being displayed.
	 * 
	 * @return ILaunchConfiguration
	 */
	protected ILaunchConfiguration getLaunchConfiguration() {
		return fLaunchConfiguration;
	}

	protected void initializeContent() {
		getTabViewer().setInput(getLaunchConfiguration());
		IStatus status = getInitialStatus();
		if (status != null) {
			handleStatus(status);
		}
	}
			
	/**
	 * @see Window#close()
	 */
	public boolean close() {
	    if (!isSafeToClose()) {
	        return false;
	    }
		persistShellGeometry();
		getBannerImage().dispose();
		getTabViewer().dispose();
		DebugPlugin.getDefault().getLaunchManager().removeLaunchConfigurationListener(this);
		return super.close();
	}
		
	/**
	 * Adds content to the dialog area
	 */
	protected void addContent(Composite dialogComp) {
		GridData gd;
		Composite topComp = new Composite(dialogComp, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		topComp.setLayoutData(gd);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		topLayout.marginHeight = 5;
		topLayout.marginWidth = 0;
		topComp.setLayout(topLayout);
		topComp.setFont(dialogComp.getFont());
	
		// Set the things that TitleAreaDialog takes care of 
		setTitle(getTitleAreaTitle());
		setMessage(""); //$NON-NLS-1$
		setModeLabelState();
	
		// Build the launch configuration edit area and put it into the composite.
		Composite editAreaComp = createLaunchConfigurationEditArea(topComp);
		setEditArea(editAreaComp);
		gd = new GridData(GridData.FILL_BOTH);
		editAreaComp.setLayoutData(gd);
		editAreaComp.setFont(dialogComp.getFont());
			
		// Build the separator line that demarcates the button bar
		Label separator = new Label(topComp, SWT.HORIZONTAL | SWT.SEPARATOR);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		separator.setLayoutData(gd);
		
		dialogComp.layout(true);
		applyDialogFont(dialogComp);
	}
	
	protected String getTitleAreaTitle() {
		return LaunchConfigurationsMessages.LaunchConfigurationPropertiesDialog_Edit_launch_configuration_properties_1; 
	}
			
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
					
	protected String getShellTitle() {
		return MessageFormat.format(LaunchConfigurationsMessages.LaunchConfigurationPropertiesDialog_Properties_for__0__2, new String[]{getLaunchConfiguration().getName()}); 
	}
	
	protected String getHelpContextId() {
		return IDebugHelpContextIds.LAUNCH_CONFIGURATION_PROPERTIES_DIALOG;
	}
		  	
  	protected void resize() {
  		// determine the maximum tab dimensions
  		PixelConverter pixelConverter = new PixelConverter(getEditArea());
  		int runningTabWidth = 0;
  		ILaunchConfigurationTabGroup group = getTabGroup();
  		if (group == null) {
  			return;
  		}
  		ILaunchConfigurationTab[] tabs = group.getTabs();
  		Point contentSize = new Point(0, 0);
  		for (int i = 0; i < tabs.length; i++) {
  			String name = tabs[i].getName();
  			Image image = tabs[i].getImage();
  			runningTabWidth += pixelConverter.convertWidthInCharsToPixels(name.length() + 5);
  			if (image != null) {
  				runningTabWidth += image.getBounds().width;
  			}
  			Control control = tabs[i].getControl();
  			if (control != null) {
  				Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
  				if (size.x > contentSize.x) {
  					contentSize.x = size.x;
  				}
  				if (size.y > contentSize.y) {
  					contentSize.y = size.y;
  				}
  			}
  		}

  		// Determine if more space is needed to show all tab labels across the top of the
  		// tab folder.  If so, only increase size of dialog to some percent of the available
  		// screen real estate.
  		if (runningTabWidth > contentSize.x) {
  			int maxAllowedWidth = (int) (getDisplay().getBounds().width * MAX_DIALOG_WIDTH_PERCENT);
  			if (runningTabWidth > maxAllowedWidth) {
  				contentSize.x = maxAllowedWidth;
  			} else {
  				contentSize.x = runningTabWidth;
  			}
  		}

  		// Adjust the maximum tab dimensions to account for the extra space required for the tab labels
  		Rectangle tabFolderBoundingBox = getEditArea().computeTrim(0, 0, contentSize.x, contentSize.y);
  		contentSize.x = tabFolderBoundingBox.width;
  		contentSize.y = tabFolderBoundingBox.height;

  		// Force recalculation of sizes
  		getEditArea().layout(true);

  		// Calculate difference between required space for tab folder and current size,
  		// then increase size of this dialog's Shell by that amount
  		Rectangle rect = getEditArea().getClientArea();
  		Point containerSize= new Point(rect.width, rect.height);
  		int hdiff= contentSize.x - containerSize.x;
  		int vdiff= contentSize.y - containerSize.y;
  		// Only increase size of dialog, never shrink it
  		if (hdiff > 0 || vdiff > 0) {
  			hdiff= Math.max(0, hdiff);
  			vdiff= Math.max(0, vdiff);
  			Shell shell= getShell();
  			Point shellSize= shell.getSize();
  			setShellSize(shellSize.x + hdiff, shellSize.y + vdiff);
  		}  		
  	}
  	 	 	 	 	
	/**
	 * @see ILaunchConfigurationDialog#updateButtons()
	 */
	public void updateButtons() {
		// apply/revert buttons
		getTabViewer().refresh();
		getButton(IDialogConstants.OK_ID).setEnabled(getTabViewer().canSave());
		
	}
		
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		getTabViewer().handleApplyPressed();
		super.okPressed();
	}

	/**
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		setOpenMode(-1);
		return super.open();
	}

	/**
	 * Returns the name of the section that this dialog stores its settings in
	 *
	 * @return String
	 */
	protected String getDialogSettingsSectionName() {
		return IDebugUIConstants.PLUGIN_ID + ".LAUNCH_CONFIGURATION_PROPERTIES_DIALOG_SECTION"; //$NON-NLS-1$
	}
	
	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationAdded(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		if (getLaunchConfiguration().equals(manager.getMovedFrom(configuration))) {
			// this config was re-named, update the dialog with the new config
			setLaunchConfiguration(configuration);
			getTabViewer().setInput(getLaunchConfiguration());
		}
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationRemoved(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
	}

}
