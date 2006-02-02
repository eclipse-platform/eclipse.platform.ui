/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.core.runtime.Assert;

import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.util.PixelConverter;
import org.eclipse.ltk.internal.ui.refactoring.util.SWTUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * Control which is capable of managing refactoring histories.
 * 
 * @since 3.2
 */
public class ManageRefactoringHistoryControl extends RefactoringHistoryControl {

	/** The delete all button, or <code>null</code> */
	private Button fDeleteAllButton= null;

	/** The delete button, or <code>null</code> */
	private Button fDeleteButton= null;

	/** The edit button, or <code>null</code> */
	private Button fEditButton= null;

	/**
	 * Creates a new manage refactoring history control.
	 * 
	 * @param parent
	 *            the parent control
	 * @param configuration
	 *            the refactoring history control configuration to use
	 */
	public ManageRefactoringHistoryControl(final Composite parent, final RefactoringHistoryControlConfiguration configuration) {
		super(parent, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createButtonBar(final Composite parent) {
		Assert.isNotNull(parent);
		final Composite composite= new Composite(parent, SWT.NONE);
		final GridLayout layout= new GridLayout(1, false);
		composite.setLayout(layout);

		GridData data= new GridData();
		data.grabExcessHorizontalSpace= false;
		data.grabExcessVerticalSpace= true;
		data.horizontalAlignment= SWT.FILL;
		data.verticalAlignment= SWT.TOP;
		composite.setLayoutData(data);

		fDeleteButton= new Button(composite, SWT.NONE);
		fDeleteButton.setEnabled(false);
		fDeleteButton.setText(RefactoringUIMessages.ManageRefactoringHistorycontrol_delete_label);
		data= new GridData();
		data.horizontalAlignment= GridData.FILL;
		data.grabExcessHorizontalSpace= true;
		data.verticalAlignment= GridData.BEGINNING;
		data.widthHint= SWTUtil.getButtonWidthHint(fDeleteButton);
		fDeleteButton.setLayoutData(data);

		fDeleteAllButton= new Button(composite, SWT.NONE);
		fDeleteAllButton.setEnabled(false);
		fDeleteAllButton.setText(RefactoringUIMessages.ManageRefactoringHistorycontrol_delete_all_label);
		data= new GridData();
		data.horizontalAlignment= GridData.FILL;
		data.grabExcessHorizontalSpace= true;
		data.verticalAlignment= GridData.BEGINNING;
		data.widthHint= SWTUtil.getButtonWidthHint(fDeleteAllButton);
		fDeleteAllButton.setLayoutData(data);

		fEditButton= new Button(composite, SWT.NONE);
		fEditButton.setEnabled(false);
		fEditButton.setText(RefactoringUIMessages.ManageRefactoringHistoryControl_edit_label);
		data= new GridData();
		data.verticalIndent= new PixelConverter(parent).convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		data.horizontalAlignment= GridData.FILL;
		data.grabExcessHorizontalSpace= true;
		data.verticalAlignment= GridData.BEGINNING;
		data.widthHint= SWTUtil.getButtonWidthHint(fEditButton);
		fEditButton.setLayoutData(data);

		Dialog.applyDialogFont(parent);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl() {
		super.createControl();
		final GridData data= new GridData();
		data.grabExcessHorizontalSpace= true;
		data.heightHint= new PixelConverter(this).convertHeightInCharsToPixels(24);
		data.horizontalAlignment= SWT.FILL;
		data.verticalAlignment= SWT.FILL;
		setLayoutData(data);
	}

	/**
	 * {@inheritDoc}
	 */
	protected TreeViewer createHistoryViewer(final Composite parent) {
		Assert.isNotNull(parent);
		if (fControlConfiguration.isCheckableViewer())
			return new RefactoringHistoryTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		else
			return new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
	}

	/**
	 * Returns the delete all button.
	 * 
	 * @return the delete all button
	 */
	public Button getDeleteAllButton() {
		return fDeleteAllButton;
	}

	/**
	 * Returns the delete button.
	 * 
	 * @return the delete button
	 */
	public Button getDeleteButton() {
		return fDeleteButton;
	}

	/**
	 * Returns the edit button.
	 * 
	 * @return the edit button
	 */
	public Button getEditButton() {
		return fEditButton;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void handleCheckStateChanged() {
		super.handleCheckStateChanged();
		fDeleteButton.setEnabled(getCheckedDescriptors().length > 0);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void handleSelectionChanged(final IStructuredSelection selection) {
		super.handleSelectionChanged(selection);
		fEditButton.setEnabled(getSelectedDescriptors().length == 1);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInput(final RefactoringHistory history) {
		super.setInput(history);
		fDeleteAllButton.setEnabled(history != null && !history.isEmpty());
		fDeleteButton.setEnabled(false);
		fEditButton.setEnabled(false);
	}
}