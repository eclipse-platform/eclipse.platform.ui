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

import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.activities.ws.ActivityMessages;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * New wizard selection tab that allows the user to select a registered
 *'New' wizard to be launched.
 *
 * NOTE: This is still under construction.
 */
class NewWizardNewPage2
	implements ISelectionChangedListener, IDoubleClickListener {
	private WizardCollectionElement wizardCategories;
	private NewWizardSelectionPage page;
	private IDialogSettings settings;

	//Keep track of the wizards we have previously selected
	private Hashtable selectedWizards = new Hashtable();

    private TabFolder tabFolder;
	private TreeViewer filteredViewer, unfilteredViewer;
    private Text unfilteredDescriptionText, filteredDescriptionText;

	private final static int SIZING_LISTS_HEIGHT = 200;
	private final static int SIZING_LISTS_WIDTH = 150;

	// id constants
	private final static String STORE_SELECTED_CATEGORY_ID = "NewWizardSelectionPage.STORE_SELECTED_CATEGORY_ID"; //$NON-NLS-1$
	private final static String STORE_EXPANDED_FILTERED_CATEGORIES_ID = "NewWizardSelectionPage.STORE_EXPANDED_FILTERED_CATEGORIES_ID"; //$NON-NLS-1$
    private final static String STORE_EXPANDED_UNFILTERED_CATEGORIES_ID = "NewWizardSelectionPage.STORE_EXPANDED_UNFILTERED_CATEGORIES_ID"; //$NON-NLS-1$
	private final static String STORE_SELECTED_WIZARD_ID = "NewWizardSelectionPage.STORE_SELECTED_WIZARD_ID"; //$NON-NLS-1$

	/**
	 *  Create an instance of this class
	 */
	public NewWizardNewPage2(
		NewWizardSelectionPage mainPage,
		IWorkbench aWorkbench,
		WizardCollectionElement wizardCategories) {
		this.page = mainPage;
		this.wizardCategories = wizardCategories;
	}
    
	public void activate() {
		page.setDescription(WorkbenchMessages.getString("NewWizardNewPage.description")); //$NON-NLS-1$
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
		outerContainer.setFont(wizardFont);

        if (WorkbenchActivityHelper.isFiltering()) {
            outerContainer.setLayout(new FillLayout());
            tabFolder = new TabFolder(outerContainer, SWT.NONE);
            tabFolder.setFont(parent.getFont());
                        
            Composite container = new Composite(tabFolder, SWT.NONE);
            container.setLayout(new GridLayout(2, true));
            filteredViewer = createViewer(container, true);
            filteredDescriptionText = createDescriptionText(container);
            createTab(container, true);                        
            
            container = new Composite(tabFolder, SWT.NONE);
            container.setLayout(new GridLayout(2, true));
            unfilteredViewer = createViewer(container, false);
            unfilteredDescriptionText = createDescriptionText(container);
            createTab(container, false);

        }
        else {
            outerContainer.setLayout(new GridLayout(2, true));            
            unfilteredViewer = createViewer(outerContainer, false);
            unfilteredDescriptionText = createDescriptionText(outerContainer);            
        }
        
        updateDescriptionText(""); //$NON-NLS-1$
                
		// wizard actions pane...create SWT table directly to
		// get single selection mode instead of multi selection.
		restoreWidgetValues();

        // we only set focus if a selection was restored
//		if (!filteredViewer.getSelection().isEmpty())
//			filteredViewer.getTree().setFocus();
		return outerContainer;
	}
    
    /**
	 * @param tabFolder2
	 * @param container
	 * @since 3.0
	 */
	private void createTab(Composite container, boolean filtering) {
        TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
        tabItem.setControl(container);
        tabItem.setText(filtering ? ActivityMessages.getString("ActivityFiltering.filtered") //$NON-NLS-1$
                         : ActivityMessages.getString("ActivityFiltering.unfiltered")); //$NON-NLS-1$
    
		
	}
	private Text createDescriptionText(Composite parent) {
        ScrolledComposite scroller = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
        scroller.setBackground(filteredViewer.getControl().getBackground());
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = SIZING_LISTS_WIDTH;
        scroller.setLayoutData(data);
        scroller.setContent(unfilteredDescriptionText);
        
        Text text = new Text(scroller, SWT.READ_ONLY | SWT.WRAP);
        text.setBackground(filteredViewer.getControl().getBackground());
        return text;
    }
    
    /**
     * Create a new viewer in the parent.
     * 
     * @param parent
     *            the parent <code>Composite</code>.
     * @param filtering
     *            whether the viewer should be filtering based on activities.
     * @return <code>TreeViewer</code>
     */
    private TreeViewer createViewer(Composite parent, boolean filtering) {
        // category tree pane...create SWT tree directly to
        // get single selection mode instead of multi selection.               
        Tree tree =
        new Tree(
                parent,
                SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        TreeViewer treeViewer = new TreeViewer(tree);
        treeViewer.setContentProvider(new WizardContentProvider(filtering));
        treeViewer.setLabelProvider(new WorkbenchLabelProvider());
        treeViewer.setSorter(NewWizardCollectionSorter2.INSTANCE);
        treeViewer.addSelectionChangedListener(this);
        treeViewer.addDoubleClickListener(this);
        if (wizardCategories.getParent(wizardCategories) == null) {
            treeViewer.setInput(wizardCategories);
        } else
            treeViewer.setInput(new RootElementProxy(wizardCategories));
        tree.setFont(parent.getFont());
        
        GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_LISTS_WIDTH;
		
		boolean needsHint = DialogUtil.inRegularFontMode(tree.getParent());
		
		//Only give a height hint if the dialog is going to be too small
		if (needsHint) {
		    data.heightHint = SIZING_LISTS_HEIGHT;
		}
		
		tree.setLayoutData(data);
        return treeViewer;       
    }
	/**
	 * @param string the new message
	 */
	private void updateDescriptionText(String string) {  
        if (unfilteredViewer.getControl().isFocusControl()) 
        	updateDescriptionText(unfilteredDescriptionText, string);
        else 
        	updateDescriptionText(filteredDescriptionText, string);
	}
    
	/**
	 * @param text the <code>Text</code> widget to update.
	 * @param string the new message
	 */
	private void updateDescriptionText(Text text, String string) {
        if (text != null && !text.isDisposed()) {
        	text.setText(string);
        	text.setSize(text.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        }
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
                event.getViewer(),
                event.getViewer().getSelection()));
		page.advanceToNextPage();
	}
	/**
	 * Expands the wizard categories in this page's category viewer that were
	 * expanded last time this page was used.  If a category that was previously
	 * expanded no longer exists then it is ignored.
	 */
	protected void expandPreviouslyExpandedCategories() {
        
        
//		String[] expandedCategoryPaths =
//			settings.getArray(STORE_EXPANDED_CATEGORIES_ID);
//		List categoriesToExpand = new ArrayList(expandedCategoryPaths.length);
//
//		for (int i = 0; i < expandedCategoryPaths.length; i++) {
//			WizardCollectionElement category =
//				wizardCategories.findChildCollection(
//					new Path(expandedCategoryPaths[i]));
//			if (category != null) // ie.- it still exists
//				categoriesToExpand.add(category);
//		}
//
//		if (!categoriesToExpand.isEmpty())
//			filteredViewer.setExpandedElements(
//				categoriesToExpand.toArray());
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
	 *	Set self's widgets to the values that they held last time this page was open
	 *
	 */
	protected void restoreWidgetValues() {
//		String[] expandedCategoryPaths =
//			settings.getArray(STORE_EXPANDED_CATEGORIES_ID);
//		if (expandedCategoryPaths == null)
//			return; // no stored values
//
//		expandPreviouslyExpandedCategories();
//		selectPreviouslySelectedCategoryAndWizard();
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
		page.setErrorMessage(null);
		page.setMessage(null);
		
		Object selectedObject =
		    getSingleSelection(
				(IStructuredSelection) selectionEvent.getSelection());
		
		if (selectedObject instanceof WizardCollectionElement) {
		    updateCategorySelection((WizardCollectionElement) selectedObject);
		}
		else if (selectedObject instanceof WorkbenchWizardElement) {
		    updateWizardSelection((WorkbenchWizardElement)selectedObject);		    
		}
	}
    /** 
     * @param selectedObject 
     */
    private void updateWizardSelection(WorkbenchWizardElement selectedObject) {
		WorkbenchWizardNode selectedNode;

		if (selectedWizards.containsKey(selectedObject)) {
		    selectedNode =
		    (WorkbenchWizardNode) selectedWizards.get(selectedObject);
		} else {
		    selectedNode = new WorkbenchWizardNode(page, (WorkbenchWizardElement)selectedObject) {
		        public IWorkbenchWizard createWizard() throws CoreException {
		            return (INewWizard) wizardElement
		            .createExecutableExtension();
		        }
		    };
		    selectedWizards.put(selectedObject, selectedNode);
		}

		page.selectWizardNode(selectedNode);

		updateDescriptionText(selectedObject.getDescription());
	}
	/** 
     * @param selectedCategory 
     */
	private void updateCategorySelection(WizardCollectionElement selectedCategory) {
		// TODO:  update description            
		page.selectWizardNode(null);
	}
	/**
	 * Selects the wizard category and wizard in this page that were selected
	 * last time this page was used.  If a category or wizard that was previously
	 * selected no longer exists then it is ignored.
	 */
	protected void selectPreviouslySelectedCategoryAndWizard() {
//		String categoryId = (String) settings.get(STORE_SELECTED_CATEGORY_ID);
//		if (categoryId == null)
//			return;
//		WizardCollectionElement category =
//			wizardCategories.findChildCollection(new Path(categoryId));
//		if (category == null)
//			return; // category no longer exists, or has moved
//
//		StructuredSelection selection = new StructuredSelection(category);
//		filteredViewer.setSelection(selection);
//		selectionChanged(
//			new SelectionChangedEvent(filteredViewer, selection));
//
//		String wizardId = (String) settings.get(STORE_SELECTED_WIZARD_ID);
//		if (wizardId == null)
//			return;
//		WorkbenchWizardElement wizard = category.findWizard(wizardId, false);
//		if (wizard == null)
//			return; // wizard no longer exists, or has moved
//
//		selectWizard(wizard);
	}
	/**
	 * Select the supplied wizard element.
	 * @param wizard. Defined to be Object but really a 
	 * <code>WorkbenchWizardElement</code>.If it is not 
	 * in the list nothing will happen.
	 */
	private void selectWizard(Object wizard) {
        selectWizard(unfilteredViewer, wizard);
        if (filteredViewer != null)
        	selectWizard(filteredViewer, wizard);
	}
    
	/**
	 * @param viewer the viewer to make the selection in
	 * @param wizard the wizard to select
	 */
	private void selectWizard(TreeViewer viewer, Object wizard) {
        StructuredSelection selection;
        selection = new StructuredSelection(wizard);
        viewer.setSelection(selection);
        selectionChanged(
                new SelectionChangedEvent(viewer, selection));
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
//		Object[] expandedElements = filteredViewer.getExpandedElements();
//		String[] expandedElementPaths = new String[expandedElements.length];
//		for (int i = 0; i < expandedElements.length; ++i) {
//			expandedElementPaths[i] =
//				((WizardCollectionElement) expandedElements[i])
//					.getPath()
//					.toString();
//		}
//		settings.put(STORE_EXPANDED_CATEGORIES_ID, expandedElementPaths);
	}
	/**
	 * Stores the currently-selected element in this page's dialog store,
	 * in order to recreate this page's state in the next instance of this page.
	 */
	protected void storeSelectedCategoryAndWizard() {
// TODO : store categories
//        WizardCollectionElement selectedCategory =
//			(WizardCollectionElement) getSingleSelection(
//				(IStructuredSelection) categoryTreeViewer
//				.getSelection());
//
//		if (selectedCategory != null) {
//			settings.put(
//				STORE_SELECTED_CATEGORY_ID,
//				selectedCategory.getPath().toString());
//		}
//
//		WorkbenchWizardElement selectedWizard =
//			(WorkbenchWizardElement) getSingleSelection(
//				(IStructuredSelection) wizardSelectionViewer
//				.getSelection());
//
//		if (selectedWizard != null) {
//			settings.put(STORE_SELECTED_WIZARD_ID, selectedWizard.getID());
//		}
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
				Object[] children = element.getChildren(null);
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
