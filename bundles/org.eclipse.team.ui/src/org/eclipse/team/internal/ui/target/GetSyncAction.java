/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ui.target;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.sync.ITeamNode;
import org.eclipse.team.internal.ui.sync.SyncSet;
import org.eclipse.team.internal.ui.sync.SyncView;

public class GetSyncAction extends TargetSyncAction {

	public GetSyncAction(TargetSyncCompareInput model, ISelectionProvider sp, String label, Shell shell) {
		super(model, sp, label, shell);
	}

	/**
	 * @see TargetSyncAction#isEnabled(ITeamNode)
	 */
	protected boolean isEnabled(ITeamNode node) {
		// Get action is enabled for any changed nodes.
		return new SyncSet(new StructuredSelection(node)).getChangedNodes().length > 0;
	}

	/**
	 * @see TargetSyncAction#removeNonApplicableNodes(SyncSet, int)
	 */
	protected void removeNonApplicableNodes(SyncSet set, int syncMode) {
		if (syncMode == SyncView.SYNC_INCOMING) {
			set.removeOutgoingNodes();
		}
	}

	/**
	 * @see TargetSyncAction#run(SyncSet, IProgressMonitor)
	 */
	protected SyncSet run(SyncSet syncSet, IProgressMonitor monitor) {
		try {
			ITeamNode[] changed = syncSet.getChangedNodes();
			if (changed.length == 0) {
				return syncSet;
			}
			List resources = new ArrayList();
			for (int i = 0; i < changed.length; i++) {
				resources.add(changed[i].getResource());
			}
			get((IResource[])resources.toArray(new IResource[resources.size()]), monitor);
		} catch (final TeamException e) {
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					ErrorDialog.openError(getShell(), null, null, e.getStatus());
				}
			});
			return null;
		}
		return syncSet;
	}
	
	/**
	 * Put the given resources to their associated providers.
	 * 
	 * @param resources  the resources to commit
	 * @param monitor  the progress monitor
	 */
	public void get(IResource[] resources, IProgressMonitor monitor) throws TeamException {
		monitor = Policy.monitorFor(monitor);
		try {
			Hashtable table = getTargetProviderMapping(resources);
			Set keySet = table.keySet();
			monitor.beginTask("", keySet.size() * 1000); //$NON-NLS-1$
			Iterator iterator = keySet.iterator();
			while (iterator.hasNext()) {
				IProgressMonitor subMonitor = new InfiniteSubProgressMonitor(monitor, 1000);
				TargetProvider provider = (TargetProvider)iterator.next();
				monitor.setTaskName(Policy.bind("GetAction.working", provider.getURL().toExternalForm()));  //$NON-NLS-1$
				List list = (List)table.get(provider);
				IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
				provider.get(providerResources, subMonitor);
			}
		} finally {
			monitor.done();
		}
	}
}
