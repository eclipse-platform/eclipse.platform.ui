package org.eclipse.ui.externaltools.internal.variable;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.externaltools.internal.group.IGroupDialogPage;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Visual component to edit the resource type variable
 * value.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class ResourceComponent extends AbstractVariableComponent {
	protected Button selectedResourceButton;
	protected Button specificResourceButton;
	protected TreeViewer resourceList;
	private IResource selectedResource;
	private boolean selectedResourceVariable= true;
	
	/**
	 * Creates the component
	 */
	public ResourceComponent() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on IVariableComponent.
	 */
	public void createContents(Composite parent, String varTag, IGroupDialogPage page) {
		super.createContents(parent, varTag, page); // Creates the main group and sets the page
		
		createSelectedResourceOption();
		createSpecificResourceOption();
		createResourceList();
		
		updateResourceListEnablement();
	}

	/**
	 * Creates the list of resources.
	 */
	protected void createResourceList() {
		Tree tree = new Tree(mainGroup, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = tree.getItemHeight() * 8;
		tree.setLayoutData(data);
		tree.setFont(mainGroup.getFont());
		
		resourceList = new TreeViewer(tree);
		resourceList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selectedResource= (IResource) ((IStructuredSelection)event.getSelection()).getFirstElement();
				validate();
			}
		});
		resourceList.setContentProvider(new WorkbenchContentProvider());
		resourceList.setLabelProvider(new WorkbenchLabelProvider());
		resourceList.setInput(ResourcesPlugin.getWorkspace().getRoot());
	}
	
	/**
	 * Creates the option button for using the selected
	 * resource.
	 */
	private void createSelectedResourceOption() {
		selectedResourceButton = new Button(mainGroup, SWT.RADIO);
		selectedResourceButton.setText(ExternalToolsVariableMessages.getString("ResourceComponent.selectedResLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		selectedResourceButton.setLayoutData(data);
		selectedResourceButton.setFont(mainGroup.getFont());
		selectedResourceButton.setSelection(true);
	}
	
	/**
	 * Creates the option button for using a specific
	 * resource.
	 */
	private void createSpecificResourceOption() {
		specificResourceButton = new Button(mainGroup, SWT.RADIO);
		specificResourceButton.setText(ExternalToolsVariableMessages.getString("ResourceComponent.specificResLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		specificResourceButton.setLayoutData(data);
		specificResourceButton.setFont(mainGroup.getFont());
		specificResourceButton.setSelection(false);
		
		specificResourceButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateResourceListEnablement();
				getPage().updateValidState();
				selectedResourceVariable= !specificResourceButton.getSelection();
			}
		});
	}
	
	/* (non-Javadoc)
	 * Method declared on IVariableComponent.
	 */
	public Control getControl() {
		return mainGroup;
	}
	
	/* (non-Javadoc)
	 * Method declared on IVariableComponent.
	 */
	public String getVariableValue() {
		if (selectedResourceVariable) {
			return null;
		}
		
		if (resourceList != null) {
			if (selectedResource != null) {
				return selectedResource.getFullPath().toString();
			}
		}
		
		return null;
	}
	
	/**
	 * Updates the enablement of the resource list if needed
	 */
	private void updateResourceListEnablement() {
		if (specificResourceButton != null && resourceList != null) {
			resourceList.getTree().setEnabled(specificResourceButton.getSelection());
			validate();
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IVariableComponent.
	 */
	public void setVariableValue(String varValue) {
		if (varValue == null || varValue.length() == 0) {
			if (selectedResourceButton != null) {
				selectedResourceButton.setSelection(true);
			}
			if (specificResourceButton != null) {
				specificResourceButton.setSelection(false);
			}
			if (resourceList != null) {
				resourceList.getTree().setEnabled(false);
			}
		} else {
			if (selectedResourceButton != null) {
				selectedResourceButton.setSelection(false);
			}
			if (specificResourceButton != null) {
				specificResourceButton.setSelection(true);
			}
			if (resourceList != null) {
				resourceList.getTree().setEnabled(true);
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varValue);
				if (member != null) {
					resourceList.setSelection(new StructuredSelection(member), true);
				} else {
					resourceList.setSelection(StructuredSelection.EMPTY);
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IVariableComponent.
	 */
	public void validate() {
		getPage().setErrorMessage(null);
		setIsValid(true);
		if (specificResourceButton != null && specificResourceButton.getSelection()) {
			validateResourceListSelection();
		}
		getPage().updateValidState();
	}

	/**
	 * Validates the resource selection list. If no resource is selected, the
	 * component is updated with an error message and isValid is set
	 * <code>false</code>
	 */
	private void validateResourceListSelection() {
		if (resourceList == null) {
			return;
		}
		if (resourceList.getSelection().isEmpty()) {
			getPage().setErrorMessage(ExternalToolsVariableMessages.getString("ResourceComponent.selectionRequired")); //$NON-NLS-1$
			setIsValid(false);
		}
	}
}
