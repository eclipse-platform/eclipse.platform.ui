package org.eclipse.ant.internal.ui.datatransfer;

import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Exports currently selected Eclipse project.
 */
public class AntBuildfileExportWizard extends Wizard implements IExportWizard
{
    private ExportPage mainPage;
    private IResource selectedResource;

    /**
     * Creates buildfile.
     */
    public boolean performFinish()
    {
        Eclipse2AntAction.convert(selectedResource);
        return true;
    }
 
    public void addPages()
    {
        super.addPages();
        mainPage = new ExportPage();
        addPage(mainPage);
    }

    public void init(IWorkbench workbench, IStructuredSelection selection)
    {
    	try
    	{
    		setDefaultPageImageDescriptor(AntUIImages.getImageDescriptor(IAntUIConstants.IMG_IMPORT_WIZARD_BANNER));
    	}
    	catch (Throwable t)
    	{
    		// // not compatible with Eclipse 3.1M3
    	}
        selectedResource = Eclipse2AntAction.getResource(selection);
    }

    /**
     * Minimalistic export page. Offers only finish button.
     */
    private static class ExportPage extends WizardPage
    {
        protected ExportPage()
        {
            super("AntBuildfileExportWizardPage");
            setPageComplete(false);
            setTitle("Export Ant Buildfile");
            setDescription("Creates an Ant Buildfile for currently selected Eclipse project.");
        }

        public void createControl(Composite parent)
        {
            initializeDialogUnits(parent);
            Composite composite = new Composite(parent, SWT.NULL);
            composite.setFont(parent.getFont());
            setErrorMessage(null);
            setMessage(null);
            setControl(composite);
            setPageComplete(true);
        }        
    }
}
