/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.dialogs;


import java.util.*;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.model.*;

import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.registry.*;

/**
 * Dialog to allow the user to select a cheat sheet from a list.
 */
public class CheatSheetCategoryBasedSelectionDialog extends SelectionDialog implements ISelectionChangedListener {
	private IDialogSettings settings;
	private CheatSheetCollectionElement cheatsheetCategories;
	private CheatSheetElement currentSelection;
	private TreeViewer categoryTreeViewer;
	private TableViewer cheatsheetSelectionViewer;
	private final static int SIZING_CATEGORY_LIST_HEIGHT = 150;
	private final static int SIZING_CATEGORY_LIST_WIDTH = 180;
	private final static int SIZING_CHEATSHEET_LIST_HEIGHT = 150;
	private final static int SIZING_CHEATSHEET_LIST_WIDTH = 350;
	private boolean okButtonState;

	// id constants
	private final static String STORE_SELECTED_CATEGORY_ID = 
		"CheatSheetCategoryBasedSelectionDialog.STORE_SELECTED_CATEGORY_ID"; //$NON-NLS-1$
	private final static String STORE_EXPANDED_CATEGORIES_ID = 
		"CheatSheetCategoryBasedSelectionDialog.STORE_EXPANDED_CATEGORIES_ID"; //$NON-NLS-1$
	private final static String STORE_SELECTED_CHEATSHEET_ID = 
		"CheatSheetCategoryBasedSelectionDialog.STORE_SELECTED_CHEATSHEET_ID"; //$NON-NLS-1$

	/**
	 * Creates an instance of this dialog to display
	 * the a list of cheat sheets.
	 * 
	 * @param shell the parent shell
	 */
	public CheatSheetCategoryBasedSelectionDialog(Shell shell, CheatSheetCollectionElement cheatsheetCategories) {
		super(shell);

		this.cheatsheetCategories = cheatsheetCategories;

		setTitle(CheatSheetPlugin.getResourceString(ICheatSheetResource.CHEAT_SHEET_SELECTION_DIALOG_TITLE));
		setMessage(CheatSheetPlugin.getResourceString(ICheatSheetResource.CHEAT_SHEET_SELECTION_DIALOG_MSG));
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
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
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);

		enableOKButton(okButtonState);
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		IDialogSettings workbenchSettings = CheatSheetPlugin.getPlugin().getDialogSettings();
		IDialogSettings dialogSettings = workbenchSettings.getSection("CheatSheetCategoryBasedSelectionDialog");//$NON-NLS-1$
		if(dialogSettings==null)
			dialogSettings = workbenchSettings.addNewSection("CheatSheetCategoryBasedSelectionDialog");//$NON-NLS-1$

		setDialogSettings(dialogSettings);

		// top level group
		Composite outerContainer = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		outerContainer.setLayout(layout);
		outerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create label
		createMessageArea(outerContainer);
					
		// category tree pane...create SWT tree directly to
		// get single selection mode instead of multi selection.
		Tree tree = new Tree(outerContainer, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		categoryTreeViewer = new TreeViewer(tree);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_CATEGORY_LIST_WIDTH;
		data.heightHint = SIZING_CATEGORY_LIST_HEIGHT;
		categoryTreeViewer.getTree().setLayoutData(data);
		categoryTreeViewer.setContentProvider(new BaseWorkbenchContentProvider());
		categoryTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
		categoryTreeViewer.setSorter(CheatSheetCollectionSorter.INSTANCE);
		categoryTreeViewer.addSelectionChangedListener(this);
		categoryTreeViewer.setInput(cheatsheetCategories);
	
		// cheatsheet actions pane...create SWT table directly to
		// get single selection mode instead of multi selection.
		Table table = new Table(outerContainer, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		cheatsheetSelectionViewer = new TableViewer(table);
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_CHEATSHEET_LIST_WIDTH;
		data.heightHint = SIZING_CHEATSHEET_LIST_HEIGHT;
		cheatsheetSelectionViewer.getTable().setLayoutData(data);
		cheatsheetSelectionViewer.setContentProvider(getCheatSheetProvider());
		cheatsheetSelectionViewer.setLabelProvider(new WorkbenchLabelProvider());
		cheatsheetSelectionViewer.addSelectionChangedListener(this);

		// Add double-click listener
		cheatsheetSelectionViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
	
		restoreWidgetValues();

		if (!categoryTreeViewer.getSelection().isEmpty())
			// we only set focus if a selection was restored
			categoryTreeViewer.getTree().setFocus();

		Dialog.applyDialogFont(outerContainer);
		return outerContainer;
	}

	/**
	 * @see org.eclipse.ui.dialogs.SelectionDialog#createMessageArea(Composite)
	 */
	protected Label createMessageArea(Composite composite) {
		Label label = new Label(composite,SWT.NONE);
		label.setText(getMessage()); 

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);

		return label;
	}

