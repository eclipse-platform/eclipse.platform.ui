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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSMergeSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSSyncInfo;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.UpdateOnlyMergableOperation;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager;
import org.eclipse.team.internal.ui.sync.views.SyncResource;
import org.eclipse.team.ui.sync.AndSyncInfoFilter;
import org.eclipse.team.ui.sync.OrSyncInfoFilter;
import org.eclipse.team.ui.sync.SyncInfoDirectionFilter;
import org.eclipse.team.ui.sync.SyncInfoFilter;
import org.eclipse.team.ui.sync.SyncResourceSet;

/**
 * This action performs a "cvs update -j start -j end ..." to merge changes
 * into the local workspace.
 */
public class MergeUpdateAction extends SubscriberUpdateAction {

	private List skippedFiles = new ArrayList();

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberAction#run(org.eclipse.team.ui.sync.SyncResourceSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(SyncResourceSet syncSet, IProgressMonitor monitor) throws CVSException {
		try {
			
			// First, remove any known failure cases
			SyncInfoFilter failFilter = getKnownFailureCases();
			SyncResource[] willFail = syncSet.getNodes(failFilter);
			syncSet.rejectNodes(failFilter);
			skippedFiles.clear();
			
			monitor.beginTask(null, (syncSet.size() + willFail.length) * 100);
			
			// Run the update on the remaining nodes in the set
			// The update will fail for conflicts that turn out to be non-automergable
			super.run(syncSet, Policy.subMonitorFor(monitor, syncSet.size() * 100));
			
			// It is possible that some of the conflicting changes were not auto-mergable
			SyncResourceSet failedSet = createFailedSet(syncSet, willFail, (IFile[]) skippedFiles.toArray(new IFile[skippedFiles.size()]));
			if (failedSet.isEmpty()) return;
			promptForOverwrite(failedSet);
			runOverwrite(failedSet.getSyncResources(), Policy.subMonitorFor(monitor, willFail.length * 100));
		} finally {
			monitor.done();
		}
	}

	/**
	 * @param syncSet
	 * @param willFail
	 * @param files
	 * @return
	 */
	private SyncResourceSet createFailedSet(SyncResourceSet syncSet, SyncResource[] willFail, IFile[] files) {
		List result = new ArrayList();
		for (int i = 0; i < files.length; i++) {
			IFile file = files[i];
			SyncResource resource = syncSet.getNodeFor(file);
			if (resource != null) result.add(resource);
		}
		for (int i = 0; i < willFail.length; i++) {
			result.add(willFail[i]);
		}
		return new SyncResourceSet((SyncResource[]) result.toArray(new SyncResource[result.size()]));
	}

	/*
	 * Return a filter which selects the cases that we know ahead of time
	 * will fail on a merge
	 */
	private SyncInfoFilter getKnownFailureCases() {
		return new OrSyncInfoFilter(new SyncInfoFilter[] {
			// Conflicting additions will fail
			SyncInfoFilter.getDirectionAndChangeFilter(SyncInfo.CONFLICTING, SyncInfo.ADDITION),
			// Conflicting changes involving a deletion on one side will aways fail
			new AndSyncInfoFilter(new SyncInfoFilter[] {
				SyncInfoFilter.getDirectionAndChangeFilter(SyncInfo.CONFLICTING, SyncInfo.CHANGE),
				new SyncInfoFilter() {
					public boolean select(SyncInfo info) {
						IRemoteResource remote = info.getRemote();
						IRemoteResource base = info.getBase();
						if (info.getLocal().exists()) {
							// local != base and no remote will fail
							return (base != null && remote == null);
						} else {
							// no local and base != remote
							return (base != null && remote != null && !base.equals(remote));
						}
					}
				}
			})
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.sync.SubscriberAction#getSyncInfoFilter()
	 */
	protected SyncInfoFilter getSyncInfoFilter() {
		// Update works for all incoming and conflicting nodes
		// TODO: there should be an instance variable for the filter
		return new OrSyncInfoFilter(new SyncInfoFilter[] {
			new SyncInfoDirectionFilter(SyncInfo.INCOMING),
			new SyncInfoDirectionFilter(SyncInfo.CONFLICTING)
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberUpdateAction#runUpdateDeletions(org.eclipse.team.internal.ui.sync.views.SyncResource[], org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void runUpdateDeletions(SyncResource[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException {
		// When merging, update deletions become outgoing deletions so just delete
		// the files locally without unmanaging (so the sync info is kept to 
		// indicate an outgoing deletion
		try {
			monitor.beginTask(null, 100 * nodes.length);
			for (int i = 0; i < nodes.length; i++) {
				IResource resource = nodes[i].getResource();
				if (resource.getType() == IResource.FILE) {
					((IFile)resource).delete(false /* force */, true /* keep local history */, Policy.subMonitorFor(monitor, 100));
				}
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} finally {
			monitor.done();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberUpdateAction#runUpdateShallow(org.eclipse.team.internal.ui.sync.views.SyncResource[], org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void runUpdateShallow(SyncResource[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException {
		// Incoming additions require different handling then incoming changes and deletions
		List additions = new ArrayList();
		List changes = new ArrayList();
		for (int i = 0; i < nodes.length; i++) {
			SyncResource resource = nodes[i];
			int kind = resource.getKind();
			if ((kind & SyncInfo.ADDITION) != 0) {
				additions.add(resource);
			} else {
				changes.add(resource);
			}
		}
		if (!additions.isEmpty()) {
			mergeWithLocal((SyncResource[]) additions.toArray(new SyncResource[additions.size()]), manager, false /* include start tag */, monitor);
		}
		if (!changes.isEmpty()) {
			mergeWithLocal((SyncResource[]) changes.toArray(new SyncResource[changes.size()]), manager, true /* include start tag */, monitor);
		}
	}
	
	/*
	 * Use "cvs update -j start -j end ..." to merge changes. This method will result in 
	 * an error for addition conflicts.
	 */
	protected void mergeWithLocal(SyncResource[] nodes, RepositoryManager manager, boolean includeStartTag, IProgressMonitor monitor) throws TeamException {
		TeamSubscriber subscriber = getSubscriber();
		if (!(subscriber instanceof CVSMergeSubscriber)) {
			throw new CVSException("Invalid subscriber: " + subscriber.getId());
		}
		CVSTag startTag = ((CVSMergeSubscriber)subscriber).getStartTag();
		CVSTag endTag = ((CVSMergeSubscriber)subscriber).getEndTag();
		
		Command.LocalOption[] options;
		if (includeStartTag) {
			options = new Command.LocalOption[] {
				Command.DO_NOT_RECURSE,
				Update.makeArgumentOption(Update.JOIN, startTag.getName()),
				Update.makeArgumentOption(Update.JOIN, endTag.getName()) };
		} else {
			options = new Command.LocalOption[] {
				Command.DO_NOT_RECURSE,
				Update.makeArgumentOption(Update.JOIN, endTag.getName()) };
		}

		// run a join update using the start and end tags and the join points
		try {
			UpdateOnlyMergableOperation operation = new UpdateOnlyMergableOperation(getShell(), getIResourcesFrom(nodes), options);
			operation.run(monitor);
			addSkippedFiles(operation.getSkippedFiles());
		} catch (InvocationTargetException e) {
			throw CVSException.wrapException(e);
		} catch (InterruptedException e) {
			Policy.cancelOperation();
		}
	}

	private void addSkippedFiles(IFile[] files) {
		skippedFiles.addAll(Arrays.asList(files));
	}
	
	/* (non-Javadoc)
	 * 
	 * Return true for all conflicting changes since the server does not report
	 * automergable conflicts properly for merge updates.
	 * 
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberUpdateAction#supportsShallowUpdateFor(org.eclipse.team.internal.ui.sync.views.SyncResource)
	 */
	protected boolean supportsShallowUpdateFor(SyncResource changedNode) {
		return (changedNode.getChangeDirection() == SyncInfo.CONFLICTING
			&& ((changedNode.getKind() & SyncInfo.CHANGE_MASK) == SyncInfo.CHANGE));
	}
	
	/**
	 * Prompt for mergeable conflicts.
	 * Note: This method is designed to be overridden by test cases.
	 * 
	 * @return 0 to cancel, 1 to only update mergeable conflicts, 2 to overwrite if unmergeable
	 */
	protected boolean promptForOverwrite(final SyncResourceSet syncSet) {
		final int[] result = new int[] {Dialog.CANCEL};
		final Shell shell = getShell();
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				MergeDialog dialog = new MergeDialog(shell, syncSet);
				result[0] = dialog.open();
			}
		});
		return (result[0] == Dialog.OK);
	}
	
	/*
	 * @see UpdateSyncAction#runUpdateDeep(IProgressMonitor, List, RepositoryManager)
	 * incoming-change
	 * incoming-deletion
	 */
	protected void runOverwrite(SyncResource[] nodes, IProgressMonitor monitor) throws CVSException {
		RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
		monitor.beginTask(null, 1000 * nodes.length);
		try {
			for (int i = 0; i < nodes.length; i++) {
				makeRemoteLocal(nodes[i], Policy.subMonitorFor(monitor, 1000));
			}
		} finally {
			monitor.done();
		}
	}
	
	/*
	 * If called on a new folder, the folder will become an outgoing addition.
	 */
	private void makeRemoteLocal(SyncResource element, IProgressMonitor monitor) throws CVSException {
		SyncInfo info = element.getSyncInfo();
		IRemoteResource remote = info.getRemote();
		IResource local = info.getLocal();
		try {
			if(remote==null) {
				local.delete(false, monitor);
			} else {
				if(remote.isContainer()) {
					ensureContainerExists(element);
				} else {
					monitor.beginTask(null, 200);
					try {
						IFile localFile = (IFile)local;
						if(local.exists()) {
							localFile.setContents(remote.getContents(Policy.subMonitorFor(monitor, 100)), false /*don't force*/, true /*keep history*/, Policy.subMonitorFor(monitor, 100));
						} else {
							ensureContainerExists(element.getParent());
							localFile.create(remote.getContents(Policy.subMonitorFor(monitor, 100)), false /*don't force*/, Policy.subMonitorFor(monitor, 100));
						}
					} finally {
						monitor.done();
					}
				}
			}
		} catch(CoreException e) {
			throw new CVSException(Policy.bind("UpdateMergeActionProblems_merging_remote_resources_into_workspace_1"), e); //$NON-NLS-1$
		}
	}
	
	private void ensureContainerExists(SyncResource resource) throws CVSException {
		IResource local = resource.getResource();
		// make sure that the parent exists
		if (!local.exists()) {
			ensureContainerExists(resource.getParent());
		}
		// make sure that the folder sync info is set
		SyncInfo info = resource.getSyncInfo();
		if (info instanceof CVSSyncInfo) {
			CVSSyncInfo cvsInfo = (CVSSyncInfo)info;
			cvsInfo.makeInSync();
		}
		// create the folder if it doesn't exist
		ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor((IContainer)local);
		if (!cvsFolder.exists()) {
			cvsFolder.mkdir();
		}
	}	
}
