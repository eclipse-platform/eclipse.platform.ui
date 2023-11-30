/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Jan-Ove Weichel <janove.weichel@vogella.com> - Bug 475879
 *******************************************************************************/
package org.eclipse.jface.util;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * SafeRunnableDialog is a dialog that can show the results of multiple safe
 * runnable errors.
 */
class SafeRunnableDialog extends ErrorDialog {

	private TableViewer statusListViewer;

	private Collection<IStatus> statuses = new ArrayList<>();

	/**
	 * Create a new instance of the receiver on a status.
	 *
	 * @param status
	 *            The status to display.
	 */
	SafeRunnableDialog(IStatus status) {

		super(null, JFaceResources.getString("error"), status.getMessage(), //$NON-NLS-1$
				status, IStatus.ERROR);

		setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS | SWT.RESIZE | SWT.MIN | SWT.MAX
				| getDefaultOrientation());

		setStatus(status);
		statuses.add(status);

		setBlockOnOpen(false);

		String reason = JFaceResources
				.getString("SafeRunnableDialog_checkDetailsMessage"); //$NON-NLS-1$
		if (status.getException() != null) {
			reason = status.getException().getMessage() == null ? status
					.getException().toString() : status.getException()
					.getMessage();
		}
		this.message = JFaceResources.format("SafeRunnableDialog_reason", //$NON-NLS-1$
				status.getMessage(), reason);
	}

	/**
	 * Method which should be invoked when new errors become available for
	 * display
	 */
	void refresh() {

		if (AUTOMATED_MODE)
			return;

		createStatusList((Composite) dialogArea);
		updateEnablements();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Control area = super.createDialogArea(parent);
		createStatusList((Composite) area);
		return area;
	}

	/**
	 * Create the status list if required.
	 *
	 * @param parent
	 *            the Control to create it in.
	 */
	private void createStatusList(Composite parent) {
		if (isMultipleStatusDialog()) {
			if (statusListViewer == null) {
				// The job list doesn't exist so create it.
				setMessage(JFaceResources
						.getString("SafeRunnableDialog_MultipleErrorsMessage")); //$NON-NLS-1$
				getShell()
						.setText(
								JFaceResources
										.getString("SafeRunnableDialog_MultipleErrorsTitle")); //$NON-NLS-1$
				createStatusListArea(parent);
				showDetailsArea();
			}
			refreshStatusList();
		}
	}

	/*
	 * Update the button enablements
	 */
	private void updateEnablements() {
		Button details = getButton(IDialogConstants.DETAILS_ID);
		if (details != null) {
			details.setEnabled(true);
		}
	}

	/**
	 * This method sets the message in the message label.
	 *
	 * @param messageString -
	 *            the String for the message area
	 */
	private void setMessage(String messageString) {
		// must not set null text in a label
		message = messageString == null ? "" : messageString; //$NON-NLS-1$
		if (messageLabel == null || messageLabel.isDisposed()) {
			return;
		}
		messageLabel.setText(message);
	}

	/**
	 * Create an area that allow the user to select one of multiple jobs that
	 * have reported errors
	 *
	 * @param parent -
	 *            the parent of the area
	 */
	private void createStatusListArea(Composite parent) {
		// Display a list of jobs that have reported errors
		statusListViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		statusListViewer.setComparator(getViewerComparator());
		Control control = statusListViewer.getControl();
		GridData data = new GridData(GridData.FILL_BOTH
				| GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		data.heightHint = convertHeightInCharsToPixels(10);
		data.horizontalSpan = 2;
		control.setLayoutData(data);
		statusListViewer.setContentProvider(ArrayContentProvider.getInstance());
		statusListViewer.setLabelProvider(getStatusListLabelProvider());
		statusListViewer
				.addSelectionChangedListener(event -> handleSelectionChange());
		applyDialogFont(parent);
		statusListViewer.setInput(statuses);
	}

	/**
	 * Return the label provider for the status list.
	 *
	 * @return CellLabelProvider
	 */
	private CellLabelProvider getStatusListLabelProvider() {
		return new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(((IStatus) cell.getElement()).getMessage());

			}
		};
	}

	/*
	 * Return whether there are multiple errors to be displayed
	 */
	private boolean isMultipleStatusDialog() {
		return statuses.size() > 1;
	}

	/**
	 * Return a viewer sorter for looking at the jobs.
	 *
	 * @return ViewerSorter
	 */
	private ViewerComparator getViewerComparator() {
		return new ViewerComparator() {
			@Override
			public int compare(Viewer testViewer, Object e1, Object e2) {
				String message1 = ((IStatus) e1).getMessage();
				String message2 = ((IStatus) e2).getMessage();
				if (message1 == null)
					return 1;
				if (message2 == null)
					return -1;

				return message1.compareTo(message2);
			}
		};
	}

	/**
	 * Refresh the contents of the viewer.
	 */
	void refreshStatusList() {
		if (statusListViewer != null
				&& !statusListViewer.getControl().isDisposed()) {
			statusListViewer.refresh();
			Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
			getShell().setSize(newSize);
		}
	}

	/**
	 * Get the single selection. Return null if the selection is not just one
	 * element.
	 *
	 * @return IStatus or <code>null</code>.
	 */
	private IStatus getSingleSelection() {
		IStructuredSelection selection = statusListViewer.getStructuredSelection();
		if (selection != null && selection.size() == 1) {
			return (IStatus) selection.getFirstElement();
		}
		return null;
	}

	/**
	 * The selection in the multiple job list has changed. Update widget
	 * enablements and repopulate the list.
	 */
	void handleSelectionChange() {
		IStatus newSelection = getSingleSelection();
		setStatus(newSelection);
		updateEnablements();
		showDetailsArea();
	}

	@Override
	protected boolean shouldShowDetailsButton() {
		return true;
	}

	/**
	 * Add the status to the receiver.
	 */
	public void addStatus(IStatus status) {
		statuses.add(status);
		refresh();

	}


}
