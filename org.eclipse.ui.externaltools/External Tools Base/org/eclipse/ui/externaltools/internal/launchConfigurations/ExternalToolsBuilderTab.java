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
import org.eclipse.debug.ui.variables.IGroupDialogPage;
import org.eclipse.debug.ui.variables.VariableUtil;
import org.eclipse.debug.ui.variables.WorkingSetComponent;
import org.eclipse.jface.dialogs.IMessageProvider;
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

public class ExternalToolsBuilderTab extends AbstractLaunchConfigurationTab implements IGroupDialogPage {

	private Button fullBuildButton;
	private Button autoBuildButton;
	private Button incrementalBuildButton;
	private Button workingSetButton;
	private WorkingSetComponent workingSetComponent;
	
	private SelectionListener selectionListener= new SelectionAdapter() {
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
		
		workingSetComponent= new WorkingSetComponent();
		workingSetComponent.createContents(mainComposite, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Working_Sets_1"), this); //$NON-NLS-1$
	}
	
	private void createBuildScheduleComponent(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		label.setText(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Run_this_builder_for__1")); //$NON-NLS-1$
		fullBuildButton= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.&Full_builds_2"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Full")); //$NON-NLS-1$ //$NON-NLS-2$
		incrementalBuildButton= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.&Incremental_builds_4"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Inc")); //$NON-NLS-1$ //$NON-NLS-2$
		autoBuildButton= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.&Auto_builds_(Not_recommended)_6"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Auto")); //$NON-NLS-1$ //$NON-NLS-2$
		
		SelectionAdapter listener= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateWorkingSetComponent();
			}
		};
		
		new Label(parent, SWT.NONE);
		
		workingSetButton= createButton(parent, listener, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.workingSet_label"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.workingSet_tooltip")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Creates a check button in the given composite with the given text
	 */
	private Button createButton(Composite parent, SelectionListener listener, String text, String tooltipText) {
		Button button= new Button(parent, SWT.CHECK);
		button.setText(text);
		button.setToolTipText(tooltipText);
		button.addSelectionListener(listener);
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
		String buildScope= null;
		try {
			buildKindString= configuration.getAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, ""); //$NON-NLS-1$
			buildScope= configuration.getAttribute(IExternalToolConstants.ATTR_REFRESH_SCOPE, (String)null);
		} catch (CoreException e) {
		}
		
		workingSetButton.setSelection(buildScope != null);
		
		updateWorkingSetComponent();
		if (buildScope != null) {
			VariableUtil.VariableDefinition variable= VariableUtil.extractVariableTag(buildScope, 0);
			workingSetComponent.setVariableValue(variable.argument);
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
		
		if (workingSetButton.getSelection()) {
			String variableTag= VariableUtil.buildVariableTag("working_set", workingSetComponent.getVariableValue()); //$NON-NLS-1$
			configuration.setAttribute(IExternalToolConstants.ATTR_REFRESH_SCOPE, variableTag);
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_REFRESH_SCOPE, (String)null);
		}
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
	/* (non-Javadoc)
	 * @see org.eclipse.ui.externaltools.internal.group.IGroupDialogPage#updateValidState()
	 */
	public void updateValidState() {
		updateLaunchConfigurationDialog();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
	 */
	public int getMessageType() {
		if (getErrorMessage() != null) {
			return IMessageProvider.ERROR;
		} else if (getMessage() != null) {
			return IMessageProvider.WARNING;
		}
		return IMessageProvider.NONE;
	}
	
	public void setErrorMessage(String errorMessage) {
		super.setErrorMessage(errorMessage);
	}
	
	private void updateWorkingSetComponent() {
		workingSetComponent.getControl().setVisible(workingSetButton.getSelection());
		setErrorMessage(null);
		if (workingSetButton.getSelection()) {
			workingSetComponent.validate();
		}
	
		updateLaunchConfigurationDialog();			
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (!workingSetComponent.isValid()) {
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		
		boolean buildKindSelected= fullBuildButton.getSelection() || incrementalBuildButton.getSelection() || autoBuildButton.getSelection();
		if (!buildKindSelected) {
			setErrorMessage(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.buildKindError")); //$NON-NLS-1$
			return false;
		}

		return workingSetComponent.isValid();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#canSave()
	 */
	public boolean canSave() {
		return isValid(null);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		super.dispose();
		workingSetComponent.dispose();
	}

}
