package org.eclipse.ui.wizards.datatransfer;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.swt.widgets.Display;


/**
 * Standard workbench wizard for exporting resources from the workspace
 * to the local file system.
 * <p>
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * IWizard wizard = new FileSystemExportWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, the user-selected workspace resources 
 * are exported to the user-specified location in the local file system,
 * the dialog closes, and the call to <code>open</code> returns.
 * </p>
 */
public class FileSystemExportWizard extends Wizard implements IExportWizard {
	private IWorkbench workbench;
	private IStructuredSelection selection;
	private WizardFileSystemResourceExportPage1 mainPage;
/**
 * Creates a wizard for exporting workspace resources to the local file system.
 */
public FileSystemExportWizard() {
	AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
	IDialogSettings workbenchSettings = plugin.getDialogSettings();
	IDialogSettings section = workbenchSettings.getSection("FileSystemExportWizard");
	if(section == null)
		section = workbenchSettings.addNewSection("FileSystemExportWizard");
	setDialogSettings(section);
}
/* (non-Javadoc)
 * Method declared on IWizard.
 */
public void addPages() {
	super.addPages();
	mainPage = new WizardFileSystemResourceExportPage1(selection);
	addPage(mainPage);
}
/**
 * Returns the image descriptor with the given relative path.
 */
private ImageDescriptor getImageDescriptor(String relativePath) {
	String iconPath;
	if(Display.getCurrent().getIconDepth() > 4)
		iconPath = "icons/full/";
	else
		iconPath = "icons/basic/";
		
	try {
		AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
		URL installURL = plugin.getDescriptor().getInstallURL();
		URL url = new URL(installURL, iconPath + relativePath);
		return ImageDescriptor.createFromURL(url);
	}
	catch (MalformedURLException e) {
		// Should not happen
		return null;
	}
}
/* (non-Javadoc)
 * Method declared on IWorkbenchWizard.
 */
public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
	this.workbench = workbench;
	selection = currentSelection;

	setWindowTitle("Export");
	setDefaultPageImageDescriptor(getImageDescriptor("wizban/exportdir_wiz.gif"));
	setNeedsProgressMonitor(true);
}
/* (non-Javadoc)
 * Method declared on IWizard.
 */
public boolean performFinish() {
	return mainPage.finish();
}
}
