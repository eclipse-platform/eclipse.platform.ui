/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.model.*;

class WorkingSetDialog extends InputDialog {

	private static class WorkingSetNameInputValidator implements IInputValidator {

		private String initalName;
		private boolean firstCheck = true;
		private CheckboxTreeViewer tree;

		public WorkingSetNameInputValidator(String initialName) {
			Assert.isNotNull(initialName, "initial name must not be null"); //$NON-NLS-1$
			this.initalName = initialName;
		}

		private void setTree(CheckboxTreeViewer tree) {
			this.tree = tree;
		}

		public String isValid(String newText) {
			if (newText == null || newText.equals("")) { //$NON-NLS-1$
				if (initalName.equals("") && firstCheck) { //$NON-NLS-1$
					firstCheck = false;
					return " "; //$NON-NLS-1$
				}
				return WorkbenchMessages.getString("WorkingSetDialog.warning.nameMustNotBeEmpty"); //$NON-NLS-1$
			}
			if (!newText.equals(initalName)) {
				IWorkingSet[] workingSets = WorkingSet.getWorkingSets();
				for (int i = 0; i < workingSets.length; i++) {
					if (newText.equals(workingSets[i].getName()))
						return WorkbenchMessages.getString("WorkingSetDialog.warning.workingSetExists"); //$NON-NLS-1$
				}
			}

			if (tree.getCheckedElements().length == 0)
				return WorkbenchMessages.getString("WorkingSetDialog.warning.resourceMustBeChecked"); //$NON-NLS-1$

			return null;
		}
	}

	// Widgets constants
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;

	private IWorkingSet workingSet;
	private CheckboxTreeViewer tree;

	WorkingSetDialog(Shell parentShell) {
		this(parentShell, ""); //$NON-NLS-1$
	}

	WorkingSetDialog(Shell parentShell, IWorkingSet workingSet) {
		this(parentShell, workingSet.getName());
		this.workingSet = workingSet;
	}

	private WorkingSetDialog(Shell parentShell, String initialSelection) {
		super(
			parentShell,
			WorkbenchMessages.getString("WorkingSetDialog.title"),
			WorkbenchMessages.getString("WorkingSetDialog.message"),
			initialSelection,
			new WorkingSetNameInputValidator(initialSelection));
		//$NON-NLS-2$ //$NON-NLS-1$
	}

	/*
	 * Overrides method from Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);

		tree = new CheckboxTreeViewer(composite);
		tree.setUseHashlookup(true);
		tree.setContentProvider(new WorkbenchContentProvider());
		tree.setLabelProvider(new WorkbenchLabelProvider());
		tree.setInput(WorkbenchPlugin.getPluginWorkspace().getRoot());
		tree.setSorter(new WorkbenchViewerSorter());

		GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		gd.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		gd.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		tree.getControl().setLayoutData(gd);

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
				if (!tree.getGrayed(element))
					BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
					public void run() {
						setSubtreeChecked((IContainer) element, tree.getChecked(element), false);
					}
				});
			}
		});

		initializeCheckedState();
		((WorkingSetNameInputValidator) getValidator()).setTree(tree);
		disableClosedProjects();

		return composite;
	}

	private void disableClosedProjects() {
		IProject[] projects = WorkbenchPlugin.getPluginWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (!projects[i].isOpen())
				tree.setGrayed(projects[i], true);
		}
	}

	/*
	 * Overrides method from Dialog
	 */
	protected void okPressed() {
		ArrayList resources = new ArrayList(10);
		findCheckedResources(resources, (IContainer) tree.getInput());
		if (workingSet == null)
			workingSet = new WorkingSet(getText().getText(), resources.toArray());
		else if (workingSet instanceof WorkingSet) {
			// Add inaccessible resources
			IAdaptable[] oldItems = workingSet.getItems();

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
			// TODO: Create a new working set?
			 ((WorkingSet) workingSet).setName(getText().getText());
			((WorkingSet) workingSet).setItems(resources.toArray());
		}
		super.okPressed();
	}

	IWorkingSet getWorkingSet() {
		return workingSet;
	}

