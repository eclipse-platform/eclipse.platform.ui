package org.eclipse.ui.wizards.datatransfer;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Standard workbench wizard for importing projects defined
 * outside of the currently defined projects into Eclipse.
 * <p>
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * IWizard wizard = new ExternalProjectImportWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, a project is created with the location
 * specified by the user.
 * </p>
 */

public class ExternalProjectImportWizard
	extends Wizard
	implements IImportWizard {
	private WizardExternalProjectImportPage mainPage;
	private IWorkbench workbench;

	/**
	 * Constructor for ExternalProjectImportWizard.
	 */
	public ExternalProjectImportWizard() {
		super();
	}

	/* (non-Javadoc)
	* Method declared on IWizard.
	*/
	public void addPages() {
		super.addPages();
		mainPage = new WizardExternalProjectImportPage();
		addPage(mainPage);
	}
	/**
	 * Returns the image descriptor with the given relative path.
	 */
	private ImageDescriptor getImageDescriptor(String relativePath) {
		String iconPath = "icons/full/"; //$NON-NLS-1$	
		try {
			AbstractUIPlugin plugin =
				(AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
			URL installURL = plugin.getDescriptor().getInstallURL();
			URL url = new URL(installURL, iconPath + relativePath);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			// Should not happen
			return null;
		}
	}
	/* (non-Javadoc)
	 * Method declared on IWorkbenchWizard.
	 */
	public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		this.workbench = workbench;
		setWindowTitle(DataTransferMessages.getString("DataTransfer.importTitle")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(getImageDescriptor("wizban/importdir_wiz.gif")); //$NON-NLS-1$
		
	}
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performCancel() {
		return true;
	}
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		return (mainPage.createExistingProject() != null);
	}

}