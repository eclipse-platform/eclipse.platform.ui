/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.variables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

/**
 * The AbstractVariableComponent provides the base implementation of an
 * IVariableComponent
 */
public abstract class AbstractVariableComponent implements IVariableComponent {
	
	protected Group mainGroup;
	protected IGroupDialogPage dialogPage;
	private boolean isValid = true;

	/**
	 * @see IVariableComponent#getControl()
	 */
	public Control getControl() {
		return null;
	}
	
	/**
	 * Returns the dialog page this component is part of
	 */
	protected IGroupDialogPage getPage() {
		return dialogPage;
	}

	/**
	 * @see IVariableComponent#createContents(Composite, String, IGroupDialogPage)
	 */
	public void createContents(Composite parent, String varTag, IGroupDialogPage page) {
		dialogPage= page;
		
		// main composite
		mainGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		GridData gridData = new GridData(GridData.FILL_BOTH);
		mainGroup.setLayout(layout);
		mainGroup.setLayoutData(gridData);
		mainGroup.setFont(parent.getFont());
		mainGroup.setText(VariableUtil.buildVariableTag(varTag, null));
	}

	/**
	 * @see IVariableComponent#getVariableValue()
	 */
	public String getVariableValue() {
		return null;
	}

	/**
	 * @see IVariableComponent#isValid()
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
	protected void setIsValid(boolean isValid) {
		if (isValid() != isValid) {
			this.isValid= isValid;
			this.dialogPage.updateValidState();
		}
	}

	/**
	 * @see IVariableComponent#setVariableValue(String)
	 */
	public void setVariableValue(String varValue) {
	}

	/**
	 * @see IVariableComponent#validate()
	 */
	public void validate() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.variables.IVariableComponent#dispose()
	 */
	public void dispose() {
		//by default do nothing
	}
}