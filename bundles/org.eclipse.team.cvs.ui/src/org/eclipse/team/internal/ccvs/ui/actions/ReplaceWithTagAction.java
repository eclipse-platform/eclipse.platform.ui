package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Action for replace with tag.
 */
public class ReplaceWithTagAction extends ReplaceWithAction {
	/*
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		
		// Setup the holders
		final IResource[] resource = new IResource[] {null};
		final CVSTag[] tag = new CVSTag[] {null};
		
		// Show a busy cursor while display the tag selection dialog
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				IResource[] resources = getSelectedResources();
				if (resources.length != 1) return;
				resource[0] = resources[0];
	
				if (isDirty(resource[0])) {
					final Shell shell = getShell();
					final boolean[] result = new boolean[] { false };
					shell.getDisplay().syncExec(new Runnable() {
						public void run() {
							result[0] = MessageDialog.openQuestion(getShell(), Policy.bind("question"), Policy.bind("localChanges"));
						}
					});
					if (!result[0]) return;
				}
				
				TagSelectionDialog dialog = new TagSelectionDialog(getShell(), resource[0]);
				dialog.setBlockOnOpen(true);
				if (dialog.open() == Dialog.CANCEL) {
					return;
				}
				tag[0] = dialog.getResult();
			}
		}, Policy.bind("ReplaceWithTagAction.replace"), this.PROGRESS_BUSYCURSOR);			
		
		if (tag[0] == null) return;
		
		// Display a progress dialog while replacing
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(resource[0].getProject(), CVSProviderPlugin.getTypeId());
					monitor.beginTask(null, 100);
					monitor.setTaskName(Policy.bind("ReplaceWithTagAction.replacing", tag[0].getName()));
					provider.get(resource, IResource.DEPTH_INFINITE, tag[0], Policy.subMonitorFor(monitor, 100));
					monitor.done();
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("ReplaceWithTagAction.replace"), this.PROGRESS_DIALOG);
	}
	
	protected boolean isEnabled() {
		return getSelectedResources().length == 1;
	}
}
