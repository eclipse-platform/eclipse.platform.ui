package org.eclipse.ui.wizards.newresource;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.internal.dialogs.DialogUtil;

/**
 * Standard workbench wizard that create a new file resource in the workspace.
 * <p>
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * IWorkbenchWizard wizard = new BasicNewFileResourceWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, a file resource at the user-specified
 * workspace path is created, the dialog closes, and the call to
 * <code>open</code> returns.
 * </p>
 */
public class BasicNewFileResourceWizard extends BasicNewResourceWizard {
	private WizardNewFileCreationPage mainPage;
/**
 * Creates a wizard for creating a new file resource in the workspace.
 */
public BasicNewFileResourceWizard() {
	super();
}
/* (non-Javadoc)
 * Method declared on IWizard.
 */
public void addPages() {
	super.addPages();
	mainPage = new WizardNewFileCreationPage("newFilePage1",  getSelection());//$NON-NLS-1$
	mainPage.setTitle(ResourceMessages.getString("FileResource.pageTitle")); //$NON-NLS-1$
	mainPage.setDescription(ResourceMessages.getString("FileResource.description")); //$NON-NLS-1$
	addPage(mainPage);
}
/* (non-Javadoc)
 * Method declared on IWorkbenchWizard.
 */
public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
	super.init(workbench, currentSelection);
	setWindowTitle(ResourceMessages.getString("FileResource.shellTitle")); //$NON-NLS-1$
	setNeedsProgressMonitor(true);
}
/* (non-Javadoc)
 * Method declared on BasicNewResourceWizard.
 */
protected void initializeDefaultPageImageDescriptor() {
	String iconPath = "icons/full/";//$NON-NLS-1$
	try {
		URL installURL = Platform.getPlugin(PlatformUI.PLUGIN_ID).getDescriptor().getInstallURL();
		URL url = new URL(installURL, iconPath + "wizban/newfile_wiz.gif");//$NON-NLS-1$
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		setDefaultPageImageDescriptor(desc);
	}
	catch (MalformedURLException e) {
		// Should not happen.  Ignore.
	}
}
/* (non-Javadoc)
 * Method declared on IWizard.
 */
public boolean performFinish() {
	IFile file = mainPage.createNewFile();
	if (file == null)
		return false;

	selectAndReveal(file);

	// Open editor on new file.
	IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
	try {
		if (dw != null) {
			IWorkbenchPage page = dw.getActivePage();
			if (page != null)
				page.openEditor(file);
		}
	} catch (PartInitException e) {
		DialogUtil.openError(
			dw.getShell(),
			ResourceMessages.getString("FileResource.errorMessage"), //$NON-NLS-1$
			e.getMessage(),
			e);
	}
			
	return true;
}
}
