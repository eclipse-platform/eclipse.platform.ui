/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.preferences;

import org.eclipse.help.internal.base.remote.RemoteIC;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class HelpContentBlock {

	private TableViewer tableViewer;
	private Button addNewICButton;
	private Button editICButton;
	private Button removeICButton;
	private Button viewPropertiesButton;
	private Button testICConnectionButton;
	private Button enableDisableICButton;
	private IHelpContentBlockContainer container;
	private RemoteICViewer remoteICviewer = null;
	private int validated = 2;
	private int indexSelected;

	private SelectionListener selectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == addNewICButton) {
				addNewIC();
			} else if (source == editICButton) {
				editICInfo();
			} else if (source == removeICButton) {
				removeIC();
			} else if (viewPropertiesButton == source) {
				viewICProperties();
			} else if (testICConnectionButton == source) {
				testICConnection();
			} else if (enableDisableICButton == source) {
				enableDisableIC();
			}
		}
	};

	public void setContainer(IHelpContentBlockContainer container) {
		this.container = container;
	}

	private void addButtonsToButtonGroup(Composite parent) {

		addNewICButton = container.createPushButton(parent,
				Messages.HelpContentBlock_addICTitle);
		addNewICButton.addSelectionListener(selectionListener);

		editICButton = container.createPushButton(parent,
				Messages.HelpContentBlock_editICTitle);
		editICButton.addSelectionListener(selectionListener);

		removeICButton = container.createPushButton(parent,
				Messages.HelpContentBlock_removeICTitle);
		removeICButton.addSelectionListener(selectionListener);

		viewPropertiesButton = container.createPushButton(parent,
				Messages.HelpContentBlock_viewICTitle);
		viewPropertiesButton.addSelectionListener(selectionListener);

		testICConnectionButton = container
				.createPushButton(
						parent,
						Messages.HelpContentBlock_testConnectionTitle);
		testICConnectionButton.addSelectionListener(selectionListener);

		String enableTitle = Messages.HelpContentBlock_3.length() > Messages.HelpContentBlock_4.length() ?
			Messages.HelpContentBlock_3 : Messages.HelpContentBlock_4;
		enableDisableICButton = container.createPushButton(parent, enableTitle); 
		enableDisableICButton.addSelectionListener(selectionListener);

	}

	private void viewICProperties() {

		// Get selected item
		RemoteIC remoteic = (RemoteIC) ((IStructuredSelection) remoteICviewer
				.getSelection()).getFirstElement();

		if (remoteic != null) {
			ViewICPropsDialog dialog = new ViewICPropsDialog(remoteICviewer
					.getControl().getShell(), remoteic.getName());

			dialog.setTextValues(remoteic.getName(), remoteic.getHost(),
					remoteic.getPort(), remoteic.getPath(), true);
			remoteICviewer.getTable().setSelection(indexSelected);

			if (dialog.open() == Window.OK) {

			}
		}
	}

	private void removeIC() {
		// Get selected item
		RemoteIC remoteic = (RemoteIC) ((IStructuredSelection) remoteICviewer
				.getSelection()).getFirstElement();

		if (remoteic != null) {
			RemoveICDialog dialog = new RemoveICDialog(remoteICviewer
					.getControl().getShell(), remoteic.getName());

			if (dialog.open() == Window.OK)
				remoteICviewer.getRemoteICList().removeRemoteIC(remoteic);

		}

	}

	private void editICInfo() {

		// Get selected item
		RemoteIC remoteic = (RemoteIC) ((IStructuredSelection) remoteICviewer
				.getSelection()).getFirstElement();

		if (remoteic != null) {
			EditICDialog dialog = new EditICDialog(remoteICviewer.getControl()
					.getShell(), remoteic.getName());

			dialog.setTextValues(remoteic.getName(), remoteic.getHost(),
					remoteic.getPort(), remoteic.getPath());

			remoteICviewer.getTable().setSelection(indexSelected);

			if (dialog.open() == Window.OK) {
				/*
				 * Reset the values in the current RemoteIC object to the user
				 * set values and update the model
				 */
				remoteic.setName(dialog.getEnteredName());
				remoteic.setHost(dialog.getEnteredHost());
				remoteic.setPath(dialog.getEnteredPath());
				remoteic.setPort(dialog.getEnteredPort());

				remoteICviewer.getRemoteICList().updateRemoteIC(remoteic);
			}

		}
	}

	private void addNewIC() {

		AddICDialog dialog = new AddICDialog(remoteICviewer.getControl()
				.getShell());

		int rowCount;

		if (dialog.open() == Window.OK) {
			RemoteIC remoteic = new RemoteIC(true, dialog.getEnteredName(),
					dialog.getEnteredHost(), dialog.getEnteredPath(), dialog
							.getEnteredPort());
			remoteICviewer.getRemoteICList().addRemoteIC(remoteic);
			rowCount = remoteICviewer.getTable().getItemCount();
			remoteICviewer.getTable().setSelection(rowCount - 1);
			indexSelected = rowCount - 1;

			addNewICButton.setEnabled(true);
			removeICButton.setEnabled(true);
			editICButton.setEnabled(true);
			viewPropertiesButton.setEnabled(true);
			testICConnectionButton.setEnabled(true);

			enableDisableICButton.setText(Messages.HelpContentBlock_3);
			enableDisableICButton.setEnabled(true);
		}

	}

	// Action method for clicking the Test Connection button

	public void testICConnection() {
		boolean isConnected;

		// Get selected item
		RemoteIC remoteic = (RemoteIC) ((IStructuredSelection) remoteICviewer
				.getSelection()).getFirstElement();

		if (remoteic != null) {
			isConnected = TestConnectionUtility.testConnection(remoteic.getHost(),
					remoteic.getPort(), remoteic.getPath());
			TestConnectionDialog dialog = new TestConnectionDialog(
					remoteICviewer.getControl().getShell());
			dialog.setValues(remoteic.getName(), remoteic.getHost(), remoteic
					.getPort(), remoteic.getPath());
			dialog.setConnectionStatus(isConnected);
			dialog.open();
		}

		remoteICviewer.getTable().setSelection(indexSelected);

	}

	// Handle Enable/Disable IC
	public void enableDisableIC() {

		RemoteIC selectedIC = remoteICviewer.getRemoteICList()
				.getRemoteICAtIndex(indexSelected);
		boolean isEnabled = selectedIC.isEnabled();
		if (isEnabled) // New status is Enabled. Set button text
		{
			selectedIC.setEnabled(false);
		} else // New status is disabled
		{
			selectedIC.setEnabled(true);
		}

		remoteICviewer.getTable().setSelection(indexSelected);
		tableSelectionChanged((IStructuredSelection) remoteICviewer
				.getTableViewer().getSelection());

		remoteICviewer.getRemoteICList().refreshRemoteIC(selectedIC,
				indexSelected);

	}

	/**
	 * Creates the group which will contain the buttons.
	 */
	private void createButtonGroup(Composite top) {
		Composite buttonGroup = new Composite(top, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonGroup.setLayout(layout);
		buttonGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttonGroup.setFont(top.getFont());

		addButtonsToButtonGroup(buttonGroup);
	}

	private void createRemoteICTable(Composite parent) {

		remoteICviewer = new RemoteICViewer(parent);
	}

	public void createContents(Composite parent) {
		createRemoteICTable(parent);
		createButtonGroup(parent);

		tableSelectionChanged((IStructuredSelection) remoteICviewer
				.getTableViewer().getSelection());
		remoteICviewer.getTableViewer().addSelectionChangedListener(
				new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						tableSelectionChanged((IStructuredSelection) event
								.getSelection());
					}
				});
	}

	private void tableSelectionChanged(IStructuredSelection selection) {

		indexSelected = remoteICviewer.getTable().getSelectionIndex();

		RemoteIC selectedEntry = (RemoteIC) ((IStructuredSelection) selection)
				.getFirstElement();

		if (selectedEntry != null) {
			addNewICButton.setEnabled(true);
			removeICButton.setEnabled(true);
			editICButton.setEnabled(true);
			viewPropertiesButton.setEnabled(true);
			testICConnectionButton.setEnabled(true);

			boolean currentEnabledStatus = selectedEntry.isEnabled();
			if (indexSelected == -1) {
				enableDisableICButton.setText(Messages.HelpContentBlock_4);
				enableDisableICButton.setEnabled(false);
			} else if (currentEnabledStatus) {
				// IC enabled. Button text is Disable

				enableDisableICButton.setText(Messages.HelpContentBlock_3);
				enableDisableICButton.setEnabled(true);
				selectedEntry.setEnabled(true);
			} else {
				enableDisableICButton.setText(Messages.HelpContentBlock_4);
				enableDisableICButton.setEnabled(true);
				selectedEntry.setEnabled(false);
			}
		} else {
			restoreDefaultButtons();
		}

	}

	public void updateContainer() {
		validated = 0;
		container.update();
	}

	public boolean isValidated() {
		return validated >= 2;
	}

	public void setValidated() {
		validated = 2;
	}

	public TableViewer getHelpBlockTableViewer() {
		return tableViewer;
	}

	public void restoreDefaultButtons() {

		addNewICButton.setEnabled(true);
		editICButton.setEnabled(false);
		removeICButton.setEnabled(false);
		viewPropertiesButton.setEnabled(false);
		testICConnectionButton.setEnabled(false);
		enableDisableICButton.setEnabled(false);
		enableDisableICButton.setText(Messages.HelpContentBlock_4);
		
		//Clear previous table selection
		indexSelected = - 1;
		

	}
	public void disableAllButtons() {

		addNewICButton.setEnabled(false);
		editICButton.setEnabled(false);
		removeICButton.setEnabled(false);
		viewPropertiesButton.setEnabled(false);
		testICConnectionButton.setEnabled(false);
		enableDisableICButton.setEnabled(false);
		enableDisableICButton.setText(Messages.HelpContentBlock_4);

	}

	public RemoteICViewer getRemoteICviewer()
	{
		return remoteICviewer;
	}
	
	public RemoteIC[] getRemoteICList() {
		return remoteICviewer.getRemoteICList().getRemoteICArray();
	}
}
