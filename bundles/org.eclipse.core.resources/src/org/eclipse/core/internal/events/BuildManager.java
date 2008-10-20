/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Isaac Pacht (isaacp3@gmail.com) - fix for bug 206540
 *******************************************************************************/
package org.eclipse.core.internal.events;

import java.util.*;
import org.eclipse.core.internal.dtree.DeltaDataTree;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

public class BuildManager implements ICoreConstants, IManager, ILifecycleListener {

	/**
	 * Cache used to optimize the common case of an autobuild against
	 * a workspace where only a single project has changed (and hence
	 * only a single delta is interesting).
	 */
	class DeltaCache {
		private Object delta;
		private ElementTree newTree;
		private ElementTree oldTree;
		private IPath projectPath;

		public void cache(IPath project, ElementTree anOldTree, ElementTree aNewTree, Object aDelta) {
			this.projectPath = project;
			this.oldTree = anOldTree;
			this.newTree = aNewTree;
			this.delta = aDelta;
		}

		public void flush() {
			this.projectPath = null;
			this.oldTree = null;
			this.newTree = null;
			this.delta = null;
		}

		/**
		 * Returns the cached resource delta for the given project and trees, or
		 * null if there is no matching delta in the cache.
		 */
		public Object getDelta(IPath project, ElementTree anOldTree, ElementTree aNewTree) {
			if (delta == null)
				return null;
			boolean pathsEqual = projectPath == null ? project == null : projectPath.equals(project);
			if (pathsEqual && this.oldTree == anOldTree && this.newTree == aNewTree)
				return delta;
			return null;
		}
	}

	/**
	 * These builders are added to build tables in place of builders that couldn't be instantiated
	 */
	class MissingBuilder extends IncrementalProjectBuilder {
		private boolean hasBeenBuilt = false;
		private String name;

		MissingBuilder(String name) {
			this.name = name;
		}

		/**
		 * Log an exception on the first build, and silently do nothing on subsequent builds.
		 */
		protected IProject[] build(int kind, Map args, IProgressMonitor monitor) {
			if (!hasBeenBuilt && Policy.DEBUG_BUILD_FAILURE) {
				hasBeenBuilt = true;
				String msg = NLS.bind(Messages.events_skippingBuilder, name, getProject().getName());
				Policy.log(IStatus.WARNING, msg, null);
			}
			return null;
		}
	}

	private static final int TOTAL_BUILD_WORK = Policy.totalWork * 1000;

	//the job for performing background autobuild
	final AutoBuildJob autoBuildJob;
	private boolean building = false;
	private final ArrayList builtProjects = new ArrayList();

	//the following four fields only apply for the lifetime of a single builder invocation.
	protected InternalBuilder currentBuilder;
	private DeltaDataTree currentDelta;
	private ElementTree currentLastBuiltTree;
	private ElementTree currentTree;

	/**
	 * Caches the IResourceDelta for a pair of trees
	 */
	final private DeltaCache deltaCache = new DeltaCache();
	/**
	 * Caches the DeltaDataTree used to determine if a build is necessary
	 */
	final private DeltaCache deltaTreeCache = new DeltaCache();

	private ILock lock;

	//used for the build cycle looping mechanism
	private boolean rebuildRequested = false;

	private final Bundle systemBundle = Platform.getBundle("org.eclipse.osgi"); //$NON-NLS-1$

	//used for debug/trace timing
	private long timeStamp = -1;
	private Workspace workspace;

	public BuildManager(Workspace workspace, ILock workspaceLock) {
		this.workspace = workspace;
		this.autoBuildJob = new AutoBuildJob(workspace);
		this.lock = workspaceLock;
		InternalBuilder.buildManager = this;
	}

