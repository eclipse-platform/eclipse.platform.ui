package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * The import/export wizard allows users to choose whether to 
 * show the import wizard or the export wizard.
 * 
 * @since 3.2
 *
 */
public class ImportExportWizard extends Wizard {
	/**
	 * Constant used to to specify to the import/export wizard
	 * which page should initially be shown. 
	 */
	public static final String IMPORT = "import";	//$NON-NLS-1$
	/**
	 * Constant used to to specify to the import/export wizard
	 * which page should initially be shown. 
	 */
	public static final String EXPORT = "export";	//$NON-NLS-1$
		
    private IWorkbench workbench;
    private IStructuredSelection selection;
    private ImportExportPage importExportPage;
    private String initialPage = null;
    
    /**
     * Create an import/export wizard, show the tab that was  
     * selected the last time Finish was pressed.
     *
     */
    public ImportExportWizard(){
    	
    }
    
    /**
     * Create an import/export wizard and show the page 
     * with the given id.
     * 
     * @param pageId
     */
    public ImportExportWizard(String pageId){
    	initialPage = pageId;
    }
    
    /**
     * Subclasses must implement this <code>IWizard</code> method 
     * to perform any special finish processing for their wizard.
     */
    public boolean performFinish() {
    	importExportPage.saveWidgetValues();
        return true;
    }

    /**
     * Creates the wizard's pages lazily.
     */
    public void addPages() {
    	importExportPage = new ImportExportPage(this.workbench, this.selection);
    	if (initialPage != null)
    		importExportPage.setInitialPage(initialPage);
        addPage(importExportPage);
    }

    /**
     * Initializes the wizard.
     * 
     * @param aWorkbench the workbench
     * @param currentSelection the current selectio
     */
    public void init(IWorkbench aWorkbench,
            IStructuredSelection currentSelection) {
        this.workbench = aWorkbench;
        this.selection = currentSelection;

        setWindowTitle(WorkbenchMessages.ImportExportWizard_title); 
        // TODO get new descriptor for import/export
        setDefaultPageImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_WIZ));
        setNeedsProgressMonitor(true);
    }
}
