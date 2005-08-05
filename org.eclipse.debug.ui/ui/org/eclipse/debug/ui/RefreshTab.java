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
package org.eclipse.debug.ui;


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
import org.eclipse.debug.internal.ui.stringsubstitution.SelectedResourceManager;
import org.eclipse.debug.internal.ui.stringsubstitution.StringSubstitutionMessages;
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
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;

/**
 * A launch configuration tab which allows the user to specify
 * which resources should be refreshed when the launch
 * terminates.
 * <p>
 * This class may be instantiated; this class is not intended
 * to be subclassed.
 * </p>
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
	
	// indicates no working set has been selected
	private static final String NO_WORKING_SET = "NONE"; //$NON-NLS-1$
	
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
	 * XML tag used to designate the root of the persisted IWorkingSet
	 */
	private static final String TAG_LAUNCH_CONFIGURATION_WORKING_SET= "launchConfigurationWorkingSet"; //$NON-NLS-1$

	/**
	 * XML tag used for setting / getting the factory ID of the persisted IWorkingSet
	 * Bug 37143
	 */	
	private static final String TAG_FACTORY_ID = "factoryID"; //$NON-NLS-1$
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_REFRESH_TAB);
		
		GridLayout layout = new GridLayout();
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gd);
		mainComposite.setFont(parent.getFont());
		
		fRefreshButton = createCheckButton(mainComposite, StringSubstitutionMessages.RefreshTab_31); 
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

		SelectionAdapter adapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button)e.getSource()).getSelection()) {
					updateEnabledState();
					updateLaunchConfigurationDialog();
				}
			}
		};
		
		fWorkspaceButton = createRadioButton(fGroup, StringSubstitutionMessages.RefreshTab_32); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fWorkspaceButton.setLayoutData(gd);
		fWorkspaceButton.addSelectionListener(adapter);

		fResourceButton = createRadioButton(fGroup, StringSubstitutionMessages.RefreshTab_33); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fResourceButton.setLayoutData(gd);
		fResourceButton.addSelectionListener(adapter);

		fProjectButton = createRadioButton(fGroup, StringSubstitutionMessages.RefreshTab_34); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fProjectButton.setLayoutData(gd);		
		fProjectButton.addSelectionListener(adapter);

		fContainerButton = createRadioButton(fGroup, StringSubstitutionMessages.RefreshTab_35); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		fContainerButton.setLayoutData(gd);
		fContainerButton.addSelectionListener(adapter);
				
		fWorkingSetButton = createRadioButton(fGroup, StringSubstitutionMessages.RefreshTab_36); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		fWorkingSetButton.setLayoutData(gd);
		fWorkingSetButton.addSelectionListener(adapter);		
		
		fSelectButton = createPushButton(fGroup, StringSubstitutionMessages.RefreshTab_37, null); 
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
			fWorkingSet = workingSetManager.createWorkingSet(StringSubstitutionMessages.RefreshTab_40, new IAdaptable[0]); 
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
		fRecursiveButton = createCheckButton(parent, StringSubstitutionMessages.RefreshTab_0); 
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		fRecursiveButton.setLayoutData(data);
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
					IResource[] resources = getRefreshResources(scope);
					IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
					fWorkingSet = workingSetManager.createWorkingSet(StringSubstitutionMessages.RefreshTab_40, resources);					 
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
		return StringSubstitutionMessages.RefreshTab_6; 
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
		if (fWorkingSetButton.getSelection() && (fWorkingSet == null || fWorkingSet.getElements().length == 0)) {
			setErrorMessage(StringSubstitutionMessages.RefreshTab_42); 
			return false;
		}
		return true;
	}
	
	/**
	 * Refreshes the resources as specified by the given launch configuration.
	 * 
	 * @param configuration launch configuration
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
			resources = getRefreshResources(scope);
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
	
		monitor.beginTask(StringSubstitutionMessages.RefreshTab_7, 
			resources.length);
	
		MultiStatus status = new MultiStatus(DebugUIPlugin.getUniqueIdentifier(), 0, StringSubstitutionMessages.RefreshTab_8, null); 
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
	 * @return collection of resources referred to by the refresh scope attribute
	 * @throws CoreException if unable to resolve a set of resources
	 */
	public static IResource[] getRefreshResources(String scope) throws CoreException {
		if (scope.startsWith("${resource:")) { //$NON-NLS-1$
			// This is an old format that is replaced with 'working_set'
			String pathString = scope.substring(11, scope.length() - 1);
			Path path = new Path(pathString);
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
			if (resource == null) {
				throw new CoreException(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, MessageFormat.format(StringSubstitutionMessages.RefreshTab_38, new String[]{pathString}), null)); 
			} 
			return new IResource[]{resource};
		} else if (scope.startsWith("${working_set:")) { //$NON-NLS-1$
			IWorkingSet workingSet =  getWorkingSet(scope);
			if (workingSet == null) {
				throw new CoreException(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IDebugUIConstants.INTERNAL_ERROR, StringSubstitutionMessages.RefreshTab_39, null)); 
			} 
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
	 * the mementoString.
	 * 
	 * @param mementoString The string memento of the working set
	 * @return the restored working set or <code>null</code> if problems occurred restoring the
	 * working set.
	 */
	private static IWorkingSet restoreWorkingSet(String mementoString) {
		if (NO_WORKING_SET.equals(mementoString)) {
			return null;
		}
		StringReader reader= new StringReader(mementoString);
		XMLMemento memento= null;
		try {
			memento = XMLMemento.createReadRoot(reader);
		} catch (WorkbenchException e) {
			DebugUIPlugin.log(e);
			return null;
		}

		IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
		return workingSetManager.createWorkingSet(memento);
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
	 * refresh attribute.
	 * 
	 * @param workingSet a working set, or <code>null</code>
	 * @return an equivalent refresh attribute
	 */
	public static String getRefreshAttribute(IWorkingSet workingSet) {
		String set = null;
		if (workingSet == null || workingSet.getElements().length == 0) {
			set = NO_WORKING_SET;
		} else {
			XMLMemento workingSetMemento = XMLMemento.createWriteRoot(TAG_LAUNCH_CONFIGURATION_WORKING_SET);
			workingSetMemento.putString(RefreshTab.TAG_FACTORY_ID, workingSet.getFactoryId());
			workingSet.saveState(workingSetMemento);
			StringWriter writer= new StringWriter();
			try {
				workingSetMemento.save(writer);
			} catch (IOException e) {
				DebugUIPlugin.log(e);
			}
			set = writer.toString();
		}
		if (set != null) {
			StringBuffer memento = new StringBuffer();
			memento.append("${working_set:"); //$NON-NLS-1$
			memento.append(set);
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
