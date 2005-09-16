package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.activities.ws.WorkbenchTriggerPoints;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * Wizard page from which an import source or export destination can be chosen.
 * 
 * @since 3.2
 *
 */
public class ImportExportPage extends WorkbenchWizardSelectionPage{
    private static final String DIALOG_SETTING_SECTION_NAME = "ImportExportPage."; //$NON-NLS-1$
    private static final String IMPORT_EXPORT_SELECTION = DIALOG_SETTING_SECTION_NAME
            + "STORE_SELECTED_TRANSFER_ID"; //$NON-NLS-1$
    private static final String STORE_SELECTED_IMPORT_WIZARD_ID = DIALOG_SETTING_SECTION_NAME
            + "STORE_SELECTED_IMPORT_WIZARD_ID"; //$NON-NLS-1$
    private static final String STORE_SELECTED_EXPORT_WIZARD_ID = DIALOG_SETTING_SECTION_NAME
    	+ "STORE_SELECTED_EXPORT_WIZARD_ID"; //$NON-NLS-1$
    private static final String STORE_EXPANDED_IMPORT_CATEGORIES = DIALOG_SETTING_SECTION_NAME
	+ "STORE_EXPANDED_IMPORT_CATEGORIES";	//$NON-NLS-1$
    private static final String STORE_EXPANDED_EXPORT_CATEGORIES = DIALOG_SETTING_SECTION_NAME
		+ "STORE_EXPANDED_EXPORT_CATEGORIES";	//$NON-NLS-1$

    private static final int IMPORT_SELECTION = 0;
    private static final int EXPORT_SELECTION = 1;
    
	private TabFolder tabFolder;
	private ImportExportTree exportList;
	private ImportExportTree importList;
	
	/*
	 * the initial tab to be selected - required for backward compatibility with Import and Export menu items.
	 */ 
	private String initialPageId = null;
	
	/*
	 * Class to create a control that shows a categorized tree of wizard types.
	 */
	protected class ImportExportTree {
		private final static int SIZING_LISTS_HEIGHT = 200;
		
		private IWizardCategory wizardCategories;
		private String message;
		private TreeViewer viewer;

		/**
		 * Constructor for ImportExportTree
		 * 
		 * @param categories root wizard category for the wizard type
		 * @param msg message describing what the user should choose from the tree.
		 */
		protected ImportExportTree(IWizardCategory categories, String msg){
			this.wizardCategories = categories;
			this.message = msg;
		}
		
		/**
		 * Create the tree viewer and a message describing what the user should choose
		 * from the tree.
		 * 
		 * @param folder tab folder on which the tab for this composite is to be created
		 * @return Comoposite with all widgets
		 */
		protected Composite createControl(TabFolder folder){
	        Font font = folder.getFont();

	        // create composite for page.
	        Composite outerContainer = new Composite(folder, SWT.NONE);
	        outerContainer.setLayout(new GridLayout());
	        outerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
	        outerContainer.setFont(font);

	        Label messageLabel = new Label(outerContainer, SWT.NONE);
	        if (message != null)
	        	messageLabel.setText(message);
	        messageLabel.setFont(font);

	        createTreeViewer(outerContainer);
	        layoutTopControl(viewer.getControl());

	        return outerContainer;
		}
		
