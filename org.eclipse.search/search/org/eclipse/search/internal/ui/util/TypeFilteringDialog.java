/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;

import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FileEditorMappingContentProvider;
import org.eclipse.ui.dialogs.FileEditorMappingLabelProvider;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

import org.eclipse.search.internal.ui.ISearchHelpContextIds;
import org.eclipse.search.internal.ui.SearchMessages;

/**
 * The TypeFilteringDialog is a SelectionDialog that allows the user to select a file editor.
 * XXX: Workbench should offer this dialog (public API), see: bug 2763: TypeFilteringDialog should be public API
 */
public class TypeFilteringDialog extends SelectionDialog {

	private Collection fInitialSelections;

	// the visual selection widget group
	private CheckboxTableViewer fListViewer;

	// sizing constants
	private final static int SIZING_SELECTION_WIDGET_HEIGHT= 250;
	private final static int SIZING_SELECTION_WIDGET_WIDTH= 300;

	private Text fUserDefinedText;

	private IFileEditorMapping[] fCurrentInput;
	/**
	 * Creates a type selection dialog using the supplied entries. Set the initial selections to those
	 * whose extensions match the preselections.
	 */
	public TypeFilteringDialog(Shell parentShell, Collection preselections) {
		super(parentShell);
		setTitle(SearchMessages.getString("TypesFiltering.title")); //$NON-NLS-1$
		fInitialSelections= preselections;
		setMessage(SearchMessages.getString("TypesFiltering.message")); //$NON-NLS-1$
	}

	/**
	 * Add the selection and deselection buttons to the dialog.
	 * @param composite org.eclipse.swt.widgets.Composite
	 */
	private void addSelectionButtons(Composite composite) {

		Composite buttonComposite= new Composite(composite, SWT.RIGHT);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		buttonComposite.setLayout(layout);
		GridData data =
			new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace= true;
		composite.setData(data);

		Button selectButton =
			createButton(
				buttonComposite,
				IDialogConstants.SELECT_ALL_ID,
				SearchMessages.getString("TypesFiltering.selectAll"), //$NON-NLS-1$
				false);

		SelectionListener listener= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getListViewer().setAllChecked(true);
			}
		};
		selectButton.addSelectionListener(listener);

		Button deselectButton =
			createButton(
				buttonComposite,
				IDialogConstants.DESELECT_ALL_ID,
				SearchMessages.getString("TypesFiltering.deselectAll"), //$NON-NLS-1$
				false);

		listener= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getListViewer().setAllChecked(false);

			}
		};
		deselectButton.addSelectionListener(listener);

	}

	/**
	 * Add the currently-specified extensions.
	 */
	private void addUserDefinedEntries(List result) {

		StringTokenizer tokenizer =
			new StringTokenizer(fUserDefinedText.getText(), FileTypeEditor.TYPE_DELIMITER);

		//Allow the *. and . prefix and strip out the extension
		while (tokenizer.hasMoreTokens()) {
			String currentExtension= tokenizer.nextToken().trim();
			if (!currentExtension.equals("")) //$NON-NLS-1$
				result.add(currentExtension);
		}
	}

	/**
	 * Visually checks the previously-specified elements in this dialog's list 
	 * viewer.
	 */
	private void checkInitialSelections() {

		IFileEditorMapping editorMappings[] =
			PlatformUI.getWorkbench().getEditorRegistry().getFileEditorMappings();
		ArrayList selectedMappings= new ArrayList();

		for (int i= 0; i < editorMappings.length; i++) {
			IFileEditorMapping mapping= editorMappings[i];
			if (fInitialSelections.contains(mapping.getLabel())) {
				fListViewer.setChecked(mapping, true);
				selectedMappings.add(mapping.getLabel());
			}
		}

		//Now add in the ones not selected to the user defined list
		Iterator initialIterator= fInitialSelections.iterator();
		StringBuffer entries= new StringBuffer();
		boolean first= true;
		while (initialIterator.hasNext()) {
			String nextExtension= (String)initialIterator.next();
			if (!selectedMappings.contains(nextExtension)) {
				if (!first) {
					entries.append(FileTypeEditor.TYPE_DELIMITER);
					entries.append(" "); //$NON-NLS-1$
				}
				first= false;
				entries.append(nextExtension);
			}
		}
		fUserDefinedText.setText(entries.toString());
	}

	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		WorkbenchHelp.setHelp(shell, ISearchHelpContextIds.TYPE_FILTERING_DIALOG);
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite= (Composite)super.createDialogArea(parent);

		createMessageArea(composite);

		fListViewer= CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData data= new GridData(GridData.FILL_BOTH);
		data.heightHint= SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint= SIZING_SELECTION_WIDGET_WIDTH;
		fListViewer.getTable().setLayoutData(data);

		fListViewer.setLabelProvider(FileEditorMappingLabelProvider.INSTANCE);
		fListViewer.setContentProvider(FileEditorMappingContentProvider.INSTANCE);

		addSelectionButtons(composite);

		createUserEntryGroup(composite);

		initializeViewer();

		// initialize page
		if (fInitialSelections != null && !fInitialSelections.isEmpty())
			checkInitialSelections();

		applyDialogFont(composite);
		return composite;
	}

	/**
	 * Create the group that shows the user defined entries for the dialog.
	 * @param parent the parent this is being created in.
	 */
	private void createUserEntryGroup(Composite parent) {

		// destination specification group
		Composite userDefinedGroup= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		userDefinedGroup.setLayout(layout);
		userDefinedGroup.setLayoutData(
			new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

		new Label(userDefinedGroup, SWT.NONE).setText(
			SearchMessages.getString("TypesFiltering.otherExtensions")); //$NON-NLS-1$

		// user defined entry field
		fUserDefinedText= new Text(userDefinedGroup, SWT.SINGLE | SWT.BORDER);
		GridData data =
			new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		fUserDefinedText.setLayoutData(data);
	}

	/**
	 * Return the input to the dialog.
	 */
	private IFileEditorMapping[] getInput() {

		//Filter the mappings to be just those with a wildcard extension
		if (fCurrentInput == null) {
			List wildcardEditors= new ArrayList();
			IFileEditorMapping[] allMappings =
				PlatformUI.getWorkbench().getEditorRegistry().getFileEditorMappings();
			for (int i= 0; i < allMappings.length; i++) {
				if (allMappings[i].getName().equals("*")) //$NON-NLS-1$
					wildcardEditors.add(allMappings[i]);
			}
			fCurrentInput= new IFileEditorMapping[wildcardEditors.size()];
			wildcardEditors.toArray(fCurrentInput);
		}

		return fCurrentInput;
	}

	/**
	 * Initializes this dialog's viewer after it has been laid out.
	 */
	private void initializeViewer() {
		fListViewer.setInput(getInput());
	}

	/**
	 * The <code>ListSelectionDialog</code> implementation of this 
	 * <code>Dialog</code> method builds a list of the selected elements for later
	 * retrieval by the client and closes this dialog.
	 */
	protected void okPressed() {

		// Get the input children.
		IFileEditorMapping[] children= getInput();

		List list= new ArrayList();

		// Build a list of selected children.
		for (int i= 0; i < children.length; ++i) {
			IFileEditorMapping element= children[i];
			if (fListViewer.getChecked(element))
				list.add(element.getLabel());
		}

		addUserDefinedEntries(list);
		setResult(list);
		super.okPressed();
	}

	protected CheckboxTableViewer getListViewer() {
		return fListViewer;
	}
}
