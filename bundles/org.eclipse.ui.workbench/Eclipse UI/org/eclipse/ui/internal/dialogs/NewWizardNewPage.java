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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
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
	private static final int DESCRIPTION_IMAGE_HEIGHT = 150;
	private static final int DESCRIPTION_IMAGE_WIDTH = 200;
	private static final String DIALOG_SETTING_SECTION_NAME = "NewWizardSelectionPage."; //$NON-NLS-1$

	private final static int SIZING_LISTS_HEIGHT = 200;
	private final static int SIZING_VIEWER_WIDTH = 300;
	private final static int SIZING_DESC_WIDTH = 200;
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
    
    private ToolItem helpButton;

    private String wizardHelpHref;

    private Canvas descImageCanvas;
    
    private Map imageTable = new HashMap();

    private Label imageSeperator;

    private WorkbenchWizardElement selectedElement;
    
    private boolean needShowAll;

	/**
	 * Create an instance of this class
	 */
	public NewWizardNewPage(
		NewWizardSelectionPage mainPage,
		IWorkbench aWorkbench,
		WizardCollectionElement wizardCategories,
		WorkbenchWizardElement[] primaryWizards) {
		this.page = mainPage;
		this.wizardCategories = wizardCategories;
		this.primaryWizards = primaryWizards;
		
		trimPrimaryWizards();
		
		needShowAll = this.primaryWizards.length > 0;
		if (needShowAll) {
		    if (allPrimary(wizardCategories)) {
		        this.wizardCategories = null; // dont bother considering the categories as all wizards are primary
		        needShowAll = false;
		    }
		}
	}

	/**
     * Remove all primary wizards that are not in the wizard collection
     */
    private void trimPrimaryWizards() {
        ArrayList newPrimaryWizards = new ArrayList(primaryWizards.length);
        for (int i = 0; i < primaryWizards.length; i++) {	
            if (wizardCategories.findWizard(primaryWizards[i].getID(), true) != null)
                newPrimaryWizards.add(primaryWizards[i]);
        }
        
        primaryWizards = (WorkbenchWizardElement []) newPrimaryWizards.toArray(new WorkbenchWizardElement [newPrimaryWizards.size()]);        
    }

    /**
     * @return whether show all is needed.  Show all is needed if any of the 
     * wizards in the category or its children are NOT primary wizards.
     */
    private boolean allPrimary(WizardCollectionElement category) {
        Object [] wizards = category.getWizards();
        for (int i = 0; i < wizards.length; i++) {
            WorkbenchWizardElement wizard = (WorkbenchWizardElement) wizards[i];
            if (!isPrimary(wizard))
                return false;
        }
        
        Object [] children = category.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (!allPrimary((WizardCollectionElement) children[i]))
                return false;
        }
        
        return true;
    }

    /**
     * @param wizard
     * @return whether the given wizard is primary
     */
    private boolean isPrimary(WorkbenchWizardElement wizard) {
        for (int j = 0; j < primaryWizards.length; j++) {
            if (primaryWizards[j].equals(wizard))
                return true;
        }

        return false;
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
		createDescription(outerContainer);
		
		if (needShowAll) {
			showAllCheck = new Button(outerContainer, SWT.CHECK);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 2;
	        showAllCheck.setLayoutData(data);
	        showAllCheck.setFont(wizardFont);
			showAllCheck.setText(WorkbenchMessages.getString("NewWizardNewPage.showAll")); //$NON-NLS-1$
			showAllCheck.setSelection(false);
			
			// flipping tabs updates the selected node
			showAllCheck.addSelectionListener(new SelectionAdapter() {
					private Object [] expandedElements = new Object[0];

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
		}

		updateDescription(null);

		// wizard actions pane...create SWT table directly to
		// get single selection mode instead of multi selection.
		restoreWidgetValues();
		
		return outerContainer;
	}

	/**
	 * Create the description controls.
	 * 
	 * @param parent the parent <code>Composite</code>.
	 * @since 3.0
	 */
	private void createDescription(Composite parent) {	    	   
	    Composite descParent = new Composite(parent, SWT.NONE);	    
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_DESC_WIDTH;
		descParent.setLayoutData(data);
		
		GridLayout layout = new GridLayout(2, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		descParent.setLayout(layout);
		
	    Label descLabel = new Label(descParent, SWT.NONE);	
	    descLabel.setFont(parent.getFont());
	    descLabel.setText(WorkbenchMessages.getString("NewWizardNewPage.descriptionLabel")); //$NON-NLS-1$
	    	    
	    data = new GridData(GridData.FILL_HORIZONTAL);
	    descLabel.setLayoutData(data);
	    
	    Image buttonImage = WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_LCL_LINKTO_HELP);
	    ToolBar toolBar =  new ToolBar(descParent, SWT.HORIZONTAL | SWT.FLAT);
	    helpButton = new ToolItem(toolBar, SWT.NONE);
	    helpButton.setImage(buttonImage);	    	
		helpButton.setToolTipText(WorkbenchMessages.getString("NewWizardNewPage.moreHelp")); //$NON-NLS-1$
        data = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
	    toolBar.setLayoutData(data);
	    
        helpButton.addSelectionListener(new SelectionAdapter() {
        	/* (non-Javadoc)
        	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
        	 */
        	public void widgetSelected(SelectionEvent e) {
        		WorkbenchHelp.displayHelpResource(wizardHelpHref);
        	}
        });	    
		
		descriptionText = new Text(descParent, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
		descriptionText.setFont(parent.getFont());				
		data = new GridData(GridData.FILL_BOTH);
		data.grabExcessVerticalSpace = true;
		data.horizontalSpan = 2;
		descriptionText.setLayoutData(data);
		        
        imageSeperator = new Label(descParent, SWT.SEPARATOR | SWT.HORIZONTAL);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        imageSeperator.setLayoutData(data);
                
        descImageCanvas = new Canvas(descParent, SWT.NONE);
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_CENTER);
        data.horizontalSpan = 2;        
        data.widthHint = DESCRIPTION_IMAGE_WIDTH;
        data.heightHint = DESCRIPTION_IMAGE_HEIGHT;
	    descImageCanvas.setLayoutData(data);    
	    
	    // hook a listener to get rid of cached images.
	    descImageCanvas.addDisposeListener(new DisposeListener() {

            /* (non-Javadoc)
             * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
             */
            public void widgetDisposed(DisposeEvent e) {
                for (Iterator i = imageTable.values().iterator(); i.hasNext(); ) {
                    ((Image) i.next()).dispose();                    
                }
                imageTable.clear();
            }});
            
    	descImageCanvas.addPaintListener(new PaintListener() {

            /* (non-Javadoc)
             * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
             */
            public void paintControl(PaintEvent e) {
		        ImageDescriptor descriptor = null;
		        if (selectedElement != null) {
			        descriptor = selectedElement.getDescriptionImage();
				}        

				
		        Point size = descImageCanvas.getSize();
                if (descriptor != null) {
					Image image = (Image) imageTable.get(descriptor);					
					if (image == null) {
			        	image = descriptor.createImage(false);			        	
			        	if (image != null) {
			        		// see if the image needs to be scaled down
			        		ImageData scaledData = scale(image.getImageData());
			        		if (scaledData != null) { 
			        			Image newImage = new Image(descImageCanvas.getDisplay(), scaledData);
			        			image.dispose();
			        			image = newImage;
			        		}
		        			imageTable.put(descriptor, image);			        		
			        	}
			        }	        
			        if (image != null) {			            
			            imageSeperator.setVisible(true);			            
				        GC gc = new GC(descImageCanvas);
				        Rectangle bounds = image.getBounds();
                        int xOffset = (size.x - bounds.width) / 2;
				        int yOffset = (size.y - bounds.height) / 2;
				        gc.drawImage(image, xOffset, yOffset);
				        gc.dispose();
				        return;
				    }        
				}
				imageSeperator.setVisible(false);
		        GC gc = new GC(descImageCanvas);
		        gc.fillRectangle(0,0, size.x, size.y);
		        gc.dispose();
		}

            /**
             * @param data
             * @return
             */
            private ImageData scale(ImageData imageData) {
		        int width = imageData.width;
				int height = imageData.height;
				float scale = 1.0f;				
				Point size = descImageCanvas.getSize();
				
                if (width > height) 
					if (width > size.x)
						scale = (float)size.x / (float)width;
				else 
					if (height > size.y)
						scale = (float)size.y / (float)height;

				if (scale == 1.0f)
					return null;
				
				width *= scale;
				height *= scale;
				return imageData.scaledTo(width, height);
            }});        		
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
	    
	    Composite treeParent = new Composite(parent, SWT.NONE);
	    treeParent.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_VIEWER_WIDTH;		

		boolean needsHint = DialogUtil.inRegularFontMode(treeParent.getParent());

		//Only give a height hint if the dialog is going to be too small
		if (needsHint) {
			data.heightHint = SIZING_LISTS_HEIGHT;
		}

		treeParent.setLayoutData(data);
		
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		treeParent.setLayout(layout);
			    
	    Label wizardLabel = new Label(treeParent, SWT.NONE);	
	    wizardLabel.setFont(parent.getFont());
	    wizardLabel.setText(WorkbenchMessages.getString("NewWizardNewPage.wizardsLabel")); //$NON-NLS-1$
	    
	    data = new GridData(GridData.FILL_HORIZONTAL);
	    wizardLabel.setLayoutData(data);
	    
		Tree tree =
			new Tree(
			    treeParent,
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer = new TreeViewer(tree);
		
		contentProvider = new WizardContentProvider(false);
        viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setSorter(NewWizardCollectionSorter.INSTANCE);
		viewer.addSelectionChangedListener(this);
		viewer.addDoubleClickListener(this);
		
        ArrayList inputArray = new ArrayList();
		
		for (int i = 0; i < primaryWizards.length; i++) {
            inputArray.add(primaryWizards[i]);
        }
		
		boolean expandTop = false;
		
		if (wizardCategories != null) {
			if (wizardCategories.getParent(wizardCategories) == null) {
			    Object [] children = wizardCategories.getChildren();
			    for (int i = 0; i < children.length; i++) {
		            inputArray.add(children[i]);
		        }
			} else {
			    expandTop = true;
			    inputArray.add(wizardCategories);
			}
		}

		// ensure the category is expanded.  If there is a remembered expansion it will be set later.
		if (expandTop)
		    viewer.setAutoExpandLevel(2);	
		
		
		AdaptableList input = new AdaptableList(inputArray);
		
		viewer.setInput(input);
		
		tree.setFont(parent.getFont());

		viewer.addDoubleClickListener(new IDoubleClickListener() {
		    /*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
			 */
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection s = (IStructuredSelection) event.getSelection();
				Object element = s.getFirstElement();
				if (viewer.isExpandable(element)) {
					viewer.setExpandedState(element, !viewer.getExpandedState(element));
				} else if (element instanceof WorkbenchWizardElement) {
				    page.advanceToNextPage();
				}
			}
		});
		
		data = new GridData(GridData.FILL_BOTH);
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

	    if (showAllCheck != null) {
	        showAllCheck.setSelection(showAll);
	        contentProvider.setFiltering(!showAll);
	    }    
	    
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

		if (selectedObject instanceof WorkbenchWizardElement) {
			updateWizardSelection((WorkbenchWizardElement) selectedObject);
		}
		else {
			selectedElement = null;
			page.selectWizardNode(null);
			updateDescription(null);
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
	    Object [] expandedElements = viewer.getExpandedElements();
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

	    if (showAllCheck != null && showAllCheck.getSelection()) {	        
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
	 * Update the current description controls.
	 * 
	 * @param selectedObject the new wizard
	 * @since 3.0
	 */
	private void updateDescription(WorkbenchWizardElement selectedObject) {
		String string = ""; //$NON-NLS-1$
		if (selectedObject != null)
			string = selectedObject.getDescription();
			
		if (descriptionText != null && !descriptionText.isDisposed()) {
        	descriptionText.setText(string);        	        	
//        	Point viewerSize = descriptionText.getSize();
//            descriptionText.setSize(descriptionText.computeSize(viewerSize.x, viewerSize.y, true));
//			descriptionText.setSize(descriptionText.getSize());
//			if (descriptionText.getVerticalBar().getMaximum() == 0) {
//				descriptionText.getVerticalBar().setVisible(false);
//			}
//			else {
//				descriptionText.getVerticalBar().setVisible(true);
//			}
        }       
        
        if (selectedObject != null) {
	        wizardHelpHref = selectedObject.getHelpHref();
		}        
		else {
			wizardHelpHref = null;
		} 
		
		if (wizardHelpHref != null) {
			helpButton.getParent().setVisible(true);
		}
		else {
			helpButton.getParent().setVisible(false);
		}
		
		descImageCanvas.redraw();
		
	}

	/**
	 * @param selectedObject
	 */
	private void updateWizardSelection(WorkbenchWizardElement selectedObject) {
		selectedElement = selectedObject;
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

		updateDescription(selectedObject);
	}
}
