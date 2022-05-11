/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *  	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font
 * 		should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A resource working set page allows the user to edit an existing working set
 * and create a new working set.
 * <p>
 * Working set elements are presented as a simple resource tree.
 * </p>
 *
 * @since 2.0
 */
public class ResourceWorkingSetPage extends WizardPage implements IWorkingSetPage {
	private static final int SIZING_SELECTION_WIDGET_WIDTH = 50;

	private static final int SIZING_SELECTION_WIDGET_HEIGHT = 200;

	private Text text;

	private CheckboxTreeViewer tree;

	private IWorkingSet workingSet;

	private boolean firstCheck = true;

	/**
	 * Creates a new instance of the receiver.
	 */
	public ResourceWorkingSetPage() {
		super("resourceWorkingSetPage", //$NON-NLS-1$
				IDEWorkbenchMessages.ResourceWorkingSetPage_title, IDEInternalWorkbenchImages
						.getImageDescriptor(IDEInternalWorkbenchImages.IMG_WIZBAN_RESOURCEWORKINGSET_WIZ));
		setDescription(IDEWorkbenchMessages.ResourceWorkingSetPage_description);
	}

	/**
	 * Adds working set elements contained in the given container to the list of
	 * checked resources.
	 *
	 * @param collectedResources list of collected resources
	 * @param container          container to collect working set elements for
	 */
	private void addWorkingSetElements(List collectedResources, IContainer container) {
		IPath containerPath = container.getFullPath();

		for (IAdaptable adaptable : workingSet.getElements()) {
			IResource resource = Adapters.adapt(adaptable, IResource.class);

			if (resource != null) {
				IPath resourcePath = resource.getFullPath();
				if (containerPath.isPrefixOf(resourcePath)) {
					collectedResources.add(adaptable);
				}
			}
		}
	}

	/**
	 * Overrides method in WizardPage.
	 *
	 * @see org.eclipse.jface.wizard.WizardPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.WORKING_SET_RESOURCE_PAGE);
		Label label = new Label(composite, SWT.WRAP);
		label.setText(IDEWorkbenchMessages.ResourceWorkingSetPage_message);
		GridData data = new GridData(
				GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(data);

		text = new Text(composite, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		text.addModifyListener(e -> validateInput());
		text.setFocus();

		label = new Label(composite, SWT.WRAP);
		label.setText(IDEWorkbenchMessages.ResourceWorkingSetPage_label_tree);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(data);

		tree = new CheckboxTreeViewer(composite);
		tree.setUseHashlookup(true);
		final ITreeContentProvider treeContentProvider = new WorkbenchContentProvider();
		tree.setContentProvider(treeContentProvider);
		tree.setLabelProvider(new DecoratingLabelProvider(new WorkbenchLabelProvider(),
				PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		tree.setInput(IDEWorkbenchPlugin.getPluginWorkspace().getRoot());
		tree.setComparator(new ResourceComparator(ResourceComparator.NAME));

		data = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		tree.getControl().setLayoutData(data);

		tree.addCheckStateListener(this::handleCheckStateChange);

		tree.addTreeListener(new ITreeViewerListener() {
			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
			}

			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				final Object element = event.getElement();
				if (tree.getGrayed(element) == false) {
					BusyIndicator.showWhile(getShell().getDisplay(),
							() -> setSubtreeChecked((IContainer) element, tree.getChecked(element), false));
				}
			}
		});

		// Add select / deselect all buttons for bug 46669
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		Button selectAllButton = new Button(buttonComposite, SWT.PUSH);
		selectAllButton.setText(IDEWorkbenchMessages.ResourceWorkingSetPage_selectAll_label);
		selectAllButton.setToolTipText(IDEWorkbenchMessages.ResourceWorkingSetPage_selectAll_toolTip);
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				BusyIndicator.showWhile(getShell().getDisplay(), () -> {
					tree.setCheckedElements(treeContentProvider.getElements(tree.getInput()));
					setSubtreeChecked((IContainer) tree.getInput(), true, false);
				});
				validateInput();
			}
		});
		setButtonLayoutData(selectAllButton);

		Button deselectAllButton = new Button(buttonComposite, SWT.PUSH);
		deselectAllButton.setText(IDEWorkbenchMessages.ResourceWorkingSetPage_deselectAll_label);
		deselectAllButton.setToolTipText(IDEWorkbenchMessages.ResourceWorkingSetPage_deselectAll_toolTip);
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				BusyIndicator.showWhile(getShell().getDisplay(), () -> {
					tree.setCheckedElements(treeContentProvider.getElements(tree.getInput()));
					setSubtreeChecked((IContainer) tree.getInput(), false, false);
				});
				validateInput();
			}
		});
		setButtonLayoutData(deselectAllButton);

		initializeCheckedState();
		if (workingSet != null) {
			text.setText(workingSet.getName());
		}
		validateInput();

		Dialog.applyDialogFont(composite);
	}

	/**
	 * Collects all checked resources in the specified container.
	 *
	 * @param checkedResources the output, list of checked resources
	 * @param container        the container to collect checked resources in
	 */
	private void findCheckedResources(List checkedResources, IContainer container) {
		IResource[] resources = null;
		try {
			resources = container.members();
		} catch (CoreException ex) {
			handleCoreException(ex, getShell(), IDEWorkbenchMessages.ResourceWorkingSetPage_error,
					IDEWorkbenchMessages.ResourceWorkingSetPage_error_updateCheckedState);
		}
		for (IResource resource : resources) {
			if (tree.getGrayed(resource)) {
				if (resource.isAccessible()) {
					findCheckedResources(checkedResources, (IContainer) resource);
				} else {
					addWorkingSetElements(checkedResources, (IContainer) resource);
				}
			} else if (tree.getChecked(resource)) {
				checkedResources.add(resource);
			}
		}
	}

