/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.model.WorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 *New wizard selection tab that allows the user to select a registered
 *'New' wizard to be launched
 */
class NewWizardNewPage
	implements ISelectionChangedListener, IDoubleClickListener {
	private WizardCollectionElement wizardCategories;
	private IWorkbench workbench;
	private NewWizardSelectionPage page;
	private IDialogSettings settings;

	//Keep track of the wizards we have previously selected
	private Hashtable selectedWizards = new Hashtable();

	private TreeViewer categoryTreeViewer;
	private TableViewer wizardSelectionViewer;

	private final static int SIZING_LISTS_HEIGHT = 200;
	private final static int SIZING_LISTS_WIDTH = 150;

	// id constants
	private final static String STORE_SELECTED_CATEGORY_ID = "NewWizardSelectionPage.STORE_SELECTED_CATEGORY_ID"; //$NON-NLS-1$
	private final static String STORE_EXPANDED_CATEGORIES_ID = "NewWizardSelectionPage.STORE_EXPANDED_CATEGORIES_ID"; //$NON-NLS-1$
	private final static String STORE_SELECTED_WIZARD_ID = "NewWizardSelectionPage.STORE_SELECTED_WIZARD_ID"; //$NON-NLS-1$

	/**
	 *  Create an instance of this class
	 */
	public NewWizardNewPage(
		NewWizardSelectionPage mainPage,
		IWorkbench aWorkbench,
		WizardCollectionElement wizardCategories) {
		this.page = mainPage;
		this.workbench = aWorkbench;
		this.wizardCategories = wizardCategories;
	}
	public void activate() {
		page.setDescription(WorkbenchMessages.getString("NewWizardNewPage.description")); //$NON-NLS-1$

		selectionChanged(
			new SelectionChangedEvent(
				wizardSelectionViewer,
				(IStructuredSelection) wizardSelectionViewer.getSelection()));

	}
	/**
	 *	Create this tab's visual components
	 *
	 *	@return org.eclipse.swt.widgets.Control
	 *	@param parent org.eclipse.swt.widgets.Composite
	 */
	protected Control createControl(Composite parent) {

		Font wizardFont = parent.getFont();
		// top level group
		Composite outerContainer = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		outerContainer.setLayout(layout);
		outerContainer.setFont(wizardFont);
		outerContainer.setLayoutData(
			new GridData(
				GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

		// category tree pane...create SWT tree directly to
		// get single selection mode instead of multi selection.
		Tree tree =
			new Tree(
				outerContainer,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		categoryTreeViewer = new TreeViewer(tree);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_LISTS_WIDTH;

		boolean needsHint = DialogUtil.inRegularFontMode(parent);

		//Only give a height hint if the dialog is going to be too small
		if (needsHint) {
			data.heightHint = SIZING_LISTS_HEIGHT;
		}

		categoryTreeViewer.getTree().setLayoutData(data);
		categoryTreeViewer.setContentProvider(new WorkbenchContentProvider());
		categoryTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
		categoryTreeViewer.setSorter(NewWizardCollectionSorter.INSTANCE);
		categoryTreeViewer.addSelectionChangedListener(this);
		if (wizardCategories.getParent(wizardCategories) == null) {
			categoryTreeViewer.setInput(wizardCategories);
		} else
			categoryTreeViewer.setInput(new RootElementProxy(wizardCategories));
		tree.setFont(wizardFont);

		// wizard actions pane...create SWT table directly to
		// get single selection mode instead of multi selection.
		Table table =
			new Table(
				outerContainer,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		wizardSelectionViewer = new TableViewer(table);
		data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_LISTS_WIDTH;
		//Only give a height hint if the dialog is going to be too small
		if (needsHint) {
			data.heightHint = SIZING_LISTS_HEIGHT;
		}

		wizardSelectionViewer.getTable().setLayoutData(data);
		wizardSelectionViewer.setContentProvider(getWizardProvider());
		wizardSelectionViewer.setLabelProvider(new WorkbenchLabelProvider());
		wizardSelectionViewer.addSelectionChangedListener(this);
		wizardSelectionViewer.addDoubleClickListener(this);
		table.setFont(wizardFont);

		restoreWidgetValues();
		if (!categoryTreeViewer.getSelection().isEmpty())
			// we only set focus if a selection was restored
			categoryTreeViewer.getTree().setFocus();
		return outerContainer;
	}
	/**
	 *	Create a viewer pane in this group for the passed viewer.
	 *
	 *	@param parent org.eclipse.swt.widgets.Composite
	 *	@param width int
	 *	@param height int
	 */
	protected Composite createViewPane(
		Composite parent,
		int width,
		int height) {
		Composite paneWindow = new Composite(parent, SWT.BORDER);
		GridData spec = new GridData(GridData.FILL_BOTH);
		spec.widthHint = width;
		spec.heightHint = height;
		paneWindow.setLayoutData(spec);
		paneWindow.setLayout(new FillLayout());
		return paneWindow;
	}
	/**
	 * A wizard in the wizard viewer has been double clicked.
	 * Treat it as a selection.
	 */
	public void doubleClick(DoubleClickEvent event) {
		selectionChanged(
			new SelectionChangedEvent(
				wizardSelectionViewer,
				wizardSelectionViewer.getSelection()));
		page.advanceToNextPage();
	}
	/**
	 * Expands the wizard categories in this page's category viewer that were
	 * expanded last time this page was used.  If a category that was previously
	 * expanded no longer exists then it is ignored.
	 */
	protected void expandPreviouslyExpandedCategories() {
		String[] expandedCategoryPaths =
			settings.getArray(STORE_EXPANDED_CATEGORIES_ID);
		List categoriesToExpand = new ArrayList(expandedCategoryPaths.length);

		for (int i = 0; i < expandedCategoryPaths.length; i++) {
			WizardCollectionElement category =
				wizardCategories.findChildCollection(
					new Path(expandedCategoryPaths[i]));
			if (category != null) // ie.- it still exists
				categoriesToExpand.add(category);
		}

		if (!categoriesToExpand.isEmpty())
			categoryTreeViewer.setExpandedElements(
				categoriesToExpand.toArray());
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
	 * Returns the content provider for this page.
	 */
	protected IContentProvider getWizardProvider() {
		//want to get the wizards of the collection element
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object o) {
				if (o instanceof WizardCollectionElement) {
					return ((WizardCollectionElement) o).getWizards();
				}
				return new Object[0];
			}
		};
	}
	/**
	 *	Handle the (de)selection of wizard element(s)
	 *
	 * @param selectionEvent SelectionChangedEvent
	 */
	private void handleCategorySelection(SelectionChangedEvent selectionEvent) {
		page.setErrorMessage(null);
		page.setMessage(null);

		Object currentSelection = wizardSelectionViewer.getInput();
		Object selectedCategory =
			getSingleSelection(
				(IStructuredSelection) selectionEvent.getSelection());
		if (currentSelection != selectedCategory) {
			page.selectWizardNode(null);
			wizardSelectionViewer.setInput(selectedCategory);
			if (selectedCategory instanceof WizardCollectionElement) {
				Object[] children =
					((WizardCollectionElement) selectedCategory).getWizards();
				if (children.length == 1)
					selectWizard(children[0]);
			}
		}
	}
	/**
	 *	Handle the (de)selection of wizard element(s)
	 *
	 *	@param selectionEvent SelectionChangedEvent
	 */
	private void handleWizardSelection(SelectionChangedEvent selectionEvent) {
		page.setErrorMessage(null);

		WorkbenchWizardElement currentSelection =
			(WorkbenchWizardElement) getSingleSelection(
				(IStructuredSelection) selectionEvent
				.getSelection());

		// If no single selection, clear and return
		if (currentSelection == null) {
			page.setMessage(null);
			page.selectWizardNode(null);
			return;
		}

		WorkbenchWizardNode selectedNode;

		if (selectedWizards.containsKey(currentSelection)) {
			selectedNode =
				(WorkbenchWizardNode) selectedWizards.get(currentSelection);
		} else {
			selectedNode = new WorkbenchWizardNode(page, currentSelection) {
				public IWorkbenchWizard createWizard() throws CoreException {
					return (INewWizard) wizardElement
						.createExecutableExtension();
				}
			};
			selectedWizards.put(currentSelection, selectedNode);
		}

		page.selectWizardNode(selectedNode);

		page.setMessage((String) currentSelection.getDescription());
	}
	/**
	 *	Set self's widgets to the values that they held last time this page was open
	 *
	 */
	protected void restoreWidgetValues() {
		String[] expandedCategoryPaths =
			settings.getArray(STORE_EXPANDED_CATEGORIES_ID);
		if (expandedCategoryPaths == null)
			return; // no stored values

		expandPreviouslyExpandedCategories();
		selectPreviouslySelectedCategoryAndWizard();
	}
	/**
	 *	Store the current values of self's widgets so that they can
	 *	be restored in the next instance of self
	 *
	 */
	public void saveWidgetValues() {
		storeExpandedCategories();
		storeSelectedCategoryAndWizard();
	}
	/**
	 *	The user selected either new wizard category(s) or wizard element(s).
	 *	Proceed accordingly.
	 *
	 *	@param newSelection ISelection
	 */
	public void selectionChanged(SelectionChangedEvent selectionEvent) {
		if (selectionEvent.getSelectionProvider().equals(categoryTreeViewer))
			handleCategorySelection(selectionEvent);
		else
			handleWizardSelection(selectionEvent);
	}
	/**
	 * Selects the wizard category and wizard in this page that were selected
	 * last time this page was used.  If a category or wizard that was previously
	 * selected no longer exists then it is ignored.
	 */
	protected void selectPreviouslySelectedCategoryAndWizard() {
		String categoryId = (String) settings.get(STORE_SELECTED_CATEGORY_ID);
		if (categoryId == null)
			return;
		WizardCollectionElement category =
			wizardCategories.findChildCollection(new Path(categoryId));
		if (category == null)
			return; // category no longer exists, or has moved

		StructuredSelection selection = new StructuredSelection(category);
		categoryTreeViewer.setSelection(selection);
		selectionChanged(
			new SelectionChangedEvent(categoryTreeViewer, selection));

		String wizardId = (String) settings.get(STORE_SELECTED_WIZARD_ID);
		if (wizardId == null)
			return;
		WorkbenchWizardElement wizard = category.findWizard(wizardId, false);
		if (wizard == null)
			return; // wizard no longer exists, or has moved

		selectWizard(wizard);
	}
	/**
	 * Select the supplied wizard element.
	 * @param wizard. Defined to be Object but really a 
	 * <code>WorkbenchWizardElement</code>.If it is not 
	 * in the list nothing will happen.
	 */
	private void selectWizard(Object wizard) {
		StructuredSelection selection;
		selection = new StructuredSelection(wizard);
		wizardSelectionViewer.setSelection(selection);
		selectionChanged(
			new SelectionChangedEvent(wizardSelectionViewer, selection));
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
				((WizardCollectionElement) expandedElements[i])
					.getPath()
					.toString();
		}
		settings.put(STORE_EXPANDED_CATEGORIES_ID, expandedElementPaths);
	}
	/**
	 * Stores the currently-selected category and wizard in this page's dialog store,
	 * in order to recreate this page's state in the next instance of this page.
	 */
	protected void storeSelectedCategoryAndWizard() {
		WizardCollectionElement selectedCategory =
			(WizardCollectionElement) getSingleSelection(
				(IStructuredSelection) categoryTreeViewer
				.getSelection());

		if (selectedCategory != null) {
			settings.put(
				STORE_SELECTED_CATEGORY_ID,
				selectedCategory.getPath().toString());
		}

		WorkbenchWizardElement selectedWizard =
			(WorkbenchWizardElement) getSingleSelection(
				(IStructuredSelection) wizardSelectionViewer
				.getSelection());

		if (selectedWizard != null) {
			settings.put(STORE_SELECTED_WIZARD_ID, selectedWizard.getID());
		}
	}

	private static final class RootElementProxy
		extends WorkbenchAdapter
		implements IAdaptable {
		private WizardCollectionElement[] elements;

		public RootElementProxy(WizardCollectionElement element) {
			super();
			//If the element has no wizard then it is an empty category
			//and we should collapse
			if (element.getWizards().length == 0) {
				Object[] children = element.getChildren();
				elements = new WizardCollectionElement[children.length];
				System.arraycopy(children, 0, elements, 0, elements.length);
			} else
				elements = new WizardCollectionElement[] { element };
		}

		public Object getAdapter(Class adapter) {
			if (adapter == IWorkbenchAdapter.class)
				return this;
			return null;
		}

		public Object[] getChildren(Object o) {
			return elements;
		}
	}
}
