/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.events;

import java.util.*;
import org.eclipse.core.internal.dtree.DeltaDataTree;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ILock;

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

		public void cache(IPath project, ElementTree oldTree, ElementTree newTree, Object delta) {
			this.projectPath = project;
			this.oldTree = oldTree;
			this.newTree = newTree;
			this.delta = delta;
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
		public Object getDelta(IPath project, ElementTree oldTree, ElementTree newTree) {
			if (delta == null)
				return null;
			boolean pathsEqual = projectPath == null ? project == null : projectPath.equals(project);
			if (pathsEqual && this.oldTree == oldTree && this.newTree == newTree)
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
		protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
			if (!hasBeenBuilt) {
				hasBeenBuilt = true;
				String msg = Policy.bind("events.skippingBuilder", new String[] {name, getProject().getName()}); //$NON-NLS-1$
				IStatus status = new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, 1, msg, null);
				ResourcesPlugin.getPlugin().getLog().log(status);
			}
			return null;
		}
	}

	//the job for performing background autobuild
	protected final AutoBuildJob autoBuildJob;
	protected boolean building = false;
	protected final ArrayList builtProjects = new ArrayList();
	protected InternalBuilder currentBuilder;
	protected DeltaDataTree currentDelta;

	//the following four fields only apply for the lifetime of 
	//a single builder invocation.
	protected ElementTree currentTree;
	/**
	 * Caches the IResourceDelta for a pair of trees
	 */
	final protected DeltaCache deltaCache = new DeltaCache();
	/**
	 * Caches the DeltaDataTree used to determine if a build is necessary
	 */
	final protected DeltaCache deltaTreeCache = new DeltaCache();

	protected ElementTree lastBuiltTree;

	//used for the build cycle looping mechanism
	protected boolean rebuildRequested = false;

	//used for debug/trace timing
	private long timeStamp = -1;
	protected Workspace workspace;
	private ILock lock;

	public BuildManager(Workspace workspace, ILock workspaceLock) {
		this.workspace = workspace;
		this.autoBuildJob = new AutoBuildJob(workspace);
		this.lock = workspaceLock;
	}

	protected void basicBuild(int trigger, IncrementalProjectBuilder builder, Map args, MultiStatus status, IProgressMonitor monitor) {
		try {
			currentBuilder = builder;
			//clear any old requests to forget built state
			currentBuilder.clearForgetLastBuiltState();
			// Figure out want kind of build is needed
			boolean clean = trigger == IncrementalProjectBuilder.CLEAN_BUILD;
			lastBuiltTree = currentBuilder.getLastBuiltTree();
			// If no tree is available we have to do a full build
			if (!clean && lastBuiltTree == null)
				trigger = IncrementalProjectBuilder.FULL_BUILD;
			boolean fullBuild = trigger == IncrementalProjectBuilder.FULL_BUILD;
			// Grab a pointer to the current state before computing the delta
			currentTree = fullBuild ? null : workspace.getElementTree();
			int depth = -1;
			try {
				//short-circuit if none of the projects this builder cares about have changed.
				if (!clean && !fullBuild && !needsBuild(currentBuilder))
					return;
				String name = currentBuilder.getLabel();
				String message;
				if (name != null)
					message = Policy.bind("events.invoking.2", name, builder.getProject().getFullPath().toString()); //$NON-NLS-1$
				else
					message = Policy.bind("events.invoking.1", builder.getProject().getFullPath().toString()); //$NON-NLS-1$
				monitor.subTask(message);
				hookStartBuild(builder, trigger);
				//release workspace lock while calling builders
				depth = getWorkManager().beginUnprotected();
				//do the build
				Platform.run(getSafeRunnable(trigger, args, status, monitor));
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
			lastBuiltTree = null;
			currentDelta = null;
		}
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

	protected void basicBuild(IProject project, int trigger, ICommand[] commands, MultiStatus status, IProgressMonitor monitor) {
		monitor = Policy.monitorFor(monitor);
		try {
			String message = Policy.bind("events.building.1", project.getFullPath().toString()); //$NON-NLS-1$
			monitor.beginTask(message, Math.max(1, commands.length));
			for (int i = 0; i < commands.length; i++) {
				checkCanceled(trigger, monitor);
				IProgressMonitor sub = Policy.subMonitorFor(monitor, 1);
				BuildCommand command = (BuildCommand) commands[i];
				basicBuild(project, trigger, command.getBuilderName(), command.getArguments(false), status, sub);
			}
		} finally {
			monitor.done();
		}
	}

	protected void basicBuild(final IProject project, final int trigger, final MultiStatus status, final IProgressMonitor monitor) {
		if (!project.isAccessible())
			return;
		final ICommand[] commands = ((Project) project).internalGetDescription().getBuildSpec(false);
		if (commands.length == 0)
			return;
		ISafeRunnable code = new ISafeRunnable() {
			public void handleException(Throwable e) {
				if (e instanceof OperationCanceledException)
					throw (OperationCanceledException) e;
				// don't log the exception....it is already being logged in Workspace#run
				// should never get here because the lower-level build code wrappers
				// builder exceptions in core exceptions if required.
				String message = e.getMessage();
				if (message == null)
					message = Policy.bind("events.unknown", e.getClass().getName(), currentBuilder.getClass().getName()); //$NON-NLS-1$
				status.add(new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, e));
			}

			public void run() throws Exception {
				basicBuild(project, trigger, commands, status, monitor);
			}
		};
		Platform.run(code);
	}

	protected void basicBuild(IProject project, int trigger, String builderName, Map args, MultiStatus status, IProgressMonitor monitor) {
		IncrementalProjectBuilder builder = null;
		try {
			builder = getBuilder(builderName, project, status);
			if (!validateNature(builder, builderName)) {
				//skip this builder and null its last built tree because it is invalid
				//if the nature gets added or re-enabled a full build will be triggered
				((InternalBuilder) builder).setLastBuiltTree(null);
				return;
			}
		} catch (CoreException e) {
			status.add(e.getStatus());
			return;
		}
		basicBuild(trigger, builder, args, status, monitor);
	}

	/**
	 * Loop the workspace build until no more builders request a rebuild.
	 */
	protected void basicBuildLoop(IProject[] ordered, IProject[] unordered, int trigger, MultiStatus status, IProgressMonitor monitor) {
		int projectWork = ordered.length + unordered.length;
		if (projectWork > 0)
			projectWork = Policy.totalWork / projectWork;
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

	public void build(int trigger, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask(ICoreConstants.MSG_EVENTS_BUILDING_0, Policy.totalWork);
			if (!canRun(trigger))
				return;
			try {
				building = true;
				IProject[] ordered = workspace.getBuildOrder();
				HashSet leftover = new HashSet(Arrays.asList(workspace.getRoot().getProjects()));
				leftover.removeAll(Arrays.asList(ordered));
				IProject[] unordered = (IProject[]) leftover.toArray(new IProject[leftover.size()]);
				MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.BUILD_FAILED, ICoreConstants.MSG_EVENTS_ERRORS, null);

				basicBuildLoop(ordered, unordered, trigger, status, monitor);

				// if the status is not ok, throw an exception 
				if (!status.isOK())
					throw new ResourceException(status);
			} finally {
				cleanup(trigger);
			}
		} finally {
			monitor.done();
			if (trigger == IncrementalProjectBuilder.INCREMENTAL_BUILD || trigger == IncrementalProjectBuilder.FULL_BUILD)
				autoBuildJob.avoidBuild();
		}
	}

	private void cleanup(int trigger) {
		building = false;
		builtProjects.clear();
		deltaCache.flush();
		deltaTreeCache.flush();
		//ensure autobuild runs after a clean
		if (trigger == IncrementalProjectBuilder.CLEAN_BUILD)
			autoBuildJob.forceBuild();
	}

	public void build(IProject project, int trigger, IProgressMonitor monitor) throws CoreException {
		if (!canRun(trigger))
			return;
		try {
			building = true;
			MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, ICoreConstants.MSG_EVENTS_ERRORS, null);
			basicBuild(project, trigger, status, monitor);
			if (!status.isOK())
				throw new ResourceException(status);
		} finally {
			cleanup(trigger);
		}
	}

	public void build(IProject project, int trigger, String builderName, Map args, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			String message = Policy.bind("events.building.1", project.getFullPath().toString()); //$NON-NLS-1$
			monitor.beginTask(message, 1);
			if (!canRun(trigger))
				return;
			try {
				building = true;
				MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, ICoreConstants.MSG_EVENTS_ERRORS, null);
				basicBuild(project, trigger, builderName, args, status, Policy.subMonitorFor(monitor, 1));
				if (!status.isOK())
					throw new ResourceException(status);
			} finally {
				cleanup(trigger);
			}
		} finally {
			monitor.done();
		}
	}

	protected boolean canRun(int trigger) {
		return !building;
	}

	/**
	 * Another thread is attempting to modify the workspace. Cancel the
	 * autobuild and wait until it completes.
	 */
	public void interrupt() {
		autoBuildJob.interrupt();
	}

	/**
	 * Cancel the build if the user has canceled or if an auto-build has been interrupted.
	 */
	private void checkCanceled(int trigger, IProgressMonitor monitor) {
		Policy.checkCanceled(monitor);
		//check for auto-cancel only if we are auto-building
		if (trigger != IncrementalProjectBuilder.AUTO_BUILD)
			return;
		//check for request to interrupt the auto-build
		if (autoBuildJob.isInterrupted())
			throw new OperationCanceledException();
	}

	protected IProject[] computeUnorderedProjects(IProject[] ordered) {
		IProject[] unordered;
		HashSet leftover = new HashSet(Arrays.asList(workspace.getRoot().getProjects()));
		leftover.removeAll(Arrays.asList(ordered));
		unordered = (IProject[]) leftover.toArray(new IProject[leftover.size()]);
		return unordered;
	}

	/**
	 * Creates and returns a Map mapping String(builder name) -> BuilderPersistentInfo. 
	 * The table includes entries for all builders that are
	 * in the builder spec, and that have a last built state, even if they 
	 * have not been instantiated this session.
	 */
	public Map createBuildersPersistentInfo(IProject project) throws CoreException {
		/* get the old builder map */
		Map oldInfos = getBuildersPersistentInfo(project);

		ICommand[] buildCommands = ((Project) project).internalGetDescription().getBuildSpec(false);
		if (buildCommands.length == 0)
			return null;

		/* build the new map */
		Map newInfos = new HashMap(buildCommands.length * 2);
		Hashtable instantiatedBuilders = getBuilders(project);
		for (int i = 0; i < buildCommands.length; i++) {
			String builderName = buildCommands[i].getBuilderName();
			BuilderPersistentInfo info = null;
			IncrementalProjectBuilder builder = (IncrementalProjectBuilder) instantiatedBuilders.get(builderName);
			if (builder == null) {
				// if the builder was not instantiated, use the old info if any.
				if (oldInfos != null)
					info = (BuilderPersistentInfo) oldInfos.get(builderName);
			} else if (!(builder instanceof MissingBuilder)) {
				ElementTree oldTree = ((InternalBuilder) builder).getLastBuiltTree();
				//don't persist build state for builders that have no last built state
				if (oldTree != null) {
					// if the builder was instantiated, construct a memento with the important info
					info = new BuilderPersistentInfo();
					info.setProjectName(project.getName());
					info.setBuilderName(builderName);
					info.setLastBuildTree(oldTree);
					info.setInterestingProjects(((InternalBuilder) builder).getInterestingProjects());
				}
			}
			if (info != null)
				newInfos.put(builderName, info);
		}
		return newInfos;
	}

	protected String debugBuilder() {
		return currentBuilder == null ? "<no builder>" : currentBuilder.getClass().getName(); //$NON-NLS-1$
	}

	protected String debugProject() {
		if (currentBuilder == null)
			return "<no project>"; //$NON-NLS-1$
		return currentBuilder.getProject().getFullPath().toString();
	}

	/**
	 * The outermost workspace operation has finished.  Do an autobuild if necessary.
	 */
	public void endTopLevel(boolean needsBuild) {
		autoBuildJob.build(needsBuild);
	}

	protected IncrementalProjectBuilder getBuilder(String builderName, IProject project, MultiStatus status) throws CoreException {
		Hashtable builders = getBuilders(project);
		IncrementalProjectBuilder result = (IncrementalProjectBuilder) builders.get(builderName);
		if (result != null)
			return result;
		result = initializeBuilder(builderName, project, status);
		builders.put(builderName, result);
		((InternalBuilder) result).setProject(project);
		((InternalBuilder) result).startupOnInitialize();
		return result;
	}

	/**
	 * Returns a hashtable of all instantiated builders for the given project.
	 * This hashtable maps String(builder name) -> Builder.
	 */
	protected Hashtable getBuilders(IProject project) {
		ProjectInfo info = (ProjectInfo) workspace.getResourceInfo(project.getFullPath(), false, false);
		if (info == null)
			Assert.isNotNull(info, Policy.bind("events.noProject", project.getName())); //$NON-NLS-1$
		return info.getBuilders();
	}

	/**
	 * Returns a Map mapping String(builder name) -> BuilderPersistentInfo.
	 * The map includes entries for all builders that are in the builder spec,
	 * and that have a last built state, even if they have not been instantiated
	 * this session.
	 */
	public Map getBuildersPersistentInfo(IProject project) throws CoreException {
		return (Map) project.getSessionProperty(K_BUILD_MAP);
	}

	protected IResourceDelta getDelta(IProject project) {
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
			IResourceDelta result = (IResourceDelta) deltaCache.getDelta(project.getFullPath(), lastBuiltTree, currentTree);
			if (result != null)
				return result;
	
			long startTime = 0L;
			if (Policy.DEBUG_BUILD_DELTA) {
				startTime = System.currentTimeMillis();
				Policy.debug("Computing delta for project: " + project.getName()); //$NON-NLS-1$
			}
			result = ResourceDeltaFactory.computeDelta(workspace, lastBuiltTree, currentTree, project.getFullPath(), -1);
			deltaCache.cache(project.getFullPath(), lastBuiltTree, currentTree, result);
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
	protected ISafeRunnable getSafeRunnable(final int trigger, final Map args, final MultiStatus status, final IProgressMonitor monitor) {
		return new ISafeRunnable() {
			public void handleException(Throwable e) {
				if (e instanceof OperationCanceledException)
					throw (OperationCanceledException) e;
				//ResourceStats.buildException(e);
				// don't log the exception....it is already being logged in Platform#run

				//add a generic message to the MultiStatus
				String builderName = currentBuilder.getLabel();
				if (builderName == null || builderName.length() == 0)
					builderName = currentBuilder.getClass().getName();
				String pluginId = currentBuilder.getPluginDescriptor().getUniqueIdentifier();
				String message = Policy.bind("events.builderError", builderName, currentBuilder.getProject().getName()); //$NON-NLS-1$
				status.add(new Status(IStatus.WARNING, pluginId, IResourceStatus.BUILD_FAILED, message, null));

				//add the exception status to the MultiStatus
				if (e instanceof CoreException)
					status.add(((CoreException) e).getStatus());
				else {
					message = e.getMessage();
					if (message == null)
						message = Policy.bind("events.unknown", e.getClass().getName(), builderName); //$NON-NLS-1$
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
	public boolean hasBeenBuilt(IProject project) {
		return builtProjects.contains(project);
	}

	/**
	 * Hook for adding trace options and debug information at the end of a build.
	 */
	private void hookEndBuild(IncrementalProjectBuilder builder) {
		if (Policy.MONITOR_BUILDERS)
			EventStats.endBuild();
		if (!Policy.DEBUG_BUILD_INVOKING || timeStamp == -1)
			return; //builder wasn't called or we are not debugging
		Policy.debug("Builder finished: " + toString(builder) + " time: " + (System.currentTimeMillis() - timeStamp) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		timeStamp = -1;
	}

	/**
	 * Hook for adding trace options and debug information at the start of a build.
	 */
	private void hookStartBuild(IncrementalProjectBuilder builder, int trigger) {
		if (Policy.MONITOR_BUILDERS)
			EventStats.startBuild(builder);
		if (Policy.DEBUG_BUILD_INVOKING) {
			timeStamp = System.currentTimeMillis();
			String type;
			switch (trigger) {
				case IncrementalProjectBuilder.FULL_BUILD :
					type = "FULL_BUILD"; //$NON-NLS-1$
					break;
				case IncrementalProjectBuilder.INCREMENTAL_BUILD :
				default :
					type = "INCREMENTAL_BUILD"; //$NON-NLS-1$
			}
			Policy.debug("Invoking (" + type + ") on builder: " + toString(builder)); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Instantiates the builder with the given name.  If the builder, its plugin, or its nature
	 * is missing, create a placeholder builder to takes its place.  This is needed to generate 
	 * appropriate exceptions when somebody tries to invoke the builder, and to
	 * prevent trying to instantiate it every time a build is run.
	 * This method NEVER returns null.
	 */
	protected IncrementalProjectBuilder initializeBuilder(String builderName, IProject project, MultiStatus status) throws CoreException {
		IncrementalProjectBuilder builder = null;
		try {
			builder = instantiateBuilder(builderName);
		} catch (CoreException e) {
			status.add(new ResourceStatus(IResourceStatus.BUILD_FAILED, project.getFullPath(), Policy.bind("events.instantiate.1", builderName), e)); //$NON-NLS-1$
			status.add(e.getStatus());
		}
		if (builder == null) {
			//unable to create the builder, so create a placeholder to fill in for it
			builder = new MissingBuilder(builderName);
		}
		// get the map of builders to get the last built tree
		Map infos = getBuildersPersistentInfo(project);
		if (infos != null) {
			BuilderPersistentInfo info = (BuilderPersistentInfo) infos.remove(builderName);
			if (info != null) {
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
	protected IncrementalProjectBuilder instantiateBuilder(String builderName) throws CoreException {
		IExtension extension = Platform.getExtensionRegistry().getExtension(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_BUILDERS, builderName);
		if (extension == null)
			return null;
		IConfigurationElement[] configs = extension.getConfigurationElements();
		if (configs.length == 0)
			return null;
		String hasNature = configs[0].getAttribute("hasNature"); //$NON-NLS-1$
		String natureId = null;
		if (hasNature != null && hasNature.equalsIgnoreCase(Boolean.TRUE.toString())) {
			//find the nature that owns this builder
			String builderId = extension.getUniqueIdentifier();
			natureId = workspace.getNatureManager().findNatureForBuilder(builderId);
			if (natureId == null)
				return null;
		}
		//The nature exists, or this builder doesn't specify a nature
		InternalBuilder builder = (InternalBuilder) configs[0].createExecutableExtension("run"); //$NON-NLS-1$
		builder.setPluginDescriptor(extension.getDeclaringPluginDescriptor());
		builder.setLabel(extension.getLabel());
		builder.setNatureId(natureId);
		return (IncrementalProjectBuilder) builder;
	}

	/**
	 * Returns true if the current builder is interested in changes
	 * to the given project, and false otherwise.
	 */
	protected boolean isInterestingProject(IProject project) {
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
	protected boolean needsBuild(InternalBuilder builder) {
		//compute the delta since the last built state
		ElementTree oldTree = builder.getLastBuiltTree();
		ElementTree newTree = workspace.getElementTree();
		long start = System.currentTimeMillis();
		currentDelta = (DeltaDataTree) deltaTreeCache.getDelta(null, oldTree, newTree);
		if (currentDelta == null) {
			if (Policy.DEBUG_NEEDS_BUILD) {
				String message = "Checking if need to build. Starting delta computation between: " + oldTree.toString() + " and " + newTree.toString(); //$NON-NLS-1$ //$NON-NLS-2$
				Policy.debug(message);
			}
			currentDelta = newTree.getDataTree().forwardDeltaWith(oldTree.getDataTree(), ResourceComparator.getComparator(false));
			if (Policy.DEBUG_NEEDS_BUILD)
				Policy.debug("End delta computation. (" + (System.currentTimeMillis() - start) + "ms)."); //$NON-NLS-1$ //$NON-NLS-2$
			deltaTreeCache.cache(null, oldTree, newTree, currentDelta);
		}

		//search for the builder's project
		if (currentDelta.findNodeAt(builder.getProject().getFullPath()) != null) {
			if (Policy.DEBUG_NEEDS_BUILD)
				Policy.debug(toString(builder) + " needs building because of changes in: " + builder.getProject().getName()); //$NON-NLS-1$
			return true;
		}

		//search for builder's interesting projects
		IProject[] projects = builder.getInterestingProjects();
		for (int i = 0; i < projects.length; i++) {
			if (currentDelta.findNodeAt(projects[i].getFullPath()) != null) {
				if (Policy.DEBUG_NEEDS_BUILD)
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
	protected void removeBuilders(IProject project, String builderId) throws CoreException {
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
	public void requestRebuild() {
		rebuildRequested = true;
	}

	/**
	 * Sets the builder map for the given project.  The builder map is
	 * a Map mapping String(builder name) -> BuilderPersistentInfo.
	 * The map includes entries for all builders that are
	 * in the builder spec, and that have a last built state, even if they 
	 * have not been instantiated this session.
	 */
	public void setBuildersPersistentInfo(IProject project, Map map) {
		try {
			project.setSessionProperty(K_BUILD_MAP, map);
		} catch (CoreException e) {
			//project is missing -- build state will be lost
			//can't throw an exception because this happens on startup
			IStatus error = new ResourceStatus(IStatus.ERROR, 1, project.getFullPath(), "Project missing in setBuildersPersistentInfo", null); //$NON-NLS-1$
			ResourcesPlugin.getPlugin().getLog().log(error);
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
	protected String toString(InternalBuilder builder) {
		String name = builder.getClass().getName();
		name = name.substring(name.lastIndexOf('.') + 1);
		return name + "(" + builder.getProject().getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns true if the nature membership rules are satisifed for the given
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
	protected boolean validateNature(InternalBuilder builder, String builderId) throws CoreException {
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
}