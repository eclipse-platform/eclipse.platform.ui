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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.dialogs.WorkingSetSelectionDialog;

public class SearchForBuildFilesDialog extends InputDialog {

	/**
	 * List of <code>IFile</code> objects that were found
	 */
	private List results = new ArrayList();
	/**
	 * List of <code>IContainer</code> objects in which to search
	 */
	private List searchScopes = new ArrayList();
	private Button workingSetScopeButton;

	public SearchForBuildFilesDialog() {
		super(Display.getCurrent().getActiveShell(), "Search for Build Files", "Input a build file name (* = any string, ? = any character):", "build.xml", new IInputValidator() {
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
	 * Change the label on the "Ok" button.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getOkButton().setText("Search");
	}

	/**
	 * Add the scope selection widgets to the dialog area
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		Group scope= new Group(composite, SWT.NONE);
		scope.setText("Scope");
		GridData data= new GridData(GridData.FILL_BOTH);
		scope.setLayoutData(data);
		GridLayout layout= new GridLayout(3, false);
		scope.setLayout(layout);
		
		Composite radioComposite= new Composite(scope, SWT.NONE);
		GridLayout radioLayout= new GridLayout();
		radioLayout.marginHeight= 0;
		radioComposite.setLayout(radioLayout);
		
		SelectionAdapter selectionListener= new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						updateOkEnabled();
					}
				};
		
		Button workspaceScope= new Button(radioComposite, SWT.RADIO);
		workspaceScope.setText("Workspace");
		workspaceScope.setSelection(true);
		workspaceScope.addSelectionListener(selectionListener);
		
		workingSetScopeButton=new Button(radioComposite, SWT.RADIO);
		workingSetScopeButton.setText("Working Set:");
		workingSetScopeButton.addSelectionListener(selectionListener);
		
		final Text workingSetText= new Text(scope, SWT.BORDER);
		data= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_END);
		workingSetText.setLayoutData(data);

		Button button = new Button(scope, SWT.PUSH);
		data= new GridData(GridData.VERTICAL_ALIGN_END);
		button.setLayoutData(data);
		button.setText("Choose...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				String workingSetName= handleChooseButtonPressed();
				if (workingSetName != null) {
					workingSetText.setText(workingSetName);
					workingSetScopeButton.setSelection(true);
				}
				updateOkEnabled();
			}
		});
		return composite;
	}
	
	/**
	 * Updates the enablement of the "Search" button based on the validity of
	 * the user's selections.
	 */
	private void updateOkEnabled() {
		if (workingSetScopeButton.getSelection()) {
			if (searchScopes.isEmpty()) {
				getErrorMessageLabel().setText("Must select a working set");
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
	private String handleChooseButtonPressed() {
		WorkingSetSelectionDialog dialog= new WorkingSetSelectionDialog(getShell(), false);
		if (dialog.open() == Dialog.CANCEL) {
			return null;
		}
		IWorkingSet[] sets= dialog.getSelection();
		if (sets == null) {
			return null;
		}
		IAdaptable[] elements= sets[0].getElements();
		for (int i = 0; i < elements.length; i++) {
			IAdaptable adaptable = elements[i];
			if (adaptable instanceof IContainer) {
				searchScopes.add(adaptable);
			}
		}
		return sets[0].getName();
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
	 * When the user presses the search button (tied to the OK id), search the
	 * workspace for files matching the regular expression in the input field.
	 */
	protected void okPressed() {
		String input = getInput();
		results = new ArrayList(); // Clear previous results
		StringMatcher matcher= new StringMatcher(input, true, false);
		if (searchScopes.isEmpty()) {
			searchForBuildFiles(matcher, ResourcesPlugin.getWorkspace().getRoot());
		} else {
			Iterator iter= searchScopes.iterator();
			while(iter.hasNext()) {
				searchForBuildFiles(matcher, (IContainer) iter.next());
			}
		}
		super.okPressed();
	}

	/**
	 * Searches for files whose name matches the given regular expression in the
	 * given container.
	 */
	private void searchForBuildFiles(StringMatcher matcher, IContainer container) {
		IResource[] members = null;
		try {
			members = container.members();
		} catch (CoreException e) {
			return;
		}
		for (int i = 0; i < members.length; i++) {
			IResource resource = members[i];
			if (resource instanceof IContainer) {
				searchForBuildFiles(matcher, (IContainer) resource);
			} else if (resource instanceof IFile) {
				if (matcher.match(((IFile) resource).getName())) {
					results.add(resource);
				}
			}
		}
	}

}
