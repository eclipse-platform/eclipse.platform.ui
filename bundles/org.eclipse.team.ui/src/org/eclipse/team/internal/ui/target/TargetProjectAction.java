package org.eclipse.team.internal.ui.target;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.ui.actions.TeamAction;

public class TargetProjectAction extends TeamAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					IProject project = getSelectedProjects()[0];
					ConfigureTargetWizard wizard = new ConfigureTargetWizard();
					wizard.init(null, project);
					WizardDialog dialog = new WizardDialog(getShell(), wizard);
					dialog.open();
				} catch (Exception e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("ConfigureTargetAction.configureProject"), PROGRESS_BUSYCURSOR); //$NON-NLS-1$
	}
	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		IProject[] selectedProjects = getSelectedProjects();
		if (selectedProjects.length != 1) return false;
		if (!selectedProjects[0].isAccessible()) return false;
/*		try {
			if (TargetManager.getProvider(selectedProjects[0]) == null) return true;
		} catch (TeamException e) {
			TeamPlugin.log(IStatus.ERROR, "Exception getting provider", e);
			return false;
		}
		return false;
*/
		return true;
	}


}
