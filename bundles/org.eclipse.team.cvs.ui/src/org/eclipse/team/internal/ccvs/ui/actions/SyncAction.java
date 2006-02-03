/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;
 
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.mappings.ModelUpdateOperation;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.synchronize.actions.OpenInCompareAction;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.*;

/**
 * Action to initiate a CVS workspace synchronize
 */
public class SyncAction extends WorkspaceTraversalAction {
	
	public void execute(IAction action) throws InvocationTargetException {
        IResource[] resources = getResourcesToCompare(getWorkspaceSubscriber());
		if (resources == null || resources.length == 0) return;
		
		if(isSingleFile(resources)) {
			showSingleFileComparison(getShell(), CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber(), resources[0], getTargetPage());
		}else if (isShowModelSync()) {
			try {
				new ModelUpdateOperation(getTargetPart(), getCVSResourceMappings()) {
					protected boolean isAttemptHeadlessMerge() {
						return false;
					}
					protected boolean isPreviewInDialog() {
						return false;
					}
					protected String getJobName() {
						return "Synchronizing CVS";
					}
				}.run();
			} catch (InterruptedException e) {
				// Ignore
			}
		} else {
			// First check if there is an existing matching participant
			WorkspaceSynchronizeParticipant participant = (WorkspaceSynchronizeParticipant)SubscriberParticipant.getMatchingParticipant(WorkspaceSynchronizeParticipant.ID, resources);
			// If there isn't, create one and add to the manager
			if (participant == null) {
                ISynchronizeScope scope;
                if (includesAllCVSProjects(resources)) {
                    scope = new WorkspaceScope();
                } else {
                    IWorkingSet[] sets = getSelectedWorkingSets();            
                    if (sets != null) {
                        scope = new WorkingSetScope(sets);
                    } else {
                        scope = new ResourceScope(resources);
                    }
                }
                participant = new WorkspaceSynchronizeParticipant(scope);
				TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[] {participant});
			}
			participant.refresh(resources, getTargetPart().getSite());
		}
	}
    
    private boolean isShowModelSync() {
		return CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_ENABLE_MODEL_SYNC);
	}

	private IWorkingSet[] getSelectedWorkingSets() {
        ResourceMapping[] mappings = getCVSResourceMappings();
        List sets = new ArrayList();
        for (int i = 0; i < mappings.length; i++) {
            ResourceMapping mapping = mappings[i];
            if (mapping.getModelObject() instanceof IWorkingSet) {
                IWorkingSet set = (IWorkingSet) mapping.getModelObject();
                sets.add(set);
            } else {
                return null;
            }
        }
        if (sets.isEmpty())
            return null;
        return (IWorkingSet[]) sets.toArray(new IWorkingSet[sets.size()]);
    }

    private boolean includesAllCVSProjects(IResource[] resources) {
        // First, make sure all the selected thinsg are projects
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            if (resource.getType() != IResource.PROJECT)
                return false;
        }
        IProject[] cvsProjects = getAllCVSProjects();
        return cvsProjects.length == resources.length;
    }

    private IProject[] getAllCVSProjects() {
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        Set cvsProjects = new HashSet();
        for (int i = 0; i < projects.length; i++) {
            IProject project = projects[i];
            if (RepositoryProvider.isShared(project) && RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId()) != null) {
                cvsProjects.add(project);
            }
        }
        return (IProject[]) cvsProjects.toArray(new IProject[cvsProjects.size()]);
    }

    /**
	 * Refresh the subscriber directly and show the resulting synchronization state in a compare editor. If there
	 * is no difference the user is prompted.
	 * 
	 * @param resources the file to refresh and compare
	 */
	public static void showSingleFileComparison(final Shell shell, final Subscriber subscriber, final IResource resource, final IWorkbenchPage page) {
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
						MessageDialog.openInformation(shell, CVSUIMessages.SyncAction_noChangesTitle, CVSUIMessages.SyncAction_noChangesMessage); // 
					} else {
						SyncInfoCompareInput input = new SyncInfoCompareInput(subscriber.getName(), info);
                        OpenInCompareAction.openCompareEditor(input, page);
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
	
	/**
	 * Enable for resources that are managed (using super) or whose parent is a
	 * CVS folder.
	 * 
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForCVSResource(ICVSResource cvsResource) throws CVSException {
		return (super.isEnabledForCVSResource(cvsResource) || (cvsResource.getParent().isCVSFolder() && !cvsResource.isIgnored()));
	}
	
	public String getId() {
		return ICVSUIConstants.CMD_SYNCHRONIZE;
	}
}
