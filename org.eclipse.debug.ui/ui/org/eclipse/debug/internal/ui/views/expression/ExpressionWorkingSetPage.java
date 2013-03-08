/*******************************************************************************
 * Copyright (c) 2012 Tensilica Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Abeer Bagul (Tensilica Inc) - initial API and implementation (Bug 372181)
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.expression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetPage;

/**
 * The Expression working set page allows the user to create
 * and edit an expression working set.
 * 
 * @since 3.9
 */
public class ExpressionWorkingSetPage extends WizardPage implements
		IWorkingSetPage {
	
	private static final String PAGE_NAME = "expressionWorkingSetPage"; //$NON-NLS-1$
	
	private Text txtWorkingSetName;
	private CheckboxTableViewer expressionsViewer;
	
	private boolean isFirstCheck;
	private IWorkingSet workingSet;

	public ExpressionWorkingSetPage()
	{
		super(PAGE_NAME,
				ExpressionWorkingSetMessages.Page_Title,
				null);
		setDescription(ExpressionWorkingSetMessages.Page_Description);
		isFirstCheck = true;
	}
	
	public void createControl(Composite parent) 
	{
		initializeDialogUnits(parent);

		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);
		
		Label label= new Label(composite, SWT.WRAP);
		label.setText(ExpressionWorkingSetMessages.WorkingSetName_label);
		GridData gd= new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);
		
		txtWorkingSetName= new Text(composite, SWT.SINGLE | SWT.BORDER);
		txtWorkingSetName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		txtWorkingSetName.addModifyListener(
			new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			}
		);
		txtWorkingSetName.setFocus();
		
		label= new Label(composite, SWT.WRAP);
		label.setText(ExpressionWorkingSetMessages.Expressions_label);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);
				
		expressionsViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		expressionsViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		expressionsViewer.setContentProvider(new ArrayContentProvider());
		expressionsViewer.setLabelProvider(new ExpressionLabelProvider());
		
		// Add select / deselect all buttons
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(2, false));
		buttonComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		
		Button selectAllButton = new Button(buttonComposite, SWT.PUSH);
		selectAllButton.setText(ExpressionWorkingSetMessages.SelectAll);
		selectAllButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent selectionEvent) {
				expressionsViewer.getTable().selectAll();
				expressionsViewer.setCheckedElements(
						((IStructuredSelection) expressionsViewer.getSelection()).toArray());
				expressionsViewer.setGrayedElements(new Object[] {});
				expressionsViewer.getTable().deselectAll();
				validateInput();
			}
		});
		Button deselectAllButton = new Button(buttonComposite, SWT.PUSH);
		deselectAllButton.setText(ExpressionWorkingSetMessages.DeselectAll);
		deselectAllButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent selectionEvent) {
				expressionsViewer.setCheckedElements(new Object[] {});
				validateInput();
			}
		});
		
		Dialog.applyDialogFont(composite);
		
		populateData();
	}
	
	private void populateData()
	{
		//get all expressions defined in the workspace
		IExpression[] allExpressions = DebugPlugin.getDefault().getExpressionManager().getExpressions();
		expressionsViewer.setInput(allExpressions);
		
		IViewPart expressionView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
									.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
		
		if (workingSet != null)
		{
			IAdaptable[] checkedExpressions = workingSet.getElements();
			expressionsViewer.setCheckedElements(checkedExpressions);
		}
		else
		{
			IStructuredSelection selection; 
			if (expressionView == null) {
				selection = new StructuredSelection();
			} else {
				selection = (IStructuredSelection)expressionView.getViewSite().getSelectionProvider().getSelection();
			}
			List checkedExpressions = new ArrayList();
			Iterator it1 = selection.iterator();
			while (it1.hasNext())
			{
				IExpression checkedExpr = (IExpression) DebugPlugin.getAdapter(it1.next(), IExpression.class);
				if (checkedExpr != null)
					checkedExpressions.add(checkedExpr);
			}
			
			expressionsViewer.setCheckedElements(checkedExpressions.toArray());
		}
		
		if (workingSet != null)
			txtWorkingSetName.setText(workingSet.getName());
		validateInput();

	}

	public void finish() {
		String workingSetName = txtWorkingSetName.getText();
		Object[] checkedElements = expressionsViewer.getCheckedElements();
		IExpression[] checkedExpressions = new IExpression[checkedElements.length];
		for (int i=0; i<checkedElements.length; i++) {
			Object checkedElement = checkedElements[i];
			checkedExpressions[i] = (IExpression) checkedElement;
		}
		
		if (workingSet == null) {
			IWorkingSetManager workingSetManager= PlatformUI.getWorkbench().getWorkingSetManager();
			workingSet = workingSetManager.createWorkingSet(workingSetName, checkedExpressions);
		} else {
			workingSet.setName(workingSetName);
			workingSet.setElements(checkedExpressions);
		}
	}

	public IWorkingSet getSelection() {
		return workingSet;
	}

	public void setSelection(IWorkingSet workingSet) {
		Assert.isNotNull(workingSet, "Working set must not be null"); //$NON-NLS-1$
		this.workingSet = workingSet;
		if (getContainer() != null && getShell() != null && txtWorkingSetName != null) {
			isFirstCheck= false;
			txtWorkingSetName.setText(workingSet.getName());
			validateInput();
		}
	}

	/**
	 * validates the current input of the page to determine if the finish button can be enabled
	 */
	private void validateInput() {
		String errorMessage= null; 
		String newText= txtWorkingSetName.getText();
		
		if (! newText.equals(newText.trim()))
			errorMessage = ExpressionWorkingSetMessages.Error_whitespace;
		if (newText.equals("")) { //$NON-NLS-1$
			if (isFirstCheck) {
				setPageComplete(false);
				isFirstCheck= false;
				return;
			}
			errorMessage = ExpressionWorkingSetMessages.Error_emptyName;
		}
		isFirstCheck = false;
		if (errorMessage == null && (workingSet == null || newText.equals(workingSet.getName()) == false)) {
			IWorkingSet[] workingSets= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets();
			for (int i= 0; i < workingSets.length; i++) {
				if (newText.equals(workingSets[i].getName())) {
					errorMessage = ExpressionWorkingSetMessages.Error_nameExists;
				}
			}
		}
		setErrorMessage(errorMessage);
		setPageComplete(errorMessage == null);
	}
}
