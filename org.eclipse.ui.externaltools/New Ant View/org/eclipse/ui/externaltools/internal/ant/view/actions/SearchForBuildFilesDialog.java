package org.eclipse.ui.externaltools.internal.ant.view.actions;
/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IPreferenceConstants;
import org.eclipse.ui.internal.dialogs.WorkingSetSelectionDialog;

/**
 * This dialog allows the user to search for Ant build files whose names match a
 * given pattern. The search may be performed on the entire workspace or it can
 * be limited to a particular working set.
 */
public class SearchForBuildFilesDialog extends InputDialog {

	/**
	 * List of <code>IFile</code> objects that were found
	 */
	private List results = new ArrayList();
	/**
	 * List of <code>IResource</code> objects in which to search.
	 * 
	 * If the searchScopes are <code>null</code>, the user has asked to search
	 * the workspace. If the searchScopes are empty, the user has asked to
	 * search a working set that has no resources.
	 */
	private List searchScopes = null;
	/**
	 * The working set scope radio button.
	 */
	private Button workingSetScopeButton;
	/**
	 * The workspace scope radio button.
	 */
	private Button workspaceScopeButton;
	/**
	 * The text field that displays the current working set name
	 */
	private Text workingSetText;
	/**
	 * The button that allows the user to decide if error results should be
	 * parsed
	 */
	private Button includeErrorResultButton;
	private static IPreferenceStore store= ExternalToolsPlugin.getDefault().getPreferenceStore();

	public SearchForBuildFilesDialog() {
		super(Display.getCurrent().getActiveShell(), "Search for Build Files", "Input a build file name (* = any string, ? = any character):",
				store.getString(IPreferenceConstants.ANTVIEW_LAST_SEARCH_STRING), new IInputValidator() {
			public String isValid(String newText) {
				String trimmedText = newText.trim();
				if (trimmedText.length() == 0) {
					return "Build name cannot be empty";
				}
				return null;
			}
		});
	}

	/**
	 * Change the label on the "Ok" button and initialize the enabled state
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getOkButton().setText("Search");

		String workingSetName= store.getString(IPreferenceConstants.ANTVIEW_LAST_WORKINGSET_SEARCH_SCOPE);
		if (workingSetName.length() > 0) {
			setWorkingSet(PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(workingSetName));
		}
		if (!store.getBoolean(IPreferenceConstants.ANTVIEW_USE_WORKINGSET_SEARCH_SCOPE)) {
			selectRadioButton(workspaceScopeButton);
			handleRadioButtonPressed();
		}
	}

	/**
	 * Add the scope selection widgets to the dialog area
	 */
	protected Control createDialogArea(Composite parent) {
		Font font = parent.getFont();
		
		Composite composite = (Composite) super.createDialogArea(parent);
		createIncludeErrorResultButton(composite, font);
		createScopeGroup(composite, font);
		return composite;
	}
	
