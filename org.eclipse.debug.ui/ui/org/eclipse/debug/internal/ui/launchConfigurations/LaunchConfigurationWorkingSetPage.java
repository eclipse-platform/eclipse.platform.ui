package org.eclipse.debug.internal.ui.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * The Launch Configuration working set page allows the user to create
 * and edit a Launch Configuration working set.
 * <p>
 * Working set elements are presented as a tree with launch configuration
 * types as the parents and launch configurations as the children.
 * </p>
 * 
 * @since 2.1
 */
public class LaunchConfigurationWorkingSetPage extends WizardPage implements IWorkingSetPage {

	/*
	 * Constants
	 */
	private final static String PAGE_TITLE= LaunchConfigurationsMessages.getString("LaunchConfigurationWorkingSetPage.Launch_Configuration_Working_Set_1"); //$NON-NLS-1$
	private final static String PAGE_ID= "launchConfigurationWorkingSetPage"; //$NON-NLS-1$
	
	private Text fWorkingSetName;
	private CheckboxTreeViewer fTree;
	private ITreeContentProvider fTreeContentProvider;
	
	private boolean fFirstCheck;
	private IWorkingSet fWorkingSet;

	/**
	 * Default constructor.
	 */
	public LaunchConfigurationWorkingSetPage() {
		super(PAGE_ID, PAGE_TITLE, null);
		fFirstCheck= true;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		Label label= new Label(composite, SWT.WRAP);
		label.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationWorkingSetPage.Working_set_name_2"));  //$NON-NLS-1$
		GridData gd= new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);

