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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * Control which is capable of displaying and exporting refactoring histories.
 * 
 * @since 3.2
 */
public final class ExportRefactoringHistoryControl extends RefactoringHistoryControl {

	/** The export all button, or <code>null</code> */
	private Button fExportAllButton= null;

	/** The export button, or <code>null</code> */
	private Button fExportButton= null;

	/**
	 * Creates a new export refactoring history control.
	 * 
	 * @param parent
	 *            the parent control
	 * @param configuration
	 *            the refactoring history control configuration to use
	 */
	public ExportRefactoringHistoryControl(final Composite parent, final RefactoringHistoryControlConfiguration configuration) {
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

		fExportButton= new Button(composite, SWT.NONE);
		fExportButton.setEnabled(false);
		fExportButton.setText(RefactoringUIMessages.ExportRefactoringHistoryControl_export_label);

		data= new GridData();
		data.horizontalAlignment= SWT.FILL;
		fExportButton.setLayoutData(data);

		fExportAllButton= new Button(composite, SWT.NONE);
		fExportAllButton.setEnabled(false);
		fExportAllButton.setText(RefactoringUIMessages.ExportRefactoringHistoryControl_export_all_label);

		data= new GridData();
		data.horizontalAlignment= SWT.FILL;
		fExportAllButton.setLayoutData(data);
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
	 * Returns the export all button.
	 * 
	 * @return the export all button
	 */
	public Button getExportAllButton() {
		return fExportAllButton;
	}

	/**
	 * Returns the export button.
	 * 
	 * @return the export button
	 */
	public Button getExportButton() {
		return fExportButton;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void handleCheckStateChanged() {
		super.handleCheckStateChanged();
		fExportButton.setEnabled(getCheckedDescriptors().length > 0);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInput(final RefactoringHistory history) {
		super.setInput(history);
		fExportAllButton.setEnabled(history != null && !history.isEmpty());
	}
}
