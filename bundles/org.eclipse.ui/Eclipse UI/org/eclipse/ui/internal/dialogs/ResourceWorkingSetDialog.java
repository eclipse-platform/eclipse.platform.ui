package org.eclipse.ui.internal.dialogs;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.dialogs.IWorkingSetDialog;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.model.*;

/**
 * A resource working set dialog allows the user to edit an 
 * existing and create a new working set that contains resources
 * (IResource).
 * 
 * @see org.eclipse.ui.dialogs.IWorkingSetDialog
 * @since 2.0
 */
public class ResourceWorkingSetDialog implements IWorkingSetDialog {

	/**
	 * The actual dialog widget. Contains an input field for the working 
	 * set name and a tree viewer to show the working set content.
	 */
	private class WorkingSetDialogWidget extends InputDialog {
		private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;
		private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;

		private CheckboxTreeViewer tree;
		private IWorkingSet workingSet;

		/**
		 * Creates a new instance of the receiver.
		 * 
		 * @param parentShell the dialog parent
		 */
		public WorkingSetDialogWidget(Shell parentShell) {
			super(parentShell, WorkbenchMessages.getString("WorkingSetDialog.title.new"), //$NON-NLS-1$
			WorkbenchMessages.getString("WorkingSetDialog.message"), //$NON-NLS-2$ 
			"", new WorkingSetNameInputValidator(""));
		}
		/**
		 * Creates a new instance of the receiver.
		 * 
		 * @param parentShell the dialog parent
		 * @param workingSet the working set initially displayed in 
		 * 	the dialog.
		 */
		WorkingSetDialogWidget(Shell parentShell, IWorkingSet workingSet) {
			super(parentShell, WorkbenchMessages.getString("WorkingSetDialog.title.edit"), //$NON-NLS-1$
			WorkbenchMessages.getString("WorkingSetDialog.message"), //$NON-NLS-2$ 
			workingSet.getName(), new WorkingSetNameInputValidator(workingSet.getName()));
			this.workingSet = workingSet;
		}
		/**
		 * Creates the dialog content and populates the tree viewer with 
		 * the workspace content.
		 * Overrides method from Dialog.
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
		 */
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);

