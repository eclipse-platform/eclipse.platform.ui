/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.target;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.core.InfiniteSubProgressMonitor;
import org.eclipse.team.internal.core.target.TargetProvider;
import org.eclipse.team.internal.ui.IHelpContextIds;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.sync.ITeamNode;
import org.eclipse.team.internal.ui.sync.SyncSet;
import org.eclipse.team.internal.ui.sync.SyncView;
import org.eclipse.ui.help.WorkbenchHelp;

public class PutSyncAction extends TargetSyncAction {

	public PutSyncAction(TargetSyncCompareInput model, ISelectionProvider sp, String label, Shell shell) {
		super(model, sp, label, shell);
		WorkbenchHelp.setHelp(this, IHelpContextIds.SYNC_PUT_ACTION);
	}

	/**
	 * @see TargetSyncAction#isEnabled(ITeamNode)
	 */
	protected boolean isEnabled(ITeamNode node) {
		// Put action is enabled for any changed nodes.
		return new SyncSet(new StructuredSelection(node)).getChangedNodes().length > 0;
	}

	/**
	 * @see TargetSyncAction#removeNonApplicableNodes(SyncSet, int)
	 */
	protected void removeNonApplicableNodes(SyncSet set, int syncMode) {
		if (syncMode == SyncView.SYNC_OUTGOING) {
			set.removeIncomingNodes();
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
			List fileResources = new ArrayList();
 			List folderDeletions = new ArrayList();
 			List folderAdditions = new ArrayList();
 			//Find the outgoing file changes the potential outgoing folder deletions:
			for (int i = 0; i < changed.length; i++) {
				if (changed[i].getChangeDirection()==ITeamNode.OUTGOING || changed[i].getChangeDirection()==ITeamNode.CONFLICTING) {
					if (changed[i].getResource().getType()==IResource.FILE) fileResources.add(changed[i].getResource());
	 				else if (changed[i].getChangeType()==Differencer.DELETION)
	 					folderDeletions.add(changed[i].getResource());
	 				else if (((IContainer)(changed[i].getResource())).members().length==0) 
	 					////If the new local folders have no children then we'd better explicitly create them remotely:
	 					folderAdditions.add(changed[i].getResource());
				}
			}
			put((IResource[])fileResources.toArray(new IResource[fileResources.size()]), monitor);
			if (folderAdditions.size()>0) 
				put((IResource[])folderAdditions.toArray(new IResource[folderDeletions.size()]), monitor);
			if (folderDeletions.size()>0) {
				//Prune the list of potential outgoing folder deletions, retaining only those that don't have remote content:
	 			boolean delete;
	 			Iterator iter=folderDeletions.iterator();
	 			for (IContainer i=(IContainer)iter.next(); iter.hasNext(); i=(IContainer)iter.next()) {
	 				delete=true;
	 				IRemoteResource[] children=getRemoteResourceFor(i).members(monitor);
	 				for (int j = 0; j < children.length; j++) {
	 					if (!folderDeletions.contains(children[j])) {
	 						delete=false;
	 						break;
	 					}
	 				}
	 				if (!delete) iter.remove();
	 			}
	 			put((IResource[])folderDeletions.toArray(new IResource[folderDeletions.size()]), monitor);
			}
		} catch (final TeamException e) {
			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					ErrorDialog.openError(getShell(), null, null, e.getStatus());
				}
			});
			return null;
		} catch (final CoreException e) {
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
	public void put(IResource[] resources, IProgressMonitor monitor) throws TeamException {
		monitor = Policy.monitorFor(monitor);
		try {
			Hashtable table = getTargetProviderMapping(resources);
			Set keySet = table.keySet();
			monitor.beginTask("", keySet.size() * 1000); //$NON-NLS-1$
			Iterator iterator = keySet.iterator();
			while (iterator.hasNext()) {
				IProgressMonitor subMonitor = new InfiniteSubProgressMonitor(monitor, 1000);
				TargetProvider provider = (TargetProvider)iterator.next();
				monitor.setTaskName(Policy.bind("PutAction.working", provider.getURL().toExternalForm()));  //$NON-NLS-1$
				List list = (List)table.get(provider);
				IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
				provider.put(providerResources, subMonitor);
			}
		} finally {
			monitor.done();
		}
	}
}
