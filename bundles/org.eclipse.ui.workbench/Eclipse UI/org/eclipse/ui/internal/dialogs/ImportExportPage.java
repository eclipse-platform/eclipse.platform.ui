package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.activities.ws.WorkbenchTriggerPoints;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerSorter;
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
    private static final int IMPORT_SELECTION = 0;
    private static final int EXPORT_SELECTION = 1;
    
	private TabFolder tabFolder;
	private ImportExportListViewer exportList;
	private ImportExportListViewer importList;
	
	private AdaptableList importWizardsList;
	private AdaptableList exportWizardsList;
	
	private String initialPageId = null;
	
	/*
	 * Class to create a viewer that shows a list of wizard types.
	 */
	protected class ImportExportListViewer {
		private final static int SIZING_LISTS_HEIGHT = 200;
		
		private AdaptableList elements;
		private String message;
		private TableViewer viewer;

		protected ImportExportListViewer(AdaptableList elements, String msg){
			this.elements = elements;
			this.message = msg;
		}
		
		protected Composite createViewer(TabFolder folder){
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

	        createTableViewer(outerContainer);
	        layoutTopControl(viewer.getControl());

	        return outerContainer;
		}
		
		private void createTableViewer(Composite parent){        
			//Create a table for the list
	        Table table = new Table(parent, SWT.BORDER);
	        table.setFont(parent.getFont());

	        // the list viewer
	        viewer = new TableViewer(table);
	        viewer.setContentProvider(new WizardContentProvider());
	        viewer.setLabelProvider(new WorkbenchLabelProvider());
	        viewer.setSorter(new WorkbenchViewerSorter());
	        viewer.setInput(elements);
		}

		protected TableViewer getViewer(){
			return viewer;
		}

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
    
    private Composite createImportTab(){
    	importList = new ImportExportListViewer(
    			getAvailableImportWizards(), WorkbenchMessages.ImportWizard_selectSource);
    	Composite importComp = importList.createViewer(tabFolder);
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
            setMessage(null);
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
    	TableViewer viewer = null;
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
            WorkbenchWizardElement currentWizardSelection = (WorkbenchWizardElement) ss
                    .getFirstElement();
            if (currentWizardSelection == null){
            	setMessage(noSelectionMsg);            	
            }
            else {
            	updateSelectedNode(currentWizardSelection);
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
        WorkbenchWizardElement currentWizardSelection = (WorkbenchWizardElement) ss
                .getFirstElement();
        updateSelectedNode(currentWizardSelection);  	
    }
    
    private Composite createExportTab(){
    	exportList = new ImportExportListViewer(
    			getAvailableExportWizards(), WorkbenchMessages.ExportWizard_selectDestination);
    	Composite exportComp = exportList.createViewer(tabFolder);
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
    
    /**
     * Returns the export wizards that are available for invocation.
     */
    protected AdaptableList getAvailableExportWizards() {
    	if (exportWizardsList == null){
	       	// TODO: exports are still flat - we need to get at the flat list. All
			// wizards will be in the "other" category.
			IWizardCategory root = WorkbenchPlugin.getDefault()
					.getExportWizardRegistry().getRootCategory();
			WizardCollectionElement otherCategory = (WizardCollectionElement) root
					.findCategory(new Path(
							WizardsRegistryReader.UNCATEGORIZED_WIZARD_CATEGORY));
			if (otherCategory == null)
				return new AdaptableList();
			exportWizardsList = otherCategory.getWizardAdaptableList();    
    	}
    	return exportWizardsList;
	}
    
    /**
     * Returns the import wizards that are available for invocation.
     */
    protected AdaptableList getAvailableImportWizards() {
    	if (importWizardsList == null){
	       	// TODO: imports are still flat - we need to get at the flat list. All
			// wizards will be in the "other" category.
			IWizardCategory root = WorkbenchPlugin.getDefault()
					.getImportWizardRegistry().getRootCategory();
			WizardCollectionElement otherCategory = (WizardCollectionElement) root
					.findCategory(new Path(
							WizardsRegistryReader.UNCATEGORIZED_WIZARD_CATEGORY));
			if (otherCategory == null)
				return new AdaptableList();
			importWizardsList = otherCategory.getWizardAdaptableList();
    	}
    	return importWizardsList;
    }
    
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
        IDialogSettings settings = getDialogSettings();
        if (settings == null)
            return;
        
        try{
        	// restore last selections for each tab
	        String importWizardId = settings.get(STORE_SELECTED_IMPORT_WIZARD_ID);
	        setWizardElements(IMPORT_SELECTION);
	        if (wizardElements != null && importWizardId != null){
		        WorkbenchWizardElement wizard = findWizard(importWizardId);
		        if (wizard == null)
		            return;

		        StructuredSelection selection = new StructuredSelection(wizard);
		        importList.getViewer().setSelection(selection);
	        }
	        
	        String exportWizardId = settings.get(STORE_SELECTED_EXPORT_WIZARD_ID);
	        setWizardElements(EXPORT_SELECTION);
	        if (wizardElements != null && importWizardId != null){
		        WorkbenchWizardElement wizard = findWizard(exportWizardId);
		        if (wizard == null)
		            return;

		        StructuredSelection selection = new StructuredSelection(wizard);
		        exportList.getViewer().setSelection(selection);
	        }
	        
        	// restore last selected tab or set to desired page (if provided)
        	if (initialPageId == null){
        		// initial page not specified, use settings 
				final int selectedTab = settings.getInt(IMPORT_EXPORT_SELECTION);
				if ((tabFolder.getItemCount() > selectedTab) && (selectedTab > 0)) {
					tabFolder.setSelection(selectedTab);
				}
				else{	// default behavior - show first tab
					updateMessage(0);
				}
        	}
        	else{
        		if (initialPageId == ImportExportWizard.IMPORT)
        			tabFolder.setSelection(IMPORT_SELECTION);
        		else if (initialPageId == ImportExportWizard.EXPORT)
        			tabFolder.setSelection(EXPORT_SELECTION);
        	}	        
        }
        catch (NumberFormatException e){
        }
    }

    protected void setWizardElements(int selectedTab){
    	if (selectedTab == EXPORT_SELECTION)
    		wizardElements = getAvailableExportWizards();
    	else if (selectedTab == IMPORT_SELECTION)
    		wizardElements = getAvailableImportWizards();
    }
    
    /**
     * Since Finish was pressed, write widget values to the dialog store so
     * that they will persist into the next invocation of this wizard page
     */
    public void saveWidgetValues() {
    	int selected = tabFolder.getSelectionIndex();
    	String wizardIDToUpdate;
        if (selected == 1){
            getDialogSettings().put(IMPORT_EXPORT_SELECTION,
            		EXPORT_SELECTION);
            wizardIDToUpdate = STORE_SELECTED_EXPORT_WIZARD_ID;
        }
        else{
            getDialogSettings().put(IMPORT_EXPORT_SELECTION,
                    IMPORT_SELECTION);
            wizardIDToUpdate = STORE_SELECTED_IMPORT_WIZARD_ID;
        }
        TableViewer viewer = 
        	selected == EXPORT_SELECTION ? exportList.getViewer() : importList.getViewer();
        IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
        if (sel.size() > 0) {
            WorkbenchWizardElement selectedWizard = (WorkbenchWizardElement) sel
                    .getFirstElement();
            
            getDialogSettings().put(wizardIDToUpdate, selectedWizard.getId());
        }
    }
    
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
