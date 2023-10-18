/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.history;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.util.SWTUtil;
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

	@Override
	protected void createBottomButtonBar(final Composite parent) {
		// No button bar
	}

	@Override
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

	@Override
	protected TreeViewer createHistoryViewer(final Composite parent) {
		Assert.isNotNull(parent);
		if (fControlConfiguration.isCheckableViewer())
			return new RefactoringHistoryTreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		else
			return new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
	}

	@Override
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
	}

	@Override
	protected void createSelectionLabel(final Composite parent) {
		// No selection label
	}

	@Override
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

	@Override
	protected int getDetailColumns() {
		return 1;
	}

	@Override
	protected void handleCheckStateChanged() {
		super.handleCheckStateChanged();
		if (fDeleteButton != null)
			fDeleteButton.setEnabled(getCheckedDescriptors().length > 0);
	}

	@Override
	protected void handleSelectionChanged(final IStructuredSelection selection) {
		super.handleSelectionChanged(selection);
		if (fDeleteButton != null)
			fDeleteButton.setEnabled(getCheckedDescriptors().length > 0);
	}

	@Override
	public void setInput(final RefactoringHistory history) {
		super.setInput(history);
		if (fDeleteAllButton != null)
			fDeleteAllButton.setEnabled(history != null && !history.isEmpty());
		if (fDeleteButton != null)
			fDeleteButton.setEnabled(false);
	}
}
