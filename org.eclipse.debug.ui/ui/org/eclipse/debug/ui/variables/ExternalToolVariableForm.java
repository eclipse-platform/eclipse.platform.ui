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
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

/**
 * Visual grouping of controls that allows the user to
 * select a variable and configure it with extra
 * information.
 */
public class ExternalToolVariableForm {
	private static final int VISIBLE_ITEM_COUNT = 6;
	
	private String variableListLabelText;
	private ExternalToolVariable[] variables;
	private IVariableComponent[] components;
	private IGroupDialogPage dialogPage;
	
	private Label variableListLabel;
	private List variableList;
	private Composite variableComposite;
	private StackLayout variableLayout;
	private int activeComponentIndex = -1;
	
	/**
	 * Creates the visual grouping
	 * 
	 * @param variableListLabelText the label text to use for identifying the list of variables
	 * @param variables the collection of variables to display to the user
	 */
	public ExternalToolVariableForm(String variableListLabelText, ExternalToolVariable[] variables) {
		super();
		this.variableListLabelText = variableListLabelText;
		this.variables = variables;
		this.components = new IVariableComponent[variables.length];
	}

	public Composite createContents(Composite parent, IGroupDialogPage page) {
		Font font = parent.getFont();
		
		dialogPage = page;
		
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		GridData data = new GridData(GridData.FILL_BOTH);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(data);

		variableListLabel = new Label(mainComposite, SWT.NONE);
		variableListLabel.setText(variableListLabelText);
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 1;
		variableListLabel.setLayoutData(data);
		variableListLabel.setFont(font);
		
		variableList = new List(mainComposite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = variableList.getItemHeight() * VISIBLE_ITEM_COUNT;
		variableList.setLayoutData(data);
		variableList.setFont(font);

		variableComposite = new Composite(mainComposite, SWT.NONE);
		variableLayout = new StackLayout();
		variableLayout.marginWidth = 0;
		variableLayout.marginHeight = 0;
		data = new GridData(GridData.FILL_BOTH);
		variableComposite.setLayout(variableLayout);
		variableComposite.setLayoutData(data);
		variableComposite.setFont(font);
		
		createVariableComponents();
		
		populateVariableList();
		
		variableList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateVariableComposite(null, false);
			}
		});
		
		setEnabled(true);
		return mainComposite;
	}
	
	/**
	 * Creates the visual component for each variable
	 * and determine the initial size so the form
	 * can be layout properly.
	 */
	private void createVariableComponents() {
		for (int i = 0; i < variables.length; i++) {
			ExternalToolVariable var = variables[i];
			components[i] = var.getComponent();
			components[i].createContents(variableComposite, var.getTag(), dialogPage);
		}
	}
	
	/**
	 * Returns the formatted variable or <code>null</code> if
	 * none selected.
	 */
	public String getSelectedVariable() {
		if (activeComponentIndex != -1) {
			String varValue = components[activeComponentIndex].getVariableValue();
			return VariableUtil.buildVariableTag(variables[activeComponentIndex].getTag(), varValue);
		}

		return null;
	}

	/**
	 * Returns the variable list of this form.
	 */
	public List getVariableList() {
		return variableList;
	}

	/**
	 * Returns whether the current variable selection is
	 * valid, including the selected variable value.
	 */
	public boolean isValid() {
		if (activeComponentIndex != -1) {
			return components[activeComponentIndex].isValid();
		}
		
		return true;
	}

	private void populateVariableList() {
		String[] items = new String[variables.length];
		StringBuffer buffer = new StringBuffer(80);
		for (int i = 0; i < variables.length; i++) {
			VariableUtil.buildVariableTag(variables[i].getTag(), null, buffer);
			buffer.append(" - "); //$NON-NLS-1$
			buffer.append(variables[i].getDescription());
			items[i] = buffer.toString();
			buffer.setLength(0);
		}
		variableList.setItems(items);
	}

	public void selectVariable(String varName, String varValue) {	
		if (varName != null && varName.length() > 0) {
			for (int i = 0; i < variables.length; i++) {
				if (varName.equals(variables[i].getTag())) {
					variableList.select(i);
					updateVariableComposite(varValue, true);
					return;
				}
			}
		}
		
		variableList.deselectAll();
		updateVariableComposite(varValue, false);
	}
	
	private void setComponentVisible(int index) {
		if (index == -1) {
			variableLayout.topControl = null;
		} else {
			variableLayout.topControl = components[index].getControl();
		}
		variableComposite.layout();
	}
	
	/**
	 * Enables or disables the variable form controls.
	 */
	public void setEnabled(boolean enabled) {
		variableListLabel.setEnabled(enabled);
		variableList.setEnabled(enabled);
		if (enabled && variableList.getSelection().length == 0) {
			if (variableList.getItemCount() > 0) {
				variableList.select(0);
				activeComponentIndex= 0;
			}
		}
		variableComposite.setVisible(enabled);
		setComponentVisible(activeComponentIndex);
	}
	
	private void updateVariableComposite(String value, boolean setValue) {
		if (variableList.getSelectionIndex() == activeComponentIndex) {
			return;
		}
		activeComponentIndex = variableList.getSelectionIndex();
		setComponentVisible(activeComponentIndex);
		if (activeComponentIndex != -1 && setValue) {
			components[activeComponentIndex].setVariableValue(value);
		}
		dialogPage.updateValidState();
	}

	/**
	 * Validates the current variable selection is and
	 * its value are acceptable.
	 */
	public void validate() {
		if (activeComponentIndex != -1) {
			components[activeComponentIndex].validate();
		}
	}
	
	public void dispose() {
		for (int i = 0; i < components.length; i++) {
			IVariableComponent varComponent = components[i];
			varComponent.dispose();
		}
	}
}