package org.eclipse.ui.externaltools.group;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.externaltools.internal.dialog.ExternalToolVariableForm;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.internal.registry.ExternalToolVariable;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.ToolUtil;

/**
 * Group of components applicable to most external tools. This group
 * will collect from the user the refresh scope to perform after
 * the external tool is done.
 * <p>
 * This group can be used or extended by clients.
 * </p>
 */
public class ExternalToolRefreshGroup extends ExternalToolGroup {
	private String initialScope = null;
	private boolean initialRecursive = true;

	private ExternalToolVariableForm variableForm;
	
	protected Button refreshField;
	protected Button recursiveField;
	
	/**
	 * Creates the group
	 */
	public ExternalToolRefreshGroup() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on ExternalToolGroup.
	 */
	protected Control createGroupContents(Composite parent, ExternalTool tool) {
		// main composite
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		
		createRefreshComponent(mainComposite);
		createRecursiveComponent(mainComposite);
		createScopeComponent(mainComposite);

		if (refreshField != null) {
			refreshField.setSelection(isEditMode() ? tool.getRefreshScope() != null : initialScope != null);
			refreshField.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					updateEnabledState();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			updateEnabledState();		
		}
		
		if (recursiveField != null) {
			recursiveField.setSelection(isEditMode() ? tool.getRefreshRecursive() : initialRecursive);
		}

		if (variableForm != null) {
			String scope = isEditMode() ? tool.getRefreshScope() : initialScope;
			updateForm(scope);
		}
		
		validate();

		return mainComposite;
	}

	/**
	 * Creates the controls needed to edit the refresh recursive
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createRecursiveComponent(Composite parent) {
		recursiveField = new Button(parent, SWT.CHECK);
		recursiveField.setText(ToolMessages.getString("ExternalToolRefreshGroup.recursiveLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		recursiveField.setLayoutData(data);
		
		createSpacer(parent);
	}
	
	/**
	 * Creates the controls needed to edit the refresh scope
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createRefreshComponent(Composite parent) {
		refreshField = new Button(parent, SWT.CHECK);
		refreshField.setText(ToolMessages.getString("ExternalToolRefreshGroup.refreshLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		refreshField.setLayoutData(data);
	}
	
	/**
	 * Creates the controls needed to edit the refresh scope variable
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createScopeComponent(Composite parent) {
		String label = ToolMessages.getString("ExternalToolRefreshGroup.scopeLabel"); //$NON-NLS-1$
		ExternalToolVariable[] vars = ExternalToolsPlugin.getDefault().getRefreshVariableRegistry().getRefreshVariables();
		variableForm = new ExternalToolVariableForm(label, vars);
		variableForm.createContents(parent, getPage());
	}
	
	/**
	 * Creates a vertical space between controls.
	 */
	protected void createSpacer(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
	}
	
	/**
	 * Returns the proposed initial refresh scope for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial refresh scope when editing new tool.
	 */
	public final String getInitialScope() {
		return initialScope;
	}

	/**
	 * Returns the proposed initial refresh recursive for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial refresh recursive when editing new tool.
	 */
	public final boolean getInitialRecursive() {
		return initialRecursive;
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public boolean isValid() {
		if (!super.isValid())
			return false;
			
		if (variableForm != null)
			return variableForm.isValid();
		else
			return true;
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void restoreValues(ExternalTool tool) {
		if (refreshField != null) {
			refreshField.setSelection(tool.getRefreshScope() != null);
			updateEnabledState();
		}
		if (recursiveField != null) {
			recursiveField.setSelection(tool.getRefreshRecursive());
		}
		if (variableForm != null) {
			updateForm(tool.getRefreshScope());
		}
	}
	
	/**
	 * Sets the proposed initial refresh scope for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialLocation the proposed initial refresh scope when editing new tool.
	 */
	public final void setInitialScope(String initialScope) {
		this.initialScope = initialScope;
	}

	/**
	 * Sets the proposed initial refresh recursive for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialName the proposed initial refresh recursive when editing new tool.
	 */
	public final void setInitialRecursive(boolean initialRecursive) {
		this.initialRecursive = initialRecursive;
	}

	/**
	 * Updates the enablement state of the fields.
	 */
	protected void updateEnabledState() {
		if (refreshField != null) {
			if (recursiveField != null)
				recursiveField.setEnabled(refreshField.getSelection());
			if (variableForm != null)
				variableForm.setEnabled(refreshField.getSelection());
		}
	}

	/**
	 * Update the variable form to match the specified
	 * refresh scope.
	 */
	protected final void updateForm(String refreshScope) {
		if (variableForm == null)
			return;
			
		String varName = null;
		String varValue = null;
		if (refreshScope != null) {
			ToolUtil.VariableDefinition varDef = ToolUtil.extractVariableTag(refreshScope, 0);
			varName = varDef.name;
			varValue = varDef.argument;
		}
		variableForm.selectVariable(varName, varValue);
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void updateTool(ExternalTool tool) {
		if (refreshField != null) {
			if (refreshField.getSelection()) {
				if (variableForm != null)
					tool.setRefreshScope(variableForm.getSelectedVariable());
				else
					tool.setRefreshScope(null);
			}
			else
				tool.setRefreshScope(null);
		}
		if (recursiveField != null)
			tool.setRefreshRecursive(recursiveField.getSelection());
	}


	/**
	 * @see org.eclipse.ui.externaltools.group.IExternalToolGroup#validate()
	 */
	public void validate() {
		if (variableForm != null) {
			variableForm.validate();
			if (!variableForm.isValid())
				return;
		}
		
		getPage().setMessage(null, IMessageProvider.NONE);
		setIsValid(true);
	}
}
