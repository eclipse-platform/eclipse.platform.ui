/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
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
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.externaltools.internal.model.ExternalToolBuilder;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.ide.IDE;

public class ExternalToolsBuilderTab extends AbstractLaunchConfigurationTab {

	private Button afterClean;
	private Button autoBuildButton;
	private Button manualBuild;
	private Button workingSetButton;
	private Button specifyResources;
	protected Button fLaunchInBackgroundButton;
	private IWorkingSet workingSet; 
	
	private SelectionListener selectionListener= new SelectionAdapter() {
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			boolean enabled= autoBuildButton.getSelection() || manualBuild.getSelection();
			workingSetButton.setEnabled(enabled);
			specifyResources.setEnabled(enabled && workingSetButton.getSelection());
			updateLaunchConfigurationDialog();
		}
	};

	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IExternalToolsHelpContextIds.EXTERNAL_TOOLS_LAUNCH_CONFIGURATION_DIALOG_BUILDER_TAB);
		
		GridLayout layout = new GridLayout();
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		mainComposite.setFont(parent.getFont());
		createLaunchInBackgroundComposite(mainComposite);
		createBuildScheduleComponent(mainComposite);
		
	}
	
	/**
	 * Creates the controls needed to edit the launch in background
	 * attribute of an external tool
	 *
	 * @param parent the composite to create the controls in
	 */
	protected void createLaunchInBackgroundComposite(Composite parent) {
		fLaunchInBackgroundButton = createCheckButton(parent, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.14")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		fLaunchInBackgroundButton.setLayoutData(data);
		fLaunchInBackgroundButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	private void createBuildScheduleComponent(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		label.setText(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Run_this_builder_for__1")); //$NON-NLS-1$
		label.setFont(parent.getFont());
		afterClean= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.&Full_builds_2"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Full"), 2); //$NON-NLS-1$ //$NON-NLS-2$
		manualBuild= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.&Incremental_builds_4"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Inc"), 2); //$NON-NLS-1$ //$NON-NLS-2$
		autoBuildButton= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.&Auto_builds_(Not_recommended)_6"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Auto"), 2); //$NON-NLS-1$ //$NON-NLS-2$
				
		createVerticalSpacer(parent, 2);
		
		workingSetButton= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.workingSet_label"), ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.workingSet_tooltip"), 1); //$NON-NLS-1$ //$NON-NLS-2$
		specifyResources= createPushButton(parent, ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.13"), null); //$NON-NLS-1$
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		specifyResources.setLayoutData(gd);
		specifyResources.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectResources();
			}
		});
	}
	
	/*
	 * Creates a check button in the given composite with the given text
	 */
	private Button createButton(Composite parent, SelectionListener listener, String text, String tooltipText, int columns) {
		Button button= createCheckButton(parent, text);
		button.setToolTipText(tooltipText);
		button.addSelectionListener(listener);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = columns;
		button.setLayoutData(gd);
		return button;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		StringBuffer buffer= new StringBuffer(IExternalToolConstants.BUILD_TYPE_FULL);
		buffer.append(',');
		buffer.append(IExternalToolConstants.BUILD_TYPE_INCREMENTAL);
		buffer.append(','); 
		configuration.setAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, buffer.toString());
		configuration.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		
		afterClean.setSelection(false);
		manualBuild.setSelection(false);
		autoBuildButton.setSelection(false);

		String buildKindString= null;
		String buildScope= null;
		try {
			buildKindString= configuration.getAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, ""); //$NON-NLS-1$
			buildScope= configuration.getAttribute(IExternalToolConstants.ATTR_BUILD_SCOPE, (String)null);
		} catch (CoreException e) {
		}
		
		workingSetButton.setSelection(buildScope != null);
		workingSetButton.setEnabled(buildScope != null);
		
		if (buildScope != null) {
			workingSet = RefreshTab.getWorkingSet(buildScope);
		}
		
		int buildTypes[]= ExternalToolBuilder.buildTypesToArray(buildKindString);
		for (int i = 0; i < buildTypes.length; i++) {
			switch (buildTypes[i]) {
				case IncrementalProjectBuilder.FULL_BUILD:
					afterClean.setSelection(true);
					break;
				case IncrementalProjectBuilder.INCREMENTAL_BUILD:
					manualBuild.setSelection(true);
					break;
				case IncrementalProjectBuilder.AUTO_BUILD:
					autoBuildButton.setSelection(true);
					break;
			}
		}
		boolean enabled= autoBuildButton.getSelection() || manualBuild.getSelection();
		workingSetButton.setEnabled(enabled);
		specifyResources.setEnabled(enabled && workingSetButton.getSelection());
		updateRunInBackground(configuration);
	}
	
	protected void updateRunInBackground(ILaunchConfiguration configuration) { 
		fLaunchInBackgroundButton.setSelection(isLaunchInBackground(configuration));
	}
	
	/**
	 * Returns whether the given configuration should be run in the background.
	 * 
	 * @param configuration the configuration
	 * @return whether the configuration is configured to run in the background
	 */
	public static boolean isLaunchInBackground(ILaunchConfiguration configuration) {
		boolean launchInBackground= false;
		try {
			launchInBackground= configuration.getAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log(ce);
		}
		return launchInBackground;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		StringBuffer buffer= new StringBuffer();
		if (afterClean.getSelection()) {
			buffer.append(IExternalToolConstants.BUILD_TYPE_FULL).append(',');
		} 
		if (manualBuild.getSelection()){
			buffer.append(IExternalToolConstants.BUILD_TYPE_INCREMENTAL).append(','); 
		} 
		if (autoBuildButton.getSelection()) {
			buffer.append(IExternalToolConstants.BUILD_TYPE_AUTO).append(',');
		}
		configuration.setAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, buffer.toString());
		
		if (workingSetButton.getSelection()) {
			String scope = RefreshTab.getRefreshAttribute(workingSet);
			configuration.setAttribute(IExternalToolConstants.ATTR_BUILD_SCOPE, scope);
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_BUILD_SCOPE, (String)null);
		}
		configuration.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, fLaunchInBackgroundButton.getSelection());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.Build_Options_9"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		
		boolean buildKindSelected= afterClean.getSelection() || manualBuild.getSelection() || autoBuildButton.getSelection();
		if (!buildKindSelected) {
			setErrorMessage(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.buildKindError")); //$NON-NLS-1$
			return false;
		}
		if (workingSetButton.getSelection() && (workingSet == null || workingSet.getElements().length == 0)) {
			setErrorMessage(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.16")); //$NON-NLS-1$
		}
		
		return true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#canSave()
	 */
	public boolean canSave() {
		return isValid(null);
	}

	/**
	 * Prompts the user to select the working set that triggers the build.
	 */
	private void selectResources() {
		IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
		
		if (workingSet == null){
			workingSet = workingSetManager.createWorkingSet(ExternalToolsLaunchConfigurationMessages.getString("ExternalToolsBuilderTab.15"), new IAdaptable[0]); //$NON-NLS-1$
		}
		IWorkingSetEditWizard wizard= workingSetManager.createWorkingSetEditWizard(workingSet);
		WizardDialog dialog = new WizardDialog(ExternalToolsPlugin.getStandardDisplay().getActiveShell(), wizard);
		dialog.create();		
		
		if (dialog.open() == Window.CANCEL) {
			return;
		}
		workingSet = wizard.getSelection();
		updateLaunchConfigurationDialog();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing on activation
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing on deactivation
	}

}
