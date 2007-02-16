/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.preferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationComparator;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.dialogs.PropertyPage;

import com.ibm.icu.text.MessageFormat;

/**
 * Displays default launch configuration settings for a selected resource - associated launch configurations.
 * 
 * @see {@link PropertyPage}
 * @see {@link ILaunchConfiguration}
 * @see {@link org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsDialog}
 * @see {@link IDebugHelpContextIds#DEFAULT_LAUNCHCONFIGURATION_PROPERTY_PAGE}
 * 
 * EXPERIMENTAL
 * CONTEXTLAUNCHING
 * 
 * @since 3.3
 */
public class RunDebugPropertiesPage extends PropertyPage {
	/**
	 * Set of configurations to be deleted
	 */
	private Set fDeletedConfigurations = new HashSet();
	
	/**
	 * Set of original default candidates for the resource
	 */
	private Set fOriginalCandidates;
	
	/**
	 * Holds configurations that need to be saved when the page closes
	 */
	private Set fChangedConfigurations = new HashSet();
	
	/**
	 * List of the applicable launch config types for the backing resource
	 */
	private List fTypeCandidates = null;
	
	//widgets
	private CheckboxTableViewer fViewer;
	private Button fNewButton = null;
	private Button fEditButton = null;
	private Button fDuplicateButton = null;
	private Button fDeleteButton = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IDebugHelpContextIds.DEFAULT_LAUNCHCONFIGURATION_PROPERTY_PAGE);
		collectConfigCandidates(getResource());
		Composite topComposite = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_BOTH);
		
		SWTFactory.createWrapLabel(topComposite, DebugPreferencesMessages.DefaultLaunchConfigurationsPropertiesPage_0, 2, 300);
		SWTFactory.createVerticalSpacer(topComposite, 2);
		SWTFactory.createWrapLabel(topComposite, MessageFormat.format(DebugPreferencesMessages.DefaultLaunchConfigurationsPropertiesPage_1, new String[]{getResource().getName()}), 2, 300);
		fViewer = createViewer(topComposite);
		
		Composite buttonComp = SWTFactory.createComposite(topComposite, 1, 1, GridData.FILL_VERTICAL);
		GridLayout layout = (GridLayout) buttonComp.getLayout();
		layout.marginHeight = 0;
		fNewButton = SWTFactory.createPushButton(buttonComp, DebugPreferencesMessages.DefaultLaunchConfigurationsPropertiesPage_2, null);
		fNewButton.setToolTipText(DebugPreferencesMessages.DefaultLaunchConfigurationsPropertiesPage_3);
		fNewButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				handleNew();
			}
		});
		
		fDuplicateButton = SWTFactory.createPushButton(buttonComp, DebugPreferencesMessages.DefaultLaunchConfigurationsPropertiesPage_4, null);
		fDuplicateButton.setToolTipText(DebugPreferencesMessages.DefaultLaunchConfigurationsPropertiesPage_5);
		fDuplicateButton.setEnabled(false);
		fDuplicateButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				handleCopy();
			}
		});
		fEditButton = SWTFactory.createPushButton(buttonComp, DebugPreferencesMessages.DefaultLaunchConfigurationsPropertiesPage_6, null);
		fEditButton.setToolTipText(DebugPreferencesMessages.DefaultLaunchConfigurationsPropertiesPage_7);
		fEditButton.setEnabled(false);
		fEditButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				handleEdit();
			}
		});
		fDeleteButton = SWTFactory.createPushButton(buttonComp, DebugPreferencesMessages.DefaultLaunchConfigurationsPropertiesPage_8, null);
		fDeleteButton.setToolTipText(DebugPreferencesMessages.DefaultLaunchConfigurationsPropertiesPage_9);
		fDeleteButton.setEnabled(false);
		fDeleteButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				handleDelete();
			}
		});
		
		fViewer.setSelection(new StructuredSelection());
		applyDialogFont(topComposite);
		return topComposite;
	}

	/**
	 * Creates and returns the viewer that will display the possible default configurations.
	 * 
	 * @param parent parent composite to create the viewer in
	 * @return viewer viewer that will display possible default configurations
	 */
	protected CheckboxTableViewer createViewer(Composite parent){
		CheckboxTableViewer viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		viewer.setLabelProvider(new DefaultLabelProvider());
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setComparator(new LaunchConfigurationComparator());
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					fViewer.setCheckedElements(new Object[] {event.getElement()});
				}
				else{
					fViewer.setCheckedElements(new Object[] {});
				}
			}
		});
		Table builderTable = viewer.getTable();
		GridData tableGridData = new GridData(GridData.FILL_BOTH);
		tableGridData.heightHint = 300;
		builderTable.setLayoutData(tableGridData);
		
		IResource resource = getResource();
		viewer.setInput(collectConfigCandidates(resource));
		try {
			ILaunchConfiguration configuration = getLaunchManager().getDefaultConfiguration(resource);
			if (configuration != null) {
				Iterator iterator = fOriginalCandidates.iterator();
				while (iterator.hasNext()) {
					ILaunchConfigurationWorkingCopy wc = (ILaunchConfigurationWorkingCopy) iterator.next();
					if (configuration.equals(wc.getOriginal())) {
						viewer.setChecked(wc, true);
						break;
					}
				}
			}
		} catch (CoreException e) {setErrorMessage(e.getMessage());}
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				boolean empty = event.getSelection().isEmpty();
				fEditButton.setEnabled(!empty);
				fDuplicateButton.setEnabled(!empty);
				fDeleteButton.setEnabled(!empty);
			}
		});
		
		return viewer;
	}

	/**
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	public void dispose() {
		if(fOriginalCandidates != null) {
			fOriginalCandidates.clear();
			fOriginalCandidates = null;
		}
		if(fChangedConfigurations != null) {
			fChangedConfigurations.clear();
			fChangedConfigurations = null;
		}
		super.dispose();
	}

	/**
	 * Returns the viewer displaying possible default configurations.
	 * 
	 * @return viewer
	 */
	protected CheckboxTableViewer getViewer() {
		return fViewer;
	}
		
	/**
	 * Returns the launch manager
	 * @return the launch manager
	 */
	protected LaunchManager getLaunchManager() {
		return (LaunchManager) DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * Collects the applicable launch configuration types for the backing resource.
	 * Default implementation uses the launch shortcut evaluation expressions and leverages the 
	 * mapping of launch shortcut to config type id to derive the applicable types.
	 * @return the listing of applicable launch configuration types for the backing resource
	 */
	protected List collectTypeCandidates() {
		if(fTypeCandidates == null) {
			fTypeCandidates = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getApplicableConfigurationTypes(getResource());
			Collections.sort(fTypeCandidates, new Comparator() {
				public int compare(Object o1, Object o2) {
					ILaunchConfigurationType t1 = (ILaunchConfigurationType) o1;
					ILaunchConfigurationType t2 = (ILaunchConfigurationType) o2;
					return t1.getName().compareTo(t2.getName());
				}
			});
		}
		return fTypeCandidates;
	}
	
	/**
	 * Returns a set of potential default configurations candidates for the given
	 * resource. The configurations are working copies.
	 *  
	 * @param resource resource
	 * @return list of default candidates
	 */
	protected Set collectConfigCandidates(IResource resource) {
		if(fOriginalCandidates == null) {
			fOriginalCandidates = new HashSet();
			try {
				List configs = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getApplicableLaunchConfigurations(resource);
				for(Iterator iter = configs.iterator(); iter.hasNext();) {
					fOriginalCandidates.add(((ILaunchConfiguration)iter.next()).getWorkingCopy());
				}
			}
			catch(CoreException ce) {DebugUIPlugin.log(ce);}
		}
		return fOriginalCandidates;
	}
	
	
	
	/**
	 * Returns the resource this property page is open on.
	 * 
	 * @return resource
	 */
	protected IResource getResource() {
		Object element = getElement();
		IResource resource = null;
		if (element instanceof IResource) {
			resource = (IResource) element;
		} else if (element instanceof IAdaptable) {
			resource = (IResource) ((IAdaptable)element).getAdapter(IResource.class);
		}
		return resource;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		Object[] checked = fViewer.getCheckedElements();
		try {
			ILaunchConfiguration def = null;
			if (checked.length == 1) {
					def = (ILaunchConfiguration) checked[0];
					def = ((ILaunchConfigurationWorkingCopy)def).doSave();
			}
			DebugPlugin.getDefault().getLaunchManager().setDefaultConfiguration(getResource(), def);
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
			return false;
		}
	//delete 
		Iterator iter = fDeletedConfigurations.iterator();
		while (iter.hasNext()) {
			ILaunchConfigurationWorkingCopy currentConfig = (ILaunchConfigurationWorkingCopy) iter.next();
			try{			
				if (currentConfig.getOriginal() != null){
					currentConfig.getOriginal().delete();
				}
			} catch (CoreException e) {
				DebugPlugin.logMessage("Problem deleting configuration " + currentConfig.getName(), e); //$NON-NLS-1$
			}
		}
	//add
		iter = fChangedConfigurations.iterator();
		while (iter.hasNext()) {
			ILaunchConfigurationWorkingCopy currentConfig = (ILaunchConfigurationWorkingCopy) iter.next();
			try{
				currentConfig.doSave();
			} catch (CoreException e) {
				DebugPlugin.logMessage("Problem saving changes to configuration " + currentConfig.getName(), e); //$NON-NLS-1$
			}
		}
		
		return super.performOk();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		fViewer.setAllChecked(false);
		setErrorMessage(null);
		setValid(true);
		super.performDefaults();
	}
	
	/**
	 * Returns the names of the launch configurations passed in as original input to the tree viewer
	 * @return the names of the original launch configurations
	 */
	private Set getConfigurationNames() {
		Set names = new HashSet();
		Iterator iter = fOriginalCandidates.iterator();
		while (iter.hasNext()) {
			names.add(((ILaunchConfiguration)iter.next()).getName());
		}
		iter = fChangedConfigurations.iterator();
		while (iter.hasNext()) {
			names.add(((ILaunchConfiguration)iter.next()).getName());
		}
		return names;
	}
	
	/**
	 * Returns selected configurations.
	 * 
	 * @return selected configurations
	 */
	private ILaunchConfigurationWorkingCopy[] getSelectedConfigurations() {
		IStructuredSelection ss = (IStructuredSelection) fViewer.getSelection();
		return (ILaunchConfigurationWorkingCopy[]) ss.toList().toArray(new ILaunchConfigurationWorkingCopy[ss.size()]);
	}

	/**
	 * Copy the selection
	 */
	private void handleCopy() {
		ILaunchConfigurationWorkingCopy configuration = getSelectedConfigurations()[0];
		try {
			ILaunchConfigurationWorkingCopy copy = configuration.copy(
					((LaunchManager)DebugPlugin.getDefault().getLaunchManager()).generateUniqueLaunchConfigurationNameFrom(configuration.getName(), getConfigurationNames()));
			copy.setAttributes(configuration.getAttributes());
			fChangedConfigurations.add(copy);
			fViewer.add(copy);
			fViewer.setSelection(new StructuredSelection(copy));
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
		}
	}

	/**
	 * Delete the selection
	 */
	private void handleDelete() {
		Table table = fViewer.getTable();
		int[] indices = table.getSelectionIndices();
		Arrays.sort(indices);
		ILaunchConfiguration[] configurations = getSelectedConfigurations();
		for (int i = 0; i < configurations.length; i++) {
			fDeletedConfigurations.add(configurations[i]);
			fChangedConfigurations.remove(configurations[i]);
			fViewer.remove(configurations[i]);
		}
		if (indices[0] < table.getItemCount()) {
			fViewer.setSelection(new StructuredSelection(table.getItem(indices[0]).getData()));
		} else if (table.getItemCount() > 0) {
			fViewer.setSelection(new StructuredSelection(table.getItem(table.getItemCount() - 1).getData()));
		}
	}

	/**
	 * Edit the selection
	 */
	private void handleEdit() {
		ILaunchConfigurationWorkingCopy config = getSelectedConfigurations()[0]; 
		if(edit(config) == IDialogConstants.OK_ID) {
			fChangedConfigurations.add(config);
			fViewer.refresh();
		}
	}

	/**
	 * Edits the given configuration as a nested working copy.
	 * Returns the code from the dialog used to edit the configuration.
	 * 
	 * @param configuration
	 * @return dialog return code - OK or CANCEL
	 */
	private int edit(ILaunchConfigurationWorkingCopy configuration) {
		return DebugUIPlugin.openLaunchConfigurationPropertiesDialog(getShell(), configuration, IDebugUIConstants.ID_RUN_LAUNCH_GROUP, getConfigurationNames(), null);
	}

	/**
	 * Create a new configuration
	 */
	private void handleNew() {
		ListDialog dialog = new ListDialog(getShell());
		dialog.setTitle(DebugPreferencesMessages.DefaultLaunchConfigurationsPropertiesPage_11);
		dialog.setContentProvider(new ArrayContentProvider());
		dialog.setLabelProvider(new DefaultLabelProvider());
		dialog.setAddCancelButton(true);
		dialog.setMessage(DebugPreferencesMessages.DefaultLaunchConfigurationsPropertiesPage_12);
		dialog.setInput(collectTypeCandidates());
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				ILaunchConfigurationType type = (ILaunchConfigurationType) result[0];
				try {
					ILaunchConfigurationWorkingCopy wc = type.newInstance(null, 
							((LaunchManager)DebugPlugin.getDefault().getLaunchManager()).
							generateUniqueLaunchConfigurationNameFrom("New_configuration", getConfigurationNames())); //$NON-NLS-1$
					if (edit(wc) == Window.OK) {
						fChangedConfigurations.add(wc);
						fViewer.add(wc);
						fViewer.setSelection(new StructuredSelection(wc));
					}
				} catch (CoreException e) {
					setErrorMessage(e.getMessage());
				}
			}
		}
	}	
}
