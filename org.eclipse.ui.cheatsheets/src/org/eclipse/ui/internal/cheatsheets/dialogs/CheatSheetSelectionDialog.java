/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.dialogs;


import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;

import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.registry.*;

/**
 * Dialog to allow the user to select a cheat sheet from a list.
 */
public class CheatSheetSelectionDialog extends SelectionDialog {
	/**
	 * The SimpleListContentProvider is a class designed to return a static list of items
	 * when queried for use in simple list dialogs.
	 */
	public class SimpleListContentProvider implements IStructuredContentProvider{

		//The elements to display
		private Object[] elements;

		/**
		 * SimpleListContentProvider constructor comment.
		 */
		public SimpleListContentProvider() {
			super();
		}
		/**
		 * Do nothing when disposing,
		 */
		public void dispose() {}
		/**
		 * Returns the elements to display in the viewer. The inputElement is ignored for this
		 * provider.
		 */
		public Object[] getElements(Object inputElement) {
			return this.elements;
		}
		/**
		 * Required method from IStructuredContentProvider. The input is assumed to not change 
		 * for the SimpleListContentViewer so do nothing here.
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		/**
		 * Set the elements to display.
		 * @param items Object[]
		 */
		public void setElements(Object[] items) {
	
			this.elements = items;
		}
	}

	/**
	 * List width in characters.
	 */
	private final static int LIST_WIDTH = 60;
	/**
	 * List height in characters.
	 */
	private final static int LIST_HEIGHT = 10;
	/**
	 * List to display the resolutions.
	 */
	private ListViewer listViewer;

	/**
	 * Creates an instance of this dialog to display
	 * the a list of cheat sheets.
	 * 
	 * @param shell the parent shell
	 */
	public CheatSheetSelectionDialog(Shell shell) {
		super(shell);

		setTitle(Messages.CHEAT_SHEET_SELECTION_DIALOG_TITLE);
		setMessage(Messages.CHEAT_SHEET_SELECTION_DIALOG_MSG);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	private void addCheatSheets(ArrayList list, CheatSheetCollectionElement cheatSheetsCollection) {
		Object[] cheatSheets = cheatSheetsCollection.getCheatSheets();
		for (int i = 0; i < cheatSheets.length; i++) {
			if (!list.contains(cheatSheets[i])) {
				list.add(cheatSheets[i]);
			}
		}

		Object[] cheatSheetsFromCollection = cheatSheetsCollection.getChildren();
		for (int nX = 0; nX < cheatSheetsFromCollection.length; nX++) {
			CheatSheetCollectionElement collection = (CheatSheetCollectionElement) cheatSheetsFromCollection[nX];
			addCheatSheets(list, collection);
		}
	}

	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		//  WorkbenchHelp.setHelp(newShell, IHelpContextIds.WELCOME_PAGE_SELECTION_DIALOG);
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		ArrayList list = new ArrayList(10);
		CheatSheetCollectionElement cheatSheetsCollection = (CheatSheetCollectionElement)CheatSheetRegistryReader.getInstance().getCheatSheets();
		addCheatSheets(list, cheatSheetsCollection);

		Composite composite = (Composite) super.createDialogArea(parent);

		// Create label
		createMessageArea(composite);
		// Create list viewer 
		listViewer = new ListViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = convertHeightInCharsToPixels(LIST_HEIGHT);
		data.widthHint = convertWidthInCharsToPixels(LIST_WIDTH);
		listViewer.getList().setLayoutData(data);
		// Set the label provider  
		listViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				// Return the features's label.
				return element == null ? ICheatSheetResource.EMPTY_STRING : ((CheatSheetElement) element).getLabel(null);
			}
		});

		// Set the content provider
		SimpleListContentProvider cp = new SimpleListContentProvider();
		cp.setElements(list.toArray());
		listViewer.setContentProvider(cp);
		listViewer.setInput(new Object()); // it is ignored but must be non-null

		// Set the initial selection
		if (getInitialElementSelections() != null)
			listViewer.setSelection(new StructuredSelection(getInitialElementSelections()), true);

		// Add a selection change listener
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				// Update OK button enablement
				getOkButton().setEnabled(!event.getSelection().isEmpty());
			}
		});

		// Add double-click listener
		listViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		
		Dialog.applyDialogFont(composite);
		return composite;
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
		setResult(selection.toList());

		super.okPressed();
	}
}