			Label label = new Label(composite, SWT.WRAP);
			label.setText(WorkbenchMessages.getString("WorkingSetDialog.label.tree"));
			GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
			label.setLayoutData(data);

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
					if (tree.getGrayed(element) == false)
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
		/**
		 * Grays all disabled projects
		 */
		private void disableClosedProjects() {
			IProject[] projects = WorkbenchPlugin.getPluginWorkspace().getRoot().getProjects();
			for (int i = 0; i < projects.length; i++) {
				if (!projects[i].isOpen())
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
				handleCoreException(ex, getShell(), WorkbenchMessages.getString("WorkingSetDialog.error"), //$NON-NLS-1$
				WorkbenchMessages.getString("WorkingSetDialog.error.updateCheckedState")); //$NON-NLS-2$ 
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
		 * Returns the working set after the dialog is closed.
		 * 
		 * @return the new or modified working set.
		 */
		public IWorkingSet getSelection() {
			return workingSet;
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
							WorkbenchMessages.getString("WorkingSetDialog.projectClosedDialog.title"),
							WorkbenchMessages.getString("WorkingSetDialog.projectClosedDialog.message"));
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
				}
			});

			String errorMessage = getValidator().isValid(getText().getText());
			if (errorMessage == null) {
				errorMessage = "";
			}
			getErrorMessageLabel().setText(errorMessage);
			getOkButton().setEnabled(errorMessage.length() == 0);
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
				MessageDialog.openError(shell, WorkbenchMessages.getString("InternalError"), exception.getLocalizedMessage());
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
		 * Set the checked state of the container's members.
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
				handleCoreException(ex, getShell(), WorkbenchMessages.getString("WorkingSetDialog.error"), //$NON-NLS-1$
				WorkbenchMessages.getString("WorkingSetDialog.error.updateCheckedState")); //$NON-NLS-2$ 				
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
		 * @param child the resource whose parent checked state should be 
		 * 	set.
		 */
		private void updateParentState(IResource child) {
			if (child == null || child.getParent() == null || child.isAccessible() == false)
				return;

			IContainer parent = child.getParent();
			boolean childChecked = false;
			IResource[] members = null;
			try {
				members = parent.members();
			} catch (CoreException ex) {
				handleCoreException(ex, getShell(), WorkbenchMessages.getString("WorkingSetDialog.error"), //$NON-NLS-1$
				WorkbenchMessages.getString("WorkingSetDialog.error.updateCheckedState")); //$NON-NLS-2$ 
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
		 * Collects the checked resources and sets them in the existing 
		 * working set or creates a new working set if none exists already.
		 * Overrides method from Dialog.
		 * 
		 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
		 */
		protected void okPressed() {
			ArrayList resources = new ArrayList(10);
			findCheckedResources(resources, (IContainer) tree.getInput());
			if (workingSet == null) {
				IWorkingSetManager workingSetManager = WorkbenchPlugin.getDefault().getWorkingSetManager();
				workingSet = workingSetManager.createWorkingSet(getText().getText(), (IAdaptable[]) resources.toArray(new IAdaptable[resources.size()]));
			} else if (workingSet instanceof WorkingSet) {
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
				((WorkingSet) workingSet).setName(getText().getText());
				((WorkingSet) workingSet).setElements((IAdaptable[]) resources.toArray(new IAdaptable[resources.size()]));
			}
			super.okPressed();
		}

	}

	/**
	 * Validates the working set name and tree viewer checked state.
	 * The working set name must not be empty and it must be unique 
	 * in the Workbench working set manager.
	 */
	private static class WorkingSetNameInputValidator implements IInputValidator {
		private String initalName;
		private boolean firstCheck = true;
		private CheckboxTreeViewer tree;

		/**
		 * Creates a new instance of the receiver
		 */
		public WorkingSetNameInputValidator(String initialName) {
			Assert.isNotNull(initialName, "initial name must not be null"); //$NON-NLS-1$
			this.initalName = initialName;
		}
		/**
		 * Sets the tree viewer to use for input validation.
		 * 
		 * @param tree the tree viewer to use for input validation.
		 */
		private void setTree(CheckboxTreeViewer tree) {
			this.tree = tree;
		}
		/**
		 * Validates the working set name and tree viewer checked state.
		 * The working set name must not be empty and it must be unique 
		 * in the Workbench working set manager.
		 * <p>
		 * Implements IInputValidator.
		 * </p>
		 * 
		 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(String)
		 */
		public String isValid(String newText) {
			if (newText == null || newText.equals("")) { //$NON-NLS-1$
				if (initalName.equals("") && firstCheck) { //$NON-NLS-1$
					firstCheck = false;
					return " "; //$NON-NLS-1$
				}
				return WorkbenchMessages.getString("WorkingSetDialog.warning.nameMustNotBeEmpty"); //$NON-NLS-1$
			}
			if (!newText.equals(initalName)) {
				IWorkingSet[] workingSets = WorkbenchPlugin.getDefault().getWorkingSetManager().getWorkingSets();
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

	private Shell parent;
	private IWorkingSet initialSelection;
	private WorkingSetDialogWidget dialogWidget;

	/**
	 * Implements IWorkingSetDialog.
	 * 
	 * @see org.eclipse.ui.dialogs.IWorkingSetDialog#getSelection()
	 */
	public IWorkingSet getSelection() {
		if (dialogWidget == null) {
			return initialSelection;
		}
		return dialogWidget.getSelection();
	}
	/**
	 * Implements IWorkingSetDialog.
	 * 
	 * @see org.eclipse.ui.dialogs.IWorkingSetDialog#init(Shell)
	 */
	public void init(Shell shell) {
		parent = shell;
	}
	/**
	 * Implements IWorkingSetDialog.
	 * Blocks execution until the dialog is closed by the user.
	 * 
	 * @see org.eclipse.ui.dialogs.IWorkingSetDialog#open()
	 */
	public int open() {
		if (parent == null) {
			return Window.CANCEL;
		}
		if (dialogWidget == null) {
			if (initialSelection != null) {
				dialogWidget = new WorkingSetDialogWidget(parent, initialSelection);
			} else {
				dialogWidget = new WorkingSetDialogWidget(parent);
			}
		}
		return dialogWidget.open();
	}
	/**
	 * Implements IWorkingSetDialog.
	 * 
	 * @see org.eclipse.ui.dialogs.IWorkingSetDialog#setSelection(IWorkingSet)
	 */
	public void setSelection(IWorkingSet workingSet) {
		Assert.isNotNull(workingSet, "Working set must not be null");
		initialSelection = workingSet;
	}
}