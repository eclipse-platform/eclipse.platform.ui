/**********************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.events;

import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * The job for performing workspace auto-builds, and pre- and post- autobuild
 * notification.  This job is run regardless of whether autobuild is on or off.
 */
class AutoBuildJob extends Job implements Preferences.IPropertyChangeListener {
	private boolean avoidBuild = false;
	private boolean buildNeeded = false;
	private boolean forceBuild = false;
	private long lastBuild = 0L;
	private Workspace workspace;

	AutoBuildJob(Workspace workspace) {
		super(ICoreConstants.MSG_EVENTS_BUILDING_0);
		setRule(workspace.getRoot());
		setSystem(!workspace.isAutoBuilding());
		this.workspace = workspace;
		ResourcesPlugin.getPlugin().getPluginPreferences().addPropertyChangeListener(this);
	}
	/**
	 * The workspace description has changed.  Update autobuild state.
	 * @param wasAutoBuilding the old autobuild state
	 * @param isAutoBuilding the new autobuild state
	 */
	private void autoBuildChanged(boolean wasAutoBuilding, boolean isAutoBuilding) {
		//make the autobuild a system job if autobuild is off
		setSystem(!isAutoBuilding);
		//force a build if autobuild has been turned on
		if (!forceBuild && !wasAutoBuilding && isAutoBuilding) {
			forceBuild = true;
			endTopLevel(false);
		}
	}
	/**
	 * Used to prevent auto-builds at the end of operations that contain
	 * explicit builds
	 */
	public synchronized void avoidBuild() {
		avoidBuild = true;
	}
	public boolean belongsTo(Object family) {
		return family == ResourcesPlugin.FAMILY_AUTO_BUILD;
	}
	private void broadcastChanges(int type) throws CoreException {
		workspace.getNotificationManager().broadcastChanges(workspace.getElementTree(), type, false);
	}
	/**
	 * Another thread is attempting to modify the workspace. Cancel the
	 * autobuild. Returns true if the build is currently running and should be
	 * interrupted, and false otherwise.
	 */
	synchronized boolean checkCancel() {
		int state = getState();
		//cancel the build job if it is waiting to run
		if (state == Job.WAITING) {
			cancel();
			return false;
		}
		//cancel the build job if another job is attempting to modify the workspace
		//while the build job is running
		return state == Job.RUNNING && InternalPlatform.getDefault().getJobManager().currentJob() != this;
	}
	private void doBuild(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask(null, Policy.opWork);
			final ISchedulingRule rule = Rules.buildRule();
			try {
				workspace.prepareOperation(rule, monitor);
				workspace.beginOperation(true);
				broadcastChanges(IResourceChangeEvent.PRE_AUTO_BUILD);
				if (shouldBuild())
					workspace.getBuildManager().build(IncrementalProjectBuilder.AUTO_BUILD, Policy.subMonitorFor(monitor, Policy.opWork));
				broadcastChanges(IResourceChangeEvent.POST_AUTO_BUILD);
			} finally {
				//building may close the tree, but we are still inside an
				// operation so open it
				if (workspace.getElementTree().isImmutable())
					workspace.newWorkingTree();
				workspace.endOperation(rule, false, Policy.subMonitorFor(monitor, Policy.buildWork));
			}
		} finally {
			monitor.done();
		}
	}
	public synchronized void endTopLevel(boolean needsBuild) {
		buildNeeded |= needsBuild;
		long delay = Math.max(Policy.MIN_BUILD_DELAY, Policy.MAX_BUILD_DELAY + lastBuild - System.currentTimeMillis());
		if (getState() == Job.NONE)
			schedule(delay);
	}
	public IStatus run(IProgressMonitor monitor) {
		//synchronized in case build starts during checkCancel
		synchronized (this) {
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
		}
		try {
			doBuild(monitor);
			lastBuild = System.currentTimeMillis();
			return Status.OK_STATUS;
		} catch (OperationCanceledException e) {
			buildNeeded = true;
			return Status.CANCEL_STATUS;
		} catch (CoreException sig) {
			return sig.getStatus();
		}
	}
	public synchronized boolean shouldBuild() {
		try {
			//if auto-build is off then we never run
			if (!workspace.isAutoBuilding())
				return false;
			//build if the workspace requires a build (description changes)
			if (forceBuild)
				return true;
			if (avoidBuild)
				return false;
			//return whether there have been any changes to the workspace tree.
			return buildNeeded;
		} finally {
			//regardless of the result, clear the build flags for next time
			forceBuild = avoidBuild = false;
		}
	}
	public void propertyChange(PropertyChangeEvent event) {
		if (!event.getProperty().equals(ResourcesPlugin.PREF_AUTO_BUILDING))
			return;
		Object oldValue = event.getOldValue();
		Object newValue = event.getNewValue();
		if (oldValue instanceof Boolean && newValue instanceof Boolean)
			autoBuildChanged(((Boolean)oldValue).booleanValue(), ((Boolean)newValue).booleanValue());
	}
}