	private void createScopeGroup(Composite composite, Font font) {
		Group scope= new Group(composite, SWT.NONE);
		scope.setText("Scope");
		GridData data= new GridData(GridData.FILL_BOTH);
		scope.setLayoutData(data);
		GridLayout layout= new GridLayout(3, false);
		scope.setLayout(layout);
		scope.setFont(font);
		
		// Create a composite for the radio buttons
		Composite radioComposite= new Composite(scope, SWT.NONE);
		GridLayout radioLayout= new GridLayout();
		radioLayout.marginHeight= 0;
		radioComposite.setLayout(radioLayout);

		SelectionAdapter selectionListener= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRadioButtonPressed();
			}
		};

		workspaceScopeButton= new Button(radioComposite, SWT.RADIO);
		workspaceScopeButton.setFont(font);
		workspaceScopeButton.setText("Workspace");
		workspaceScopeButton.addSelectionListener(selectionListener);

		workingSetScopeButton=new Button(radioComposite, SWT.RADIO);
		workingSetScopeButton.setFont(font);
		workingSetScopeButton.setText("Working Set:");
		workingSetScopeButton.addSelectionListener(selectionListener);
		
		selectRadioButton(workspaceScopeButton);

		workingSetText= new Text(scope, SWT.BORDER);
		workingSetText.setEditable(false);
		data= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		workingSetText.setLayoutData(data);
		workingSetText.setFont(font);

		Button chooseButton = new Button(scope, SWT.PUSH);
		data= new GridData(GridData.VERTICAL_ALIGN_END);
		chooseButton.setLayoutData(data);
		chooseButton.setFont(font);
		chooseButton.setText("Choose...");
		chooseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleChooseButtonPressed();
			}
		});
	}
	
	/**
	 * Programatically selects the given radio button, deselecting the other
	 * radio button.
	 * 
	 * @param button the radio button to select. This parameter must be one of
	 * either the <code>workingSetScopeButton</code> or the
	 * <code>workspaceScopeButton</code> or this method will have no effect.
	 */
	private void selectRadioButton(Button button) {
		if (button == workingSetScopeButton) {
			workingSetScopeButton.setSelection(true);
			workspaceScopeButton.setSelection(false);
		} else if (button == workspaceScopeButton) {
			workspaceScopeButton.setSelection(true);
			workingSetScopeButton.setSelection(false);
		}
	}
	
	/**
	 * One of the search scope radio buttons has been pressed. Update the dialog
	 * accordingly.
	 */
	private void handleRadioButtonPressed() {
		if (workingSetScopeButton.getSelection()) {
			IWorkingSet set= PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(getWorkingSetName());
			if (set != null) {
				setWorkingSet(set);
				return;
			}
		}
		setWorkingSet(null);
	}
	
	/**
	 * Returns the working set name currently displayed.
	 */
	private String getWorkingSetName() {
		return workingSetText.getText().trim();
	}
	
	/**
	 * Creates the button that allows the user to specify whether or not build
	 * files should that cannot be parsed should be included in the results.
	 */
	private void createIncludeErrorResultButton(Composite composite, Font font) {
		includeErrorResultButton= new Button(composite, SWT.CHECK);
		includeErrorResultButton.setFont(font);
		includeErrorResultButton.setText("Include build files that contain errors");
		includeErrorResultButton.setSelection(store.getBoolean(IPreferenceConstants.ANTVIEW_INCLUDE_ERROR_SEARCH_RESULTS));
	}
	
	/**
	 * Updates the enablement of the "Search" button based on the validity of
	 * the user's selections.
	 */
	private void updateOkEnabled() {
		if (workingSetScopeButton.getSelection()) {
			String error= null;
			if (searchScopes == null) {
				error= "Must select a working set";
			} else if (searchScopes.isEmpty()) {
				error= "No searchable resources found in the selected working set";
			}
			if (error != null) {
				getErrorMessageLabel().setText(error);
				getErrorMessageLabel().getParent().update();
				getOkButton().setEnabled(false);
				return;
			}
		}
		getOkButton().setEnabled(true);
		getErrorMessageLabel().setText("");
		getErrorMessageLabel().getParent().update();
	}

	/**
	 * Handles the working set choose button pressed. Returns the name of the
	 * chosen working set or <code>null</code> if none.
	 */
	private void handleChooseButtonPressed() {
		WorkingSetSelectionDialog dialog= new WorkingSetSelectionDialog(getShell(), false);
		if (dialog.open() == Dialog.CANCEL) {
			return;
		}
		IWorkingSet[] sets= dialog.getSelection();
		if (sets == null) {
			return;
		}
		
		setWorkingSet(sets[0]); // We disallowed multi-select
	}
	
	/**
	 * Sets the current working set search scope. This populates the search
	 * scope with resources found in the given working set and updates the
	 * enabled state of the dialog based on the sets contents.
	 * 
	 * @param set the working set scope for the search
	 */
	private void setWorkingSet(IWorkingSet set) {
		if (set == null) {
			searchScopes= null;
			updateOkEnabled();
			return;
		}
		IAdaptable[] elements= set.getElements();
		searchScopes= new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			// Try to get an IResource object from each element
			IResource resource= null;
			IAdaptable adaptable = elements[i];
			if (adaptable instanceof IResource) {
				resource= (IResource) adaptable;
			} else {
				resource= (IResource) adaptable.getAdapter(IResource.class);
			}
			if (resource != null) {
				searchScopes.add(resource);
			}
		}
		workingSetText.setText(set.getName());
		selectRadioButton(workingSetScopeButton);
		
		updateOkEnabled();
	}

	/**
	 * Returns the trimmed user input
	 */
	private String getInput() {
		return getText().getText().trim();
	}

	/**
	 * Returns the search results
	 */
	public IFile[] getResults() {
		return (IFile[]) results.toArray(new IFile[results.size()]);
	}
	
	/**
	 * Returns whether the user wishes to include results which cannot be
	 * parsed.
	 */
	public boolean getIncludeErrorResults() {
		return store.getBoolean(IPreferenceConstants.ANTVIEW_INCLUDE_ERROR_SEARCH_RESULTS);
	}

	/**
	 * When the user presses the search button (tied to the OK id), search the
	 * workspace for files matching the regular expression in the input field.
	 */
	protected void okPressed() {
		String input = getInput();
		store.setValue(IPreferenceConstants.ANTVIEW_LAST_SEARCH_STRING, input);
		store.setValue(IPreferenceConstants.ANTVIEW_INCLUDE_ERROR_SEARCH_RESULTS, includeErrorResultButton.getSelection());
		store.setValue(IPreferenceConstants.ANTVIEW_LAST_WORKINGSET_SEARCH_SCOPE, getWorkingSetName());
		store.setValue(IPreferenceConstants.ANTVIEW_USE_WORKINGSET_SEARCH_SCOPE, workingSetScopeButton.getSelection());
		results = new ArrayList(); // Clear previous results
		StringMatcher matcher= new StringMatcher(input, true, false);
		if (searchScopes == null || searchScopes.isEmpty()) {
			searchForBuildFiles(matcher, ResourcesPlugin.getWorkspace().getRoot());
		} else {
			Iterator iter= searchScopes.iterator();
			while(iter.hasNext()) {
				searchForBuildFiles(matcher, (IResource) iter.next());
			}
		}
		super.okPressed();
	}

	/**
	 * Searches for files whose name matches the given regular expression in the
	 * given container.
	 * 
	 * @param matcher the string matcher used to determine which files should
	 * be added to the results
	 * @param resource the resource to search
	 */
	private void searchForBuildFiles(StringMatcher matcher, IResource resource) {
		if (resource instanceof IContainer) {
			IContainer container= (IContainer) resource;
			IResource[] members = null;
			try {
				members = container.members();
			} catch (CoreException e) {
				return;
			}
			for (int i = 0; i < members.length; i++) {
				searchForBuildFiles(matcher, members[i]);
			}
		} else if (resource instanceof IFile) {
			if (matcher.match(((IFile) resource).getName())) {
				results.add(resource);
			}
		}
	}

}
