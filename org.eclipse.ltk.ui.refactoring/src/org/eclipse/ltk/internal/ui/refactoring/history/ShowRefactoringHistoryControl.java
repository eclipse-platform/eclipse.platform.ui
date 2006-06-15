/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * Control which is capable of displaying and editing the global workspace
 * refactoring history.
 * 
 * @since 3.2
 */
public class ShowRefactoringHistoryControl extends SortableRefactoringHistoryControl {

	/** The delete all button, or <code>null</code> */
	private Button fDeleteAllButton= null;

	/** The delete button, or <code>null</code> */
	private Button fDeleteButton= null;

	/** The edit button, or <code>null</code> */
	private Button fEditButton= null;

	/**
	 * Creates a new show refactoring history control.
	 * 
	 * @param parent
	 *            the parent control
	 * @param configuration
	 *            the refactoring history control configuration to use
	 */
	public ShowRefactoringHistoryControl(final Composite parent, final RefactoringHistoryControlConfiguration configuration) {
		super(parent, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createBottomButtonBar(final Composite parent) {
		// No button bar
	}

	/**
	 * {@inheritDoc}
	 */
	public void createControl() {
		super.createControl();
		final GridData data= new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		data.heightHint= new PixelConverter(this).convertHeightInCharsToPixels(24);
		setLayoutData(data);
	}

	/**
	 * Creates the delete all button of the control.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createDeleteAllButton(final Composite parent) {
		Assert.isNotNull(parent);
		fDeleteAllButton= new Button(parent, SWT.NONE);
		fDeleteAllButton.setEnabled(false);
		fDeleteAllButton.setText(RefactoringUIMessages.ShowRefactoringHistoryControl_delete_all_label);
		final GridData data= new GridData();
		data.horizontalAlignment= GridData.FILL;
		data.grabExcessHorizontalSpace= true;
		data.verticalAlignment= GridData.BEGINNING;
		data.widthHint= SWTUtil.getButtonWidthHint(fDeleteAllButton);
		fDeleteAllButton.setLayoutData(data);
	}

	/**
	 * Creates the delete button of the control.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param alignment
	 *            the alignment of the button
	 */
	protected void createDeleteButton(final Composite parent, final int alignment) {
		Assert.isNotNull(parent);
		fDeleteButton= new Button(parent, SWT.NONE);
		fDeleteButton.setEnabled(false);
		fDeleteButton.setText(RefactoringUIMessages.ShowRefactoringHistoryControl_delete_label);
		final GridData data= new GridData();
		data.horizontalAlignment= alignment;
		data.grabExcessHorizontalSpace= true;
		data.verticalAlignment= GridData.BEGINNING;
		data.widthHint= SWTUtil.getButtonWidthHint(fDeleteButton);
		fDeleteButton.setLayoutData(data);
	}

	/**
	 * Creates the edit button of the control.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	protected void createEditButton(final Composite parent) {
		Assert.isNotNull(parent);
		fEditButton= new Button(parent, SWT.NONE);
		fEditButton.setEnabled(false);
		fEditButton.setText(RefactoringUIMessages.ShowRefactoringHistoryControl_edit_label);
		final GridData data= new GridData();
		data.verticalIndent= new PixelConverter(parent).convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		data.horizontalAlignment= GridData.FILL;
		data.grabExcessHorizontalSpace= true;
		data.verticalAlignment= GridData.BEGINNING;
		data.widthHint= SWTUtil.getButtonWidthHint(fEditButton);
		fEditButton.setLayoutData(data);
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
	 * {@inheritDoc}
	 */
	protected void createRightButtonBar(final Composite parent) {
		Assert.isNotNull(parent);
		final Composite composite= new Composite(parent, SWT.NONE);
		final GridLayout layout= new GridLayout(1, false);
		composite.setLayout(layout);

		final GridData data= new GridData();
		data.grabExcessHorizontalSpace= false;
		data.grabExcessVerticalSpace= true;
		data.horizontalAlignment= SWT.FILL;
		data.verticalAlignment= SWT.TOP;
		composite.setLayoutData(data);

		createDeleteButton(composite, GridData.FILL);
		createDeleteAllButton(composite);
		createEditButton(composite);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void createSelectionLabel(final Composite parent) {
		// No selection label
	}

	/**
	 * {@inheritDoc}
	 */
	protected int getContainerColumns() {
		return 2;
	}

	/**
	 * Returns the delete all button.
	 * 
	 * @return the delete all button, or <code>null</code>
	 */
	public Button getDeleteAllButton() {
		return fDeleteAllButton;
	}

	/**
	 * Returns the delete button.
	 * 
	 * @return the delete button, or <code>null</code>
	 */
	public Button getDeleteButton() {
		return fDeleteButton;
	}

	/**
	 * {@inheritDoc}
	 */
	protected int getDetailColumns() {
		return 1;
	}

	/**
	 * Returns the edit button.
	 * 
	 * @return the edit button, or <code>null</code>
	 */
	public Button getEditButton() {
		return fEditButton;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void handleCheckStateChanged() {
		super.handleCheckStateChanged();
		if (fDeleteButton != null)
			fDeleteButton.setEnabled(getCheckedDescriptors().length > 0);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void handleSelectionChanged(final IStructuredSelection selection) {
		super.handleSelectionChanged(selection);
		if (fEditButton != null)
			fEditButton.setEnabled(getSelectedDescriptors().length == 1);
		if (fDeleteButton != null)
			fDeleteButton.setEnabled(getCheckedDescriptors().length > 0);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInput(final RefactoringHistory history) {
		super.setInput(history);
		if (fDeleteAllButton != null)
			fDeleteAllButton.setEnabled(history != null && !history.isEmpty());
		if (fDeleteButton != null)
			fDeleteButton.setEnabled(false);
		if (fEditButton != null)
			fEditButton.setEnabled(false);
	}
}
