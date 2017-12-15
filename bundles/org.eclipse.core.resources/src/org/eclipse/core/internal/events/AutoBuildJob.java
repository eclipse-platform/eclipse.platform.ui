/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Warren Paul (Nokia) - Fix for build scheduling bug 209236
 *******************************************************************************/
package org.eclipse.core.internal.events;

import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;

/**
 * The job for performing workspace auto-builds, and pre- and post- autobuild
 * notification.  This job is run whenever the workspace changes regardless
 * of whether autobuild is on or off.
 */
class AutoBuildJob extends Job implements Preferences.IPropertyChangeListener {
	private boolean avoidBuild = false;
	private boolean buildNeeded = false;
	private boolean forceBuild = false;
	/**
	 * Indicates that another thread tried to modify the workspace during
	 * the autobuild.  The autobuild should be immediately rescheduled
	 * so that it will run as soon as the next workspace modification completes.
	 */
	private boolean interrupted = false;
	private boolean isAutoBuilding = false;
	private volatile long lastBuild = 0L;
	private Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
	private final Bundle systemBundle = Platform.getBundle("org.eclipse.osgi"); //$NON-NLS-1$
	private Workspace workspace;

	AutoBuildJob(Workspace workspace) {
		super(Messages.events_building_0);
		setRule(workspace.getRoot());
		setPriority(BUILD);
		isAutoBuilding = workspace.isAutoBuilding();
		this.workspace = workspace;
		this.preferences.addPropertyChangeListener(this);
	}

	/**
	 * Used to prevent auto-builds at the end of operations that contain
	 * explicit builds
	 */
	synchronized void avoidBuild() {
		avoidBuild = true;
	}

	@Override
	public boolean belongsTo(Object family) {
		return family == ResourcesPlugin.FAMILY_AUTO_BUILD;
	}

	/**
	 * Instructs the build job that a build is required.  Ensure the build
	 * job is scheduled to run.
	 * @param needsBuild Whether a build is required, either due to
	 * workspace change or other factor that invalidates the built state.
	 */
	synchronized void build(boolean needsBuild) {
		buildNeeded |= needsBuild;
		long delay = computeScheduleDelay();
		int state = getState();
		if (Policy.DEBUG_BUILD_NEEDED)
			Policy.debug("Auto-Build requested, needsBuild: " + needsBuild + " state: " + state + " delay: " + delay); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (needsBuild && Policy.DEBUG_BUILD_NEEDED_STACK && state != Job.RUNNING)
			Policy.debug(new RuntimeException("Build needed")); //$NON-NLS-1$
		//don't mess with the interrupt flag if the job is still running
		if (state != Job.RUNNING)
			setInterrupted(false);
		switch (state) {
			case Job.SLEEPING :
				wakeUp(delay);
				break;
			case NONE :
				try {
					setSystem(!isAutoBuilding);
				} catch (IllegalStateException e) {
					//ignore - the job has been scheduled since we last checked its state
				}
				schedule(delay);
				break;
		}
	}

	/**
	 * Computes the delay time that autobuild should be scheduled with.  The
	 * value will be in the range (MIN_BUILD_DELAY, MAX_BUILD_DELAY).
	 */
	private long computeScheduleDelay() {
		// don't assume that the last build time is always less than the current system time
		long maxDelay = Math.min(Policy.MAX_BUILD_DELAY, Policy.MAX_BUILD_DELAY + lastBuild - System.currentTimeMillis());
		return Math.max(Policy.MIN_BUILD_DELAY, maxDelay);
	}

	/**
	 * The autobuild job has been canceled.  There are two flavours of
	 * cancel, explicit user cancelation, and implicit interruption due to another
	 * thread trying to modify the workspace.  In the latter case, we must
	 * make sure the build is immediately rescheduled if it was interrupted
	 * by another thread, so that clients waiting to join autobuild will properly
	 * continue waiting
	 * @return a status with severity <code>CANCEL</code>
	 */
	private synchronized IStatus canceled() {
		//regardless of the form of cancelation, the build state is not happy
		buildNeeded = true;
		//schedule a rebuild immediately if build was implicitly canceled
		if (interrupted) {
			if (Policy.DEBUG_BUILD_INTERRUPT)
				Policy.debug("Scheduling rebuild due to interruption"); //$NON-NLS-1$
			setInterrupted(false);
			schedule(computeScheduleDelay());
		}
		return Status.CANCEL_STATUS;
	}