	private void basicBuild(int trigger, IncrementalProjectBuilder builder, Map args, MultiStatus status, IProgressMonitor monitor) {
		try {
			currentBuilder = builder;
			//clear any old requests to forget built state
			currentBuilder.clearForgetLastBuiltState();
			// Figure out want kind of build is needed
			boolean clean = trigger == IncrementalProjectBuilder.CLEAN_BUILD;
			currentLastBuiltTree = currentBuilder.getLastBuiltTree();
			// If no tree is available we have to do a full build
			if (!clean && currentLastBuiltTree == null)
				trigger = IncrementalProjectBuilder.FULL_BUILD;
			//don't build if this builder doesn't respond to the given trigger
			if (!builder.getCommand().isBuilding(trigger)) {
				if (clean)
					currentBuilder.setLastBuiltTree(null);
				return;
			}
			// For incremental builds, grab a pointer to the current state before computing the delta
			currentTree = ((trigger == IncrementalProjectBuilder.FULL_BUILD) || clean) ? null : workspace.getElementTree();
			int depth = -1;
			try {
				//short-circuit if none of the projects this builder cares about have changed.
				if (!needsBuild(currentBuilder, trigger)) {
					//use up the progress allocated for this builder
					monitor.beginTask("", 1); //$NON-NLS-1$
					monitor.done();
					return;
				}
				String name = currentBuilder.getLabel();
				String message;
				if (name != null)
					message = NLS.bind(Messages.events_invoking_2, name, builder.getProject().getFullPath());
				else
					message = NLS.bind(Messages.events_invoking_1, builder.getProject().getFullPath());
				monitor.subTask(message);
				hookStartBuild(builder, trigger);
				//release workspace lock while calling builders
				depth = getWorkManager().beginUnprotected();
				//do the build
				SafeRunner.run(getSafeRunnable(trigger, args, status, monitor));
			} finally {
				if (depth >= 0)
					getWorkManager().endUnprotected(depth);
				// Be sure to clean up after ourselves.
				if (clean || currentBuilder.wasForgetStateRequested()) {
					currentBuilder.setLastBuiltTree(null);
				} else {
					// remember the current state as the last built state.
					ElementTree lastTree = workspace.getElementTree();
					lastTree.immutable();
					currentBuilder.setLastBuiltTree(lastTree);
				}
				hookEndBuild(builder);
			}
		} finally {
			currentBuilder = null;
			currentTree = null;
			currentLastBuiltTree = null;
			currentDelta = null;
		}
	}

	protected void basicBuild(IProject project, int trigger, ICommand[] commands, MultiStatus status, IProgressMonitor monitor) {
		try {
			for (int i = 0; i < commands.length; i++) {
				checkCanceled(trigger, monitor);
				BuildCommand command = (BuildCommand) commands[i];
				IProgressMonitor sub = Policy.subMonitorFor(monitor, 1);
				IncrementalProjectBuilder builder = getBuilder(project, command, i, status);
				if (builder != null)
					basicBuild(trigger, builder, command.getArguments(false), status, sub);
			}
		} catch (CoreException e) {
			status.add(e.getStatus());
		}
	}