	private void findCheckedResources(List checkedResources, IContainer container) {
		IResource[] resources = null;
		try {
			resources = container.members();
		} catch (CoreException ex) {
			handleCoreException(
				ex,
				getShell(),
				WorkbenchMessages.getString("WorkingSetDialog.error"),
				WorkbenchMessages.getString("WorkingSetDialog.error.updateCheckedState"));
			//$NON-NLS-2$ //$NON-NLS-1$
		}
		for (int i = 0; i < resources.length; i++) {
			if (tree.getGrayed(resources[i]))
				findCheckedResources(checkedResources, (IContainer) resources[i]);
			else if (tree.getChecked(resources[i]))
				checkedResources.add(resources[i]);
		}
	}

	//--- Checked state handling --------------------------	

	void handleCheckStateChange(final CheckStateChangedEvent event) {
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				IResource resource = (IResource) event.getElement();
				if (!resource.isAccessible()) {
					MessageDialog.openInformation(
						getShell(),
						WorkbenchMessages.getString("WorkingSetDialog.projectClosedDialog.title"),
						WorkbenchMessages.getString("WorkingSetDialog.projectClosedDialog.message"));
					//$NON-NLS-2$ //$NON-NLS-1$
					tree.setChecked(resource, false);
					tree.setGrayed(resource, true);
					return;
				}
				boolean state = event.getChecked();
				tree.setGrayed(resource, false);
				if (resource instanceof IContainer)
					setSubtreeChecked((IContainer) resource, state, true);

				updateParentState(resource, state);
			}
		});

		String errorMessage = getValidator().isValid(getText().getText());
		if (errorMessage == null)
			errorMessage = "";
		getErrorMessageLabel().setText(errorMessage);
		getOkButton().setEnabled(errorMessage.length() == 0);
	}

	private void handleCoreException(CoreException exception, Shell shell, String title, String message) {
		IStatus status = exception.getStatus();
		if (status != null) {
			ErrorDialog.openError(shell, title, message, status);
		} else {
			MessageDialog.openError(shell, WorkbenchMessages.getString("InternalError"), exception.getLocalizedMessage());
		}
	}

	private void setSubtreeChecked(IContainer container, boolean state, boolean checkExpandedState) {
		if (!container.isAccessible() || !tree.getExpandedState(container) && checkExpandedState)
			return;

		IResource[] members = null;
		try {
			members = container.members();
		} catch (CoreException ex) {
			handleCoreException(
				ex,
				getShell(),
				WorkbenchMessages.getString("WorkingSetDialog.error"),
				WorkbenchMessages.getString("WorkingSetDialog.error.updateCheckedState"));
			//$NON-NLS-2$ //$NON-NLS-1$
		}
		for (int i = members.length - 1; i >= 0; i--) {
			IResource element = members[i];
			if (state) {
				tree.setChecked(element, true);
				tree.setGrayed(element, false);
			} else
				tree.setGrayChecked(element, false);
			if (element instanceof IContainer)
				setSubtreeChecked((IContainer) element, state, true);
		}
	}

	private void updateParentState(IResource child, boolean baseChildState) {

		if (child == null || child.getParent() == null || !child.isAccessible())
			return;

		IContainer parent = child.getParent();
		boolean allSameState = true;
		IResource[] members = null;
		try {
			members = parent.members();
		} catch (CoreException ex) {
			handleCoreException(
				ex,
				getShell(),
				WorkbenchMessages.getString("WorkingSetDialog.error"),
				WorkbenchMessages.getString("WorkingSetDialog.error.updateCheckedState"));
			//$NON-NLS-2$ //$NON-NLS-1$
		}
		for (int i = members.length - 1; i >= 0; i--) {
			if (tree.getChecked(members[i]) != baseChildState || tree.getGrayed(members[i])) {
				allSameState = false;
				break;
			}
		}

		tree.setGrayed(parent, !allSameState);
		tree.setChecked(parent, !allSameState || baseChildState);

		updateParentState(parent, baseChildState);
	}

	private void initializeCheckedState() {
		if (workingSet == null)
			return;

		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				IAdaptable[] items = workingSet.getItems();
				tree.setCheckedElements(items);
				for (int i = 0; i < items.length; i++) {
					IAdaptable item = items[i];
					IContainer container = null;
					IResource resource = null;
					
					if (item instanceof IContainer) {
						container = (IContainer) item;
					}
					else {
						container = (IContainer) item.getAdapter(IContainer.class);
					}
					if (container != null) {
						setSubtreeChecked(container, true, true);
					}
					if (item instanceof IResource) {
						resource = (IResource) item;
					}
					else {
						resource = (IResource) item.getAdapter(IResource.class);
					}						
					updateParentState(resource, true);
				}
			}
		});
	}
}