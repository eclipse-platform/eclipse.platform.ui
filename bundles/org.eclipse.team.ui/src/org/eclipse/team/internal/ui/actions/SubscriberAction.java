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
package org.eclipse.team.internal.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.synchronize.viewers.SynchronizeModelElement;
import org.eclipse.team.ui.synchronize.viewers.SyncInfoModelElement;
import org.eclipse.ui.*;

/**
 * This action provides utilities for performing operations on selections that
 * contain instances of {@link SyncInfoModelElement}. Subclasses can use this support
 * to:
 * <ul>
 * <li>provides easy filtering of selection
 * <li>provides scheduling action via workbench part (provide feedback via view)
 * <li>provides support for running action in background or foreground
 * <li>provides support for locking workspace resources
 * </ul>
 * @see SyncInfo
 * @see SyncInfoSet
 * @see SyncInfoModelElement
 * @since 3.0
 */
public abstract class SubscriberAction implements IObjectActionDelegate, IViewActionDelegate {
	
	private IStructuredSelection selection;
	private IWorkbenchPart part;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public final void run(IAction action) {
		// TODO: We used to prompt for unsaved changes in any editor. We don't anymore. Would
		// it be better to prompt for unsaved changes to editors affected by this action?
		SyncInfoSet syncSet = makeSyncInfoSetFromSelection(getFilteredSyncInfos());
		if (syncSet == null || syncSet.isEmpty()) return;
		try {
			getRunnableContext().run(getJobName(syncSet), getSchedulingRule(syncSet), true, getRunnable(syncSet));
		} catch (InvocationTargetException e) {
			handle(e);
		} catch (InterruptedException e) {
			handle(e);
		}
	}

	/**
	 * Subsclasses must override to provide behavior for the action.
	 * @param syncSet the set of filtered sync info objects on which to perform the action.
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * 		  reporting and cancellation are not desired 
	 * @throws TeamException if something went wrong running the action
	 */
	protected abstract void run(SyncInfoSet syncSet, IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Subsclasses may override to perform custom processing on the selection before
	 * the action is run. This can be used to prompt the user for more information.
	 * @param infos the 
	 */
	protected SyncInfoSet makeSyncInfoSetFromSelection(SyncInfo[] infos) {
		return new SyncInfoSet(infos);		
	}
	
	protected void handle(Exception e) {
		Utils.handle(e);
	}
	
	/**
	 * This method returns all instances of SyncInfo that are in the current
	 * selection. For a tree view, this is any descendants of the selected resource that are
	 * contained in the view.
	 * 
	 * @return the selected resources
	 */
	protected IDiffElement[] getDiffElements() {
		return Utils.getDiffNodes(((IStructuredSelection)selection).toArray());
	}

	/**
	 * The default enablement behavior for subscriber actions is to enable
	 * the action if there is at least one SyncInfo in the selection
	 * for which the action is enabled (determined by invoking 
	 * <code>isEnabled(SyncInfo)</code>).
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return (getFilteredDiffElements().length > 0);
	}

	/**
	 * Default filter includes all out-of-sync elements in the current
	 * selection.
	 * @return a sync info filter which selects all out-of-sync resources.
	 */
	protected FastSyncInfoFilter getSyncInfoFilter() {
		return new FastSyncInfoFilter();
	}

	/**
	 * Return the selected diff element for which this action is enabled.
	 * @return the list of selected diff elements for which this action is enabled.
	 */
	protected IDiffElement[] getFilteredDiffElements() {
		IDiffElement[] elements = getDiffElements();
		List filtered = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			IDiffElement e = elements[i];
			if (e instanceof SyncInfoModelElement) {
				SyncInfo info = ((SyncInfoModelElement) e).getSyncInfo();
				if (info != null && getSyncInfoFilter().select(info)) {
					filtered.add(e);
				}
			}
		}
		return (IDiffElement[]) filtered.toArray(new IDiffElement[filtered.size()]);
	}
	