		/**
		 * Create the categorized tree viewer.
		 * 
		 * @param parent
		 */
		private void createTreeViewer(Composite parent){        
			//Create a tree for the list
	        Tree tree = new Tree(parent, SWT.SINGLE | SWT.H_SCROLL
	                | SWT.V_SCROLL | SWT.BORDER);
	        viewer = new TreeViewer(tree);
	        tree.setFont(parent.getFont());

	        viewer.setContentProvider(new WizardContentProvider());
	        viewer.setLabelProvider(new WorkbenchLabelProvider());
	        viewer.setSorter(NewWizardCollectionSorter.INSTANCE);
	        
	        ArrayList inputArray = new ArrayList();
	        boolean expandTop = false;

	        if (wizardCategories != null) {
	            if (wizardCategories.getParent() == null) {
	                IWizardCategory [] children = wizardCategories.getCategories();
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
		}

		/**
		 * 
		 * @return the categorized tree viewer
		 */
		protected TreeViewer getViewer(){
			return viewer;
		}

		/**
		 * Layout for the given control.
		 * 
		 * @param control
		 */
	    private void layoutTopControl(Control control) {
	        GridData data = new GridData(GridData.FILL_BOTH);

	        int availableRows = DialogUtil.availableRows(control.getParent());

	        //Only give a height hint if the dialog is going to be too small
	        if (availableRows > 50) {
	            data.heightHint = SIZING_LISTS_HEIGHT;
	        } else {
	            data.heightHint = availableRows * 3;
	        }

	        control.setLayoutData(data);
	    }
	}
	
	/**
	 * Constructor for import/export wizard page.
	 * 
	 * @param aWorkbench current workbench
	 * @param currentSelection current selection
	 */
	protected ImportExportPage(IWorkbench aWorkbench, IStructuredSelection currentSelection){
		super("importExportPage", aWorkbench, currentSelection, null, null);	//$NON-NLS-1$
		setTitle(WorkbenchMessages.Select);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
	    Font font = parent.getFont();
	
	    // create composite for page.
	    Composite outerContainer = new Composite(parent, SWT.NONE);
	    outerContainer.setLayout(new GridLayout());
	    outerContainer.setLayoutData(new GridData(GridData.FILL_BOTH));
	    outerContainer.setFont(font);
	    
	    setMessage(WorkbenchMessages.ImportExportPage_selectTransferType);
	    
	    tabFolder = new TabFolder(outerContainer, SWT.NULL);
	    tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
	    
	    createTabs();
	    
	    tabFolder.addSelectionListener(new SelectionAdapter(){
	    	public void widgetSelected(SelectionEvent e) {
	    		tabSelectionChanged();
	    	}
	    });
	    
		Dialog.applyDialogFont(tabFolder);
		
	    restoreWidgetValues();
	
	    setControl(outerContainer);	
	    
	    PlatformUI.getWorkbench().getHelpSystem().setHelp(
	    		outerContainer, IWorkbenchHelpContextIds.IMPORT_EXPORT_WIZARD);
	}

	/**
	 * Create the tabs for the tab folder of this wizard page.
	 */
    protected void createTabs(){
		// Import tab
		TabItem importTab = new TabItem(tabFolder, SWT.NULL);
		importTab.setText(WorkbenchMessages.ImportExportPage_importTab); 
		importTab.setControl(createImportTab());
		
		// Export tab
		TabItem exportTab = new TabItem(tabFolder, SWT.NULL);
		exportTab.setText(WorkbenchMessages.ImportExportPage_exportTab); 
		exportTab.setControl(createExportTab());
    }
    
    /*
     * Create control for Import tab.
     */
    private Composite createImportTab(){
		IWizardCategory root = WorkbenchPlugin.getDefault()
			.getImportWizardRegistry().getRootCategory();
    	importList = new ImportExportTree(
    			root, WorkbenchMessages.ImportWizard_selectSource);
    	Composite importComp = importList.createControl(tabFolder);
    	importList.getViewer().addSelectionChangedListener(new ISelectionChangedListener(){
    		public void selectionChanged(SelectionChangedEvent event) {
    			listSelectionChanged(event.getSelection());    	       			
    		}
    	});
    	importList.getViewer().addDoubleClickListener(new IDoubleClickListener(){
        	public void doubleClick(DoubleClickEvent event) {
        		listDoubleClicked(event.getViewer().getSelection());
        	}
        });
    	return importComp;
    }
    
    /*
     * Method to call when an item in one of the lists is double-clicked.
     * Shows the first page of the selected wizard.
     */
    private void listDoubleClicked(ISelection selection){
    	listSelectionChanged(selection);
        getContainer().showPage(getNextPage());    	
    }
    /*
     * Method to call whenever the selected tab has changes.
     * Updates the wizard's message to reflect the tab selected and the selected wizard 
     * on that tab, if there is one.
     */
    private void tabSelectionChanged(){
    	int selected = tabFolder.getSelectionIndex();
    	updateMessage(selected);
    }
    
    /*
     * Update the wizard's message based on the given (selected) wizard element.
     */
    private void updateSelectedNode(WorkbenchWizardElement wizardElement){
        setErrorMessage(null);
        if (wizardElement == null) {
        	updateMessage(tabFolder.getSelectionIndex());
            setSelectedNode(null);
            return;
        }

        setSelectedNode(createWizardNode(wizardElement));
        setMessage(wizardElement.getDescription()); 
    }
    
    /*
     * Update the wizard's message based on the currently selected tab
     * and the selected wizard on that tab.
     */
    private void updateMessage(int selected){
    	TreeViewer viewer = null;
    	String noSelectionMsg = null;
    	if (selected == IMPORT_SELECTION){
    		viewer = importList.getViewer();  
    		noSelectionMsg = WorkbenchMessages.ImportExportPage_chooseImportSource;
    	}
    	else if (selected == EXPORT_SELECTION){
    		viewer = exportList.getViewer();
    		noSelectionMsg = WorkbenchMessages.ImportExportPage_chooseExportDestination;
    	}
    	if (viewer != null){
    		ISelection selection = viewer.getSelection();
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object sel = ss.getFirstElement();
            if (sel instanceof WorkbenchWizardElement){
               	updateSelectedNode((WorkbenchWizardElement)sel);
            }
            else{
            	setMessage(noSelectionMsg);  
            	setSelectedNode(null);
            }
    	}
    	else 
    		setMessage(WorkbenchMessages.ImportExportPage_selectTransferType);
    }
    
    /*
     * Method to call whenever the selection in one of the lists has changed.
     * Updates the wizard's message to relect the description of the currently 
     * selected wizard.
     */
    private void listSelectionChanged(ISelection selection){
        setErrorMessage(null);
        IStructuredSelection ss = (IStructuredSelection) selection;
        Object sel = ss.getFirstElement();
        if (sel instanceof WorkbenchWizardElement){
	        WorkbenchWizardElement currentWizardSelection = (WorkbenchWizardElement) sel;        
	        updateSelectedNode(currentWizardSelection);
        }
        else
        	updateSelectedNode(null);
    }
    
    /*
     * Create control for Export tab.
     */
    private Composite createExportTab(){
		IWizardCategory root = WorkbenchPlugin.getDefault()
			.getExportWizardRegistry().getRootCategory();
    	exportList = new ImportExportTree(
    			root, WorkbenchMessages.ExportWizard_selectDestination);
    	Composite exportComp = exportList.createControl(tabFolder);
        exportList.getViewer().addSelectionChangedListener(new ISelectionChangedListener(){
    		public void selectionChanged(SelectionChangedEvent event) {
    			listSelectionChanged(event.getSelection());    	       			
    		}
    	});
        exportList.getViewer().addDoubleClickListener(new IDoubleClickListener(){
        	public void doubleClick(DoubleClickEvent event) {
        		listDoubleClicked(event.getViewer().getSelection());
        	}
        });
        return exportComp;
    }

    /*
     * Create a wizard node given a wizard's descriptor.
     */
	private IWizardNode createWizardNode(IWizardDescriptor element) {
        return new WorkbenchWizardNode(this, element) {
            public IWorkbenchWizard createWizard() throws CoreException {
                return wizardElement.createWizard();
            }
        };
    }
    
    /**
     * Uses the dialog store to restore widget values to the values that they
     * held last time this wizard was used to completion.
     */
    protected void restoreWidgetValues() {
    	// restore each tabs last selection and tree state
    	IWizardCategory importRoot = WorkbenchPlugin.getDefault().getImportWizardRegistry().getRootCategory();
        expandPreviouslyExpandedCategories(STORE_EXPANDED_IMPORT_CATEGORIES, importRoot,importList.getViewer());
        selectPreviouslySelected(STORE_SELECTED_IMPORT_WIZARD_ID, importRoot, importList.getViewer());
        
        IWizardCategory exportRoot = WorkbenchPlugin.getDefault().getExportWizardRegistry().getRootCategory();
        expandPreviouslyExpandedCategories(STORE_EXPANDED_EXPORT_CATEGORIES, exportRoot, exportList.getViewer());
        selectPreviouslySelected(STORE_SELECTED_EXPORT_WIZARD_ID, exportRoot, exportList.getViewer());       

        // restore last selected tab or set to desired page (if provided)
    	try{
	    	if (initialPageId == null){
	    		// initial page not specified, use settings 
				int selectedTab = getDialogSettings().getInt(IMPORT_EXPORT_SELECTION);
				if ((tabFolder.getItemCount() > selectedTab)) {
					tabFolder.setSelection(selectedTab);
					updateMessage(selectedTab);
				}
				else{	// default behavior - show first tab
					updateMessage(0);
				}
	    	}
	    	else{	
	    		// initial page specified - set it and set the selected node for it, if one exists        		
	    		if (initialPageId == ImportExportWizard.IMPORT){
	    			tabFolder.setSelection(IMPORT_SELECTION);
	    			updateMessage(tabFolder.getSelectionIndex());
	    		}
	    		else if (initialPageId == ImportExportWizard.EXPORT){
	    			tabFolder.setSelection(EXPORT_SELECTION);
	    			updateMessage(tabFolder.getSelectionIndex());
	    		}
	    	}
		}
		catch (NumberFormatException e){
		} 
    }

    /**
     * Expands the wizard categories in this page's category viewer that were
     * expanded last time this page was used. If a category that was previously
     * expanded no longer exists then it is ignored.
     */
    protected void expandPreviouslyExpandedCategories(String setting, IWizardCategory wizardCategories, TreeViewer viewer) {
        String[] expandedCategoryPaths =  getDialogSettings()
                .getArray(setting);
        if (expandedCategoryPaths == null || expandedCategoryPaths.length == 0)
            return;

        List categoriesToExpand = new ArrayList(expandedCategoryPaths.length);

        if (wizardCategories != null) {
            for (int i = 0; i < expandedCategoryPaths.length; i++) {
                IWizardCategory category = wizardCategories
                        .findCategory(new Path(expandedCategoryPaths[i]));
                if (category != null) // ie.- it still exists
                    categoriesToExpand.add(category);
            }
        }

        if (!categoriesToExpand.isEmpty())
            viewer.setExpandedElements(categoriesToExpand.toArray());

    }

    /**
     * Selects the wizard category and wizard in this page that were selected
     * last time this page was used. If a category or wizard that was
     * previously selected no longer exists then it is ignored.
     */
    protected void selectPreviouslySelected(String setting, IWizardCategory wizardCategories, final TreeViewer viewer) {
        String selectedId = getDialogSettings().get(setting);
        if (selectedId == null)
            return;

        if (wizardCategories == null)
            return;

        Object selected = wizardCategories.findCategory(new Path(
                selectedId));

        if (selected == null) {
            selected = wizardCategories.findWizard(selectedId);

            if (selected == null)
                // if we cant find either a category or a wizard, abort.
                return;
        }

        viewer.setSelection(new StructuredSelection(selected), true);
    }

    /**
     * Since Finish was pressed, write widget values to the dialog store so
     * that they will persist into the next invocation of this wizard page
     */
    public void saveWidgetValues() {
    	// store which tab is selected as well as its expanded categories and selected wizard
    	int selected = tabFolder.getSelectionIndex();
        if (selected == 1){
            getDialogSettings().put(IMPORT_EXPORT_SELECTION,
            		EXPORT_SELECTION);
        	storeExpandedCategories(STORE_EXPANDED_EXPORT_CATEGORIES, exportList.getViewer());
            storeSelectedCategoryAndWizard(STORE_SELECTED_EXPORT_WIZARD_ID, exportList.getViewer()); 
        }
        else{
            getDialogSettings().put(IMPORT_EXPORT_SELECTION,
                    IMPORT_SELECTION);
        	storeExpandedCategories(STORE_EXPANDED_IMPORT_CATEGORIES, importList.getViewer());
            storeSelectedCategoryAndWizard(STORE_SELECTED_IMPORT_WIZARD_ID, importList.getViewer());   
        }
    }
 
    /**
     * Stores the collection of currently-expanded categories in this page's
     * dialog store, in order to recreate this page's state in the next
     * instance of this page.
     */
    protected void storeExpandedCategories(String setting, TreeViewer viewer) {
        Object[] expandedElements = viewer.getExpandedElements();
        List expandedElementPaths = new ArrayList(expandedElements.length);
        for (int i = 0; i < expandedElements.length; ++i) {
            if (expandedElements[i] instanceof IWizardCategory)
                expandedElementPaths
                        .add(((IWizardCategory) expandedElements[i])
                                .getPath().toString());
        }
        getDialogSettings().put(setting,
                (String[]) expandedElementPaths
                        .toArray(new String[expandedElementPaths.size()]));
    }

    /**
     * Stores the currently-selected element in this page's dialog store, in
     * order to recreate this page's state in the next instance of this page.
     */
    protected void storeSelectedCategoryAndWizard(String setting, TreeViewer viewer) {
        Object selected = ((IStructuredSelection) viewer
                .getSelection()).getFirstElement();

        if (selected != null) {
            if (selected instanceof IWizardCategory)
                getDialogSettings().put(setting,
                        ((IWizardCategory) selected).getPath()
                                .toString());
            else
                // else its a wizard
            	getDialogSettings().put(setting,
                        ((IWizardDescriptor) selected).getId());
        }
    }
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
     */
    public IWizardPage getNextPage() { 
    	int selected = tabFolder.getSelectionIndex();
    	ITriggerPoint triggerPoint = null;
    	if (selected == EXPORT_SELECTION){
    		triggerPoint = getWorkbench().getActivitySupport()
            .getTriggerPointManager().getTriggerPoint(WorkbenchTriggerPoints.EXPORT_WIZARDS);
    	}
    	else if (selected == IMPORT_SELECTION){
	    	triggerPoint = getWorkbench().getActivitySupport()
	        .getTriggerPointManager().getTriggerPoint(WorkbenchTriggerPoints.IMPORT_WIZARDS);
    	}
    	else
    		return null;
        
        if (triggerPoint == null || WorkbenchActivityHelper.allowUseOf(triggerPoint, getSelectedNode()))
            return super.getNextPage();
        return null;
    }
    
    /**
     * Set the initial page to import or export according to the given id.
     * 
     * @param pageId
     */
    public void setInitialPage(String pageId){
    	initialPageId = pageId;
    }
}
