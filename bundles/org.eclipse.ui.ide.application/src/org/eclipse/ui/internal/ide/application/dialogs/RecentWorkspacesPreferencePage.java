/*******************************************************************************
 * Copyright (c) 2009, 2010 Eric Rizzo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eric Rizzo - initial API and implementation
 *     Helena Halperin (IBM) - bug 299031 [BiDi] Incorrect file path display
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application.dialogs;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.ide.ChooseWorkspaceData;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * Preference page for editing the list of recent workspaces and whether or not
 * the user is prompted at startup.
 * 
 * @since 3.5
 */
public class RecentWorkspacesPreferencePage extends PreferencePage
	implements IWorkbenchPreferencePage {

	private static final int MIN_WORKSPACS = 5;
	private static final int MAX_WORKSPACES = 99;
	private static final int MAX_WORKSPACES_DIGIT_COUNT = 2;

	private ChooseWorkspaceData workspacesData;

	private Button promptOption;
	private Spinner maxWorkspacesField;
	private List workspacesList;
	private Button removeButton;


	public void init(IWorkbench workbench) {
		workspacesData = new ChooseWorkspaceData(Platform.getInstanceLocation().getURL());
	}

	public Control createContents(Composite parent) {
    	PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IWorkbenchHelpContextIds.WORKSPACES_PREFERENCE_PAGE);

		Composite container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		container.setLayout(gridLayout);

		createPromptOption(container);
		createMaxWorkspacesField(container);
		createWorkspacesList(container);
		
		Dialog.applyDialogFont(container);

		return container;
	}


	protected void createPromptOption(Composite parent) {
		promptOption = new Button(parent, SWT.CHECK);
		promptOption.setText(IDEWorkbenchMessages.RecentWorkspacesPreferencePage_PromptAtStartup_label);
		promptOption.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		promptOption.setSelection(workspacesData.getShowDialog());
		promptOption.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent event) {
					workspacesData.toggleShowDialog();
				}
			});
	}


	protected void createMaxWorkspacesField(Composite parent) {
		final Label maxWorkspacesLabel = new Label(parent, SWT.NONE);
		maxWorkspacesLabel.setText(IDEWorkbenchMessages.RecentWorkspacesPreferencePage_NumberOfWorkspaces_label);
		maxWorkspacesField = new Spinner(parent, SWT.BORDER);
		maxWorkspacesField.setTextLimit(MAX_WORKSPACES_DIGIT_COUNT);
		maxWorkspacesField.setMinimum(MIN_WORKSPACS);
		maxWorkspacesField.setMaximum(MAX_WORKSPACES);

		maxWorkspacesField.setSelection(workspacesData.getRecentWorkspaces().length);
	}


	protected void createWorkspacesList(Composite parent) {
		final Group recentWorkspacesGroup = new Group(parent, SWT.NONE);
		recentWorkspacesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		recentWorkspacesGroup.setText(IDEWorkbenchMessages.RecentWorkspacesPreferencePage_RecentWorkspacesList_label);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		recentWorkspacesGroup.setLayout(gridLayout_1);
		
		workspacesList = new List(recentWorkspacesGroup, SWT.BORDER | SWT.MULTI);
		final GridData gd_workspacesList = new GridData(SWT.FILL, SWT.FILL, true, true);
		workspacesList.setLayoutData(gd_workspacesList);
		
		removeButton = new Button(recentWorkspacesGroup, SWT.NONE);
		final GridData gd_removeButton = new GridData(SWT.CENTER, SWT.TOP, false, false);
		removeButton.setLayoutData(gd_removeButton);
		removeButton.setText(IDEWorkbenchMessages.RecentWorkspacesPreferencePage_RemoveButton_label);
		removeButton.setEnabled(false);

		removeButton.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent event) {
					removeSelectedWorkspaces();
					updateRemoveButton();
				}
			});

		workspacesList.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					updateRemoveButton();
				}
			});

		String[] recentWorkspaces = workspacesData.getRecentWorkspaces();
		for (int i = 0; i < recentWorkspaces.length; i++) {
			String aWorkspace = recentWorkspaces[i];
			if (aWorkspace != null) {
				workspacesList.add(TextProcessor.process(aWorkspace));
			}
		}
	}


	protected void removeSelectedWorkspaces() {
		// This would be a lot less code if we could use Jakarta CollectionUtils and/or ArrayUtils

		int[] selected = workspacesList.getSelectionIndices();
		java.util.List workspaces = new ArrayList(Arrays.asList(workspacesList.getItems()));

		// Iterate bottom-up because removal changes indices in the list
		for (int i = selected.length-1; i >= 0; i--) {
			workspaces.remove(selected[i]);
		}

		String[] newItems = new String[workspaces.size()];
		workspaces.toArray(newItems);
		workspacesList.setItems(newItems);
	}


	protected void performDefaults() {
		promptOption.setSelection(true);
		super.performDefaults();
	}


	public boolean performOk() {
		int maxWorkspaces = maxWorkspacesField.getSelection();
		String[] workspaces = new String[maxWorkspaces];
		String[] tmpListItem = workspacesList.getItems();
		String[] listItems = new String[tmpListItem.length];

		for (int i = 0; i < tmpListItem.length; i++){
			listItems[i] = TextProcessor.deprocess(tmpListItem[i]); 
		}

		if (maxWorkspaces < listItems.length) {
			// TODO: maybe alert the user that the list will be truncated?
			System.arraycopy(listItems, 0, workspaces, 0, maxWorkspaces);
		} else  {
			System.arraycopy(listItems, 0, workspaces, 0, listItems.length);
		}

		workspacesData.setRecentWorkspaces(workspaces);
		workspacesData.writePersistedData();
		return true;
	}


	protected void updateRemoveButton() {
		removeButton.setEnabled(workspacesList.getSelectionCount() > 0);
	}

}