	private void doBuild(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, Policy.opWork + 1);
		final ISchedulingRule rule = workspace.getRuleFactory().buildRule();
		try {
			workspace.prepareOperation(rule, subMonitor.split(1));
			workspace.beginOperation(true);
			final int trigger = IncrementalProjectBuilder.AUTO_BUILD;
			workspace.broadcastBuildEvent(workspace, IResourceChangeEvent.PRE_BUILD, trigger);
			IStatus result = Status.OK_STATUS;
			try {
				if (shouldBuild())
					result = workspace.getBuildManager().build(workspace.getBuildOrder(), ICoreConstants.EMPTY_BUILD_CONFIG_ARRAY, trigger, subMonitor.split(Policy.opWork));
			} finally {
				//always send POST_BUILD if there has been a PRE_BUILD
				workspace.broadcastBuildEvent(workspace, IResourceChangeEvent.POST_BUILD, trigger);
			}
			if (!result.isOK()) {
				throw new ResourceException(result);
			}
			buildNeeded = false;
		} finally {
			//building may close the tree, but we are still inside an
			// operation so open it
			if (workspace.getElementTree().isImmutable()) {
				workspace.newWorkingTree();
			}
			workspace.endOperation(rule, false);
		}
	}

	/**
	 * Forces an autobuild to occur, even if nothing has changed since the last
	 * build. This is used to force a build after a clean.
	 */
	public void forceBuild() {
		forceBuild = true;
	}

	/**
	 * Another thread is attempting to modify the workspace. Flag the auto-build
	 * as interrupted so that it will cancel and reschedule itself
	 */
	synchronized void interrupt() {
		//if already interrupted, do nothing
		if (interrupted)
			return;
		switch (getState()) {
			case NONE :
				return;
			case WAITING :
				//put the job to sleep if it is waiting to run
				setInterrupted(!sleep());
				break;
			case RUNNING :
				//make sure autobuild doesn't interrupt itself
				if (Job.getJobManager().currentJob() == this)
					return;
				setInterrupted(true);
				break;
		}
		//clear the autobuild avoidance flag if we were interrupted
		if (interrupted)
			avoidBuild = false;
	}

	synchronized boolean isInterrupted() {
		if (interrupted)
			return true;
		//check if another job is blocked by the build job
		if (isBlocking())
			setInterrupted(true);
		return interrupted;
	}

	@Deprecated
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (!event.getProperty().equals(ResourcesPlugin.PREF_AUTO_BUILDING))
			return;
		// get the new value of auto-build directly from the preferences
		boolean wasAutoBuilding = isAutoBuilding;
		isAutoBuilding = preferences.getBoolean(ResourcesPlugin.PREF_AUTO_BUILDING);
		//force a build if autobuild has been turned on
		if (!forceBuild && !wasAutoBuilding && isAutoBuilding) {
			forceBuild = true;
			build(false);
		}
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
		synchronized (this) {
			if (subMonitor.isCanceled()) {
				return canceled();
			}
		}
		//if the system is shutting down, don't build
		if (systemBundle.getState() == Bundle.STOPPING)
			return Status.OK_STATUS;
		try {
			doBuild(subMonitor.split(1));
			lastBuild = System.currentTimeMillis();
			//if the build was successful then it should not be recorded as interrupted
			setInterrupted(false);
			return Status.OK_STATUS;
		} catch (OperationCanceledException e) {
			return canceled();
		} catch (CoreException sig) {
			return sig.getStatus();
		}
	}

	/**
	 * Sets or clears the interrupted flag.
	 */
	private synchronized void setInterrupted(boolean value) {
		interrupted = value;
		if (interrupted && Policy.DEBUG_BUILD_INTERRUPT)
			Policy.debug(new RuntimeException("Autobuild was interrupted")); //$NON-NLS-1$
	}

	/**
	 * Returns true if a build is actually needed, and false otherwise.
	 */
	private synchronized boolean shouldBuild() {
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
			forceBuild = avoidBuild = buildNeeded = false;
		}
	}
}