	/**
	 * Return the selected SyncInfo for which this action is enabled.
	 * @return the selected SyncInfo for which this action is enabled.
	 */
	protected SyncInfo[] getFilteredSyncInfos() {
		IDiffElement[] elements = getFilteredDiffElements();
		List filtered = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			IDiffElement e = elements[i];
			if (e instanceof SyncInfoModelElement) {
				filtered.add(((SyncInfoModelElement)e).getSyncInfo());
			}
		}
		return (SyncInfo[]) filtered.toArray(new SyncInfo[filtered.size()]);
	}
	
	private void markBusy(IDiffElement[] elements, boolean isBusy) {
		for (int i = 0; i < elements.length; i++) {
			IDiffElement element = elements[i];
			if (element instanceof SynchronizeModelElement) {
				((SynchronizeModelElement)element).setPropertyToRoot(SynchronizeModelElement.BUSY_PROPERTY, isBusy);
			}
		}
	}

	/**
	 * Uses the {@link #canRunAsJob()} hint to return a {@link ITeamRunnableContext}. 
	 * 
	 * @return the runnable context in which to run this action.
	 */
	protected ITeamRunnableContext getRunnableContext() {
		if (canRunAsJob()) {
			// mark resources that will be affected by job
			final IDiffElement[] affectedElements = getFilteredDiffElements();
			markBusy(affectedElements, true);
			
			// register to unmark when job is finished
			IJobChangeListener listener = new JobChangeAdapter() {
				public void done(IJobChangeEvent event) {
					markBusy(affectedElements, false);
				}
			};
			return new JobRunnableContext(listener, getSite());
		} else {
			return new ProgressDialogRunnableContext(getShell());
		}
	}

	/**
	 * If this action can safely be run in the background, then subclasses can
	 * override this method and return <code>true</code>. This will make their
	 * action run in a {@link Job}. 
	 * 
	 * @return <code>true</code> if this action can be run in the background and
	 * <code>false</code> otherwise.
	 */
	protected boolean canRunAsJob() {
		return false;
	}
	
	/**
	 * Return the job name to be used if the action can run as a job.
	 * 
	 * @param syncSet
	 * @return
	 */
	protected String getJobName(SyncInfoSet syncSet) {
		return ""; //$NON-NLS-1$
	}

	/**
	 * Return a scheduling rule that includes all resources that will be operated 
	 * on by the subscriber action. The default behavior is to include all projects
	 * effected by the operation. Subclasses may override.
	 * 
	 * @param syncSet
	 * @return
	 */
	protected ISchedulingRule getSchedulingRule(SyncInfoSet syncSet) {
		IResource[] resources = syncSet.getResources();
		Set set = new HashSet();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			set.add(resource.getProject());
		}
		IProject[] projects = (IProject[]) set.toArray(new IProject[set.size()]);
		if (projects.length == 1) {
			return projects[0];
		} else {
			return new MultiRule(projects);
		}
	}

	protected IRunnableWithProgress getRunnable(final SyncInfoSet syncSet) {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					SubscriberAction.this.run(syncSet, monitor);
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
	}
	
	private IWorkbenchSite getSite() {
		IWorkbenchSite site = null;
		if(part != null) {
			site = part.getSite();
		}
		return site;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.part = targetPart;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		this.part = view;
	}
	
	/*
	 * Method declared on IActionDelegate.
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
			if (action != null) {
				setActionEnablement(action);
			}
		}
	}
	
	/**
	 * Method invoked from <code>selectionChanged(IAction, ISelection)</code> 
	 * to set the enablement status of the action. The instance variable 
	 * <code>selection</code> will contain the latest selection so the methods
	 * <code>getSelectedResources()</code> and <code>getSelectedProjects()</code>
	 * will provide the proper objects.
	 * 
	 * This method can be overridden by subclasses but should not be invoked by them.
	 */
	protected void setActionEnablement(IAction action) {
		try {
			action.setEnabled(isEnabled());
		} catch (TeamException e) {
			if (e.getStatus().getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
				// Enable the action to allow the user to discover the problem
				action.setEnabled(true);
			} else {
				action.setEnabled(false);
				// We should not open a dialog when determining menu enablements so log it instead
				TeamPlugin.log(e);
			}
		}
	}
	
	protected Shell getShell() {
		if(part != null) {
			return part.getSite().getShell();
		} else {
			IWorkbench workbench = TeamUIPlugin.getPlugin().getWorkbench();
			if (workbench == null) return null;
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			if (window == null) return null;
			return window.getShell();
		}
	}
}