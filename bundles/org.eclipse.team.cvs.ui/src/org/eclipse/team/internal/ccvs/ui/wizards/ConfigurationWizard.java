package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * This wizard helps the user to import a new project in their workspace
 * into a CVS repository for the first time.
 */
public class ConfigurationWizard extends ConnectionWizard implements IConfigurationWizard {
	// The project to configure
	private IProject project;
	
	/**
	 * ConfigurationWizard constructor
	 */
	public ConfigurationWizard() {
		IDialogSettings workbenchSettings = CVSUIPlugin.getPlugin().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("CVSWizard");//$NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("CVSWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
	}
	protected String getMainPageDescription() {
		return Policy.bind("ConfigurationWizard.description");
	}
	protected String getMainPageTitle() {
		return Policy.bind("ConfigurationWizard.title");
	}
	protected int getStyle() {
		return ConfigurationWizardMainPage.CONNECTION_METHOD |
			ConfigurationWizardMainPage.USER |
			ConfigurationWizardMainPage.PASSWORD |
			ConfigurationWizardMainPage.PORT |
			ConfigurationWizardMainPage.HOST |
			ConfigurationWizardMainPage.REPOSITORY_PATH |
			ConfigurationWizardMainPage.MODULE_RADIO;
	}
	/*
	 * @see IWizard#performFinish
	 */
	public boolean performFinish() {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					getMainPage().getControl().getDisplay().syncExec(new Runnable() {
						public void run() {
							getMainPage().finish(new NullProgressMonitor());
						}
					});
					// Get the result of the wizard page
					Properties properties = getProperties();
					
					if (properties.getProperty("module") == null) {
						properties.setProperty("module", project.getName());
					}
					CVSProviderPlugin.getProvider().importAndCheckout(project, properties, monitor);
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}		
			}
		}, Policy.bind("ConfigurationWizard.import"));
		return true;
	}
	/*
	 * @see IConfigurationWizard#init(IWorkbench, IProject)
	 */
	public void init(IWorkbench workbench, IProject project) {
		this.project = project;
	}
}
