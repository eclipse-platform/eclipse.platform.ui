/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/

package org.eclipse.team.internal.ccvs.ui.sync;
 
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.compare.structuremergeviewer.DiffContainer;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.internal.ui.sync.ChangedTeamContainer;
import org.eclipse.team.internal.ui.sync.ITeamNode;
import org.eclipse.team.internal.ui.sync.SyncSet;
import org.eclipse.team.internal.ui.sync.SyncView;
import org.eclipse.team.internal.ui.sync.TeamFile;

/**
 * This is a CVS sync view action that will  
 */
public class AddSyncAction extends MergeAction {
	public AddSyncAction(CVSSyncCompareInput model, ISelectionProvider sp, String label, Shell shell) {
		super(model, sp, label, shell);
	}

	protected SyncSet run(SyncSet syncSet, IProgressMonitor monitor) {
		boolean result = saveIfNecessary();
		if (!result) return null;

		ITeamNode[] changed = syncSet.getChangedNodes();
		if (changed.length == 0) {
			return syncSet;
		}
		List additions = new ArrayList();

		for (int i = 0; i < changed.length; i++) {
			int kind = changed[i].getKind();
			// leave the added nodes in the sync view. Their sync state
			// won't change but the decoration should.
			IResource resource = changed[i].getResource();
			if (resource.getType() == resource.FILE) {
				additions.add(resource);
			}
		}
		try {
			RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
			if (additions.size() != 0) {
				manager.add((IResource[])additions.toArray(new IResource[0]), monitor);
			}
			
			// for all files ensure that parent folders are made in sync after
			// the add completes.
			for (int i = 0; i < changed.length; i++) {
				ITeamNode node = changed[i];
				IResource resource = changed[i].getResource();
				if (resource.getType() == resource.FILE) {
					syncSet.remove(node);
				}
			}
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
	 * Enabled for folders and files that aren't added.
	 */
	protected boolean isEnabled(ITeamNode node) {
		try {
			return new CVSSyncSet(new StructuredSelection(node)).hasNonAddedChanges();
		} catch (CVSException e) {
			CVSUIPlugin.log(e.getStatus());
			return false;
		}
	}	
	
	/**
	 * Remove all nodes that aren't files and folders that need to be added.
	 */
	protected void removeNonApplicableNodes(SyncSet set, int syncMode) {
		set.removeConflictingNodes();
		set.removeIncomingNodes();
		((CVSSyncSet)set).removeAddedChanges();
	}	
}
