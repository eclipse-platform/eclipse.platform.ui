package org.eclipse.ui.wizards.newresource;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import java.net.*;

/**
 * Standard workbench wizard that create a new file resource in the workspace.
 * <p>
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * INewWizard wizard = new BasicNewFileResourceWizard();
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
	mainPage = new WizardNewFileCreationPage("newFilePage1",  getSelection());
	mainPage.setTitle("File");
	mainPage.setDescription("Create a new file resource.");
	addPage(mainPage);
}
/* (non-Javadoc)
 * Method declared on IWorkbenchWizard.
 */
public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
	super.init(workbench, currentSelection);
	setWindowTitle("New File");
	setNeedsProgressMonitor(true);
}
/* (non-Javadoc)
 * Method declared on BasicNewResourceWizard.
 */
protected void initializeDefaultPageImageDescriptor() {
	String iconPath;
	if(Display.getCurrent().getIconDepth() > 4)
		iconPath = "icons/full/";
	else
		iconPath = "icons/basic/";
		
	try {
		URL installURL = Platform.getPlugin(PlatformUI.PLUGIN_ID).getDescriptor().getInstallURL();
		URL url = new URL(installURL, iconPath + "wizban/newfile_wiz.gif");
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
		IWorkbenchPage page = dw.getActivePage();
		if (page != null)
			page.openEditor(file);
	} catch (PartInitException e) {
		DialogUtil.openError(
			dw.getShell(),
			"Problems Opening Editor",
			e.getMessage(),
			e);
	}
			
	return true;
}
}
