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

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.util.SWTUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * Control which is capable of selecting elements of a refactoring history.
 * <p>
 * This control exspects a control configuration with a checkable tree viewer.
 * </p>
 * 
 * @since 3.2
 */
public class SelectRefactoringHistoryControl extends RefactoringHistoryControl {

	/** The empty descriptors constant */
	private static final RefactoringDescriptorProxy[] EMPTY_DESCRIPTORS= {};

	/** The deselect all button, or <code>null</code> */
	private Button fDeselectAllButton= null;

	/** The select all button, or <code>null</code> */
	private Button fSelectAllButton= null;

	/**
	 * Creates a new select refactoring history control.
	 * 
	 * @param parent
	 *            the parent control
	 * @param configuration
	 *            the refactoring history control configuration to use
	 */
	public SelectRefactoringHistoryControl(final Composite parent, final RefactoringHistoryControlConfiguration configuration) {
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

		fSelectAllButton= new Button(composite, SWT.NONE);
		fSelectAllButton.setEnabled(false);
		fSelectAllButton.setText(RefactoringUIMessages.SelectRefactoringHistoryControl_select_all_label);
		data= new GridData();
		data.horizontalAlignment= GridData.FILL;
		data.grabExcessHorizontalSpace= true;
		data.verticalAlignment= GridData.BEGINNING;
		data.widthHint= SWTUtil.getButtonWidthHint(fSelectAllButton);
		fSelectAllButton.setLayoutData(data);

		fSelectAllButton.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleSelectAll();
			}
		});

		fDeselectAllButton= new Button(composite, SWT.NONE);
		fDeselectAllButton.setEnabled(false);
		fDeselectAllButton.setText(RefactoringUIMessages.SelectRefactoringHistoryControl_deselect_all_label);
		data= new GridData();
		data.horizontalAlignment= GridData.FILL;
		data.grabExcessHorizontalSpace= true;
		data.verticalAlignment= GridData.BEGINNING;
		data.widthHint= SWTUtil.getButtonWidthHint(fDeselectAllButton);
		fDeselectAllButton.setLayoutData(data);

		fDeselectAllButton.addSelectionListener(new SelectionAdapter() {

			public final void widgetSelected(final SelectionEvent event) {
				handleDeselectAll();
			}
		});

		Dialog.applyDialogFont(parent);
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
	 * Returns the deselect all button.
	 * 
	 * @return the deselect all button
	 */
	public Button getDeselectAllButton() {
		return fDeselectAllButton;
	}

	/**
	 * Returns the select all button.
	 * 
	 * @return the select all button
	 */
	public Button getSelectAllButton() {
		return fSelectAllButton;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void handleCheckStateChanged() {
		super.handleCheckStateChanged();
		final RefactoringHistory history= getInput();
		if (history != null) {
			final int checked= getCheckedDescriptors().length;
			final int total= history.getDescriptors().length;
			fSelectAllButton.setEnabled(checked < total);
			fDeselectAllButton.setEnabled(checked > 0);
		}
	}

	/**
	 * Handles the deselect all event.
	 */
	protected void handleDeselectAll() {
		setCheckedDescriptors(EMPTY_DESCRIPTORS);
	}

	/**
	 * Handles the select all event.
	 */
	protected void handleSelectAll() {
		final RefactoringHistory history= getInput();
		if (history != null)
			setCheckedDescriptors(history.getDescriptors());
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInput(final RefactoringHistory history) {
		super.setInput(history);
		fDeselectAllButton.setEnabled(false);
		fSelectAllButton.setEnabled(history != null && !history.isEmpty());
	}
}