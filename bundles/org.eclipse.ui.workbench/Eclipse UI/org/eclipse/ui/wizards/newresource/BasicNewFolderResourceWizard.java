package org.eclipse.ui.wizards.newresource;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFolderMainPage;
import org.eclipse.ui.dialogs.WizardNewLinkPage;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Standard workbench wizard that create a new folder resource in the workspace.
 * <p>
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * IWorkbenchWizard wizard = new BasicNewFolderResourceWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, a folder resource at the user-specified
 * workspace path is created, the dialog closes, and the call to
 * <code>open</code> returns.
 * </p>
 */
public class BasicNewFolderResourceWizard extends BasicNewResourceWizard {
	private WizardNewFolderMainPage mainPage;
	private WizardNewLinkPage linkPage;	
/**
 * Creates a wizard for creating a new folder resource in the workspace.
 */
public BasicNewFolderResourceWizard() {
	super();
}
/* (non-Javadoc)
 * Method declared on IWizard.
 */
public void addPages() {
	super.addPages();
	mainPage = new WizardNewFolderMainPage(ResourceMessages.getString("NewFolder.text"), getSelection()); //$NON-NLS-1$
	addPage(mainPage);

	linkPage = new WizardNewLinkPage("newLinkPage", IResource.FOLDER);	//$NON-NLS-1$
	linkPage.setTitle(ResourceMessages.getString("NewLink.pageTitle")); //$NON-NLS-1$
	linkPage.setDescription(ResourceMessages.getString("NewLink.description")); //$NON-NLS-1$
	addPage(linkPage);
}
/**
 * Returns the link creation page if the selected container is a 
 * valid link parent.
 * Sets the currently selected container in the link creation page.
 * 
 * @see org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
 */
public IWizardPage getNextPage(IWizardPage page) {
	if (page == mainPage) {
		IPath fullPath = mainPage.getContainerFullPath();
		IWorkspaceRoot workspaceRoot = WorkbenchPlugin.getPluginWorkspace().getRoot();
		IResource resource = workspaceRoot.findMember(fullPath);
		
		if (resource != null && resource instanceof IProject) {
			linkPage.setContainer((IProject) resource);
			return linkPage;
		}
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on IWorkbenchWizard.
 */
public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
	super.init(workbench, currentSelection);
	setWindowTitle(ResourceMessages.getString("NewFolder.title")); //$NON-NLS-1$
	setNeedsProgressMonitor(true);
}
/* (non-Javadoc)
 * Method declared on BasicNewResourceWizard.
 */
protected void initializeDefaultPageImageDescriptor() {
	String iconPath = "icons/full/";//$NON-NLS-1$
	try {
		URL installURL = Platform.getPlugin(PlatformUI.PLUGIN_ID).getDescriptor().getInstallURL();
		URL url = new URL(installURL, iconPath + "wizban/newfolder_wiz.gif");//$NON-NLS-1$
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
	String linkTarget = linkPage.getLinkTarget();
	mainPage.setLinkTarget(linkTarget);

	IFolder folder = mainPage.createNewFolder();
	if (folder == null)
		return false;

	selectAndReveal(folder);	
	
	return true;
}
}
