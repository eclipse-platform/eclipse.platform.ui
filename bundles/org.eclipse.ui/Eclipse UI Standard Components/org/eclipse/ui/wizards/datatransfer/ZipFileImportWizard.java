package org.eclipse.ui.wizards.datatransfer;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Standard workbench wizard for importing resources from a zip file
 * into the workspace.
 * <p>
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * IWizard wizard = new ZipFileImportWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, the user-selected zip file is imported
 * into the workspace, the dialog closes, and the call to <code>open</code>
 * returns.
 * </p>
 */
public class ZipFileImportWizard extends Wizard implements IImportWizard {
	private IWorkbench workbench;
	private IStructuredSelection selection;
	private WizardZipFileResourceImportPage1 mainPage;
/**
 * Creates a wizard for importing resources into the workspace from
 * a zip file.
 */
public ZipFileImportWizard() {
	AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
	IDialogSettings workbenchSettings = plugin.getDialogSettings();
	IDialogSettings section = workbenchSettings.getSection("ZipFileImportWizard");
	if(section == null)
		section = workbenchSettings.addNewSection("ZipFileImportWizard");
	setDialogSettings(section);
}
/* (non-Javadoc)
 * Method declared on IWizard.
 */
public void addPages() {
	super.addPages();
	mainPage = new WizardZipFileResourceImportPage1(workbench,selection);
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

	setWindowTitle("Import");
	setDefaultPageImageDescriptor(getImageDescriptor("wizban/importzip_wiz.gif"));
	setNeedsProgressMonitor(true);
}
/* (non-Javadoc)
 * Method declared on IWizard.
 */
public boolean performCancel() {
	return mainPage.cancel();
}
/* (non-Javadoc)
 * Method declared on IWizard.
 */
public boolean performFinish() {
	return mainPage.finish();
}
}
