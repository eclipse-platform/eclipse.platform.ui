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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.UpdateOnlyMergableOperation;
import org.eclipse.team.ui.sync.AndSyncInfoFilter;
import org.eclipse.team.ui.sync.OrSyncInfoFilter;
import org.eclipse.team.ui.sync.SyncInfoDirectionFilter;
import org.eclipse.team.ui.sync.SyncInfoFilter;
import org.eclipse.team.ui.sync.SyncInfoSet;

/**
 * This update action will update all mergable resources first and then prompt the
 * user to iverwrite any resources that failed the safe update
 */
public abstract class SafeUpdateAction extends CVSSubscriberAction {

	private List skippedFiles = new ArrayList();
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberAction#run(org.eclipse.team.ui.sync.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(SyncInfoSet syncSet, IProgressMonitor monitor) throws TeamException {
		try {
			
			// First, remove any known failure cases
			SyncInfoFilter failFilter = getKnownFailureCases();
			SyncInfo[] willFail = syncSet.getNodes(failFilter);
			syncSet.rejectNodes(failFilter);
			skippedFiles.clear();
			
			monitor.beginTask(null, (syncSet.size() + willFail.length) * 100);
			
			// Run the update on the remaining nodes in the set
			// The update will fail for conflicts that turn out to be non-automergable
			safeUpdate(syncSet, Policy.subMonitorFor(monitor, syncSet.size() * 100));
			
			// It is possible that some of the conflicting changes were not auto-mergable.
			// Accumulate all resources that have not been updated so far
			final SyncInfoSet failedSet = createFailedSet(syncSet, willFail, (IFile[]) skippedFiles.toArray(new IFile[skippedFiles.size()]));
			
			// Remove all these from the original sync set
			syncSet.rejectNodes(new SyncInfoFilter() {
				public boolean select(SyncInfo info) {
					return failedSet.getNodeFor(info.getLocal()) != null;
				}
			});
			
			// Ask the user if a replace should be performed on the remaining nodes
			if (!failedSet.isEmpty() && promptForOverwrite(failedSet)) {
				overwriteUpdate(failedSet, Policy.subMonitorFor(monitor, willFail.length * 100));
				syncSet.addAll(failedSet);
			}
			
			updated(syncSet.getResources());
		} finally {
			monitor.done();
		}
	}

	/**
	 * Perform a safe update on the resources in the provided set. Any included resources
	 * that cannot be updated safely wil be added to the skippedFiles list.
	 * @param syncSet the set containing the resources to be updated
	 * @param monitor
	 */
	protected void safeUpdate(SyncInfoSet syncSet, IProgressMonitor monitor) throws TeamException {
		SyncInfo[] changed = syncSet.getSyncInfos();
		if (changed.length == 0) return;
		
		// The list of sync resources to be updated using "cvs update"
		List updateShallow = new ArrayList();
		// A list of sync resource folders which need to be created locally 
		// (incoming addition or previously pruned)
		Set parentCreationElements = new HashSet();
		// A list of sync resources that are incoming deletions.
		// We do these first to avoid case conflicts
		List updateDeletions = new ArrayList();
	
		for (int i = 0; i < changed.length; i++) {
			SyncInfo changedNode = changed[i];
			
			// Make sure that parent folders exist
			SyncInfo parent = getParent(changedNode);
			if (parent != null && isOutOfSync(parent)) {
				// We need to ensure that parents that are either incoming folder additions
				// or previously pruned folders are recreated.
				parentCreationElements.add(parent);
			}
			
			IResource resource = changedNode.getLocal();
			int kind = changedNode.getKind();
			boolean willBeAttempted = false;
			if (resource.getType() == IResource.FILE) {	
				// Not all change types will require a "cvs update"
				// Some can be deleted locally without performing an update
				switch (kind & SyncInfo.DIRECTION_MASK) {
					case SyncInfo.INCOMING:
						switch (kind & SyncInfo.CHANGE_MASK) {
							case SyncInfo.DELETION:
								// Incoming deletions can just be deleted instead of updated
								updateDeletions.add(changedNode);
								willBeAttempted = true;
								break;
							default:
								// add the file to the list of files to be updated
								updateShallow.add(changedNode);
								willBeAttempted = true;
								break;
						}
						break;
					case SyncInfo.CONFLICTING:
						switch (kind & SyncInfo.CHANGE_MASK) {
							case SyncInfo.CHANGE:
								// add the file to the list of files to be updated
								updateShallow.add(changedNode);
								willBeAttempted = true;
								break;
						}
						break;
				}
				if (!willBeAttempted) {
					skippedFiles.add(resource);
				}
			} else {
				// Special handling for folders to support shallow operations on files
				// (i.e. folder operations are performed using the sync info already
				// contained in the sync info.
				if (isOutOfSync(changedNode)) {
					parentCreationElements.add(changedNode);
				}
			}

		}
		try {
			// Calculate the total amount of work needed
			int work = (updateDeletions.size() + updateShallow.size()) * 100;
			monitor.beginTask(null, work);

			if (parentCreationElements.size() > 0) {
				makeInSync((SyncInfo[]) parentCreationElements.toArray(new SyncInfo[parentCreationElements.size()]));				
			}
			if (updateDeletions.size() > 0) {
				runUpdateDeletions((SyncInfo[])updateDeletions.toArray(new SyncInfo[updateDeletions.size()]), Policy.subMonitorFor(monitor, updateDeletions.size() * 100));
			}			
			if (updateShallow.size() > 0) {
				runSafeUpdate((SyncInfo[])updateShallow.toArray(new SyncInfo[updateShallow.size()]), Policy.subMonitorFor(monitor, updateShallow.size() * 100));
			}
		} finally {
			monitor.done();
		}
		return;
	}

	/**
	 * Perform an overwrite (unsafe) update on the resources in the provided set.
	 * @param syncSet the set containing the resources to be updated
	 * @param monitor
	 */
	protected abstract void overwriteUpdate(SyncInfoSet syncSet, IProgressMonitor monitor) throws TeamException;

	/*
	 * Return a filter which selects the cases that we know ahead of time
	 * will fail on an update
	 */
	protected SyncInfoFilter getKnownFailureCases() {
		return new OrSyncInfoFilter(new SyncInfoFilter[] {
			// Conflicting additions of files will fail
			new AndSyncInfoFilter(new SyncInfoFilter[] {
				SyncInfoFilter.getDirectionAndChangeFilter(SyncInfo.CONFLICTING, SyncInfo.ADDITION),
				new SyncInfoFilter() {
					public boolean select(SyncInfo info) {
						return info.getLocal().getType() == IResource.FILE;
					}
				}
			}),
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
			}),
			// Outgoing changes may not fail but they are skipped as well
			new SyncInfoDirectionFilter(SyncInfo.OUTGOING)
		});
	}
	
	/*
	 * Return the complete set of selected resources that failed to update safely
	 */
	private SyncInfoSet createFailedSet(SyncInfoSet syncSet, SyncInfo[] willFail, IFile[] files) {
		List result = new ArrayList();
		for (int i = 0; i < files.length; i++) {
			IFile file = files[i];
			SyncInfo resource = syncSet.getNodeFor(file);
			if (resource != null) result.add(resource);
		}
		for (int i = 0; i < willFail.length; i++) {
			result.add(willFail[i]);
		}
		return new SyncInfoSet((SyncInfo[]) result.toArray(new SyncInfo[result.size()]));
	}
	
	/**
	 * Prompt to overwrite those resources that could not be safely updated
	 * Note: This method is designed to be overridden by test cases.
	 * 
	 * @return whether to perform the overwrite
	 */
	protected boolean promptForOverwrite(final SyncInfoSet syncSet) {
		final int[] result = new int[] {Dialog.CANCEL};
		final Shell shell = getShell();
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				UpdateDialog dialog = new UpdateDialog(shell, syncSet);
				result[0] = dialog.open();
			}
		});
		return (result[0] == UpdateDialog.YES);
	}
	
	/**
	 * This method is invoked for all resources in the sync set that are incoming deletions.
	 * It is done separately to allow deletions to be performed before additions that may
	 * be the same name with different letter case.
	 * @param nodes the SyncInfo nodes that are incoming deletions
	 * @param monitor
	 * @throws TeamException
	 */
	protected abstract void runUpdateDeletions(SyncInfo[] nodes, IProgressMonitor monitor) throws TeamException;
	
	/**
	 * This method is invoked for all resources in the sync set that are incoming changes
	 * (but not deletions: @see runUpdateDeletions) or conflicting changes.
	 * This method should only update those conflicting resources that are automergable.
	 * @param nodes the incoming or conflicting SyncInfo nodes
	 * @param monitor
	 * @throws TeamException
	 */
	protected abstract void runSafeUpdate(SyncInfo[] nodes, IProgressMonitor monitor) throws TeamException;
	
	protected void safeUpdate(IResource[] resources, LocalOption[] localOptions, IProgressMonitor monitor) throws TeamException {
		try {
			UpdateOnlyMergableOperation operation = new UpdateOnlyMergableOperation(getShell(), resources, localOptions);
			operation.run(monitor);
			addSkippedFiles(operation.getSkippedFiles());
		} catch (InvocationTargetException e) {
			throw CVSException.wrapException(e);
		} catch (InterruptedException e) {
			Policy.cancelOperation();
		}
	}
	
	/**
	 * Notification of all resource that were updated (either safely or othrwise)
	 */
	protected abstract void updated(IResource[] resources) throws TeamException;
	
	private void addSkippedFiles(IFile[] files) {
		skippedFiles.addAll(Arrays.asList(files));
	}
	
	protected String getErrorTitle() {
		return Policy.bind("UpdateAction.update"); //$NON-NLS-1$
	}
}