	/**
	 * Implements IWorkingSetPage.
	 *
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#finish()
	 */
	@Override
	public void finish() {
		ArrayList<IAdaptable> resources = new ArrayList<>(10);
		findCheckedResources(resources, (IContainer) tree.getInput());
		if (workingSet == null) {
			IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
			workingSet = workingSetManager.createWorkingSet(getWorkingSetName(),
					resources.toArray(new IAdaptable[resources.size()]));
		} else {
			workingSet.setName(getWorkingSetName());
			workingSet.setElements(resources.toArray(new IAdaptable[resources.size()]));
		}
	}

	/**
	 * Implements IWorkingSetPage.
	 *
	 * @see org.eclipse.ui.dialogs.IWorkingSetPage#getSelection()
	 */
	@Override
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
		BusyIndicator.showWhile(getShell().getDisplay(), () -> {
			IResource resource = (IResource) event.getElement();
			boolean state = event.getChecked();

			tree.setGrayed(resource, false);
			if (resource instanceof IContainer) {
				setSubtreeChecked((IContainer) resource, state, true);
			}
			updateParentState(resource);
			validateInput();
		});
	}

	/**
	 * Displays an error message when a CoreException occured.
	 *
	 * @param exception the CoreException
	 * @param shell     parent shell for the message box
	 * @param title     the mesage box title
	 * @param message   additional error message
	 */
	private void handleCoreException(CoreException exception, Shell shell, String title, String message) {
		IStatus status = exception.getStatus();
		if (status != null) {
			ErrorDialog.openError(shell, title, message, status);
		} else {
			MessageDialog.openError(shell, IDEWorkbenchMessages.InternalError, exception.getLocalizedMessage());
		}
	}

	/**
	 * Sets the checked state of tree items based on the initial working set, if
	 * any.
	 */
	private void initializeCheckedState() {
		BusyIndicator.showWhile(getShell().getDisplay(), () -> {
			Object[] items = null;
			if (workingSet == null) {

				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage();
				if (page == null) {
					return;
				}
				IWorkbenchPart part = page.getActivePart();
				if (part == null) {
					return;
				}
				ISelection selection = page.getSelection();
				if (selection instanceof IStructuredSelection) {
					items = ((IStructuredSelection) selection).toArray();
				}

			} else {
				items = workingSet.getElements();
			}
			if (items == null) {
				return;
			}
			tree.setCheckedElements(items);
			for (Object i : items) {
				if (!(i instanceof IAdaptable)) {
					continue;
				}
				IAdaptable item = (IAdaptable) i;
				IContainer container = Adapters.adapt(item, IContainer.class);

				if (container != null) {
					setSubtreeChecked(container, true, true);
				}
				IResource resource = Adapters.adapt(item, IResource.class);
				if (resource != null && resource.isAccessible() == false) {
					IProject project = resource.getProject();
					if (tree.getChecked(project) == false) {
						tree.setGrayChecked(project, true);
					}
				} else {
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
	@Override
	public void setSelection(IWorkingSet workingSet) {
		if (workingSet == null) {
			throw new IllegalArgumentException("Working set must not be null"); //$NON-NLS-1$
		}
		this.workingSet = workingSet;
		if (getShell() != null && text != null) {
			firstCheck = false;
			text.setText(workingSet.getName());
			initializeCheckedState();
			validateInput();
		}
	}

	/**
	 * Sets the checked state of the container's members.
	 *
	 * @param container          the container whose children should be
	 *                           checked/unchecked
	 * @param state              true=check all members in the container.
	 *                           false=uncheck all members in the container.
	 * @param checkExpandedState true=recurse into sub-containers and set the
	 *                           checked state. false=only set checked state of
	 *                           members of this container
	 */
	private void setSubtreeChecked(IContainer container, boolean state, boolean checkExpandedState) {
		// checked state is set lazily on expand, don't set it if container is collapsed
		if (container.isAccessible() == false
				|| (tree.getExpandedState(container) == false && state && checkExpandedState)) {
			return;
		}
		IResource[] members = null;
		try {
			members = container.members();
		} catch (CoreException ex) {
			handleCoreException(ex, getShell(), IDEWorkbenchMessages.ResourceWorkingSetPage_error,
					IDEWorkbenchMessages.ResourceWorkingSetPage_error_updateCheckedState);
		}
		for (int i = members.length - 1; i >= 0; i--) {
			IResource element = members[i];
			boolean elementGrayChecked = tree.getGrayed(element) || tree.getChecked(element);

			if (state) {
				tree.setChecked(element, true);
				tree.setGrayed(element, false);
			} else {
				tree.setGrayChecked(element, false);
			}
			// unchecked state only needs to be set when the container is
			// checked or grayed
			if (element instanceof IContainer && (state || elementGrayChecked)) {
				setSubtreeChecked((IContainer) element, state, true);
			}
		}
	}

	/**
	 * Check and gray the resource parent if all resources of the parent are
	 * checked.
	 *
	 * @param child the resource whose parent checked state should be set.
	 */
	private void updateParentState(IResource child) {
		if (child == null || child.getParent() == null) {
			return;
		}

		IContainer parent = child.getParent();
		boolean childChecked = false;
		IResource[] members = null;
		try {
			members = parent.members();
		} catch (CoreException ex) {
			handleCoreException(ex, getShell(), IDEWorkbenchMessages.ResourceWorkingSetPage_error,
					IDEWorkbenchMessages.ResourceWorkingSetPage_error_updateCheckedState);
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
	 * Validates the working set name and the checked state of the resource tree.
	 */
	private void validateInput() {
		String errorMessage = null;
		String infoMessage = null;
		String newText = text.getText();

		if (newText.equals(newText.trim()) == false) {
			errorMessage = IDEWorkbenchMessages.ResourceWorkingSetPage_warning_nameWhitespace;
		}
		if (newText.isEmpty()) {
			if (firstCheck) {
				setPageComplete(false);
				firstCheck = false;
				return;
			}
			errorMessage = IDEWorkbenchMessages.ResourceWorkingSetPage_warning_nameMustNotBeEmpty;
		}
		firstCheck = false;
		if (errorMessage == null && (workingSet == null || newText.equals(workingSet.getName()) == false)) {
			for (IWorkingSet workingSet : PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets()) {
				if (newText.equals(workingSet.getName())) {
					errorMessage = IDEWorkbenchMessages.ResourceWorkingSetPage_warning_workingSetExists;
				}
			}
		}
		if (infoMessage == null && tree.getCheckedElements().length == 0) {
			infoMessage = IDEWorkbenchMessages.ResourceWorkingSetPage_warning_resourceMustBeChecked;
		}
		setMessage(infoMessage, INFORMATION);
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}
}
