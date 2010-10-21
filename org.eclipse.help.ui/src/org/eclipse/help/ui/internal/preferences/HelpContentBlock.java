/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.preferences;

import java.util.Vector;

import org.eclipse.help.internal.base.remote.RemoteIC;
import org.eclipse.help.internal.base.util.TestConnectionUtility;
import org.eclipse.help.ui.internal.Messages;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
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
	private Button upButton;
	private Button downButton;
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
			} else if (upButton == source) {
				moveICUp();
			} else if (downButton == source) {
				moveICDown();
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
		
		upButton = container.createPushButton(parent, 
				Messages.HelpContentBlock_upTitle);
		upButton.addSelectionListener(selectionListener);
		
		downButton = container.createPushButton(parent, 
				Messages.HelpContentBlock_downTitle);
		downButton.addSelectionListener(selectionListener);
	}
	
	private void viewICProperties() {

		// Get selected item
		RemoteIC remoteic = (RemoteIC) ((IStructuredSelection) remoteICviewer
				.getSelection()).getFirstElement();

		if (remoteic != null) {
			ViewICPropsDialog dialog = new ViewICPropsDialog(remoteICviewer
					.getControl().getShell(), remoteic.getName());

			dialog.setTextValues(remoteic.getName(), remoteic.getHost(),
					remoteic.getPort(), remoteic.getPath(), remoteic.getProtocol(),remoteic.isEnabled());
			remoteICviewer.getTable().setSelection(indexSelected);

			if (dialog.open() == Window.OK) {

			}
		}
	}

	private void removeIC() {
		// Get selected item
		RemoteIC remoteic = (RemoteIC) ((IStructuredSelection) remoteICviewer
				.getSelection()).getFirstElement();

		boolean shouldRemove =
	          MessageDialog.openQuestion(
	        	remoteICviewer.getControl().getShell(),
	            NLS.bind(Messages.HelpContentBlock_rmvTitle , remoteic.getName()),
	            NLS.bind(Messages.HelpContentBlock_rmvLabel , remoteic.getName()));	

		if (shouldRemove)
			remoteICviewer.getRemoteICList().removeRemoteIC(remoteic);
		

	}

	private void editICInfo() {

		// Get selected item
		RemoteIC remoteic = (RemoteIC) ((IStructuredSelection) remoteICviewer
				.getSelection()).getFirstElement();

		if (remoteic != null) {
			EditICDialog dialog = new EditICDialog(remoteICviewer.getControl()
					.getShell(), remoteic.getName());

			dialog.setTextValues(remoteic.getName(), remoteic.getHost(),
					remoteic.getPort(), remoteic.getPath(),remoteic.getProtocol());

			remoteICviewer.getTable().setSelection(indexSelected);

			if (dialog.open() == Window.OK) {
				/*
				 * Reset the values in the current RemoteIC object to the user
				 * set values and update the model
				 */
				
				// Check hostname for http or https, and remove
				String host = dialog.getEnteredHost();
				if (host.toLowerCase().indexOf("https://")==0) //$NON-NLS-1$
					host = host.substring(8);
				else if (host.toLowerCase().indexOf("http://")==0) //$NON-NLS-1$
					host = host.substring(7);
				
				remoteic.setName(dialog.getEnteredName());
				remoteic.setHost(host);
				remoteic.setPath(dialog.getEnteredPath());
				remoteic.setPort(dialog.getEnteredPort());
				remoteic.setProtocol(dialog.getEnteredProtocol());

				remoteICviewer.getRemoteICList().updateRemoteIC(remoteic);
			}

		}
	}

	private void addNewIC() {

		AddICDialog dialog = new AddICDialog(remoteICviewer.getControl()
				.getShell());

		int rowCount;

		if (dialog.open() == Window.OK) {
			
			// For now, remove http or https if user
			// puts it in the hostname field
			String host = dialog.getEnteredHost();
			if (host.toLowerCase().indexOf("https://")==0) //$NON-NLS-1$
				host = host.substring(8);
			else if (host.toLowerCase().indexOf("http://")==0) //$NON-NLS-1$
				host = host.substring(7);
			
			RemoteIC remoteic = new RemoteIC(true, dialog.getEnteredName(),
					host, dialog.getEnteredPath(), dialog.getEnteredProtocol(),dialog
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
			
			upButton.setEnabled(true);
			downButton.setEnabled(true);
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
					remoteic.getPort(), remoteic.getPath(),remoteic.getProtocol());
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
	 * Raise the search priority of the selected InfoCenter
	 */
	public void moveICUp() {
		// Get selected item
		RemoteIC remoteic = (RemoteIC) ((IStructuredSelection) remoteICviewer
				.getSelection()).getFirstElement();
		
		RemoteIC[] rics = remoteICviewer.getRemoteICList().getRemoteICArray();
		
		for(int i = 0; i < rics.length; i++) {
			if(rics[i] == remoteic) {
				// Move the item as long as it's not already at the top of the list
				if(i > 0) { 
					remoteic = rics[i - 1];
					rics[i - 1] = rics[i];
					rics[i] = remoteic;
				}
			}
		}
		updateRemoteICs(rics);
	}
	
	/**
	 * Lower the search priority of the selected InfoCenter
	 */
	public void moveICDown() {
		// Get selected item
		RemoteIC remoteic = (RemoteIC) ((IStructuredSelection) remoteICviewer
				.getSelection()).getFirstElement();
		
		RemoteIC[] rics = remoteICviewer.getRemoteICList().getRemoteICArray();
		
		for(int i = 0; i < rics.length; i++) {
			if(rics[i] == remoteic) {
				// Move the item as long as it's not already at the bottom of the list
				if(i < (rics.length - 1)) { 
					remoteic = rics[i + 1];
					rics[i + 1] = rics[i];
					rics[i] = remoteic;
				}
			}
		}
		updateRemoteICs(rics);
	}
	
	/**
	 * @param rics the ordered ICs
	 */
	public void updateRemoteICs(RemoteIC[] rics) {
		Vector v = new Vector();
		for(int i = 0; i < rics.length; i++) { v.add(rics[i]); }
		getRemoteICviewer().updateRemoteICList(v);
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
			
			upButton.setEnabled(true);
			downButton.setEnabled(true);
			
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
		upButton.setEnabled(false);
		downButton.setEnabled(false);
		
		// Clear previous table selection
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
		upButton.setEnabled(false);
		downButton.setEnabled(false);
	}

	public RemoteICViewer getRemoteICviewer()
	{
		return remoteICviewer;
	}
	
	public RemoteIC[] getRemoteICList() {
		return remoteICviewer.getRemoteICList().getRemoteICArray();
	}
}
