/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.launchConfigurations;


import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolBuilder;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.help.WorkbenchHelp;

public class ExternalToolsBuilderTab extends AbstractLaunchConfigurationTab {

	private Button fullBuildButton;
	private Button autoBuildButton;
	private Button incrementalBuildButton;
	
	private SelectionListener fSelectionListener= new SelectionAdapter() {
		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			updateLaunchConfigurationDialog();
		}
	};

	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		WorkbenchHelp.setHelp(getControl(), IExternalToolsHelpContextIds.EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_BUILDER_TAB);
		
		GridLayout layout = new GridLayout();
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		mainComposite.setFont(parent.getFont());
		createBuildScheduleComponent(mainComposite);
	}
	
	private void createBuildScheduleComponent(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		label.setText(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Run_this_builder_for__1")); //$NON-NLS-1$
		fullBuildButton= createButton(parent, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.&Full_builds_2"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Full")); //$NON-NLS-1$ //$NON-NLS-2$
		incrementalBuildButton= createButton(parent, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.&Incremental_builds_4"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Inc")); //$NON-NLS-1$ //$NON-NLS-2$
		autoBuildButton= createButton(parent, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.&Auto_builds_(Not_recommended)_6"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Auto")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Creates a check button in the given composite with the given text
	 */
	private Button createButton(Composite parent, String text, String tooltipText) {
		Button button= new Button(parent, SWT.CHECK);
		button.setText(text);
		button.setToolTipText(tooltipText);
		button.addSelectionListener(fSelectionListener);
		return button;
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		StringBuffer buffer= new StringBuffer(IExternalToolConstants.BUILD_TYPE_FULL);
		buffer.append(',');
		buffer.append(IExternalToolConstants.BUILD_TYPE_INCREMENTAL);
		buffer.append(','); 
		configuration.setAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, buffer.toString());
	}

	/**
	 * Sets the state of the widgets from the given configuration
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		
		fullBuildButton.setSelection(false);
		incrementalBuildButton.setSelection(false);
		autoBuildButton.setSelection(false);
		
		String buildKindString= null;
		try {
			buildKindString= configuration.getAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, ""); //$NON-NLS-1$
		} catch (CoreException e) {
		}
		int buildTypes[]= ExternalToolBuilder.buildTypesToArray(buildKindString);
		for (int i = 0; i < buildTypes.length; i++) {
			switch (buildTypes[i]) {
				case IncrementalProjectBuilder.FULL_BUILD:
					fullBuildButton.setSelection(true);
					break;
				case IncrementalProjectBuilder.INCREMENTAL_BUILD:
					incrementalBuildButton.setSelection(true);
					break;
				case IncrementalProjectBuilder.AUTO_BUILD:
					autoBuildButton.setSelection(true);
					break;
			}
		}
	}

	/**
	 * Stores the settings from the dialog into the launch configuration
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		StringBuffer buffer= new StringBuffer();
		if (fullBuildButton.getSelection()) {
			buffer.append(IExternalToolConstants.BUILD_TYPE_FULL).append(',');
		} 
		if (incrementalBuildButton.getSelection()){
			buffer.append(IExternalToolConstants.BUILD_TYPE_INCREMENTAL).append(','); 
		} 
		if (autoBuildButton.getSelection()) {
			buffer.append(IExternalToolConstants.BUILD_TYPE_AUTO).append(',');
		}
		configuration.setAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, buffer.toString());
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Build_Options_9"); //$NON-NLS-1$
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_PROJECT);
	}
}