		fWorkingSetName= new Text(composite, SWT.SINGLE | SWT.BORDER);
		fWorkingSetName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		fWorkingSetName.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			}
		);
		fWorkingSetName.setFocus();
		
		label= new Label(composite, SWT.WRAP);
		label.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationWorkingSetPage.Working_set_content_3"));  //$NON-NLS-1$
		gd= new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);

		fTree= new CheckboxTreeViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		gd= new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		gd.heightHint= convertHeightInCharsToPixels(15);
		fTree.getControl().setLayoutData(gd);
		
		fTreeContentProvider= new LaunchConfigurationTreeContentProvider(null, getShell());
		fTree.setContentProvider(fTreeContentProvider);
		
		fTree.setLabelProvider(DebugUITools.newDebugModelPresentation());		
		fTree.setSorter(new WorkbenchViewerSorter());
		fTree.setUseHashlookup(true);		
		fTree.setInput(ResourcesPlugin.getWorkspace().getRoot());

		fTree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleCheckStateChange(event);
			}
		});

		validateInput();

		// Set help context for the page 
		WorkbenchHelp.setHelp(getControl(), IDebugHelpContextIds.WORKING_SET_PAGE);
	}

	/**
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#getSelection()
	 */
	public IWorkingSet getSelection() {
		return fWorkingSet;
	}

	/**
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#setSelection(IWorkingSet)
	 */
	public void setSelection(IWorkingSet workingSet) {
		fWorkingSet= workingSet;
		if (getShell() != null && fWorkingSetName != null) {
			fFirstCheck= false;
			fWorkingSetName.setText(fWorkingSet.getName());
			initializeCheckedState();
			validateInput();
		}
	}

	/**
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#finish()
	 */
	public void finish() {
		String workingSetName= fWorkingSetName.getText();
		ArrayList elements= new ArrayList(10);
		findCheckedElements(elements, fTree.getInput());
		if (fWorkingSet == null) {
			IWorkingSetManager workingSetManager= getWorkingSetManager();
			fWorkingSet= workingSetManager.createWorkingSet(workingSetName, (IAdaptable[])elements.toArray(new IAdaptable[elements.size()]));
		} else {
			// Add inaccessible resources
			IAdaptable[] oldItems= fWorkingSet.getElements();

			for (int i= 0; i < oldItems.length; i++) {
				IResource oldResource= null;
				if (oldItems[i] instanceof IResource) {
					oldResource= (IResource)oldItems[i];
				} else {
					oldResource= (IResource)oldItems[i].getAdapter(IResource.class);
				}
				if (oldResource != null && oldResource.isAccessible() == false) {
					elements.add(oldResource);
				}
			}
			
			fWorkingSet.setName(workingSetName);
			fWorkingSet.setElements((IAdaptable[]) elements.toArray(new IAdaptable[elements.size()]));
		}
	}

	private void validateInput() {
		String errorMessage= null;
		String newText= fWorkingSetName.getText().trim();

		if (newText.length() < 1) { 
			errorMessage= LaunchConfigurationsMessages.getString("LaunchConfigurationWorkingSetPage.Name_must_not_be_empty_4"); //$NON-NLS-1$
		}

		if (errorMessage == null && (fWorkingSet == null || newText.equals(fWorkingSet.getName()) == false)) {
			IWorkingSet[] workingSets= getWorkingSetManager().getWorkingSets();
			for (int i= 0; i < workingSets.length; i++) {
				if (newText.equals(workingSets[i].getName())) {
					errorMessage= LaunchConfigurationsMessages.getString("LaunchConfigurationWorkingSetPage.A_working_set_with_this_name_already_exists_5"); //$NON-NLS-1$
				}
			}
		}
		if (errorMessage == null && fTree.getCheckedElements().length == 0) {
			errorMessage= LaunchConfigurationsMessages.getString("LaunchConfigurationWorkingSetPage.At_least_one_launch_configuration_or_launch_configuration_type_must_be_checked_6"); //$NON-NLS-1$
		}

		if (fFirstCheck) {
			fFirstCheck= false;
		}
		else {
			setErrorMessage(errorMessage);
		}

		setPageComplete(errorMessage == null);
	}
	
	private void findCheckedElements(List checkedElements, Object parent) {
		Object[] children= fTreeContentProvider.getChildren(parent);
		for (int i= 0; i < children.length; i++) {
			if (fTree.getGrayed(children[i])) {
				findCheckedElements(checkedElements, children[i]);
			}
			else if (fTree.getChecked(children[i])) {
				checkedElements.add(children[i]);
			}
		}
	}

	private void handleCheckStateChange(final CheckStateChangedEvent event) {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				IAdaptable element= (IAdaptable)event.getElement();
				boolean state= event.getChecked();		
				if (isExpandable(element)) {
					setSubtreeChecked(element, state, true);
				}
					
				updateParentState(element, state);
				validateInput();
			}
		});
	}
	
	private void setSubtreeChecked(Object parent, boolean state, boolean checkExpandedState) {
		if (!(parent instanceof IAdaptable)) {
			return;
		}
		
		Object[] children= fTreeContentProvider.getChildren(parent);
		for (int i= children.length - 1; i >= 0; i--) {
			Object element= children[i];
			if (state) {
				fTree.setChecked(element, true);
				fTree.setGrayed(element, false);
			}
			else {
				fTree.setGrayChecked(element, false);
			}
			if (isExpandable(element)) {
				setSubtreeChecked(element, state, true);
			}
		}
	}

	private void updateParentState(Object child, boolean baseChildState) {
		if (child == null) {			
			return;
		}
		
		Object parent= fTreeContentProvider.getParent(child);
		if (parent == null) {
			return;
		}

		boolean allSameState= true;
		Object[] children= null;
		children= fTreeContentProvider.getChildren(parent);

		for (int i= children.length -1; i >= 0; i--) {
			if (fTree.getChecked(children[i]) != baseChildState || fTree.getGrayed(children[i])) {
				allSameState= false;
				break;
			}
		}
	
		fTree.setGrayed(parent, !allSameState);
		fTree.setChecked(parent, !allSameState || baseChildState);
		
		updateParentState(parent, baseChildState);
	}

	private void initializeCheckedState() {
		if (fWorkingSet == null) {
			return;
		}

		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				Object[] elements= fWorkingSet.getElements();
				fTree.setCheckedElements(elements);
				for (int i= 0; i < elements.length; i++) {
					Object element= elements[i];
					if (isExpandable(element)) {
						setSubtreeChecked(element, true, true);
					}
					updateParentState(element, true);
				}
			}
		});
	}
	
	private boolean isExpandable(Object element) {
		return (element instanceof ILaunchConfigurationType);
	}

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	private IWorkingSetManager getWorkingSetManager() {
		return PlatformUI.getWorkbench().getWorkingSetManager()		;
	}

}
