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
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * New wizard selection tab that allows the user to select a registered 'New'
 * wizard to be launched.
 */
class NewWizardNewPage
	implements ISelectionChangedListener, IDoubleClickListener {

//	private static final class RootElementProxy
//		extends WorkbenchAdapter
//		implements IAdaptable {
//		private WizardCollectionElement[] elements;
//
//		public RootElementProxy(WizardCollectionElement element) {
//			super();
//			//If the element has no wizard then it is an empty category
//			//and we should collapse
//			if (element.getWizards().length == 0) {
//				Object[] children = element.getChildren(null);
//				elements = new WizardCollectionElement[children.length];
//				System.arraycopy(children, 0, elements, 0, elements.length);
//			} else
//				elements = new WizardCollectionElement[] { element };
//		}
//
//		public Object getAdapter(Class adapter) {
//			if (adapter == IWorkbenchAdapter.class)
//				return this;
//			return null;
//		}
//
//		public Object[] getChildren(Object o) {
//			return elements;
//		}
//	}

	// id constants
	private final static String DIALOG_SETTING_SECTION_NAME = "NewWizardSelectionPage."; //$NON-NLS-1$

	private final static int SIZING_LISTS_HEIGHT = 200;
	private final static int SIZING_LISTS_WIDTH = 150;
	private final static String STORE_EXPANDED_CATEGORIES_ID = DIALOG_SETTING_SECTION_NAME + "STORE_EXPANDED_CATEGORIES_ID"; //$NON-NLS-1$
	private final static String STORE_SELECTED_ID = DIALOG_SETTING_SECTION_NAME + "STORE_SELECTED_ID"; //$NON-NLS-1$
	private static final String SHOW_ALL_ENABLED = DIALOG_SETTING_SECTION_NAME + ".SHOW_ALL_SELECTED"; //$NON-NLS-1$
	private TreeViewer viewer;
	private NewWizardSelectionPage page;

	//Keep track of the wizards we have previously selected
	private Hashtable selectedWizards = new Hashtable();
	private IDialogSettings settings;

	private Button showAllCheck;

	private Text descriptionText;
	private WizardCollectionElement wizardCategories;

    private WorkbenchWizardElement[] primaryWizards;

    private WizardContentProvider contentProvider;
    
    private Object[] expandedElements = new Object[0];

	/**
	 * Create an instance of this class
	 */
	public NewWizardNewPage(
		NewWizardSelectionPage mainPage,
		IWorkbench aWorkbench,
		WizardCollectionElement wizardCategories, WorkbenchWizardElement[] primaryWizards) {
		this.page = mainPage;
		this.wizardCategories = wizardCategories;
		this.primaryWizards = primaryWizards;
	}

	/**
	 * @since 3.0
	 */
	public void activate() {
		page.setDescription(WorkbenchMessages.getString("NewWizardNewPage.description")); //$NON-NLS-1$
	}
	/**
	 * Create this tab's visual components
	 * 
	 * @param parent Composite
	 * @return Control
	 */
	protected Control createControl(Composite parent) {

		Font wizardFont = parent.getFont();
		// top level group
		Composite outerContainer = new Composite(parent, SWT.NONE);
		outerContainer.setLayout(new GridLayout(2, false));
		outerContainer.setFont(wizardFont);

		createViewer(outerContainer);
		createDescriptionText(outerContainer);

		showAllCheck = new Button(outerContainer, SWT.CHECK);
		
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
        showAllCheck.setLayoutData(data);
		showAllCheck.setText(WorkbenchMessages.getString("NewWizardNewPage.showAll")); //$NON-NLS-1$
		showAllCheck.setSelection(false);
		
		// flipping tabs updates the selected node
		showAllCheck.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {				    
				    boolean showAll = showAllCheck.getSelection();
                    if (!showAll)
				        expandedElements = viewer.getExpandedElements();
                    
                    if (showAll) {
                        viewer.getControl().setRedraw(false);
                    }

                    try {
					    contentProvider.setFiltering(!showAll);
					    
					    if (showAll)
					        viewer.setExpandedElements(expandedElements);
                    }
                    finally {
                        if (showAll)
                            viewer.getControl().setRedraw(true);
                    }
				}
			});
		

		updateDescriptionText(""); //$NON-NLS-1$

		// wizard actions pane...create SWT table directly to
		// get single selection mode instead of multi selection.
		restoreWidgetValues();

		return outerContainer;
	}

	/**
	 * Create a new description text control.
	 * 
	 * @param parent the parent <code>Composite</code>.
	 * @since 3.0
	 */
	private void createDescriptionText(Composite parent) {
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_LISTS_WIDTH;

//		ScrolledComposite scroller =
//			new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
//		scroller.setBackground(viewer.getControl().getBackground());
//		scroller.setLayoutData(data);
//		scroller.setContent(descriptionText);

		descriptionText = new Text(parent, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		descriptionText.setLayoutData(data);
		descriptionText.setBackground(viewer.getControl().getBackground());
	}

	/**
	 * Create a new viewer in the parent.
	 * 
	 * @param parent the parent <code>Composite</code>.
	 * @since 3.0
	 */
	private void createViewer(Composite parent) {
		// category tree pane...create SWT tree directly to
		// get single selection mode instead of multi selection.
		Tree tree =
			new Tree(
				parent,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer = new TreeViewer(tree);
		
		contentProvider = new WizardContentProvider(true);
        viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setSorter(NewWizardCollectionSorter.INSTANCE);
		viewer.addSelectionChangedListener(this);
		viewer.addDoubleClickListener(this);
		
		ArrayList inputArray = new ArrayList();
		
		for (int i = 0; i < primaryWizards.length; i++) {
            inputArray.add(primaryWizards[i]);
        }
		
		if (wizardCategories.getParent(wizardCategories) == null) {
		    Object [] children = wizardCategories.getChildren();
		    for (int i = 0; i < children.length; i++) {
	            inputArray.add(children[i]);
	        }
		} else {
			//inputArray.add(new RootElementProxy(wizardCategories));
		    inputArray.add(wizardCategories);
		}
		
		AdaptableList input = new AdaptableList(inputArray);
		
		viewer.setInput(input);
		tree.setFont(parent.getFont());

		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_LISTS_WIDTH;

		boolean needsHint = DialogUtil.inRegularFontMode(tree.getParent());

		//Only give a height hint if the dialog is going to be too small
		if (needsHint) {
			data.heightHint = SIZING_LISTS_HEIGHT;
		}

		tree.setLayoutData(data);
	}
	
	/**
	 * A wizard in the wizard viewer has been double clicked. Treat it as a
	 * selection.
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
	 * expanded last time this page was used. If a category that was previously
	 * expanded no longer exists then it is ignored.
	 */
	protected void expandPreviouslyExpandedCategories() {
		boolean showAll =
			settings.getBoolean(SHOW_ALL_ENABLED);

	    showAllCheck.setSelection(showAll);
	    contentProvider.setFiltering(!showAll);
	    
		String[] expandedCategoryPaths = settings.getArray(STORE_EXPANDED_CATEGORIES_ID);
		if (expandedCategoryPaths == null || expandedCategoryPaths.length == 0)
			return;

		List categoriesToExpand = new ArrayList(expandedCategoryPaths.length);

		for (int i = 0; i < expandedCategoryPaths.length; i++) {
			WizardCollectionElement category =
				wizardCategories.findChildCollection(
					new Path(expandedCategoryPaths[i]));
			if (category != null) // ie.- it still exists
				categoriesToExpand.add(category);
		}

		if (!categoriesToExpand.isEmpty())
			viewer.setExpandedElements(categoriesToExpand.toArray());
	
	}
	/**
	 * Returns the single selected object contained in the passed
	 * selectionEvent, or <code>null</code> if the selectionEvent contains
	 * either 0 or 2+ selected objects.
	 */
	protected Object getSingleSelection(IStructuredSelection selection) {
		return selection.size() == 1 ? selection.getFirstElement() : null;
	}

	/**
	 * Set self's widgets to the values that they held last time this page was
	 * open
	 *  
	 */
	protected void restoreWidgetValues() {
		expandPreviouslyExpandedCategories();
		selectPreviouslySelected();
	}

	/**
	 * Store the current values of self's widgets so that they can be restored
	 * in the next instance of self
	 *  
	 */
	public void saveWidgetValues() {
		storeExpandedCategories();
		storeSelectedCategoryAndWizard();
	}
	/**
	 * The user selected either new wizard category(s) or wizard element(s).
	 * Proceed accordingly.
	 * 
	 * @param newSelection ISelection
	 */
	public void selectionChanged(SelectionChangedEvent selectionEvent) {
		page.setErrorMessage(null);
		page.setMessage(null);

		Object selectedObject =
			getSingleSelection(
				(IStructuredSelection) selectionEvent.getSelection());

		if (selectedObject instanceof WizardCollectionElement) {
			updateCategorySelection((WizardCollectionElement) selectedObject);
		} else if (selectedObject instanceof WorkbenchWizardElement) {
			updateWizardSelection((WorkbenchWizardElement) selectedObject);
		}
		else {
		    updateDescriptionText("");	//$NON-NLS-1$	    
		}
	}

	/**
	 * Selects the wizard category and wizard in this page that were selected
	 * last time this page was used. If a category or wizard that was
	 * previously selected no longer exists then it is ignored.
	 */
	protected void selectPreviouslySelected() {
		String selectedId = settings.get(STORE_SELECTED_ID);
		if (selectedId == null)
			return;

		Object selected =
			wizardCategories.findChildCollection(new Path(selectedId));

		if (selected == null) {
			selected = wizardCategories.findWizard(selectedId, true);

			if (selected == null)
				// if we cant find either a category or a wizard, abort.
				return;
		}
		StructuredSelection selection = new StructuredSelection(selected);
		viewer.setSelection(selection);	    
	}

	/**
	 * Set the dialog store to use for widget value storage and retrieval
	 * 
	 * @param settings IDialogSettings
	 */
	public void setDialogSettings(IDialogSettings settings) {
		this.settings = settings;
	}

	/**
	 * Stores the collection of currently-expanded categories in this page's
	 * dialog store, in order to recreate this page's state in the next
	 * instance of this page.
	 */
	protected void storeExpandedCategories() {
	    if (expandedElements == null)
	        return;
        List expandedElementPaths = new ArrayList(expandedElements.length);
        for (int i = 0; i < expandedElements.length; ++i) {
        	if (expandedElements[i] instanceof WizardCollectionElement)
        		expandedElementPaths.add(
        			((WizardCollectionElement) expandedElements[i])
        				.getPath()
        				.toString());
        }
        settings.put(
        	STORE_EXPANDED_CATEGORIES_ID,
        	(String[]) expandedElementPaths.toArray(
        		new String[expandedElementPaths.size()]));
	}

	/**
	 * Stores the currently-selected element in this page's dialog store, in
	 * order to recreate this page's state in the next instance of this page.
	 */
	protected void storeSelectedCategoryAndWizard() {

	    if (showAllCheck.getSelection()) {	        
	        settings.put(
				SHOW_ALL_ENABLED,
				true);	        
	    }
	    else {
	    	settings.put(
				SHOW_ALL_ENABLED,
				false);			
		}
	    
		Object selected =
        	getSingleSelection((IStructuredSelection) viewer.getSelection());
        
        if (selected != null) {
        	if (selected instanceof WizardCollectionElement)
        		settings.put(
        			STORE_SELECTED_ID,
        			((WizardCollectionElement) selected).getPath().toString());
        	else // else its a wizard
        		settings.put(STORE_SELECTED_ID, ((WorkbenchWizardElement) selected).getID());
        }
	}

	/**
	 * @param selectedCategory
	 */
	private void updateCategorySelection(WizardCollectionElement selectedCategory) {
		page.selectWizardNode(null);
		updateDescriptionText(""); //$NON-NLS-1$
	}

	/**
	 * Update the current description control with the provided message.
	 * 
	 * @param string the new message
	 * @since 3.0
	 */
	private void updateDescriptionText(String string) {
		if (descriptionText != null && !descriptionText.isDisposed()) {
        	descriptionText.setText(string);        	
        	Point viewerSize = viewer.getControl().getSize();
            descriptionText.setSize(descriptionText.computeSize(viewerSize.x, viewerSize.y, true));
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
			selectedNode =
				new WorkbenchWizardNode(
					page,
					selectedObject) {
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
}
