package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.ui.IPromptCondition;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.PromptingDialog;
import org.eclipse.team.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class ReplaceWithRemoteAction extends TeamAction {
	public void run(IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					PromptingDialog dialog = new PromptingDialog(getShell(), getSelectedResources(), 
						getPromptCondition(), Policy.bind("ReplaceWithAction.confirmOverwrite"));
					IResource[] resources = dialog.promptForMultiple();
					if(resources.length == 0) {
						// nothing to do
						return;
					}
					// Do the replace
					Hashtable table = getProviderMapping(resources);
					Set keySet = table.keySet();
					monitor.beginTask("", keySet.size() * 1000); //$NON-NLS-1$
					monitor.setTaskName(Policy.bind("ReplaceWithRemoteAction.replacing")); //$NON-NLS-1$
					Iterator iterator = keySet.iterator();
					while (iterator.hasNext()) {
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
						List list = (List)table.get(provider);
						IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
						provider.get(providerResources, IResource.DEPTH_INFINITE, subMonitor);
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, Policy.bind("ReplaceWithRemoteAction.problemMessage"), PROGRESS_DIALOG); //$NON-NLS-1$
	}
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(resources[i].getProject(), CVSProviderPlugin.getTypeId());
			if (provider == null) return false;
			if (!provider.hasRemote(resources[i])) return false;
		}
		return true;
	}
	/**
	 * Note: This method is designed to be overridden by test cases.
	 */
	protected IPromptCondition getPromptCondition() {
		return PromptingDialog.getOverwriteLocalChangesPrompt();
	}
}
