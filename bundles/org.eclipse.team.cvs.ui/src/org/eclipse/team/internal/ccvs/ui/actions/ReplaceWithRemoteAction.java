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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.ITeamManager;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class ReplaceWithRemoteAction extends ReplaceWithAction {
	public void run(IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					// Check if any resource is dirty.
					IResource[] resources = getSelectedResources();
					for (int i = 0; i < resources.length; i++) {
						IResource resource = resources[i];
						CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(resource.getProject());
						if (isDirty(resource)) {
							// Warn the user they have local changes that will be overwritten
							final Shell shell = getShell();
							final boolean[] result = new boolean[] { false };
							shell.getDisplay().syncExec(new Runnable() {
								public void run() {
									result[0] = MessageDialog.openQuestion(getShell(), Policy.bind("question"), Policy.bind("localChanges"));
								}
							});
							if (!result[0]) return;
						}						
					}
					// Do the replace
					Hashtable table = getProviderMapping();
					Set keySet = table.keySet();
					monitor.beginTask("", keySet.size() * 1000);
					monitor.setTaskName(Policy.bind("ReplaceWithRemoteAction.replacing"));
					Iterator iterator = keySet.iterator();
					while (iterator.hasNext()) {
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						ITeamProvider provider = (ITeamProvider)iterator.next();
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
		}, Policy.bind("ReplaceWithRemoteAction.problemMessage"), PROGRESS_DIALOG);
	}
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		if (resources.length == 0) return false;
		ITeamManager manager = TeamPlugin.getManager();
		for (int i = 0; i < resources.length; i++) {
			ITeamProvider provider = manager.getProvider(resources[i].getProject());
			if (provider == null) return false;
			if (!provider.hasRemote(resources[i])) return false;
		}
		return true;
	}
}
