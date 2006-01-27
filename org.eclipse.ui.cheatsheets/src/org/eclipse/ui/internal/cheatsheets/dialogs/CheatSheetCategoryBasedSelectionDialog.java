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
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.ICheatSheetResource;
import org.eclipse.ui.internal.cheatsheets.Messages;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetCollectionElement;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetCollectionSorter;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetElement;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * Dialog to allow the user to select a cheat sheet from a list.
 */
public class CheatSheetCategoryBasedSelectionDialog extends SelectionDialog
		implements ISelectionChangedListener {
	private IDialogSettings settings;

	private CheatSheetCollectionElement cheatsheetCategories;

	private CheatSheetElement currentSelection;

	private TreeViewer treeViewer;

	private Text desc;

	private Button showAllButton;
	
	private Button selectRegisteredRadio;
	
	private Button selectFileRadio;
	
	private Text selectFileText;

	private ActivityViewerFilter activityViewerFilter = new ActivityViewerFilter();

	private boolean okButtonState;

	// id constants

	private final static String STORE_EXPANDED_CATEGORIES_ID = "CheatSheetCategoryBasedSelectionDialog.STORE_EXPANDED_CATEGORIES_ID"; //$NON-NLS-1$

	private final static String STORE_SELECTED_CHEATSHEET_ID = "CheatSheetCategoryBasedSelectionDialog.STORE_SELECTED_CHEATSHEET_ID"; //$NON-NLS-1$

	private Button browseFileButton;

	private static class ActivityViewerFilter extends ViewerFilter {
		private boolean hasEncounteredFilteredItem = false;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			if (WorkbenchActivityHelper.filterItem(element)) {
				setHasEncounteredFilteredItem(true);
				return false;
			}
			return true;
		}

		/**
		 * @return returns whether the filter has filtered an item
		 */
		public boolean getHasEncounteredFilteredItem() {
			return hasEncounteredFilteredItem;
		}

		/**
		 * @param sets
		 *            whether the filter has filtered an item
		 */
		public void setHasEncounteredFilteredItem(
				boolean hasEncounteredFilteredItem) {
			this.hasEncounteredFilteredItem = hasEncounteredFilteredItem;
		}
	}

	private class CheatsheetLabelProvider extends LabelProvider {
		public String getText(Object obj) {
			if (obj instanceof WorkbenchAdapter) {
				return ((WorkbenchAdapter) obj).getLabel(null);
			}
			return super.getText(obj);
		}

		public Image getImage(Object obj) {
			if (obj instanceof CheatSheetElement) {
				CheatSheetElement element = (CheatSheetElement)obj;
				if (element.isComposite()) {
					return CheatSheetPlugin.getPlugin().getImageRegistry().get(
							ICheatSheetResource.COMPOSITE_OBJ);
				} else {
					return CheatSheetPlugin.getPlugin().getImageRegistry().get(
							ICheatSheetResource.CHEATSHEET_OBJ);
			    }
			}
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_FOLDER);
		}
	}

	/**
	 * Creates an instance of this dialog to display the a list of cheat sheets.
	 * 
	 * @param shell
	 *            the parent shell
	 */
	public CheatSheetCategoryBasedSelectionDialog(Shell shell,
			CheatSheetCollectionElement cheatsheetCategories) {
		super(shell);

		this.cheatsheetCategories = cheatsheetCategories;

		setTitle(Messages.CHEAT_SHEET_SELECTION_DIALOG_TITLE);
		setMessage(Messages.CHEAT_SHEET_SELECTION_DIALOG_MSG);

		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/*
	 * (non-Javadoc) Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		//TODO need to add help context id
		// WorkbenchHelp.setHelp(newShell,
		// IHelpContextIds.WELCOME_PAGE_SELECTION_DIALOG);
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);

		enableOKButton(okButtonState);
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		IDialogSettings workbenchSettings = CheatSheetPlugin.getPlugin()
				.getDialogSettings();
		IDialogSettings dialogSettings = workbenchSettings
				.getSection("CheatSheetCategoryBasedSelectionDialog");//$NON-NLS-1$
		if (dialogSettings == null)
			dialogSettings = workbenchSettings
					.addNewSection("CheatSheetCategoryBasedSelectionDialog");//$NON-NLS-1$

		setDialogSettings(dialogSettings);

		// top level group
		Composite outerContainer = (Composite) super.createDialogArea(parent);

		GridLayout gridLayout = new GridLayout();
		outerContainer.setLayout(gridLayout);
		outerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create label
		createMessageArea(outerContainer);
        
		// Create radio button
		selectRegisteredRadio = new Button(outerContainer, SWT.RADIO);
		selectRegisteredRadio.setText(Messages.SELECTION_DIALOG_OPEN_REGISTERED);
		
		// category tree pane...create SWT tree directly to
		// get single selection mode instead of multi selection.
		Tree tree = new Tree(outerContainer, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		treeViewer = new TreeViewer(tree);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 300;
		data.heightHint = 300;
		treeViewer.getTree().setLayoutData(data);
		treeViewer.setContentProvider(getCheatSheetProvider());
		treeViewer.setLabelProvider(new CheatsheetLabelProvider());
		treeViewer.setSorter(CheatSheetCollectionSorter.INSTANCE);
		treeViewer.addFilter(activityViewerFilter);
		treeViewer.addSelectionChangedListener(this);
		treeViewer.setInput(cheatsheetCategories);

		desc = new Text(outerContainer, SWT.MULTI | SWT.WRAP);
		desc.setEditable(false);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 100;
		data.heightHint = 48;
		desc.setLayoutData(data);

		if (activityViewerFilter.getHasEncounteredFilteredItem())
			createShowAllButton(outerContainer);

		// Add double-click listener
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				Object obj = selection.getFirstElement();
				if (obj instanceof CheatSheetCollectionElement) {
					boolean state = treeViewer.getExpandedState(obj);
					treeViewer.setExpandedState(obj, !state);
				}
				else {
					okPressed();
				}
			}
		});

		
        // Create radio button
		selectFileRadio = new Button(outerContainer, SWT.RADIO);
		selectFileRadio.setText(Messages.SELECTION_DIALOG_OPEN_FROM_FILE);
		
		Composite selectFileComposite = new Composite(outerContainer, SWT.NULL);
		GridLayout selectFileLayout = new GridLayout();
		selectFileLayout.numColumns = 2;
		selectFileComposite.setLayout(selectFileLayout);
		GridData sfCompositeData = new GridData(GridData.FILL_HORIZONTAL);
		selectFileComposite.setLayoutData(sfCompositeData);
		selectFileText = new Text(selectFileComposite, SWT.BORDER);
		GridData sfTextData = new GridData(GridData.FILL_HORIZONTAL);
		selectFileText.setLayoutData(sfTextData);
		browseFileButton = new Button(selectFileComposite, SWT.NULL);
		browseFileButton.setText(Messages.SELECTION_DIALOG_FILEPICKER_BROWSE);
		selectRegisteredRadio.setSelection(true);
		
		restoreWidgetValues();

		if (!treeViewer.getSelection().isEmpty())
			// we only set focus if a selection was restored
			treeViewer.getTree().setFocus();

		Dialog.applyDialogFont(outerContainer);
		selectFileText.addModifyListener(new FileTextListener());
		browseFileButton.addSelectionListener(new BrowseListener());
		selectRegisteredRadio.addSelectionListener(new RadioSelectionListener());
		checkRadioButtons();
		return outerContainer;
	}
	
	private class RadioSelectionListener implements SelectionListener {

		public void widgetSelected(SelectionEvent e) {
			checkRadioButtons();			
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			// do nothing
		}
	}
	
	private class BrowseListener implements SelectionListener {

		public void widgetSelected(SelectionEvent e) {
			// Launch a file dialog to select a cheatsheet file
			FileDialog fileDlg = new FileDialog(PlatformUI.getWorkbench()
	                .getActiveWorkbenchWindow().getShell());
			fileDlg.setFilterExtensions(new String[]{"*.xml"}); //$NON-NLS-1$
			fileDlg.setText(Messages.SELECTION_DIALOG_FILEPICKER_TITLE);
			fileDlg.open();
			String filename = fileDlg.getFileName();
			if (filename != null) {				
				IPath folderPath = new Path(fileDlg.getFilterPath());
				IPath filePath = folderPath.append(filename);
				selectFileText.setText(filePath.toOSString());
				checkRadioButtons();
			}
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			// do nothing			
		}
		
	}
	
	private class FileTextListener implements ModifyListener {

		public void modifyText(ModifyEvent e) {
			setOkButton();
		}	
	}

	/*
	 * Check the state of the Radio buttons and disable those parts of teh UI that don't apply
	 */
	private void checkRadioButtons() {
		selectFileText.setEnabled(selectFileRadio.getSelection());	
		browseFileButton.setEnabled(selectFileRadio.getSelection());	
		treeViewer.getTree().setEnabled(selectRegisteredRadio.getSelection());
		setOkButton();
	}

	/**
	 * Create a show all button in the parent.
	 * 
	 * @param parent
	 *            the parent <code>Composite</code>.
	 */
	private void createShowAllButton(Composite parent) {
		showAllButton = new Button(parent, SWT.CHECK);
		showAllButton
				.setText(Messages.CheatSheetCategoryBasedSelectionDialog_showAll);
		showAllButton.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				if (showAllButton.getSelection()) {
					treeViewer.resetFilters();
				} else {
					treeViewer.addFilter(activityViewerFilter);
				}
			}
		});
	}

	/**
	 * Method enableOKButton enables/diables the OK button for the dialog and
	 * saves the state, allowing the enabling/disabling to occur even if the
	 * button has not been created yet.
	 * 
	 * @param value
	 */
	private void enableOKButton(boolean value) {
		Button button = getButton(IDialogConstants.OK_ID);

		okButtonState = value;
		if (button != null) {
			button.setEnabled(value);
		}
	}

	/**
	 * Expands the cheatsheet categories in this page's category viewer that
	 * were expanded last time this page was used. If a category that was
	 * previously expanded no longer exists then it is ignored.
	 */
	protected CheatSheetCollectionElement expandPreviouslyExpandedCategories() {
		String[] expandedCategoryPaths = settings
				.getArray(STORE_EXPANDED_CATEGORIES_ID);
		List categoriesToExpand = new ArrayList(expandedCategoryPaths.length);

		for (int i = 0; i < expandedCategoryPaths.length; i++) {
			CheatSheetCollectionElement category = cheatsheetCategories
					.findChildCollection(new Path(expandedCategoryPaths[i]));
			if (category != null) // ie.- it still exists
				categoriesToExpand.add(category);
		}

		if (!categoriesToExpand.isEmpty())
			treeViewer.setExpandedElements(categoriesToExpand.toArray());
		return categoriesToExpand.isEmpty() ? null
				: (CheatSheetCollectionElement) categoriesToExpand
						.get(categoriesToExpand.size() - 1);
	}

	/**
	 * Returns the content provider for this page.
	 */
	protected IContentProvider getCheatSheetProvider() {
		// want to get the cheatsheets of the collection element
		return new BaseWorkbenchContentProvider() {
			public Object[] getChildren(Object o) {
				if (o instanceof CheatSheetCollectionElement) {
					Object[] cheatsheets = ((CheatSheetCollectionElement) o)
							.getCheatSheets();
					if (cheatsheets.length > 0)
						return cheatsheets;
				}
				return super.getChildren(o);
			}
		};
	}

	/**
	 * Returns the single selected object contained in the passed
	 * selectionEvent, or <code>null</code> if the selectionEvent contains
	 * either 0 or 2+ selected objects.
	 */
	protected Object getSingleSelection(ISelection selection) {
		IStructuredSelection ssel = (IStructuredSelection) selection;
		return ssel.size() == 1 ? ssel.getFirstElement() : null;
	}

	/**
	 * The user selected either new cheatsheet category(s) or cheatsheet
	 * element(s). Proceed accordingly.
	 * 
	 * @param newSelection
	 *            ISelection
	 */
	public void selectionChanged(SelectionChangedEvent selectionEvent) {
		Object obj = getSingleSelection(selectionEvent.getSelection());
		if (obj instanceof CheatSheetCollectionElement) {
			currentSelection = null;
		} else {
			currentSelection = (CheatSheetElement) obj;
		}

		if (currentSelection != null) {
			desc.setText(currentSelection.getDescription());
		} else {
			desc.setText(""); //$NON-NLS-1$
		}
		setOkButton();
	}
	
	private void setOkButton() {
		if (selectRegisteredRadio.getSelection()) {
			enableOKButton(currentSelection != null);
		} else {
			enableOKButton(selectFileText.getText().length() > 0);
		}
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected void okPressed() {
		if (selectFileRadio.getSelection()) {
			setResultFromFile();
		} else {
			setResultFromTree();
		}

		// save our selection state
		saveWidgetValues();

		super.okPressed();
	}

	private void setResultFromTree() {
		if (currentSelection != null) {
			ArrayList result = new ArrayList(1);
			ITriggerPoint triggerPoint = PlatformUI.getWorkbench()
					.getActivitySupport().getTriggerPointManager()
					.getTriggerPoint(ICheatSheetResource.TRIGGER_POINT_ID);
			if (WorkbenchActivityHelper.allowUseOf(triggerPoint,
					currentSelection)) {
				result.add(currentSelection);
				setResult(result);
			}
		}
	}

	private void setResultFromFile() {
		// Use the filename without extension as the id of this cheatsheet
		IPath filePath = new Path(selectFileText.getText());
		String id = filePath.lastSegment();
		int extensionIndex = id.indexOf('.');
		if (extensionIndex > 0) {
		    id = id.substring(0, extensionIndex);
		}
		// Use the id as the name
		CheatSheetElement element = new CheatSheetElement(id);
		element.setID(id);
		element.setContentFile(selectFileText.getText());
		setSelectionResult(new Object[]{element});
	}

	/**
	 * Set self's widgets to the values that they held last time this page was
	 * open
	 * 
	 */
	protected void restoreWidgetValues() {
		String[] expandedCategoryPaths = settings
				.getArray(STORE_EXPANDED_CATEGORIES_ID);
		if (expandedCategoryPaths == null)
			return; // no stored values

		CheatSheetCollectionElement category = expandPreviouslyExpandedCategories();
		if (category != null)
			selectPreviouslySelectedCheatSheet(category);
	}

	/**
	 * Store the current values of self's widgets so that they can be restored
	 * in the next instance of self
	 * 
	 */
	public void saveWidgetValues() {
		storeExpandedCategories();
		storeSelectedCheatSheet();
	}

	/**
	 * Selects the cheatsheet category and cheatsheet in this page that were
	 * selected last time this page was used. If a category or cheatsheet that
	 * was previously selected no longer exists then it is ignored.
	 */
	protected void selectPreviouslySelectedCheatSheet(
			CheatSheetCollectionElement category) {
		String cheatsheetId = settings.get(STORE_SELECTED_CHEATSHEET_ID);
		if (cheatsheetId == null)
			return;
		CheatSheetElement cheatsheet = category.findCheatSheet(cheatsheetId,
				false);
		if (cheatsheet == null)
			return; // cheatsheet no longer exists, or has moved

		treeViewer.setSelection(new StructuredSelection(cheatsheet));
	}

	/**
	 * Set the dialog store to use for widget value storage and retrieval
	 * 
	 * @param settings
	 *            IDialogSettings
	 */
	public void setDialogSettings(IDialogSettings settings) {
		this.settings = settings;
	}

	/**
	 * Stores the collection of currently-expanded categories in this page's
	 * dialog store, in order to recreate this page's state in the next instance
	 * of this page.
	 */
	protected void storeExpandedCategories() {
		Object[] expandedElements = treeViewer.getExpandedElements();
		String[] expandedElementPaths = new String[expandedElements.length];
		for (int i = 0; i < expandedElements.length; ++i) {
			expandedElementPaths[i] = ((CheatSheetCollectionElement) expandedElements[i])
					.getPath().toString();
		}
		settings.put(STORE_EXPANDED_CATEGORIES_ID, expandedElementPaths);
	}

	/**
	 * Stores the currently-selected category and cheatsheet in this page's
	 * dialog store, in order to recreate this page's state in the next instance
	 * of this page.
	 */
	protected void storeSelectedCheatSheet() {
		CheatSheetElement element = null;

		Object el = getSingleSelection(treeViewer.getSelection());
		if (el == null)
			return;

		if (el instanceof CheatSheetElement) {
			element = (CheatSheetElement) el;
		} else
			return;

		settings.put(STORE_SELECTED_CHEATSHEET_ID, element.getID());
	}
}