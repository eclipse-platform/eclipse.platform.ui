/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Isaac Pacht (isaacp3@gmail.com) - fix for bug 206540
 *     Anton Leherbauer (Wind River) - [305858] Allow Builder to return null rule
 *     James Blackburn (Broadcom) - [306822] Provide Context for Builder getRule()
 *     Broadcom Corporation - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
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
		@Override
		protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) {
			if (!hasBeenBuilt && Policy.DEBUG_BUILD_FAILURE) {
				hasBeenBuilt = true;
				String msg = NLS.bind(Messages.events_skippingBuilder, name, getProject().getName());
				Policy.log(IStatus.WARNING, msg, null);
			}
			return null;
		}

		String getName() {
			return name;
		}

	}

	private static final int TOTAL_BUILD_WORK = Policy.totalWork * 1000;

	//the job for performing background autobuild
	final AutoBuildJob autoBuildJob;
	private boolean building = false;
	private final Set<IProject> builtProjects = new HashSet<>();

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
	private long overallTimeStamp = -1;
	private Workspace workspace;

	public BuildManager(Workspace workspace, ILock workspaceLock) {
		this.workspace = workspace;
		this.autoBuildJob = new AutoBuildJob(workspace);
		this.lock = workspaceLock;
		InternalBuilder.buildManager = this;
	}

	private void basicBuild(int trigger, IncrementalProjectBuilder builder, Map<String, String> args, MultiStatus status, IProgressMonitor monitor) {
		try {
			currentBuilder = builder;
			//clear any old requests to forget built state
			currentBuilder.clearLastBuiltStateRequests();
			// Figure out want kind of build is needed
			boolean clean = trigger == IncrementalProjectBuilder.CLEAN_BUILD;
			currentLastBuiltTree = currentBuilder.getLastBuiltTree();

			// Does the build command respond to this trigger?
			boolean isBuilding = builder.getCommand().isBuilding(trigger);

			// If no tree is available we have to do a full build
			if (!clean && currentLastBuiltTree == null) {
				// Bug 306746 - Don't promote build to FULL_BUILD if builder doesn't AUTO_BUILD
				if (trigger == IncrementalProjectBuilder.AUTO_BUILD && !isBuilding)
					return;
				// Without a build tree the build is promoted to FULL_BUILD
				trigger = IncrementalProjectBuilder.FULL_BUILD;
				isBuilding = isBuilding || builder.getCommand().isBuilding(trigger);
			}

			//don't build if this builder doesn't respond to the trigger
			if (!isBuilding) {
				if (clean)
					currentBuilder.setLastBuiltTree(null);
				return;
			}

			// For incremental builds, grab a pointer to the current state before computing the delta
			currentTree = ((trigger == IncrementalProjectBuilder.FULL_BUILD) || clean) ? null : workspace.getElementTree();
			int depth = -1;
			ISchedulingRule rule = null;
			try {
				//short-circuit if none of the projects this builder cares about have changed.
				if (!needsBuild(currentBuilder, trigger)) {
					//use up the progress allocated for this builder
					monitor.beginTask("", 1); //$NON-NLS-1$
					monitor.done();
					return;
				}
				rule = builder.getRule(trigger, args);
				String name = currentBuilder.getLabel();
				String message;
				if (name != null)
					message = NLS.bind(Messages.events_invoking_2, name, builder.getProject().getFullPath());
				else
					message = NLS.bind(Messages.events_invoking_1, builder.getProject().getFullPath());
				monitor.subTask(message);
				hookStartBuild(builder, trigger);
				// Make the current tree immutable before releasing the WS lock
				if (rule != null && currentTree != null)
					workspace.newWorkingTree();
				//release workspace lock while calling builders
				depth = getWorkManager().beginUnprotected();
				// Acquire the rule required for running this builder
				if (rule != null) {
					Job.getJobManager().beginRule(rule, monitor);
					// Now that we've acquired the rule, changes may have been made concurrently, ensure we're pointing at the
					// correct currentTree so delta contains concurrent changes made in areas guarded by the scheduling rule
					if (currentTree != null)
						currentTree = workspace.getElementTree();
				}
				//do the build
				SafeRunner.run(getSafeRunnable(trigger, args, status, monitor));
			} finally {
				// Re-acquire the WS lock, then release the scheduling rule
				if (depth >= 0)
					getWorkManager().endUnprotected(depth);
				if (rule != null)
					Job.getJobManager().endRule(rule);
				// Be sure to clean up after ourselves.
				if (clean || currentBuilder.wasForgetStateRequested()) {
					currentBuilder.setLastBuiltTree(null);
				} else if (currentBuilder.wasRememberStateRequested()) {
					// If remember last build state, and FULL_BUILD
					// last tree must be set to => null for next build
					if (trigger == IncrementalProjectBuilder.FULL_BUILD)
						currentBuilder.setLastBuiltTree(null);
					// else don't modify the last built tree
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

	protected void basicBuild(IBuildConfiguration buildConfiguration, int trigger, IBuildContext context, ICommand[] commands, MultiStatus status, IProgressMonitor monitor) {
		try {
			for (int i = 0; i < commands.length; i++) {
				checkCanceled(trigger, monitor);
				BuildCommand command = (BuildCommand) commands[i];
				IProgressMonitor sub = Policy.subMonitorFor(monitor, 1);
				IncrementalProjectBuilder builder = getBuilder(buildConfiguration, command, i, status, context);
				if (builder != null)
					basicBuild(trigger, builder, command.getArguments(false), status, sub);
			}
		} catch (CoreException e) {
			status.add(e.getStatus());
		}
	}

	/**
	 * Runs all builders on the given project config.
	 * @return A status indicating if the build succeeded or failed
	 */
	private IStatus basicBuild(IBuildConfiguration buildConfiguration, int trigger, IBuildContext context, IProgressMonitor monitor) {
		if (!canRun(trigger))
			return Status.OK_STATUS;
		try {
			hookStartBuild(new IBuildConfiguration[] {buildConfiguration}, trigger);
			MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, Messages.events_errors, null);
			basicBuild(buildConfiguration, trigger, context, status, monitor);
			return status;
		} finally {
			hookEndBuild(trigger);
		}
	}

	private void basicBuild(final IBuildConfiguration buildConfiguration, final int trigger, final IBuildContext context, final MultiStatus status, final IProgressMonitor monitor) {
		try {
			final IProject project = buildConfiguration.getProject();
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
				@Override
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

				@Override
				public void run() throws Exception {
					basicBuild(buildConfiguration, trigger, context, commands, status, monitor);
				}
			};
			SafeRunner.run(code);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Runs the builder with the given name on the given project config.
	 * @return A status indicating if the build succeeded or failed
	 */
	private IStatus basicBuild(IBuildConfiguration buildConfiguration, int trigger, String builderName, Map<String, String> args, IProgressMonitor monitor) {
		final IProject project = buildConfiguration.getProject();
		monitor = Policy.monitorFor(monitor);
		try {
			String message = NLS.bind(Messages.events_building_1, project.getFullPath());
			monitor.beginTask(message, 1);
			if (!canRun(trigger))
				return Status.OK_STATUS;
			try {
				hookStartBuild(new IBuildConfiguration[] {buildConfiguration}, trigger);
				MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, Messages.events_errors, null);
				ICommand command = getCommand(project, builderName, args);
				try {
					IBuildContext context = new BuildContext(buildConfiguration);
					IncrementalProjectBuilder builder = getBuilder(buildConfiguration, command, -1, status, context);
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
	private void basicBuildLoop(IBuildConfiguration[] configs, IBuildConfiguration[] requestedConfigs, int trigger, MultiStatus status, IProgressMonitor monitor) {
		int projectWork = configs.length;
		if (projectWork > 0)
			projectWork = TOTAL_BUILD_WORK / projectWork;
		int maxIterations = workspace.getDescription().getMaxBuildIterations();
		if (maxIterations <= 0)
			maxIterations = 1;
		rebuildRequested = true;
		for (int iter = 0; rebuildRequested && iter < maxIterations; iter++) {
			rebuildRequested = false;
			builtProjects.clear();
			for (int i = 0; i < configs.length; i++) {
				if (configs[i].getProject().isAccessible()) {
					IBuildContext context = new BuildContext(configs[i], requestedConfigs, configs);
					basicBuild(configs[i], trigger, context, status, Policy.subMonitorFor(monitor, projectWork));
					builtProjects.add(configs[i].getProject());
				}
			}
			//subsequent builds should always be incremental
			trigger = IncrementalProjectBuilder.INCREMENTAL_BUILD;
		}
	}

	/**
	 * Runs all builders on all the given project configs, in the order that
	 * they are given.
	 * @return A status indicating if the build succeeded or failed
	 */
	public IStatus build(IBuildConfiguration[] configs, IBuildConfiguration[] requestedConfigs, int trigger, IProgressMonitor monitor) {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask(Messages.events_building_0, TOTAL_BUILD_WORK);
			if (!canRun(trigger))
				return Status.OK_STATUS;
			try {
				hookStartBuild(configs, trigger);
				MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.BUILD_FAILED, Messages.events_errors, null);
				basicBuildLoop(configs, requestedConfigs, trigger, status, monitor);
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
	 * Runs the builder with the given name on the given project config.
	 * @return A status indicating if the build succeeded or failed
	 */
	public IStatus build(IBuildConfiguration buildConfiguration, int trigger, String builderName, Map<String, String> args, IProgressMonitor monitor) {
		monitor = Policy.monitorFor(monitor);
		if (builderName == null) {
			IBuildContext context = new BuildContext(buildConfiguration);
			return basicBuild(buildConfiguration, trigger, context, monitor);
		}
		return basicBuild(buildConfiguration, trigger, builderName, args, monitor);
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
	 * The list includes entries for all builders for all configs that are
	 * in the builder spec, and that have a last built state, even if they
	 * have not been instantiated this session.
	 *
	 * e.g.
	 * For a project with 3 builders, 2 build configurations and the second
	 * builder doesn't support configurations.
	 * The returned List of BuilderInfos is ordered:
	 * builder_id, config_name,builder_index
	 * builder_1,  config_1, 1
	 * builder_1,  config_2, 1
	 * builder_2,  null,     2
	 * builder_3,  config_1, 3
	 * builder_3,  config_1, 3
	 *
	 */
	public ArrayList<BuilderPersistentInfo> createBuildersPersistentInfo(IProject project) throws CoreException {
		/* get the old builders (those not yet instantiated) */
		ArrayList<BuilderPersistentInfo> oldInfos = getBuildersPersistentInfo(project);

		ProjectDescription desc = ((Project) project).internalGetDescription();
		ICommand[] commands = desc.getBuildSpec(false);
		if (commands.length == 0)
			return null;
		IBuildConfiguration[] configs = project.getBuildConfigs();

		/* build the new list */
		ArrayList<BuilderPersistentInfo> newInfos = new ArrayList<>(commands.length * configs.length);
		for (int i = 0; i < commands.length; i++) {
			BuildCommand command = (BuildCommand) commands[i];
			String builderName = command.getBuilderName();

			// If the builder doesn't support configurations, only 1 delta tree to persist
			boolean supportsConfigs = command.supportsConfigs();
			int numberConfigs = supportsConfigs ? configs.length : 1;

			for (int j = 0; j < numberConfigs; j++) {
				IBuildConfiguration config = configs[j];
				BuilderPersistentInfo info = null;
				IncrementalProjectBuilder builder = ((BuildCommand) commands[i]).getBuilder(config);
				if (builder == null) {
					// if the builder was not instantiated, use the old info if any.
					if (oldInfos != null)
						info = getBuilderInfo(oldInfos, builderName, supportsConfigs ? config.getName() : null, i);
				} else if (!(builder instanceof MissingBuilder)) {
					ElementTree oldTree = ((InternalBuilder) builder).getLastBuiltTree();
					//don't persist build state for builders that have no last built state
					if (oldTree != null) {
						// if the builder was instantiated, construct a memento with the important info
						info = new BuilderPersistentInfo(project.getName(), supportsConfigs ? config.getName() : null, builderName, i);
						info.setLastBuildTree(oldTree);
						info.setInterestingProjects(((InternalBuilder) builder).getInterestingProjects());
					}
				}
				if (info != null)
					newInfos.add(info);
			}
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
	 * @param buildConfiguration The project config this builder corresponds to
	 * @param command The build command
	 * @param buildSpecIndex The index of this builder in the build spec, or -1 if
	 * the index is unknown
	 * @param status MultiStatus for collecting errors
	 */
	private IncrementalProjectBuilder getBuilder(IBuildConfiguration buildConfiguration, ICommand command, int buildSpecIndex, MultiStatus status) throws CoreException {
		InternalBuilder result = ((BuildCommand) command).getBuilder(buildConfiguration);
		if (result == null) {
			result = initializeBuilder(command.getBuilderName(), buildConfiguration, buildSpecIndex, status);
			result.setCommand(command);
			result.setBuildConfig(buildConfiguration);
			result.startupOnInitialize();
			((BuildCommand) command).addBuilder(buildConfiguration, (IncrementalProjectBuilder) result);
		}
		// Ensure the build configuration stays fresh for non-config aware builders
		result.setBuildConfig(buildConfiguration);
		if (!validateNature(result, command.getBuilderName())) {
			//skip this builder and null its last built tree because it is invalid
			//if the nature gets added or re-enabled a full build will be triggered
			result.setLastBuiltTree(null);
			return null;
		}
		return (IncrementalProjectBuilder) result;
	}

	/**
	 * Returns the builder instance corresponding to the given command, or
	 * <code>null</code> if the builder was not valid, and sets its context
	 * to the one supplied.
	 *
	 * @param buildConfiguration The project config this builder corresponds to
	 * @param command The build command
	 * @param buildSpecIndex The index of this builder in the build spec, or -1 if
	 * the index is unknown
	 * @param status MultiStatus for collecting errors
	 */
	private IncrementalProjectBuilder getBuilder(IBuildConfiguration buildConfiguration, ICommand command, int buildSpecIndex, MultiStatus status, IBuildContext context) throws CoreException {
		InternalBuilder builder = getBuilder(buildConfiguration, command, buildSpecIndex, status);
		if (builder != null)
			builder.setContext(context);
		return (IncrementalProjectBuilder) builder;
	}

	/**
	 * Removes the builder persistent info from the map corresponding to the
	 * given builder name, configuration name and build spec index, or <code>null</code> if not found
	 *
	 * @param configName or null if the builder doesn't support configurations
	 * @param buildSpecIndex The index in the build spec, or -1 if unknown
	 */
	private BuilderPersistentInfo getBuilderInfo(ArrayList<BuilderPersistentInfo> infos, String builderName, String configName, int buildSpecIndex) {
		//try to match on builder index, but if not match is found, use the builder name and config name
		//this is because older workspace versions did not store builder infos in build spec order
		BuilderPersistentInfo nameMatch = null;
		for (Iterator<BuilderPersistentInfo> it = infos.iterator(); it.hasNext();) {
			BuilderPersistentInfo info = it.next();
			// match on name, config name and build spec index if known
			// Note: the config name may be null for builders that don't support configurations, or old workspaces
			if (info.getBuilderName().equals(builderName) && (info.getConfigName() == null || info.getConfigName().equals(configName))) {
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
	@SuppressWarnings({"unchecked"})
	public ArrayList<BuilderPersistentInfo> getBuildersPersistentInfo(IProject project) throws CoreException {
		return (ArrayList<BuilderPersistentInfo>) project.getSessionProperty(K_BUILD_LIST);
	}

	/**
	 * Returns a build command for the given builder name and project.
	 * First looks for matching command in the project's build spec. If none
	 * is found, a new command is created and returned. This is necessary
	 * because IProject.build allows a builder to be executed that is not in the
	 * build spec.
	 */
	private ICommand getCommand(IProject project, String builderName, Map<String, String> args) {
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

	/**
	 * Gets a workspace delta for a given project, based on the state of the workspace
	 * tree the last time the current builder was run.
	 * <p>
	 * Returns null if:
	 * <ul>
	 * <li> The state of the workspace is unknown. </li>
	 * <li> The current builder has not indicated that it is interested in deltas
	 * for the given project. </li>
	 * <li> If the project does not exist. </li>
	 * </ul>
	 * <p>
	 * Deltas are computed once and cached for efficiency.
	 *
	 * @param project the project to get a delta for
	 */
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
				Policy.debug("Finished computing delta, time: " + (System.currentTimeMillis() - startTime) + "ms" + ((ResourceDelta) result).toDeepDebugString()); //$NON-NLS-1$ //$NON-NLS-2$
			return result;
		} finally {
			lock.release();
		}
	}

	/**
	 * Returns the safe runnable instance for invoking a builder
	 */
	private ISafeRunnable getSafeRunnable(final int trigger, final Map<String, String> args, final MultiStatus status, final IProgressMonitor monitor) {
		return new ISafeRunnable() {
			@Override
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
				status.add(new Status(IStatus.ERROR, pluginId, IResourceStatus.BUILD_FAILED, message, e));

				//add the exception status to the MultiStatus
				if (e instanceof CoreException)
					status.add(((CoreException) e).getStatus());
			}

			@Override
			public void run() throws Exception {
				IProject[] prereqs = null;
				//invoke the appropriate build method depending on the trigger
				if (trigger != IncrementalProjectBuilder.CLEAN_BUILD)
					prereqs = currentBuilder.build(trigger, args, monitor);
				else
					currentBuilder.clean(monitor);
				if (prereqs == null)
					prereqs = new IProject[0];
				currentBuilder.setInterestingProjects(prereqs.clone());
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

	@Override
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
	 * Returns true if at least one of the given project's configs have been built
	 * during this build cycle; and false otherwise.
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
		if (Policy.DEBUG_BUILD_INVOKING) {
			Policy.debug("Top-level build-end time: " + (System.currentTimeMillis() - overallTimeStamp)); //$NON-NLS-1$
			overallTimeStamp = -1;
		}
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
	private void hookStartBuild(IBuildConfiguration[] configs, int trigger) {
		building = true;
		if (Policy.DEBUG_BUILD_STACK)
			Policy.debug(new RuntimeException("Starting build: " + debugTrigger(trigger))); //$NON-NLS-1$
		if (Policy.DEBUG_BUILD_INVOKING) {
			overallTimeStamp = System.currentTimeMillis();
			StringBuilder sb = new StringBuilder("Top-level build-start of: "); //$NON-NLS-1$
			for (int i = 0; i < configs.length; i++)
				sb.append(configs[i]).append(", "); //$NON-NLS-1$
			sb.append(debugTrigger(trigger));
			Policy.debug(sb.toString());
		}
	}

	/**
	 * Instantiates the builder with the given name.  If the builder, its plugin, or its nature
	 * is missing, create a placeholder builder to takes its place.  This is needed to generate
	 * appropriate exceptions when somebody tries to invoke the builder, and to
	 * prevent trying to instantiate it every time a build is run.
	 * This method NEVER returns null.
	 */
	private IncrementalProjectBuilder initializeBuilder(String builderName, IBuildConfiguration buildConfiguration, int buildSpecIndex, MultiStatus status) throws CoreException {
		IProject project = buildConfiguration.getProject();
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
		ArrayList<BuilderPersistentInfo> infos = getBuildersPersistentInfo(project);
		if (infos != null) {
			BuilderPersistentInfo info = getBuilderInfo(infos, builderName, buildConfiguration.getName(), buildSpecIndex);
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
	 * Returns whether an autobuild is pending (requested but not yet completed).
	 */
	public boolean isAutobuildBuildPending() {
		return autoBuildJob.getState() != Job.NONE;

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
	 * The algorithm is to compute the intersection of the set of build configs that
	 * have changed since the last build, and the set of build configs this builder
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
	 * Sets the builder infos for the given build config.  The builder infos are
	 * an ArrayList of BuilderPersistentInfo.
	 * The list includes entries for all builders that are
	 * in the builder spec, and that have a last built state, even if they
	 * have not been instantiated this session.
	 */
	public void setBuildersPersistentInfo(IProject project, List<BuilderPersistentInfo> list) {
		try {
			project.setSessionProperty(K_BUILD_LIST, list);
		} catch (CoreException e) {
			//project is missing -- build state will be lost
			//can't throw an exception because this happens on startup
			Policy.log(new ResourceStatus(IStatus.ERROR, 1, project.getFullPath(), "Project missing in setBuildersPersistentInfo", null)); //$NON-NLS-1$
		}
	}

	@Override
	public void shutdown(IProgressMonitor monitor) {
		autoBuildJob.cancel();
	}

	@Override
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
		if (builder instanceof MissingBuilder)
			name = name + ": '" + ((MissingBuilder) builder).getName() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
		return name + "(" + builder.getBuildConfig() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
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
	public ISchedulingRule getRule(IBuildConfiguration buildConfiguration, int trigger, String builderName, Map<String, String> args) {
		IProject project = buildConfiguration.getProject();
		MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, Messages.events_errors, null);
		if (builderName == null) {
			final ICommand[] commands;
			if (project.isAccessible()) {
				Set<ISchedulingRule> rules = new HashSet<>();
				commands = ((Project) project).internalGetDescription().getBuildSpec(false);
				boolean hasNullBuildRule = false;
				BuildContext context = new BuildContext(buildConfiguration);
				for (int i = 0; i < commands.length; i++) {
					BuildCommand command = (BuildCommand) commands[i];
					try {
						IncrementalProjectBuilder builder = getBuilder(buildConfiguration, command, i, status, context);
						if (builder != null) {
							ISchedulingRule builderRule = builder.getRule(trigger, args);
							if (builderRule != null)
								rules.add(builderRule);
							else
								hasNullBuildRule = true;
						}
					} catch (CoreException e) {
						status.add(e.getStatus());
					}
				}
				if (rules.isEmpty())
					return null;
				// Bug 306824 - Builders returning a null rule can't work safely if other builders require a non-null rule
				// Be pessimistic and fall back to the default build rule (workspace root) in this case.
				if (!hasNullBuildRule)
					return new MultiRule(rules.toArray(new ISchedulingRule[rules.size()]));
			}
		} else {
			// Returns the derived resources for the specified builderName
			ICommand command = getCommand(project, builderName, args);
			try {
				IncrementalProjectBuilder builder = getBuilder(buildConfiguration, command, -1, status);
				if (builder != null)
					return builder.getRule(trigger, args);

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
