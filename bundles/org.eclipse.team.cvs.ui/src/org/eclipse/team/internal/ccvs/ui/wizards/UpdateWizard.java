package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class UpdateWizard extends Wizard {

	UpdateWizardPage updatePage;
	IProject project;
	
	public UpdateWizard() {
		setNeedsProgressMonitor(true);
		setWindowTitle(Policy.bind("UpdateWizard.title"));
	}
	
	public void addPages() {
		// Provide a progress monitor to indicate what is going on
		try {
			new ProgressMonitorDialog(getShell()).run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask(null, 100);
					updatePage = new UpdateWizardPage("updatePage", Policy.bind("UpdateWizard.updatePage"), CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_WIZBAN_SHARE));
					updatePage.setProject(project);
					addPage(updatePage);
					monitor.done();
				}
			});
		} catch (InvocationTargetException e) {
			CVSUIPlugin.log(new Status(IStatus.ERROR, CVSUIPlugin.ID, 0, Policy.bind("internal"), e.getTargetException()));
		} catch (InterruptedException e) {
			// Ignore
		}
	}
	
	/*
	 * @see IWizard#performFinish()
	 */
	public boolean performFinish() {
		final boolean[] result = new boolean[] {false};
		try {
			getContainer().run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						CVSTeamProvider provider = ((CVSTeamProvider)RepositoryProvider.getProvider(project));	
						provider.update(new IResource[] { project }, updatePage.getLocalOptions(),
							updatePage.getTag(), true /*createBackups*/, monitor);
						result[0] = true;
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InterruptedException e) {
			return true;
		} catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof CVSException) {
				ErrorDialog.openError(getShell(), null, null, ((CVSException)target).getStatus());
				return false;
			}
			if (target instanceof RuntimeException) {
				throw (RuntimeException)target;
			}
			if (target instanceof Error) {
				throw (Error)target;
			}
		}
		return result[0];
	}

	public void setProject(IProject project) {
		this.project = project;
	}
}