	/**
	 * Method enableOKButton enables/diables the OK button for the dialog
	 * and saves the state, allowing the enabling/disabling to occur even if
	 * the button has not been created yet.
	 * 
	 * @param value
	 */
	private void enableOKButton(boolean value) {
		Button button = getButton(IDialogConstants.OK_ID);
		
		okButtonState = value;
		if( button != null ) {
			button.setEnabled(value);
		}
	}

	/**
	 * Expands the cheatsheet categories in this page's category viewer that were
	 * expanded last time this page was used.  If a category that was previously
	 * expanded no longer exists then it is ignored.
	 */
	protected void expandPreviouslyExpandedCategories() {
		String[] expandedCategoryPaths = settings.getArray(STORE_EXPANDED_CATEGORIES_ID);
		List categoriesToExpand = new ArrayList(expandedCategoryPaths.length);
	
		for (int i = 0; i < expandedCategoryPaths.length; i++){
			CheatSheetCollectionElement category =
				cheatsheetCategories.findChildCollection(
					new Path(expandedCategoryPaths[i]));
			if (category != null)	// ie.- it still exists
				categoriesToExpand.add(category);
		}
	
		if (!categoriesToExpand.isEmpty())
			categoryTreeViewer.setExpandedElements(categoriesToExpand.toArray());
	}

	/**
	 * Returns the content provider for this page.
	 */
	protected IContentProvider getCheatSheetProvider() {
		//want to get the cheatsheets of the collection element
		return new BaseWorkbenchContentProvider() {
			public Object[] getChildren(Object o) {
				if (o instanceof CheatSheetCollectionElement) {
					return ((CheatSheetCollectionElement)o).getCheatSheets();
				}
				return new Object[0];
			}
		};
	}

	/**
	 * Returns the single selected object contained in the passed selectionEvent,
	 * or <code>null</code> if the selectionEvent contains either 0 or 2+ selected
	 * objects.
	 */
	protected Object getSingleSelection(IStructuredSelection selection) {
		return selection.size() == 1 ? selection.getFirstElement() : null;
	}

	/**
	 *	Handle the (de)selection of cheatsheet element(s)
	 *
	 * @param selectionEvent SelectionChangedEvent
	 */
	private void handleCategorySelection(SelectionChangedEvent selectionEvent) {
		Object currentSelection = cheatsheetSelectionViewer.getInput();
		Object selectedCategory =
			getSingleSelection((IStructuredSelection)selectionEvent.getSelection());
		if (currentSelection != selectedCategory) {
			cheatsheetSelectionViewer.setInput(selectedCategory);

			enableOKButton(false);
		}
	}

