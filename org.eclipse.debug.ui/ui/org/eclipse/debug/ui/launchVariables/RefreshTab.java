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


import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.launchVariables.LaunchVariableMessages;
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * A launch configuration tab which allows the user to specify
 * which resources should be refreshed when the launch
 * terminates.
 * 
 * @since 3.0
 */
public class RefreshTab extends AbstractLaunchConfigurationTab {

	/**
	 * Boolean attribute indicating if a refresh scope is recursive. Default
	 * value is <code>false</code>.
	 */
	public static final String ATTR_REFRESH_RECURSIVE = DebugPlugin.getUniqueIdentifier() + ".ATTR_REFRESH_RECURSIVE"; //$NON-NLS-1$

	/**
	 * String attribute identifying the scope of resources that should be
	 * refreshed after an external tool is run. The value is either a refresh
	 * variable or the default value, <code>null</code>, indicating no refresh.
	 */
	public static final String ATTR_REFRESH_SCOPE = DebugPlugin.getUniqueIdentifier() + ".ATTR_REFRESH_SCOPE"; //$NON-NLS-1$
	
	// Check Buttons
	private Button fRefreshButton;
	private Button fRecursiveButton;
	
	// Group box
	private Group fGroup;
	
	// Radio Buttons
	private Button fContainerButton;
	private Button fProjectButton;
	private Button fResourceButton;
	private Button fWorkingSetButton;
	private Button fWorkspaceButton;
	
	// Push Button
	private Button fSelectButton;
	
