package org.eclipse.ui.externaltools.variable;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.externaltools.group.IGroupDialogPage;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ToolUtil;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Visual component to edit the resource type variable
 * value.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 */
public class ResourceComponent implements IVariableComponent {
	private IGroupDialogPage page;
	private boolean isValid = true;
	
	protected Group mainGroup;
	protected Button selectedResourceButton;
	protected Button specificResourceButton;
	protected TreeViewer resourceList;
	
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
		this.page = page;
		
		// main composite
		mainGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_BOTH);
		mainGroup.setLayout(layout);
		mainGroup.setLayoutData(gridData);
		mainGroup.setText(ToolUtil.buildVariableTag(varTag, null));
		
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
		data.heightHint = tree.getItemHeight() * getInitialVisibleItemCount();
		tree.setLayoutData(data);
		
		resourceList = new TreeViewer(tree);
		resourceList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validateResourceListSelection();
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
	protected void createSelectedResourceOption() {
		selectedResourceButton = new Button(mainGroup, SWT.RADIO);
		selectedResourceButton.setText(ToolMessages.getString("ResourceComponent.selectedResLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		selectedResourceButton.setLayoutData(data);
		selectedResourceButton.setSelection(true);
	}
	
	/**
	 * Creates the option button for using a specific
	 * resource.
	 */
	protected void createSpecificResourceOption() {
		specificResourceButton = new Button(mainGroup, SWT.RADIO);
		specificResourceButton.setText(ToolMessages.getString("ResourceComponent.specificResLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		specificResourceButton.setLayoutData(data);
		specificResourceButton.setSelection(false);
		
		specificResourceButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateResourceListEnablement();
			}
		});
	}
	
	/* (non-Javadoc)
	 * Method declared on IVariableComponent.
	 */
	public Control getControl() {
		return mainGroup;
	}

	/**
	 * Returns the dialog page this component is part of
	 */
	protected final IGroupDialogPage getPage() {
		return page;
	}
	
	/* (non-Javadoc)
	 * Method declared on IVariableComponent.
	 */
	public String getVariableValue() {
		if (selectedResourceButton != null && selectedResourceButton.getSelection())
			return null;
		
		if (resourceList != null) {
			IStructuredSelection sel = (IStructuredSelection) resourceList.getSelection();
			IResource resource = (IResource) sel.getFirstElement();
			if (resource != null)
				return resource.getFullPath().toString();
		}
		
		return null;
	}

	/**
	 * Returns the number of items to be visible in the
	 * resource list. This will determine the initial height.
	 */
	protected int getInitialVisibleItemCount() {
		return 10;
	}
	
	/* (non-Javadoc)
	 * Method declared on IVariableComponent.
	 */
	public boolean isValid() {
		return isValid;
	}

	/**
	 * Sets whether the component's values are all valid.
	 * Updates the components's page valid state. No action
	 * taken if new valid state same as current one.
	 * 
	 * @param isValid <code>true</code> if all values valid,
	 * 		<code>false</code> otherwise
	 */
	protected final void setIsValid(boolean isValid) {
		if (this.isValid != isValid) {
			this.isValid = isValid;
			this.page.updateValidState();
		}
	}
	
	/**
	 * Updates the enablement of the resource list if needed
	 */
	protected void updateResourceListEnablement() {
		if (specificResourceButton != null && resourceList != null)
			resourceList.getTree().setEnabled(specificResourceButton.getSelection());
	}
	
	/* (non-Javadoc)
	 * Method declared on IVariableComponent.
	 */
	public void setVariableValue(String varValue) {
		if (varValue == null || varValue.length() == 0) {
			if (selectedResourceButton != null)
				selectedResourceButton.setSelection(true);
			if (specificResourceButton != null)
				specificResourceButton.setSelection(false);
			if (resourceList != null)
				resourceList.getTree().setEnabled(false);
		} else {
			if (selectedResourceButton != null)
				selectedResourceButton.setSelection(false);
			if (specificResourceButton != null)
				specificResourceButton.setSelection(true);
			if (resourceList != null) {
				resourceList.getTree().setEnabled(true);
				IResource member = ResourcesPlugin.getWorkspace().getRoot().findMember(varValue);
				if (member != null)
					resourceList.setSelection(new StructuredSelection(member), true);
				else
					resourceList.setSelection(StructuredSelection.EMPTY);
			}
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IVariableComponent.
	 */
	public void validate() {
		if (specificResourceButton != null && specificResourceButton.getSelection()) {
			validateResourceListSelection();
		}

		getPage().setMessage(null, IMessageProvider.NONE);
		setIsValid(true);
	}

	/**
	 * Returns whether that the resource list selection is valid.
	 * If the list was not created, returns <code>true</code>.
	 * 
	 * @return <code>true</code> to continue validating other
	 * 	fields, <code>false</code> to stop.
	 */
	protected boolean validateResourceListSelection() {
		if (resourceList == null)
			return true;
			
		if (resourceList.getSelection().isEmpty()) {
			getPage().setMessage(ToolMessages.getString("ResourceComponent.selectionRequired"), IMessageProvider.WARNING); //$NON-NLS-1$
			setIsValid(false);
			return false;
		}
		
		return true;
	}
}