	/**
	 *	Handle the (de)selection of cheatsheet element(s)
	 *
	 *	@param selectionEvent SelectionChangedEvent
	 */
	private void handleCheatSheetSelection(SelectionChangedEvent selectionEvent) {
		currentSelection = (CheatSheetElement)getSingleSelection((IStructuredSelection)selectionEvent.getSelection());

		if( currentSelection != null ) {
			enableOKButton(true);
		}
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void okPressed() {
		if( currentSelection != null ) {
			ArrayList result = new ArrayList(1);
			result.add(currentSelection);
			setResult(result);
		} else {
			return;
		}

		//save our selection state
		saveWidgetValues();

		super.okPressed();
	}

	/**
	 *	Set self's widgets to the values that they held last time this page was open
	 *
	 */
	protected void restoreWidgetValues() {
		String[] expandedCategoryPaths = settings.getArray(STORE_EXPANDED_CATEGORIES_ID);
		if (expandedCategoryPaths == null)
			return;				// no stored values

		expandPreviouslyExpandedCategories();
		selectPreviouslySelectedCategoryAndCheatSheet();
	}

	/**
	 *	Store the current values of self's widgets so that they can
	 *	be restored in the next instance of self
	 *
	 */
	public void saveWidgetValues() {
		storeExpandedCategories();
		storeSelectedCategoryAndCheatSheet();
	}

	/**
	 *	The user selected either new cheatsheet category(s) or cheatsheet element(s).
	 *	Proceed accordingly.
	 *
	 *	@param newSelection ISelection
	 */
	public void selectionChanged(SelectionChangedEvent selectionEvent) {
		if (selectionEvent.getSelectionProvider().equals(categoryTreeViewer))
			handleCategorySelection(selectionEvent);
		else
			handleCheatSheetSelection(selectionEvent);
	}

	/**
	 * Selects the cheatsheet category and cheatsheet in this page that were selected
	 * last time this page was used.  If a category or cheatsheet that was previously
	 * selected no longer exists then it is ignored.
	 */
	protected void selectPreviouslySelectedCategoryAndCheatSheet() {
		String categoryId = settings.get(STORE_SELECTED_CATEGORY_ID);
		if (categoryId == null)
			return;
		CheatSheetCollectionElement category =
			cheatsheetCategories.findChildCollection(new Path(categoryId));
		if (category == null)
			return;				// category no longer exists, or has moved
		
		StructuredSelection selection = new StructuredSelection(category);
		categoryTreeViewer.setSelection(selection);
		selectionChanged(new SelectionChangedEvent(categoryTreeViewer,selection));
	
		String cheatsheetId = settings.get(STORE_SELECTED_CHEATSHEET_ID);
		if (cheatsheetId == null)
			return;
		CheatSheetElement cheatsheet = category.findCheatSheet(cheatsheetId,false);
		if (cheatsheet == null)
			return;				// cheatsheet no longer exists, or has moved
	
		selection = new StructuredSelection(cheatsheet);
		cheatsheetSelectionViewer.setSelection(selection);
		selectionChanged(new SelectionChangedEvent(cheatsheetSelectionViewer,selection));
	}

	/**
	 *	Set the dialog store to use for widget value storage and retrieval
	 *
	 *	@param settings IDialogSettings
	 */
	public void setDialogSettings(IDialogSettings settings) {
		this.settings = settings;
	}

	/**
	 * Stores the collection of currently-expanded categories in this page's dialog store,
	 * in order to recreate this page's state in the next instance of this page.
	 */
	protected void storeExpandedCategories() {
		Object[] expandedElements = categoryTreeViewer.getExpandedElements();
		String[] expandedElementPaths = new String[expandedElements.length];
		for (int i = 0; i < expandedElements.length; ++i) {
			expandedElementPaths[i] =
				((CheatSheetCollectionElement)expandedElements[i]).getPath().toString();
		}
		settings.put(
			STORE_EXPANDED_CATEGORIES_ID,
			expandedElementPaths);
	}

	/**
	 * Stores the currently-selected category and cheatsheet in this page's dialog store,
	 * in order to recreate this page's state in the next instance of this page.
	 */
	protected void storeSelectedCategoryAndCheatSheet() {
		CheatSheetCollectionElement selectedCategory = (CheatSheetCollectionElement)
			getSingleSelection((IStructuredSelection)categoryTreeViewer.getSelection());
	
		if (selectedCategory != null) {
			settings.put(
				STORE_SELECTED_CATEGORY_ID,
				selectedCategory.getPath().toString());
		}
	
		CheatSheetElement selectedCheatSheet = (CheatSheetElement)
			getSingleSelection((IStructuredSelection)cheatsheetSelectionViewer.getSelection());
	
		if (selectedCheatSheet != null) {
			settings.put(
				STORE_SELECTED_CHEATSHEET_ID,
				selectedCheatSheet.getID());
		}
	}
}
