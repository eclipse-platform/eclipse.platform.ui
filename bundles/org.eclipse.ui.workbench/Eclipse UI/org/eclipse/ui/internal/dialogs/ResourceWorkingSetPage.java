/*******************************************************************************
 * Copyright (c) 2002, 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 * activated and used by other components.
 * *****************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.model.*;

/**
 * A resource working set page allows the user to edit an 
 * existing working set and create a new working set.
 * <p>
 * Working set elements are presented as a simple resource tree.
 * </p>
 * 
 * @since 2.0
 */
public class ResourceWorkingSetPage extends WizardPage implements IWorkingSetPage {
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;

	private Text text;
	private CheckboxTreeViewer tree;
	private IWorkingSet workingSet;
	private boolean firstCheck = false;		// set to true if selection is set in setSelection

	/**
	 * Creates a new instance of the receiver.
	 */
	public ResourceWorkingSetPage() {
		super("resourceWorkingSetPage", WorkbenchMessages.getString("ResourceWorkingSetPage.title"), WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_RESOURCEWORKINGSET_WIZ));	//$NON-NLS-1$ $NON-NLS-2$
		setDescription(WorkbenchMessages.getString("ResourceWorkingSetPage.description")); //$NON-NLS-1$
	}
	/**
	 * Overrides method in WizardPage.
	 * 
	 * @see org.eclipse.jface.wizard.WizardPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		WorkbenchHelp.setHelp(composite, IHelpContextIds.WORKING_SET_RESOURCE_PAGE);		
		Label label = new Label(composite, SWT.WRAP);
		label.setText(WorkbenchMessages.getString("ResourceWorkingSetPage.message")); //$NON-NLS-1$
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(data);
		label.setFont(font);

		text = new Text(composite, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		text.setFont(font);
		text.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			}
		);
		text.setFocus();
		
		label = new Label(composite, SWT.WRAP);
		label.setText(WorkbenchMessages.getString("ResourceWorkingSetPage.label.tree")); //$NON-NLS-1$
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(data);
		label.setFont(font);

		tree = new CheckboxTreeViewer(composite);
		tree.setUseHashlookup(true);
		tree.setContentProvider(new WorkbenchContentProvider());
		tree.setLabelProvider(
			new DecoratingLabelProvider(
				new WorkbenchLabelProvider(), 
				WorkbenchPlugin.getDefault().getWorkbench().getDecoratorManager().getLabelDecorator()));
		tree.setInput(WorkbenchPlugin.getPluginWorkspace().getRoot());
		tree.setSorter(new ResourceSorter(ResourceSorter.NAME));

		data = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		tree.getControl().setLayoutData(data);
		tree.getControl().setFont(font);

		tree.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				handleCheckStateChange(event);
			}
		});

		tree.addTreeListener(new ITreeViewerListener() {
			public void treeCollapsed(TreeExpansionEvent event) {
			}
			public void treeExpanded(TreeExpansionEvent event) {
				final Object element = event.getElement();
				if (tree.getGrayed(element) == false)
					BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
					public void run() {
						setSubtreeChecked((IContainer) element, tree.getChecked(element), false);
					}
				});
			}
		});
		initializeCheckedState();
		disableClosedProjects();
		if (workingSet != null) {
			text.setText(workingSet.getName());
		}
		setPageComplete(false);
	}
	/**
	 * Grays all closed projects
	 */
	private void disableClosedProjects() {
		IProject[] projects = WorkbenchPlugin.getPluginWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].isOpen() == false && tree.getGrayed(projects[i]) == false)
				tree.setGrayed(projects[i], true);
		}
	}
	/**
	 * Collects all checked resources in the specified container.
	 * 
	 * @param checkedResources the output, list of checked resources
	 * @param container the container to collect checked resources in
	 */
	private void findCheckedResources(List checkedResources, IContainer container) {
		IResource[] resources = null;
		try {
			resources = container.members();
		} catch (CoreException ex) {
			handleCoreException(ex, getShell(), WorkbenchMessages.getString("ResourceWorkingSetPage.error"), //$NON-NLS-1$
			WorkbenchMessages.getString("ResourceWorkingSetPage.error.updateCheckedState")); //$NON-NLS-1$
		}
		for (int i = 0; i < resources.length; i++) {
			if (tree.getGrayed(resources[i])) {
				findCheckedResources(checkedResources, (IContainer) resources[i]);
			} else if (tree.getChecked(resources[i])) {
				checkedResources.add(resources[i]);
			}
		}
	}
	/**
	 * Implements IWorkingSetPage.
	 * 
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#finish()
	 */
	public void finish() {
		ArrayList resources = new ArrayList(10);
		findCheckedResources(resources, (IContainer) tree.getInput());
		if (workingSet == null) {
			IWorkingSetManager workingSetManager = WorkbenchPlugin.getDefault().getWorkingSetManager();
			workingSet = workingSetManager.createWorkingSet(getWorkingSetName(), (IAdaptable[]) resources.toArray(new IAdaptable[resources.size()]));
		} else {
			// Add inaccessible resources
			IAdaptable[] oldItems = workingSet.getElements();

			for (int i = 0; i < oldItems.length; i++) {
				IResource oldResource = null;
				if (oldItems[i] instanceof IResource) {
					oldResource = (IResource) oldItems[i];
				} else {
					oldResource = (IResource) oldItems[i].getAdapter(IResource.class);
				}
				if (oldResource != null && oldResource.isAccessible() == false) {
					resources.add(oldResource);
				}
			}
			((WorkingSet) workingSet).setName(getWorkingSetName());
			((WorkingSet) workingSet).setElements((IAdaptable[]) resources.toArray(new IAdaptable[resources.size()]));
		}
	}	
	/**
	 * Implements IWorkingSetPage.
	 * 
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#getSelection()
	 */
	public IWorkingSet getSelection() {
		return workingSet;
	}
	/**
	 * Returns the name entered in the working set name field.
	 * 
	 * @return the name entered in the working set name field.
	 */
	private String getWorkingSetName() {
		return text.getText();
	}
	/**
	 * Called when the checked state of a tree item changes.
	 * 
	 * @param event the checked state change event.
	 */
	private void handleCheckStateChange(final CheckStateChangedEvent event) {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				IResource resource = (IResource) event.getElement();
				if (resource.isAccessible() == false) {
					MessageDialog.openInformation(
						getShell(),
						WorkbenchMessages.getString("ResourceWorkingSetPage.projectClosedDialog.title"), //$NON-NLS-1$
						WorkbenchMessages.getString("ResourceWorkingSetPage.projectClosedDialog.message")); //$NON-NLS-1$
					//$NON-NLS-2$ //$NON-NLS-1$
					tree.setChecked(resource, false);
					tree.setGrayed(resource, true);
					return;
				}
				boolean state = event.getChecked();
				tree.setGrayed(resource, false);
				if (resource instanceof IContainer) {
					setSubtreeChecked((IContainer) resource, state, true);
				}
				updateParentState(resource);
				validateInput();
			}
		});
	}
	/**
	 * Displays an error message when a CoreException occured.
	 * 
	 * @param exception the CoreException 
	 * @param shell parent shell for the message box
	 * @param title the mesage box title
	 * @param message additional error message
	 */
	private void handleCoreException(CoreException exception, Shell shell, String title, String message) {
		IStatus status = exception.getStatus();
		if (status != null) {
			ErrorDialog.openError(shell, title, message, status);
		} else {
			MessageDialog.openError(shell, WorkbenchMessages.getString("InternalError"), exception.getLocalizedMessage()); //$NON-NLS-1$
		}
	}
	/**
	 * Sets the checked state of tree items based on the initial 
	 * working set, if any.
	 */
	private void initializeCheckedState() {
		if (workingSet == null)
			return;

		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				IAdaptable[] items = workingSet.getElements();
				tree.setCheckedElements(items);
				for (int i = 0; i < items.length; i++) {
					IAdaptable item = items[i];
					IContainer container = null;
					IResource resource = null;

					if (item instanceof IContainer) {
						container = (IContainer) item;
					} else {
						container = (IContainer) item.getAdapter(IContainer.class);
					}
					if (container != null) {
						setSubtreeChecked(container, true, true);
					}
					if (item instanceof IResource) {
						resource = (IResource) item;
					} else {
						resource = (IResource) item.getAdapter(IResource.class);
					}
					updateParentState(resource);
				}
			}
		});
	}
	/**
	 * Implements IWorkingSetPage.
	 * 
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#setSelection(IWorkingSet)
	 */
	public void setSelection(IWorkingSet workingSet) {
		Assert.isNotNull(workingSet, "Working set must not be null"); //$NON-NLS-1$
		this.workingSet = workingSet;
		if (getShell() != null && text != null) {
			firstCheck = true;
			initializeCheckedState();
			disableClosedProjects();
			text.setText(workingSet.getName());
		}
	}	
	/**
	 * Sets the checked state of the container's members.
	 * 
	 * @param container the container whose children should be checked/unchecked
	 * @param state true=check all members in the container. false=uncheck all 
	 * 	members in the container.
	 * @param checkExpandedState true=recurse into sub-containers and set the 
	 * 	checked state. false=only set checked state of members of this container
	 */
	private void setSubtreeChecked(IContainer container, boolean state, boolean checkExpandedState) {
		if (container.isAccessible() == false || tree.getExpandedState(container) == false && checkExpandedState) {
			return;
		}
		IResource[] members = null;
		try {
			members = container.members();
		} catch (CoreException ex) {
			handleCoreException(ex, getShell(), WorkbenchMessages.getString("ResourceWorkingSetPage.error"), //$NON-NLS-1$
			WorkbenchMessages.getString("ResourceWorkingSetPage.error.updateCheckedState")); //$NON-NLS-1$
		}
		for (int i = members.length - 1; i >= 0; i--) {
			IResource element = members[i];
			if (state) {
				tree.setChecked(element, true);
				tree.setGrayed(element, false);
			} else {
				tree.setGrayChecked(element, false);
			}
			if (element instanceof IContainer) {
				setSubtreeChecked((IContainer) element, state, true);
			}
		}
	}
	/**
	 * Check and gray the resource parent if all resources of the 
	 * parent are checked.
	 * 
	 * @param child the resource whose parent checked state should 
	 * 	be set.
	 */
	private void updateParentState(IResource child) {
		if (child == null || child.getParent() == null)
			return;
		if (child.isAccessible() == false) {
			if (child.getType() == IResource.PROJECT) {
				tree.setGrayChecked(child, true);
				return;
			}
			else {
				updateParentState(child.getParent());
			}
		}

		IContainer parent = child.getParent();
		boolean childChecked = false;
		IResource[] members = null;
		try {
			members = parent.members();
		} catch (CoreException ex) {
			handleCoreException(ex, getShell(), WorkbenchMessages.getString("ResourceWorkingSetPage.error"), //$NON-NLS-1$
			WorkbenchMessages.getString("ResourceWorkingSetPage.error.updateCheckedState")); //$NON-NLS-1$
		}
		for (int i = members.length - 1; i >= 0; i--) {
			if (tree.getChecked(members[i]) || tree.getGrayed(members[i])) {
				childChecked = true;
				break;
			}
		}
		tree.setGrayChecked(parent, childChecked);
		updateParentState(parent);
	}
	/**
	 * Validates the working set name and the checked state of the 
	 * resource tree.
	 */
	private void validateInput() {
		String errorMessage = null;	//$NON-NLS-1$
		String newText = text.getText();

		if (newText.equals(newText.trim()) == false) {
			errorMessage = WorkbenchMessages.getString("ResourceWorkingSetPage.warning.nameWhitespace"); //$NON-NLS-1$
		}
		else
		if (firstCheck) {
			firstCheck = false;
			return;
		}
		if (newText.equals("")) { //$NON-NLS-1$
			errorMessage = WorkbenchMessages.getString("ResourceWorkingSetPage.warning.nameMustNotBeEmpty"); //$NON-NLS-1$
		}
		if (errorMessage == null && (workingSet == null || newText.equals(workingSet.getName()) == false)) {
			IWorkingSet[] workingSets = WorkbenchPlugin.getDefault().getWorkingSetManager().getWorkingSets();
			for (int i = 0; i < workingSets.length; i++) {
				if (newText.equals(workingSets[i].getName())) {
					errorMessage = WorkbenchMessages.getString("ResourceWorkingSetPage.warning.workingSetExists"); //$NON-NLS-1$
				}
			}
		}
		if (errorMessage == null && tree.getCheckedElements().length == 0) {
			errorMessage = WorkbenchMessages.getString("ResourceWorkingSetPage.warning.resourceMustBeChecked"); //$NON-NLS-1$
		}
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}
}