	// Working set
	private IWorkingSet fWorkingSet;
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		WorkbenchHelp.setHelp(getControl(), IDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_REFRESH_TAB);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gd);
		mainComposite.setFont(parent.getFont());
		
		createVerticalSpacer(mainComposite, 2);
		
		fRefreshButton = new Button(mainComposite, SWT.CHECK);
		fRefreshButton.setFont(mainComposite.getFont());
		fRefreshButton.setText(LaunchVariableMessages.getString("RefreshTab.31")); //$NON-NLS-1$
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 2;
		fRefreshButton.setLayoutData(gd);
		fRefreshButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEnabledState();
				updateLaunchConfigurationDialog();
			}
		});
		
		fGroup = new Group(mainComposite, SWT.NONE);
		fGroup.setFont(mainComposite.getFont());
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		fGroup.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fGroup.setLayoutData(gd);

		fWorkspaceButton = createRadioButton(fGroup, LaunchVariableMessages.getString("RefreshTab.32")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fWorkspaceButton.setLayoutData(gd);

		fResourceButton = createRadioButton(fGroup, LaunchVariableMessages.getString("RefreshTab.33")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fResourceButton.setLayoutData(gd);

		fProjectButton = createRadioButton(fGroup, LaunchVariableMessages.getString("RefreshTab.34")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fProjectButton.setLayoutData(gd);		

		fContainerButton = createRadioButton(fGroup, LaunchVariableMessages.getString("RefreshTab.35")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fContainerButton.setLayoutData(gd);
				
		fWorkingSetButton = createRadioButton(fGroup, LaunchVariableMessages.getString("RefreshTab.36")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		fWorkingSetButton.setLayoutData(gd);
		fWorkingSetButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateEnabledState();
				updateLaunchConfigurationDialog();
			}
		});		
		
		fSelectButton = createPushButton(fGroup, LaunchVariableMessages.getString("RefreshTab.37"), null); //$NON-NLS-1$
		gd = (GridData)fSelectButton.getLayoutData();
		gd.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		fSelectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectResources();
			}
		});
		
		createVerticalSpacer(fGroup, 2);
		createRecursiveComponent(fGroup);
	}

	/**
	 * Prompts the user to select the resources to refresh.
	 */
	private void selectResources() {
		IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
		
		if (fWorkingSet == null){
			fWorkingSet = workingSetManager.createWorkingSet("workingSet", new IAdaptable[0]); //$NON-NLS-1$
		}
		IWorkingSetEditWizard wizard= workingSetManager.createWorkingSetEditWizard(fWorkingSet);
		WizardDialog dialog = new WizardDialog(DebugUIPlugin.getStandardDisplay().getActiveShell(), wizard);
		dialog.create();		
		
		if (dialog.open() == Window.CANCEL) {
			return;
		}
		fWorkingSet = wizard.getSelection();
		updateLaunchConfigurationDialog();
	}
	
	/**
	 * Creates the controls needed to edit the refresh recursive
	 * attribute of a launch configuration
	 * 
	 * @param parent the composite to create the controls in
	 */
	private void createRecursiveComponent(Composite parent) {
		fRecursiveButton = new Button(parent, SWT.CHECK);
		fRecursiveButton.setText(LaunchVariableMessages.getString("RefreshTab.0")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		fRecursiveButton.setLayoutData(data);
		fRecursiveButton.setFont(parent.getFont());
		fRecursiveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
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
		updateEnabledState();		
	}
	
	/**
	 * Updates the tab to display the refresh scope specified by the launch config
	 */
	private void updateScope(ILaunchConfiguration configuration) {
		String scope = null;
		try {
			scope= configuration.getAttribute(ATTR_REFRESH_SCOPE, (String)null);
		} catch (CoreException ce) {
			DebugUIPlugin.log(DebugUIPlugin.newErrorStatus("Exception reading launch configuration", ce)); //$NON-NLS-1$
		}
		fWorkspaceButton.setSelection(false);
		fResourceButton.setSelection(false);
		fContainerButton.setSelection(false);
		fProjectButton.setSelection(false);
		fWorkingSetButton.setSelection(false);
		if (scope == null) {
			// select the workspace by default
			fWorkspaceButton.setSelection(true);
		} else {
			if (scope.equals("${workspace}")) { //$NON-NLS-1$
				fWorkspaceButton.setSelection(true);
			} else if (scope.equals("${resource}")) { //$NON-NLS-1$
				fResourceButton.setSelection(true);
			} else if (scope.equals("${container}")) { //$NON-NLS-1$
				fContainerButton.setSelection(true);
			} else if (scope.equals("${project}")) { //$NON-NLS-1$
				fProjectButton.setSelection(true);
			} else if (scope.startsWith("${resource:")) { //$NON-NLS-1$
				fWorkingSetButton.setSelection(true);
				try {
					IResource[] resources = getRefreshResources(scope, null);
					IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
					fWorkingSet = workingSetManager.createWorkingSet(LaunchVariableMessages.getString("RefreshTab.40"), resources);					 //$NON-NLS-1$
				} catch (CoreException e) {
					fWorkingSet = null;
				}
			} else if (scope.startsWith("${working_set:")) { //$NON-NLS-1$
				fWorkingSetButton.setSelection(true);
				String memento = scope.substring(14, scope.length() - 1);
				fWorkingSet = restoreWorkingSet(memento);
			}
		}
	}
	/**
	 * Method updateRecursive.
	 * @param configuration
	 */
	private void updateRecursive(ILaunchConfiguration configuration) {
		boolean recursive= true;
		try {
			recursive= configuration.getAttribute(ATTR_REFRESH_RECURSIVE, true);
		} catch (CoreException ce) {
			DebugUIPlugin.log(DebugUIPlugin.newErrorStatus("Exception reading launch configuration", ce)); //$NON-NLS-1$
		}
		fRecursiveButton.setSelection(recursive);
	}
	/**
	 * Method updateRefresh.
	 * @param configuration
	 */
	private void updateRefresh(ILaunchConfiguration configuration) {
		String scope= null;
		try {
			scope= configuration.getAttribute(ATTR_REFRESH_SCOPE, (String)null);
		} catch (CoreException ce) {
			DebugUIPlugin.log(DebugUIPlugin.newErrorStatus("Exception reading launch configuration", ce)); //$NON-NLS-1$
		}
		fRefreshButton.setSelection(scope != null);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (fRefreshButton.getSelection()) {
			String scope = generateScopeMemento();
			configuration.setAttribute(ATTR_REFRESH_SCOPE, scope);
			setAttribute(ATTR_REFRESH_RECURSIVE, configuration, fRecursiveButton.getSelection(), true);
		} else {
			//clear the refresh attributes
			configuration.setAttribute(ATTR_REFRESH_SCOPE, (String)null);
			setAttribute(ATTR_REFRESH_RECURSIVE, configuration, true, true);
		}
	}

	/**
	 * Generates a memento for the refresh scope. This is based on old refresh
	 * variables.
	 * 
	 * @return a memento
	 */
	private String generateScopeMemento() {
		if (fWorkspaceButton.getSelection()) {
			return "${workspace}"; //$NON-NLS-1$
		}
		if (fResourceButton.getSelection()) {
			return "${resource}"; //$NON-NLS-1$
		}
		if (fContainerButton.getSelection()) {
			return "${container}"; //$NON-NLS-1$
		}
		if (fProjectButton.getSelection()) {
			return "${project}"; //$NON-NLS-1$
		}
		if (fWorkingSetButton.getSelection()) {
			return getRefreshAttribute(fWorkingSet);
		}
		return null;
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
		boolean enabled= fRefreshButton.getSelection();
		fRecursiveButton.setEnabled(enabled);
		fGroup.setEnabled(enabled);
		fWorkspaceButton.setEnabled(enabled);
		fResourceButton.setEnabled(enabled);
		fContainerButton.setEnabled(enabled);
		fProjectButton.setEnabled(enabled);
		fWorkingSetButton.setEnabled(enabled);
		fSelectButton.setEnabled(enabled && fWorkingSetButton.getSelection());
		if (!enabled) {
			super.setErrorMessage(null);
		}
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return DebugPluginImages.getImage(IInternalDebugUIConstants.IMG_OBJS_REFRESH_TAB);
	}

	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		if (fWorkingSetButton.getSelection() && fWorkingSet == null) {
			setErrorMessage(LaunchVariableMessages.getString("RefreshTab.42")); //$NON-NLS-1$
			return false;
		}
		return true;
	}
	
	/**
	 * Refreshes the resources as specified by the given launch configuration.
	 * 
	 * @param configuration launch configuration
	 * @param context context used to expand variables
	 * @param monitor progress monitor which may be <code>null</code>
	 * @throws CoreException if an exception occurs while refreshing resources
	 */
	public static void refreshResources(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		String scope = getRefreshScope(configuration);
		IResource[] resources= null;
		if (scope != null) {
			resources = getRefreshResources(scope, monitor);
		}
		if (resources == null || resources.length == 0){
			return;
		}
		int depth = IResource.DEPTH_ONE;
		if (isRefreshRecursive(configuration))
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
	 * Returns a collection of resources referred to by a refresh scope attribute.
	 * 
	 * @param scope refresh scope attribute (<code>ATTR_REFRESH_SCOPE</code>)
	 * @param monitor progress monitor
	 * @return collection of resources referred to by the refresh scope attribute
	 * @throws CoreException if unable to resolve a set of resources
	 */
	public static IResource[] getRefreshResources(String scope, IProgressMonitor monitor) throws CoreException {
		if (scope.startsWith("${resource:")) { //$NON-NLS-1$
			// This is an old format that is replaced with 'working_set'
			String pathString = scope.substring(11, scope.length() - 1);
			Path path = new Path(pathString);
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			if (resource == null) {
				throw new CoreException(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, MessageFormat.format(LaunchVariableMessages.getString("RefreshTab.38"), new String[]{pathString}), null)); //$NON-NLS-1$
			} else {
				return new IResource[]{resource};
			}
		} else if (scope.startsWith("${working_set:")) { //$NON-NLS-1$
			IWorkingSet workingSet =  getWorkingSet(scope);
			if (workingSet == null) {
				throw new CoreException(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, LaunchVariableMessages.getString("RefreshTab.39"), null)); //$NON-NLS-1$
			} else {
				IAdaptable[] elements = workingSet.getElements();
				IResource[] resources = new IResource[elements.length];
				for (int i = 0; i < elements.length; i++) {
					IAdaptable adaptable = elements[i];
					if (adaptable instanceof IResource) {
						resources[i] = (IResource) adaptable;
					} else {
						resources[i] = (IResource) adaptable.getAdapter(IResource.class);
					}
				}
				return resources;				
			}
		} else if(scope.equals("${workspace}")) { //$NON-NLS-1$
			return new IResource[]{ResourcesPlugin.getWorkspace().getRoot()};
		} else {
			IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
			if (resource == null) {
				// empty selection
				return new IResource[]{};
			}
			if (scope.equals("${resource}")) { //$NON-NLS-1$
				// resource = resource
			} else if (scope.equals("${container}")) { //$NON-NLS-1$
				resource = resource.getParent();
			} else if (scope.equals("${project}")) { //$NON-NLS-1$
				resource = resource.getProject();
			}
			return new IResource[]{resource};
		}
	}
	
	/**
	 * Restores a working set based on the XMLMemento represented within
	 * the varValue.
	 * 
	 * see bug 37143.
	 * @param mementoString The string memento of the working set
	 * @return the restored working set or <code>null</code> if problems occurred restoring the
	 * working set.
	 */
	private static IWorkingSet restoreWorkingSet(String mementoString) {
		StringReader reader= new StringReader(mementoString);
		XMLMemento memento= null;
		try {
			memento = XMLMemento.createReadRoot(reader);
		} catch (WorkbenchException e) {
			DebugUIPlugin.log(e);
			return null;
		}

		String factoryID = memento.getString(IVariableConstants.TAG_FACTORY_ID);

		if (factoryID == null) {
			DebugUIPlugin.logErrorMessage(LaunchVariableMessages.getString("WorkingSetExpander.2")); //$NON-NLS-1$
			return null;
		}
		IElementFactory factory = WorkbenchPlugin.getDefault().getElementFactory(factoryID);
		if (factory == null) {
			DebugUIPlugin.logErrorMessage(LaunchVariableMessages.getString("WorkingSetExpander.3") + factoryID); //$NON-NLS-1$
			return null;
		}
		IAdaptable adaptable = factory.createElement(memento);
		if (adaptable == null) {
			DebugUIPlugin.logErrorMessage(LaunchVariableMessages.getString("WorkingSetExpander.4") + factoryID); //$NON-NLS-1$
		}
		if ((adaptable instanceof IWorkingSet) == false) {
			DebugUIPlugin.logErrorMessage(LaunchVariableMessages.getString("WorkingSetExpander.5") + factoryID); //$NON-NLS-1$
			return null;
		}
			
		return (IWorkingSet) adaptable;
	}	
	
	/**
	 * Returns the refresh scope attribute specified by the given launch configuration
	 * or <code>null</code> if none.
	 * 
	 * @param configuration launch configuration
	 * @return refresh scope attribute (<code>ATTR_REFRESH_SCOPE</code>)
	 * @throws CoreException if unable to access the associated attribute
	 */
	public static String getRefreshScope(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ATTR_REFRESH_SCOPE, (String) null);
	}

	/**
	 * Returns whether the refresh scope specified by the given launch
	 * configuration is recursive.
	 * 
	 * @param configuration
	 * @return whether the refresh scope is recursive
	 * @throws CoreException if unable to access the associated attribute
	 */
	public static boolean isRefreshRecursive(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ATTR_REFRESH_RECURSIVE, true);
	}	
	
	/**
	 * Creates and returns a memento for the given working set, to be used as a
	 * refresh attribute, or <code>null</code> if the working set is empty or
	 * <code>null</code>.
	 * 
	 * @param workingSet a working set, or <code>null</code>
	 * @return an equivalent refresh attribute, or <code>null</code>
	 */
	public static String getRefreshAttribute(IWorkingSet workingSet) {
		if (workingSet == null || workingSet.getElements().length == 0) {
			return null;
		}
		XMLMemento workingSetMemento = XMLMemento.createWriteRoot(IVariableConstants.TAG_LAUNCH_CONFIGURATION_WORKING_SET);
		IPersistableElement persistable = null;
		if (workingSet instanceof IPersistableElement) {
			persistable = (IPersistableElement) workingSet;
		} else if (workingSet instanceof IAdaptable) {
			persistable = (IPersistableElement) ((IAdaptable) workingSet).getAdapter(IPersistableElement.class);
		}
		if (persistable != null) {
			workingSetMemento.putString(IVariableConstants.TAG_FACTORY_ID, persistable.getFactoryId());
			persistable.saveState(workingSetMemento);
			StringWriter writer= new StringWriter();
			try {
				workingSetMemento.save(writer);
			} catch (IOException e) {
				DebugUIPlugin.log(e);
			}
			StringBuffer memento = new StringBuffer();
			memento.append("${working_set:"); //$NON-NLS-1$
			memento.append(writer.toString());
			memento.append("}"); //$NON-NLS-1$
			return memento.toString();
		}		
		return null;
	}
	
	/**
	 * Creates and returns a working set from the given refresh attribute created by
	 * the method <code>getRefreshAttribute(IWorkingSet)</code>, or <code>null</code>
	 * if none.
	 * 
	 * @param refreshAttribute a refresh attribute that represents a working set
	 * @return equivalent working set, or <code>null</code>
	 */
	public static IWorkingSet getWorkingSet(String refreshAttribute) {
		if (refreshAttribute.startsWith("${working_set:")) { //$NON-NLS-1$
			String memento = refreshAttribute.substring(14, refreshAttribute.length() - 1);
			return  restoreWorkingSet(memento);
		}
		return null;
	}
}