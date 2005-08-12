/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
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
import org.eclipse.ui.externaltools.internal.model.BuilderUtils;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.ide.IDE;

public class ExternalToolsBuilderTab extends AbstractLaunchConfigurationTab {

	protected Button afterClean;
	protected Button fDuringClean;
	protected Button autoBuildButton;
	protected Button manualBuild;
	protected Button workingSetButton;
	protected Button specifyResources;
	protected Button fLaunchInBackgroundButton;
	protected IWorkingSet workingSet; 
	protected ILaunchConfiguration fConfiguration;
	
    private boolean fCreateBuildScheduleComponent= true;
    
    public ExternalToolsBuilderTab() {
    }
    
    public ExternalToolsBuilderTab(boolean createBuildScheduleComponent) {
        fCreateBuildScheduleComponent= createBuildScheduleComponent;
    }
    
	protected SelectionListener selectionListener= new SelectionAdapter() {
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			boolean enabled= !fCreateBuildScheduleComponent || autoBuildButton.getSelection() || manualBuild.getSelection();
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
        createVerticalSpacer(mainComposite, 2);
		createBuildScheduleComponent(mainComposite);
		
	}
	
	/**
	 * Creates the controls needed to edit the launch in background
	 * attribute of an external tool
	 *
	 * @param parent the composite to create the controls in
	 */
	protected void createLaunchInBackgroundComposite(Composite parent) {
		fLaunchInBackgroundButton = createCheckButton(parent, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_14);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		fLaunchInBackgroundButton.setLayoutData(data);
		fLaunchInBackgroundButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	protected void createBuildScheduleComponent(Composite parent) {
        if (fCreateBuildScheduleComponent) {
    		Label label= new Label(parent, SWT.NONE);
    		label.setText(ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_Run_this_builder_for__1);
    		label.setFont(parent.getFont());
    		afterClean= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab__Full_builds_2, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_Full, 2);
    		manualBuild= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab__Incremental_builds_4, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_Inc, 2);
    		autoBuildButton= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab__Auto_builds__Not_recommended__6, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_Auto, 2);  
    		fDuringClean= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_0, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_1, 2);
    		
    		createVerticalSpacer(parent, 2);
        }
		
		workingSetButton= createButton(parent, selectionListener, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_workingSet_label, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_workingSet_tooltip, 1);
		specifyResources= createPushButton(parent, ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_13, null);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		specifyResources.setLayoutData(gd);
		specifyResources.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectResources();
			}
		});
        Label label= new Label(parent, SWT.NONE);
        label.setText(ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_2);
        label.setFont(parent.getFont());
	}
	
	/*
	 * Creates a check button in the given composite with the given text
	 */
	protected Button createButton(Composite parent, SelectionListener listener, String text, String tooltipText, int columns) {
		Button button= createCheckButton(parent, text);
		button.setToolTipText(tooltipText);
		button.addSelectionListener(listener);
        GridData gd= new GridData(GridData.FILL_HORIZONTAL);
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
		configuration.setAttribute(IExternalToolConstants.ATTR_TRIGGERS_CONFIGURED, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		fConfiguration= configuration;
        if (fCreateBuildScheduleComponent) {
            afterClean.setSelection(false);
            manualBuild.setSelection(false);
            autoBuildButton.setSelection(false);
            fDuringClean.setSelection(false);
        }

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
		
        if (fCreateBuildScheduleComponent) {
    		int buildTypes[]= BuilderUtils.buildTypesToArray(buildKindString);
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
    				case IncrementalProjectBuilder.CLEAN_BUILD:
    					fDuringClean.setSelection(true);
    					break;
    			}
    		}
        }
        
		boolean enabled= true;
		if (fCreateBuildScheduleComponent) {
			enabled= autoBuildButton.getSelection() || manualBuild.getSelection();
		}
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
        if (fCreateBuildScheduleComponent) {
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
    		
    		if (fDuringClean.getSelection()) {
    			buffer.append(IExternalToolConstants.BUILD_TYPE_CLEAN);
    		}
    		configuration.setAttribute(IExternalToolConstants.ATTR_RUN_BUILD_KINDS, buffer.toString());
        }
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
		return ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_Build_Options_9;
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
		if (fCreateBuildScheduleComponent) {
		    boolean buildKindSelected= afterClean.getSelection() || manualBuild.getSelection() || autoBuildButton.getSelection() || fDuringClean.getSelection();
    		if (!buildKindSelected) {
    			setErrorMessage(ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_buildKindError);
    			return false;
    		}
        }
		if (workingSetButton.getSelection() && (workingSet == null || workingSet.getElements().length == 0)) {
			setErrorMessage(ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_16);
            return false;
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
			workingSet = workingSetManager.createWorkingSet(ExternalToolsLaunchConfigurationMessages.ExternalToolsBuilderTab_15, new IAdaptable[0]);
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
