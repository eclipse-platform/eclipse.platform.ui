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
package org.eclipse.debug.ui.launchVariables;


import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.variables.IContextLaunchVariable;
import org.eclipse.debug.core.variables.LaunchVariableUtil;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.launchVariables.LaunchVariableMessages;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A launch configuration tab which allows the user to specify
 * which resources should be refreshed when the launch
 * finishes.
 */
public class RefreshTab extends AbstractLaunchConfigurationTab implements IVariableComponentContainer {

	private LaunchConfigurationVariableForm variableForm;
	
	protected Button refreshField;
	protected Button recursiveField;
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		WorkbenchHelp.setHelp(getControl(), IDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_REFRESH_TAB);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		mainComposite.setFont(parent.getFont());
		createVerticalSpacer(mainComposite, 1);
		createRefreshComponent(mainComposite);
		createRecursiveComponent(mainComposite);
		createScopeComponent(mainComposite);
	}
	
	/**
	 * Creates the controls needed to edit the refresh recursive
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	private void createRecursiveComponent(Composite parent) {
		recursiveField = new Button(parent, SWT.CHECK);
		recursiveField.setText(LaunchVariableMessages.getString("RefreshTab.0")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		recursiveField.setLayoutData(data);
		recursiveField.setFont(parent.getFont());
		recursiveField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	/**
	 * Creates the controls needed to edit the refresh scope
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	private void createRefreshComponent(Composite parent) {
		refreshField = new Button(parent, SWT.CHECK);
		refreshField.setText(LaunchVariableMessages.getString("RefreshTab.1")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		refreshField.setLayoutData(data);
		refreshField.setFont(parent.getFont());
		refreshField.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEnabledState();
				updateLaunchConfigurationDialog();
			}
		});
	}
	
	/**
	 * Creates the controls needed to edit the refresh scope variable
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	private void createScopeComponent(Composite parent) {
		String label = LaunchVariableMessages.getString("RefreshTab.2"); //$NON-NLS-1$
		IContextLaunchVariable[] vars = DebugPlugin.getDefault().getLaunchVariableManager().getRefreshVariables();
		variableForm = new LaunchConfigurationVariableForm(label, vars);
		variableForm.createContents(parent, this);
	}
	

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		updateRefresh(configuration);
		updateRecursive(configuration);
		updateScope(configuration);
	}
	/**
	 * Method udpateScope.
	 * @param configuration
	 */
	private void updateScope(ILaunchConfiguration configuration) {
		String scope = null;
		try {
			scope= configuration.getAttribute(LaunchVariableUtil.ATTR_REFRESH_SCOPE, (String)null);
		} catch (CoreException ce) {
			DebugUIPlugin.log(DebugUIPlugin.newErrorStatus("Exception reading launch configuration", ce)); //$NON-NLS-1$
		}
		String varName = null;
		String varValue = null;
		if (scope != null) {
			LaunchVariableUtil.VariableDefinition varDef = LaunchVariableUtil.extractVariableDefinition(scope, 0);
			varName = varDef.name;
			varValue = varDef.argument;
		}
		variableForm.selectVariable(varName, varValue);
	}
	/**
	 * Method updateRecursive.
	 * @param configuration
	 */
	private void updateRecursive(ILaunchConfiguration configuration) {
		boolean recursive= true;
		try {
			recursive= configuration.getAttribute(LaunchVariableUtil.ATTR_REFRESH_RECURSIVE, true);
		} catch (CoreException ce) {
			DebugUIPlugin.log(DebugUIPlugin.newErrorStatus("Exception reading launch configuration", ce)); //$NON-NLS-1$
		}
		recursiveField.setSelection(recursive);
	}
	/**
	 * Method updateRefresh.
	 * @param configuration
	 */
	private void updateRefresh(ILaunchConfiguration configuration) {
		String scope= null;
		try {
			scope= configuration.getAttribute(LaunchVariableUtil.ATTR_REFRESH_SCOPE, (String)null);
		} catch (CoreException ce) {
			DebugUIPlugin.log(DebugUIPlugin.newErrorStatus("Exception reading launch configuration", ce)); //$NON-NLS-1$
		}
		refreshField.setSelection(scope != null);
		updateEnabledState();		
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		if (refreshField.getSelection()) {
			configuration.setAttribute(LaunchVariableUtil.ATTR_REFRESH_SCOPE, variableForm.getSelectedVariable());
		} else {
			configuration.setAttribute(LaunchVariableUtil.ATTR_REFRESH_SCOPE, (String)null);
		}
		
		setAttribute(LaunchVariableUtil.ATTR_REFRESH_RECURSIVE, configuration, recursiveField.getSelection(), true);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LaunchVariableMessages.getString("RefreshTab.6"); //$NON-NLS-1$
	}
	
	/**
	 * Updates the enablement state of the fields.
	 */
	private void updateEnabledState() {
		if (refreshField != null) {
			if (recursiveField != null) {
				recursiveField.setEnabled(refreshField.getSelection());
			}
			if (variableForm != null) {
				variableForm.setEnabled(refreshField.getSelection());
			}
		}
	}
	
	/**
	 * @see IVariableComponentContainer#setErrorMessage(String)
	 */
	public void setErrorMessage(String errorMessage) {
		super.setErrorMessage(errorMessage);
	}

	/**
	 * @see org.eclipse.ui.externaltools.group.IVariableComponentContainer#updateValidState()
	 */
	public void updateValidState() {
		updateLaunchConfigurationDialog();
	}

	/**
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
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return DebugPluginImages.getImage(IInternalDebugUIConstants.IMG_OBJS_REFRESH_TAB);
	}

	public boolean isValid(ILaunchConfiguration launchConfig) {
		return getErrorMessage() == null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		if (variableForm != null) {
			variableForm.dispose();
		}
		super.dispose();
	}

	/**
	 * Refreshes the resources as specified by the given launch configuration.
	 * 
	 * @param configuration launch configuration
	 * @param context context used to expand variables
	 * @param monitor progress monitor which may be <code>null</code>
	 * @throws CoreException if an exception occurrs while refreshing resources
	 */
	public static void refreshResources(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		IResource[] resources= getResourcesForRefreshScope(configuration, monitor);
		if (resources == null || resources.length == 0){
			return;
		}
		int depth = IResource.DEPTH_ONE;
		if (LaunchVariableUtil.isRefreshRecursive(configuration))
			depth = IResource.DEPTH_INFINITE;
	
		if (monitor.isCanceled()) {
			return;
		}
	
		monitor.beginTask(LaunchVariableMessages.getString("RefreshTab.7"), //$NON-NLS-1$
			resources.length);
	
		MultiStatus status = new MultiStatus(DebugUIPlugin.getUniqueIdentifier(), 0, LaunchVariableMessages.getString("RefreshTab.8"), null); //$NON-NLS-1$
		for (int i = 0; i < resources.length; i++) {
			if (monitor.isCanceled())
				break;
			if (resources[i] != null && resources[i].isAccessible()) {
				try {
					resources[i].refreshLocal(depth, null);
				} catch (CoreException e) {
					status.merge(e.getStatus());
				}
			}
			monitor.worked(1);
		}
	
		monitor.done();
		if (!status.isOK()) {
			throw new CoreException(status);
		}
	}

	/**
	 * Returns the collection of resources for the refresh scope as specified by the given launch configuration.
	 * 
	 * @param configuration launch configuration
	 * @param context context used to expand variables
	 * @param monitor progress monitor
	 * @throws CoreException if an exception occurs while refreshing resources
	 */
	public static IResource[] getResourcesForRefreshScope(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		String scope = LaunchVariableUtil.getRefreshScope(configuration);
		if (scope == null) {
			return null;
		}
	
		return expandResources(scope, monitor);
	}

	/**
	 * Expands the given variable string to a set of resources. The 
	 * variable string is a variable which is a refresh variable 
	 * contained in the refresh variable registry.
	 * 
	 * @param refreshString
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public static IResource[] expandResources(String variableString, IProgressMonitor monitor) throws CoreException {
		LaunchVariableUtil.VariableDefinition varDef = LaunchVariableUtil.extractVariableDefinition(variableString, 0);
		if (varDef.start == -1 || varDef.end == -1 || varDef.name == null) {
			String msg = MessageFormat.format(LaunchVariableMessages.getString("RefreshTab.9"), new Object[] { variableString }); //$NON-NLS-1$
			throw new CoreException(DebugUIPlugin.newErrorStatus(msg, null));
		}
	
		IContextLaunchVariable variable = DebugPlugin.getDefault().getLaunchVariableManager().getRefreshVariable(varDef.name);
		if (variable == null) {
			String msg = MessageFormat.format(LaunchVariableMessages.getString("RefreshTab.10"), new Object[] { varDef.name }); //$NON-NLS-1$
			throw new CoreException(DebugUIPlugin.newErrorStatus(msg, null));
		}
	
		if (monitor.isCanceled()) {
			return null;
		}
	
		return variable.getExpander().getResources(varDef.name, varDef.argument, LaunchVariableContextManager.getDefault().getVariableContext());
	}
}