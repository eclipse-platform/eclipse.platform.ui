package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * This wizard helps the user to check out an existing project from
 * an existing CVS repository location, and places it in the workspace
 * with the provider set.
 */
public class CheckoutWizard extends ConnectionWizard {

	/**
	 * CheckoutWizard constructor
	 */
	public CheckoutWizard() {
		IDialogSettings workbenchSettings = CVSUIPlugin.getPlugin().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("CVSWizard");//$NON-NLS-1$
		if (section == null) {
			section = workbenchSettings.addNewSection("CVSWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
	}
	protected String getMainPageDescription() {
		return Policy.bind("CheckoutWizard.description");
	}
	protected String getMainPageTitle() {
		return Policy.bind("CheckoutWizard.title");
	}
	protected int getStyle() {
		return ConfigurationWizardMainPage.CONNECTION_METHOD |
			ConfigurationWizardMainPage.USER |
			ConfigurationWizardMainPage.PASSWORD |
			ConfigurationWizardMainPage.PORT |
			ConfigurationWizardMainPage.HOST |
			ConfigurationWizardMainPage.REPOSITORY_PATH |
			ConfigurationWizardMainPage.MODULE_TEXT |
			ConfigurationWizardMainPage.PROJECT_NAME |
			ConfigurationWizardMainPage.TAG;
	}
	/*
	 * @see IWizard#performFinish
	 */
	public boolean performFinish() {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					// Get the values. Run in UI thread.
					getMainPage().getControl().getDisplay().syncExec(new Runnable() {
						public void run() {
							getMainPage().finish(new NullProgressMonitor());
						}
					});
					Properties properties = getProperties();
					// Prepare the location
					String projectName = properties.getProperty("project");
					if (projectName == null) {
						projectName = properties.getProperty("module");
					}
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
					if (project.exists()) {
						// Make sure the user understands they will overwrite the project.
					}
					CVSTeamProvider.checkout(project, properties, monitor);
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("CheckoutWizard.checkout"));
		return true;	
	}
}

