/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.PlatformUI;

/**
 * Action to initiate a CVS workspace synchronize
 */
public class SyncAction extends WorkspaceAction {
	
	public void execute(IAction action) throws InvocationTargetException {
		final IResource[] resources = getResourcesToSync();
		if (resources == null || resources.length == 0) return;
		
		if(isSingleFile(resources)) {
			showSingleFileComparison(getShell(), CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber(), resources[0]);
		} else {
			// First check if there is an existing matching participant
			WorkspaceSynchronizeParticipant participant = (WorkspaceSynchronizeParticipant)SubscriberParticipant.getMatchingParticipant(WorkspaceSynchronizeParticipant.ID, resources);
			// If there isn't, create one and add to the manager
			if (participant == null) {
				participant = new WorkspaceSynchronizeParticipant(new ResourceScope(resources));
				TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[] {participant});
			}
			participant.refresh(resources, getTargetPart().getSite());
		}
	}
	
	/**
	 * Refresh the subscriber directly and show the resulting synchronization state in a compare editor. If there
	 * is no difference the user is prompted.
	 * 
	 * @param resources the file to refresh and compare
	 */
	public static void showSingleFileComparison(final Shell shell, final Subscriber subscriber, final IResource resource) {
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {	
						subscriber.refresh(new IResource[]{resource}, IResource.DEPTH_ZERO, monitor);
					} catch (TeamException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
			final SyncInfo info = subscriber.getSyncInfo(resource);
			if (info == null) return;
			shell.getDisplay().syncExec(new Runnable() {
				public void run() {
					if (info.getKind() == SyncInfo.IN_SYNC) {
						MessageDialog.openInformation(shell, Policy.bind("SyncAction.noChangesTitle"), Policy.bind("SyncAction.noChangesMessage")); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						SyncInfoCompareInput input = new SyncInfoCompareInput(subscriber.getName(), info);
						CompareUI.openCompareEditor(input);
					}
				}
			});
		} catch (InvocationTargetException e) {
			Utils.handle(e);
		} catch (InterruptedException e) {
		} catch (TeamException e) {
			Utils.handle(e);
		}
	}

	public static boolean isSingleFile(IResource[] resources) {
		return resources.length == 1 && resources[0].getType() == IResource.FILE;
	}
	
	protected IResource[] getResourcesToSync() {
		return getSelectedResources();
	}
	
	/**
	 * Enable for resources that are managed (using super) or whose parent is a
	 * CVS folder.
	 * 
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForCVSResource(ICVSResource cvsResource) throws CVSException {
		return (super.isEnabledForCVSResource(cvsResource) || (cvsResource.getParent().isCVSFolder() && !cvsResource.isIgnored()));
	}
}