	/**
	 * Runs all builders on the given project. 
	 * @return A status indicating if the build succeeded or failed
	 */
	private IStatus basicBuild(IProject project, int trigger, IProgressMonitor monitor) {
		if (!canRun(trigger))
			return Status.OK_STATUS;
		try {
			hookStartBuild(trigger);
			MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, Messages.events_errors, null);
			basicBuild(project, trigger, status, monitor);
			return status;
		} finally {
			hookEndBuild(trigger);
		}
	}

	private void basicBuild(final IProject project, final int trigger, final MultiStatus status, final IProgressMonitor monitor) {
		try {
			final ICommand[] commands;
			if (project.isAccessible())
				commands = ((Project) project).internalGetDescription().getBuildSpec(false);
			else
				commands = null;
			int work = commands == null ? 0 : commands.length;
			monitor.beginTask(NLS.bind(Messages.events_building_1, project.getFullPath()), work);
			if (work == 0)
				return;
			ISafeRunnable code = new ISafeRunnable() {
				public void handleException(Throwable e) {
					if (e instanceof OperationCanceledException) {
						if (Policy.DEBUG_BUILD_INVOKING)
							Policy.debug("Build canceled"); //$NON-NLS-1$
						throw (OperationCanceledException) e;
					}
					// don't log the exception....it is already being logged in Workspace#run
					// should never get here because the lower-level build code wrappers
					// builder exceptions in core exceptions if required.
					String errorText = e.getMessage();
					if (errorText == null)
						errorText = NLS.bind(Messages.events_unknown, e.getClass().getName(), project.getName());
					status.add(new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, errorText, e));
				}

				public void run() throws Exception {
					basicBuild(project, trigger, commands, status, monitor);
				}
			};
			SafeRunner.run(code);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Runs the builder with the given name on the given project. 
	 * @return A status indicating if the build succeeded or failed
	 */
	private IStatus basicBuild(IProject project, int trigger, String builderName, Map args, IProgressMonitor monitor) {
		monitor = Policy.monitorFor(monitor);
		try {
			String message = NLS.bind(Messages.events_building_1, project.getFullPath());
			monitor.beginTask(message, 1);
			if (!canRun(trigger))
				return Status.OK_STATUS;
			try {
				hookStartBuild(trigger);
				MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, Messages.events_errors, null);
				ICommand command = getCommand(project, builderName, args);
				try {
					IncrementalProjectBuilder builder = getBuilder(project, command, -1, status);
					if (builder != null)
						basicBuild(trigger, builder, args, status, Policy.subMonitorFor(monitor, 1));
				} catch (CoreException e) {
					status.add(e.getStatus());
				}
				return status;
			} finally {
				hookEndBuild(trigger);
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Loop the workspace build until no more builders request a rebuild.
	 */
	private void basicBuildLoop(IProject[] ordered, IProject[] unordered, int trigger, MultiStatus status, IProgressMonitor monitor) {
		int projectWork = ordered.length + unordered.length;
		if (projectWork > 0)
			projectWork = TOTAL_BUILD_WORK / projectWork;
		int maxIterations = workspace.getDescription().getMaxBuildIterations();
		if (maxIterations <= 0)
			maxIterations = 1;
		rebuildRequested = true;
		for (int iter = 0; rebuildRequested && iter < maxIterations; iter++) {
			rebuildRequested = false;
			builtProjects.clear();
			for (int i = 0; i < ordered.length; i++) {
				if (ordered[i].isAccessible()) {
					basicBuild(ordered[i], trigger, status, Policy.subMonitorFor(monitor, projectWork));
					builtProjects.add(ordered[i]);
				}
			}
			for (int i = 0; i < unordered.length; i++) {
				if (unordered[i].isAccessible()) {
					basicBuild(unordered[i], trigger, status, Policy.subMonitorFor(monitor, projectWork));
					builtProjects.add(unordered[i]);
				}
			}
			//subsequent builds should always be incremental
			trigger = IncrementalProjectBuilder.INCREMENTAL_BUILD;
		}
	}

	/**
	 * Runs all builders on all projects. 
	 * @return A status indicating if the build succeeded or failed
	 */
	public IStatus build(int trigger, IProgressMonitor monitor) {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask(Messages.events_building_0, TOTAL_BUILD_WORK);
			if (!canRun(trigger))
				return Status.OK_STATUS;
			try {
				hookStartBuild(trigger);
				IProject[] ordered = workspace.getBuildOrder();
				HashSet leftover = new HashSet(Arrays.asList(workspace.getRoot().getProjects(IContainer.INCLUDE_HIDDEN)));
				leftover.removeAll(Arrays.asList(ordered));
				IProject[] unordered = (IProject[]) leftover.toArray(new IProject[leftover.size()]);
				MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.BUILD_FAILED, Messages.events_errors, null);
				basicBuildLoop(ordered, unordered, trigger, status, monitor);
				return status;
			} finally {
				hookEndBuild(trigger);
			}
		} finally {
			monitor.done();
			if (trigger == IncrementalProjectBuilder.INCREMENTAL_BUILD || trigger == IncrementalProjectBuilder.FULL_BUILD)
				autoBuildJob.avoidBuild();
		}
	}

	/**
	 * Runs the builder with the given name on the given project. 
	 * @return A status indicating if the build succeeded or failed
	 */
	public IStatus build(IProject project, int trigger, String builderName, Map args, IProgressMonitor monitor) {
		monitor = Policy.monitorFor(monitor);
		if (builderName == null)
			return basicBuild(project, trigger, monitor);
		return basicBuild(project, trigger, builderName, args, monitor);
	}

	private boolean canRun(int trigger) {
		return !building;
	}

	/**
	 * Cancel the build if the user has canceled or if an auto-build has been interrupted.
	 */
	private void checkCanceled(int trigger, IProgressMonitor monitor) {
		//if the system is shutting down, don't build
		if (systemBundle.getState() == Bundle.STOPPING)
			throw new OperationCanceledException();
		Policy.checkCanceled(monitor);
		//check for auto-cancel only if we are auto-building
		if (trigger != IncrementalProjectBuilder.AUTO_BUILD)
			return;
		//check for request to interrupt the auto-build
		if (autoBuildJob.isInterrupted())
			throw new OperationCanceledException();
	}

	/**
	 * Creates and returns an ArrayList of BuilderPersistentInfo. 
	 * The list includes entries for all builders that are
	 * in the builder spec, and that have a last built state, even if they 
	 * have not been instantiated this session.
	 */
	public ArrayList createBuildersPersistentInfo(IProject project) throws CoreException {
		/* get the old builders (those not yet instantiated) */
		ArrayList oldInfos = getBuildersPersistentInfo(project);

		ICommand[] commands = ((Project) project).internalGetDescription().getBuildSpec(false);
		if (commands.length == 0)
			return null;

		/* build the new list */
		ArrayList newInfos = new ArrayList(commands.length);
		for (int i = 0; i < commands.length; i++) {
			String builderName = commands[i].getBuilderName();
			BuilderPersistentInfo info = null;
			IncrementalProjectBuilder builder = ((BuildCommand) commands[i]).getBuilder();
			if (builder == null) {
				// if the builder was not instantiated, use the old info if any.
				if (oldInfos != null)
					info = getBuilderInfo(oldInfos, builderName, i);
			} else if (!(builder instanceof MissingBuilder)) {
				ElementTree oldTree = ((InternalBuilder) builder).getLastBuiltTree();
				//don't persist build state for builders that have no last built state
				if (oldTree != null) {
					// if the builder was instantiated, construct a memento with the important info
					info = new BuilderPersistentInfo(project.getName(), builderName, i);
					info.setLastBuildTree(oldTree);
					info.setInterestingProjects(((InternalBuilder) builder).getInterestingProjects());
				}
			}
			if (info != null)
				newInfos.add(info);
		}
		return newInfos;
	}

	private String debugBuilder() {
		return currentBuilder == null ? "<no builder>" : currentBuilder.getClass().getName(); //$NON-NLS-1$
	}

	private String debugProject() {
		if (currentBuilder == null)
			return "<no project>"; //$NON-NLS-1$
		return currentBuilder.getProject().getFullPath().toString();
	}

	/**
	 * Returns a string representation of a build trigger for debugging purposes.
	 * @param trigger The trigger to compute a representation of
	 * @return A string describing the trigger.
	 */
	private String debugTrigger(int trigger) {
		switch (trigger) {
			case IncrementalProjectBuilder.FULL_BUILD :
				return "FULL_BUILD"; //$NON-NLS-1$
			case IncrementalProjectBuilder.CLEAN_BUILD :
				return "CLEAN_BUILD"; //$NON-NLS-1$
			case IncrementalProjectBuilder.AUTO_BUILD :
			case IncrementalProjectBuilder.INCREMENTAL_BUILD :
			default :
				return "INCREMENTAL_BUILD"; //$NON-NLS-1$
		}
	}

	/**
	 * The outermost workspace operation has finished.  Do an autobuild if necessary.
	 */
	public void endTopLevel(boolean needsBuild) {
		autoBuildJob.build(needsBuild);
	}

	/**
	 * Returns the value of the boolean configuration element attribute with the
	 * given name, or <code>false</code> if the attribute is missing.
	 */
	private boolean getBooleanAttribute(IConfigurationElement element, String name) {
		String valueString = element.getAttribute(name);
		return valueString != null && valueString.equalsIgnoreCase(Boolean.TRUE.toString());
	}

	/**
	 * 	Returns the builder instance corresponding to the given command, or
	 * <code>null</code> if the builder was not valid.
	 * @param project The project this builder corresponds to
	 * @param command The build command
	 * @param buildSpecIndex The index of this builder in the build spec, or -1 if
	 * the index is unknown
	 * @param status MultiStatus for collecting errors
	 */
	private IncrementalProjectBuilder getBuilder(IProject project, ICommand command, int buildSpecIndex, MultiStatus status) throws CoreException {
		InternalBuilder result = ((BuildCommand) command).getBuilder();
		if (result == null) {
			result = initializeBuilder(command.getBuilderName(), project, buildSpecIndex, status);
			((BuildCommand) command).setBuilder((IncrementalProjectBuilder) result);
			result.setCommand(command);
			result.setProject(project);
			result.startupOnInitialize();
		}
		if (!validateNature(result, command.getBuilderName())) {
			//skip this builder and null its last built tree because it is invalid
			//if the nature gets added or re-enabled a full build will be triggered
			result.setLastBuiltTree(null);
			return null;
		}
		return (IncrementalProjectBuilder) result;
	}

	/**
	 * Removes the builder persistent info from the map corresponding to the
	 * given builder name and build spec index, or <code>null</code> if not found
	 * 
	 * @param buildSpecIndex The index in the build spec, or -1 if unknown
	 */
	private BuilderPersistentInfo getBuilderInfo(ArrayList infos, String builderName, int buildSpecIndex) {
		//try to match on builder index, but if not match is found, use the name alone
		//this is because older workspace versions did not store builder infos in build spec order
		BuilderPersistentInfo nameMatch = null;
		for (Iterator it = infos.iterator(); it.hasNext();) {
			BuilderPersistentInfo info = (BuilderPersistentInfo) it.next();
			//match on name and build spec index if known
			if (info.getBuilderName().equals(builderName)) {
				//we have found a match on name alone
				if (nameMatch == null)
					nameMatch = info;
				//see if the index matches
				if (buildSpecIndex == -1 || info.getBuildSpecIndex() == -1 || buildSpecIndex == info.getBuildSpecIndex())
					return info;
			}
		}
		//no exact index match, so return name match, if any
		return nameMatch;
	}

	/**
	 * Returns a list of BuilderPersistentInfo.
	 * The list includes entries for all builders that are in the builder spec,
	 * and that have a last built state but have not been instantiated this session.
	 */
	public ArrayList getBuildersPersistentInfo(IProject project) throws CoreException {
		return (ArrayList) project.getSessionProperty(K_BUILD_LIST);
	}

	/**
	 * Returns a build command for the given builder name and project.
	 * First looks for matching command in the project's build spec. If none
	 * is found, a new command is created and returned. This is necessary
	 * because IProject.build allows a builder to be executed that is not in the
	 * build spec.
	 */
	private ICommand getCommand(IProject project, String builderName, Map args) {
		ICommand[] buildSpec = ((Project) project).internalGetDescription().getBuildSpec(false);
		for (int i = 0; i < buildSpec.length; i++)
			if (buildSpec[i].getBuilderName().equals(builderName))
				return buildSpec[i];
		//none found, so create a new command
		BuildCommand result = new BuildCommand();
		result.setBuilderName(builderName);
		result.setArguments(args);
		return result;
	}

	IResourceDelta getDelta(IProject project) {
		try {
			lock.acquire();
			if (currentTree == null) {
				if (Policy.DEBUG_BUILD_FAILURE)
					Policy.debug("Build: no tree for delta " + debugBuilder() + " [" + debugProject() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return null;
			}
			//check if this builder has indicated it cares about this project
			if (!isInterestingProject(project)) {
				if (Policy.DEBUG_BUILD_FAILURE)
					Policy.debug("Build: project not interesting for this builder " + debugBuilder() + " [" + debugProject() + "] " + project.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return null;
			}
			//check if this project has changed
			if (currentDelta != null && currentDelta.findNodeAt(project.getFullPath()) == null) {
				//if the project never existed (not in delta and not in current tree), return null
				if (!project.exists())
					return null;
				//just return an empty delta rooted at this project
				return ResourceDeltaFactory.newEmptyDelta(project);
			}
			//now check against the cache
			IResourceDelta result = (IResourceDelta) deltaCache.getDelta(project.getFullPath(), currentLastBuiltTree, currentTree);
			if (result != null)
				return result;

			long startTime = 0L;
			if (Policy.DEBUG_BUILD_DELTA) {
				startTime = System.currentTimeMillis();
				Policy.debug("Computing delta for project: " + project.getName()); //$NON-NLS-1$
			}
			result = ResourceDeltaFactory.computeDelta(workspace, currentLastBuiltTree, currentTree, project.getFullPath(), -1);
			deltaCache.cache(project.getFullPath(), currentLastBuiltTree, currentTree, result);
			if (Policy.DEBUG_BUILD_FAILURE && result == null)
				Policy.debug("Build: no delta " + debugBuilder() + " [" + debugProject() + "] " + project.getFullPath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (Policy.DEBUG_BUILD_DELTA)
				Policy.debug("Finished computing delta, time: " + (System.currentTimeMillis() - startTime) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
			return result;
		} finally {
			lock.release();
		}
	}

	/**
	 * Returns the safe runnable instance for invoking a builder
	 */
	private ISafeRunnable getSafeRunnable(final int trigger, final Map args, final MultiStatus status, final IProgressMonitor monitor) {
		return new ISafeRunnable() {
			public void handleException(Throwable e) {
				if (e instanceof OperationCanceledException) {
					if (Policy.DEBUG_BUILD_INVOKING)
						Policy.debug("Build canceled"); //$NON-NLS-1$
					//just discard built state when a builder cancels, to ensure
					//that it is called again on the very next build.
					currentBuilder.forgetLastBuiltState();
					throw (OperationCanceledException) e;
				}
				//ResourceStats.buildException(e);
				// don't log the exception....it is already being logged in SafeRunner#run

				//add a generic message to the MultiStatus
				String builderName = currentBuilder.getLabel();
				if (builderName == null || builderName.length() == 0)
					builderName = currentBuilder.getClass().getName();
				String pluginId = currentBuilder.getPluginId();
				String message = NLS.bind(Messages.events_builderError, builderName, currentBuilder.getProject().getName());
				status.add(new Status(IStatus.WARNING, pluginId, IResourceStatus.BUILD_FAILED, message, null));

				//add the exception status to the MultiStatus
				if (e instanceof CoreException)
					status.add(((CoreException) e).getStatus());
				else {
					message = e.getMessage();
					if (message == null)
						message = NLS.bind(Messages.events_unknown, e.getClass().getName(), builderName);
					status.add(new Status(IStatus.WARNING, pluginId, IResourceStatus.BUILD_FAILED, message, e));
				}
			}

			public void run() throws Exception {
				IProject[] prereqs = null;
				//invoke the appropriate build method depending on the trigger
				if (trigger != IncrementalProjectBuilder.CLEAN_BUILD)
					prereqs = currentBuilder.build(trigger, args, monitor);
				else
					currentBuilder.clean(monitor);
				if (prereqs == null)
					prereqs = new IProject[0];
				currentBuilder.setInterestingProjects((IProject[]) prereqs.clone());
			}
		};
	}

	/**
	 * We know the work manager is always available in the middle of
	 * a build.
	 */
	private WorkManager getWorkManager() {
		try {
			return workspace.getWorkManager();
		} catch (CoreException e) {
			//cannot happen
		}
		//avoid compile error
		return null;
	}

	public void handleEvent(LifecycleEvent event) {
		IProject project = null;
		switch (event.kind) {
			case LifecycleEvent.PRE_PROJECT_DELETE :
			case LifecycleEvent.PRE_PROJECT_MOVE :
				project = (IProject) event.resource;
				//make sure the builder persistent info is deleted for the project move case
				if (project.isAccessible())
					setBuildersPersistentInfo(project, null);
		}
	}

	/**
	 * Returns true if the given project has been built during this build cycle, and
	 * false otherwise.
	 */
	boolean hasBeenBuilt(IProject project) {
		return builtProjects.contains(project);
	}

	/**
	 * Hook for adding trace options and debug information at the end of a build.
	 * This hook is called after each builder instance is called.
	 */
	private void hookEndBuild(IncrementalProjectBuilder builder) {
		if (ResourceStats.TRACE_BUILDERS)
			ResourceStats.endBuild();
		if (!Policy.DEBUG_BUILD_INVOKING || timeStamp == -1)
			return; //builder wasn't called or we are not debugging
		Policy.debug("Builder finished: " + toString(builder) + " time: " + (System.currentTimeMillis() - timeStamp) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		timeStamp = -1;
	}

	/**
	 * Hook for adding trace options and debug information at the end of a build.
	 * This hook is called at the end of a build cycle invoked by calling a
	 * build API method.
	 */
	private void hookEndBuild(int trigger) {
		building = false;
		builtProjects.clear();
		deltaCache.flush();
		deltaTreeCache.flush();
		//ensure autobuild runs after a clean
		if (trigger == IncrementalProjectBuilder.CLEAN_BUILD)
			autoBuildJob.forceBuild();
	}

	/**
	 * Hook for adding trace options and debug information at the start of a build.
	 * This hook is called before each builder instance is called.
	 */
	private void hookStartBuild(IncrementalProjectBuilder builder, int trigger) {
		if (ResourceStats.TRACE_BUILDERS)
			ResourceStats.startBuild(builder);
		if (Policy.DEBUG_BUILD_INVOKING) {
			timeStamp = System.currentTimeMillis();
			Policy.debug("Invoking (" + debugTrigger(trigger) + ") on builder: " + toString(builder)); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Hook for adding trace options and debug information at the start of a build.
	 * This hook is called when a build API method is called, before any builders
	 * start running.
	 */
	private void hookStartBuild(int trigger) {
		building = true;
		if (Policy.DEBUG_BUILD_STACK) {
			IStatus info = new Status(IStatus.INFO, ResourcesPlugin.PI_RESOURCES, 1, "Starting build: " + debugTrigger(trigger), new RuntimeException().fillInStackTrace()); //$NON-NLS-1$
			Policy.log(info);
		}
	}

	/**
	 * Instantiates the builder with the given name.  If the builder, its plugin, or its nature
	 * is missing, create a placeholder builder to takes its place.  This is needed to generate 
	 * appropriate exceptions when somebody tries to invoke the builder, and to
	 * prevent trying to instantiate it every time a build is run.
	 * This method NEVER returns null.
	 */
	private IncrementalProjectBuilder initializeBuilder(String builderName, IProject project, int buildSpecIndex, MultiStatus status) throws CoreException {
		IncrementalProjectBuilder builder = null;
		try {
			builder = instantiateBuilder(builderName);
		} catch (CoreException e) {
			status.add(new ResourceStatus(IResourceStatus.BUILD_FAILED, project.getFullPath(), NLS.bind(Messages.events_instantiate_1, builderName), e));
			status.add(e.getStatus());
		}
		if (builder == null) {
			//unable to create the builder, so create a placeholder to fill in for it
			builder = new MissingBuilder(builderName);
		}
		// get the map of builders to get the last built tree
		ArrayList infos = getBuildersPersistentInfo(project);
		if (infos != null) {
			BuilderPersistentInfo info = getBuilderInfo(infos, builderName, buildSpecIndex);
			if (info != null) {
				infos.remove(info);
				ElementTree tree = info.getLastBuiltTree();
				if (tree != null)
					((InternalBuilder) builder).setLastBuiltTree(tree);
				((InternalBuilder) builder).setInterestingProjects(info.getInterestingProjects());
			}
			// delete the build map if it's now empty 
			if (infos.size() == 0)
				setBuildersPersistentInfo(project, null);
		}
		return builder;
	}

	/**
	 * Instantiates and returns the builder with the given name.  If the builder, its plugin, or its nature
	 * is missing, returns null.
	 */
	private IncrementalProjectBuilder instantiateBuilder(String builderName) throws CoreException {
		IExtension extension = Platform.getExtensionRegistry().getExtension(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_BUILDERS, builderName);
		if (extension == null)
			return null;
		IConfigurationElement[] configs = extension.getConfigurationElements();
		if (configs.length == 0)
			return null;
		String natureId = null;
		if (getBooleanAttribute(configs[0], "hasNature")) { //$NON-NLS-1$
			//find the nature that owns this builder
			String builderId = extension.getUniqueIdentifier();
			natureId = workspace.getNatureManager().findNatureForBuilder(builderId);
			if (natureId == null)
				return null;
		}
		//The nature exists, or this builder doesn't specify a nature
		InternalBuilder builder = (InternalBuilder) configs[0].createExecutableExtension("run"); //$NON-NLS-1$
		builder.setPluginId(extension.getContributor().getName());
		builder.setLabel(extension.getLabel());
		builder.setNatureId(natureId);
		builder.setCallOnEmptyDelta(getBooleanAttribute(configs[0], "callOnEmptyDelta")); //$NON-NLS-1$
		return (IncrementalProjectBuilder) builder;
	}

	/**
	 * Another thread is attempting to modify the workspace. Cancel the
	 * autobuild and wait until it completes.
	 */
	public void interrupt() {
		autoBuildJob.interrupt();
	}

	/**
	 * Returns true if the current builder is interested in changes
	 * to the given project, and false otherwise.
	 */
	private boolean isInterestingProject(IProject project) {
		if (project.equals(currentBuilder.getProject()))
			return true;
		IProject[] interestingProjects = currentBuilder.getInterestingProjects();
		for (int i = 0; i < interestingProjects.length; i++) {
			if (interestingProjects[i].equals(project)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the given builder needs to be invoked, and false
	 * otherwise.
	 * 
	 * The algorithm is to compute the intersection of the set of projects that
	 * have changed since the last build, and the set of projects this builder
	 * cares about.  This is an optimization, under the assumption that computing
	 * the forward delta once (not the resource delta) is more efficient than
	 * computing project deltas and invoking builders for projects that haven't
	 * changed.
	 */
	private boolean needsBuild(InternalBuilder builder, int trigger) {
		//on some triggers we build regardless of the delta
		switch (trigger) {
			case IncrementalProjectBuilder.CLEAN_BUILD :
				return true;
			case IncrementalProjectBuilder.FULL_BUILD :
				return true;
			case IncrementalProjectBuilder.INCREMENTAL_BUILD :
				if (currentBuilder.callOnEmptyDelta())
					return true;
				//fall through and check if there is a delta
		}

		//compute the delta since the last built state
		ElementTree oldTree = builder.getLastBuiltTree();
		ElementTree newTree = workspace.getElementTree();
		long start = System.currentTimeMillis();
		currentDelta = (DeltaDataTree) deltaTreeCache.getDelta(null, oldTree, newTree);
		if (currentDelta == null) {
			if (Policy.DEBUG_BUILD_NEEDED) {
				String message = "Checking if need to build. Starting delta computation between: " + oldTree.toString() + " and " + newTree.toString(); //$NON-NLS-1$ //$NON-NLS-2$
				Policy.debug(message);
			}
			currentDelta = newTree.getDataTree().forwardDeltaWith(oldTree.getDataTree(), ResourceComparator.getBuildComparator());
			if (Policy.DEBUG_BUILD_NEEDED)
				Policy.debug("End delta computation. (" + (System.currentTimeMillis() - start) + "ms)."); //$NON-NLS-1$ //$NON-NLS-2$
			deltaTreeCache.cache(null, oldTree, newTree, currentDelta);
		}

		//search for the builder's project
		if (currentDelta.findNodeAt(builder.getProject().getFullPath()) != null) {
			if (Policy.DEBUG_BUILD_NEEDED)
				Policy.debug(toString(builder) + " needs building because of changes in: " + builder.getProject().getName()); //$NON-NLS-1$
			return true;
		}

		//search for builder's interesting projects
		IProject[] projects = builder.getInterestingProjects();
		for (int i = 0; i < projects.length; i++) {
			if (currentDelta.findNodeAt(projects[i].getFullPath()) != null) {
				if (Policy.DEBUG_BUILD_NEEDED)
					Policy.debug(toString(builder) + " needs building because of changes in: " + projects[i].getName()); //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	/**
	 * Removes all builders with the given ID from the build spec.
	 * Does nothing if there were no such builders in the spec
	 */
	private void removeBuilders(IProject project, String builderId) throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] oldSpec = desc.getBuildSpec();
		int oldLength = oldSpec.length;
		if (oldLength == 0)
			return;
		int remaining = 0;
		//null out all commands that match the builder to remove
		for (int i = 0; i < oldSpec.length; i++) {
			if (oldSpec[i].getBuilderName().equals(builderId))
				oldSpec[i] = null;
			else
				remaining++;
		}
		//check if any were actually removed
		if (remaining == oldSpec.length)
			return;
		ICommand[] newSpec = new ICommand[remaining];
		for (int i = 0, newIndex = 0; i < oldLength; i++) {
			if (oldSpec[i] != null)
				newSpec[newIndex++] = oldSpec[i];
		}
		desc.setBuildSpec(newSpec);
		project.setDescription(desc, IResource.NONE, null);
	}

	/**
	 * Hook for builders to request a rebuild.
	 */
	void requestRebuild() {
		rebuildRequested = true;
	}

	/**
	 * Sets the builder infos for the given project.  The builder infos are
	 * an ArrayList of BuilderPersistentInfo.
	 * The list includes entries for all builders that are
	 * in the builder spec, and that have a last built state, even if they 
	 * have not been instantiated this session.
	 */
	public void setBuildersPersistentInfo(IProject project, ArrayList list) {
		try {
			project.setSessionProperty(K_BUILD_LIST, list);
		} catch (CoreException e) {
			//project is missing -- build state will be lost
			//can't throw an exception because this happens on startup
			Policy.log(new ResourceStatus(IStatus.ERROR, 1, project.getFullPath(), "Project missing in setBuildersPersistentInfo", null)); //$NON-NLS-1$
		}
	}

	public void shutdown(IProgressMonitor monitor) {
		autoBuildJob.cancel();
	}

	public void startup(IProgressMonitor monitor) {
		workspace.addLifecycleListener(this);
	}

	/**
	 * Returns a string representation of the given builder.  
	 * For debugging purposes only.
	 */
	private String toString(InternalBuilder builder) {
		String name = builder.getClass().getName();
		name = name.substring(name.lastIndexOf('.') + 1);
		return name + "(" + builder.getProject().getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns true if the nature membership rules are satisfied for the given
	 * builder extension on the given project, and false otherwise.  A builder that 
	 * does not specify that it belongs to a nature is always valid.  A builder 
	 * extension that belongs to a nature can be invalid for the following reasons:
	 * <ul>
	 * <li>The nature that owns the builder does not exist on the given project</li>
	 * <li>The nature that owns the builder is disabled on the given project</li>
	 * </ul>
	 * Furthermore, if the nature that owns the builder does not exist on the project,
	 * that builder will be removed from the build spec.
	 * 
	 * Note: This method only validates nature constraints that can vary at runtime.
	 * Additional checks are done in the instantiateBuilder method for constraints
	 * that cannot vary once the plugin registry is initialized.
	 */
	private boolean validateNature(InternalBuilder builder, String builderId) throws CoreException {
		String nature = builder.getNatureId();
		if (nature == null)
			return true;
		IProject project = builder.getProject();
		if (!project.hasNature(nature)) {
			//remove this builder from the build spec
			removeBuilders(project, builderId);
			return false;
		}
		return project.isNatureEnabled(nature);
	}
	
	/**
	 * Returns the scheduling rule that is required for building the project.
	 */
	public ISchedulingRule getRule(IProject project, int trigger, String builderName, Map args) {

		MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, Messages.events_errors, null);
		if (builderName == null) {
			final ICommand[] commands;
			if (project.isAccessible()) {
				Set rules = new HashSet();
				commands = ((Project) project).internalGetDescription().getBuildSpec(false);
				for (int i = 0; i < commands.length; i++) {
					BuildCommand command = (BuildCommand) commands[i];
					try {
						IncrementalProjectBuilder builder = getBuilder(project, command, i, status);
						if (builder != null)
							rules.add(builder.getRule());
					} catch (CoreException e) {
						status.add(e.getStatus());
					}
				}
				return new MultiRule((ISchedulingRule[]) rules.toArray(new ISchedulingRule[rules.size()]));
			}
		} else {
			// Returns the derived resources for the specified builderName
			ICommand command = getCommand(project, builderName, args);
			try {
				IncrementalProjectBuilder builder = getBuilder(project, command, -1, status);
				if (builder != null)
					return builder.getRule();

			} catch (CoreException e) {
				status.add(e.getStatus());
			}
		}
		// Log any errors
		if (!status.isOK())
			Policy.log(status);
		return workspace.getRoot();
	}
}
