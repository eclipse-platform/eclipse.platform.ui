package org.eclipse.core.internal.events;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.internal.dtree.DeltaDataTree;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.watson.ElementTree;
import java.util.*;
import java.util.Map;

public class BuildManager implements ICoreConstants, IManager {
	protected Workspace workspace;
	protected boolean building = false;
	
	//used for debug/trace timing
	private long timeStamp = -1;
	
	//the following four fields only apply for the lifetime of 
	//a single builder invocation.
	protected ElementTree currentTree;
	protected ElementTree lastBuiltTree;
	protected InternalBuilder currentBuilder;
	protected DeltaDataTree currentDelta;
	
	public static boolean DEBUG_BUILD_FAILURE = false;
	public static boolean DEBUG_NEEDS_BUILD = false;
	public static boolean DEBUG_BUILD_INVOKING = false;
	public static boolean DEBUG_COMPUTE_DELTA = false;
	public static final String OPTION_BUILD_FAILURE = ResourcesPlugin.PI_RESOURCES + "/build/failure";
	public static final String OPTION_NEEDS_BUILD = ResourcesPlugin.PI_RESOURCES + "/build/needbuild";
	public static final String OPTION_BUILD_INVOKING = ResourcesPlugin.PI_RESOURCES + "/build/invoking";
	public static final String OPTION_COMPUTE_DELTA = ResourcesPlugin.PI_RESOURCES + "/build/delta";
	
	/**
	 * Cache used to optimize the common case of an autobuild against
	 * a workspace where only a single project has changed (and hence
	 * only a single delta is interesting).
	 */
	class DeltaCache {
		private ElementTree oldTree;
		private ElementTree newTree;
		private IPath projectPath;
		private IResourceDelta delta;
		/**
		 * Returns the cached resource delta for the given project and trees, or
		 * null if there is no matching delta in the cache.
		 */
		public IResourceDelta getDelta(IPath project, ElementTree oldTree, ElementTree newTree) {
			if (delta == null)
				return null;
			if (projectPath.equals(project) && this.oldTree == oldTree && this.newTree == newTree)
				return delta;
			return null;
		}
		public void cache(IPath project, ElementTree oldTree, ElementTree newTree, IResourceDelta delta) {
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
	}
	final protected DeltaCache deltaCache = new DeltaCache();
	/**
	 * These builders are added to build tables in place of builders that couldn't be instantiated
	 */
	class MissingBuilder extends IncrementalProjectBuilder {
		private String name;
		private IStatus status;
		MissingBuilder(String name) {
			this.name = name;
		}
		/**
		 * Throw an exception the first time this is called, and just log subsequent attempts.
		 */
		protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
			if (status == null) {
				String msg = Policy.bind("events.missing", new String[] {name, getProject().getName()});
				status = new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, 1, msg, null);
				throw new CoreException(status);
			}
			ResourcesPlugin.getPlugin().getLog().log(status);
			return null;
		}
}
	
public BuildManager(Workspace workspace) {
	this.workspace = workspace;
	if (ResourcesPlugin.getPlugin().isDebugging()) {
		DEBUG_BUILD_FAILURE = "true".equalsIgnoreCase(Platform.getDebugOption(OPTION_BUILD_FAILURE));
		DEBUG_NEEDS_BUILD = "true".equalsIgnoreCase(Platform.getDebugOption(OPTION_NEEDS_BUILD));
		DEBUG_BUILD_INVOKING = "true".equalsIgnoreCase(Platform.getDebugOption(OPTION_BUILD_INVOKING));
		DEBUG_COMPUTE_DELTA = "true".equalsIgnoreCase(Platform.getDebugOption(OPTION_COMPUTE_DELTA));
	}
}

protected void basicBuild(int trigger, IncrementalProjectBuilder builder, Map args, MultiStatus status, IProgressMonitor monitor) {
	try {
		currentBuilder = (InternalBuilder) builder;
		// Figure out which trees are involved based on the trigger and tree availabilty.
		lastBuiltTree = currentBuilder.getLastBuiltTree();
		boolean fullBuild = (trigger == IncrementalProjectBuilder.FULL_BUILD) || (lastBuiltTree == null);
		// Grab a pointer to the current state before computing the delta
		// as this will be the last built state of the builder when we are done.
		currentTree = fullBuild ? null : workspace.getElementTree();
		try {
			//short-circuit if none of the projects this builder cares about have changed.
			if (!fullBuild && !needsBuild(currentBuilder))
				return;
			if (DEBUG_BUILD_INVOKING) hookStartBuild(builder);
			Platform.run(getSafeRunnable(trigger, args, status, monitor));
		} finally {
			if (DEBUG_BUILD_INVOKING) hookEndBuild(builder);
			// Always remember the current state as the last built state.
			// Be sure to clean up after ourselves.
			ElementTree lastTree = workspace.getElementTree();
			lastTree.immutable();
			currentBuilder.setLastBuiltTree(lastTree);
		}
	} finally {
		currentBuilder = null;
		currentTree = null;
		lastBuiltTree = null;
		currentDelta = null;
	}
}
protected void basicBuild(final IProject project, final int trigger, final MultiStatus status, final IProgressMonitor monitor) {
	if (!project.isAccessible())
		return;
	final ICommand[] commands = ((Project) project).internalGetDescription().getBuildSpec(false);
	if (commands.length == 0)
		return;
	ISafeRunnable code = new ISafeRunnable() {
		public void run() throws Exception {
			basicBuild(project, trigger, commands, status, monitor);
		}
		public void handleException(Throwable e) {
			if (e instanceof OperationCanceledException)
				throw (OperationCanceledException) e;
			// don't log the exception....it is already being logged in Workspace#run
			// should never get here because the lower-level build code wrappers
			// builder exceptions in core exceptions if required.
			String message = e.getMessage();
			if (message == null)
				message = Policy.bind("events.unknown", e.getClass().getName(), currentBuilder.getClass().getName());
			status.add(new Status(Status.WARNING, ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, e));
		}
	};
	Platform.run(code);
}
protected void basicBuild(IProject project, int trigger, String builderName, Map args, MultiStatus status, IProgressMonitor monitor) {
	IncrementalProjectBuilder builder = null;
	try {
		builder = getBuilder(builderName, project);
	} catch (CoreException e) {
		status.add(e.getStatus());
		return;
	}
	if (builder == null) {
		String message = Policy.bind("events.instantiate.1", builderName);
		status.add(new ResourceStatus(IResourceStatus.BUILD_FAILED, project.getFullPath(), message));
		return;
	}
	// get the builder name to be used as a progress message
	IExtension extension = Platform.getPluginRegistry().getExtension(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_BUILDERS, builderName);
	String message = null;
	if (extension != null) {
		String name = extension.getLabel();
		if (name != null) {
			message = Policy.bind("events.invoking.2", name, project.getFullPath().toString());
		}
	}
	if (message == null)
		message = Policy.bind("events.invoking.1", project.getFullPath().toString());
	monitor.subTask(message);
	basicBuild(trigger, builder, args, status, monitor);
}
protected void basicBuild(IProject project, int trigger, ICommand[] commands, MultiStatus status, IProgressMonitor monitor) {
	monitor = Policy.monitorFor(monitor);
	try {
		String message = Policy.bind("events.building.1", project.getFullPath().toString());
		monitor.beginTask(message, Math.max(1, commands.length));
		for (int i = 0; i < commands.length; i++) {
			IProgressMonitor sub = Policy.subMonitorFor(monitor, 1);
			BuildCommand command = (BuildCommand) commands[i];
			basicBuild(project, trigger, command.getBuilderName(), command.getArguments(false), status, sub);
			Policy.checkCanceled(monitor);
		}
	} finally {
		monitor.done();
	}
}
public void build(int trigger, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(Policy.bind("events.building.0"), Policy.totalWork);
		if (!canRun(trigger))
			return;
		try {
			building = true;
			IProject[] ordered = workspace.getBuildOrder();
			IProject[] unordered = null;
			HashSet leftover = new HashSet(5);
			leftover.addAll(Arrays.asList(workspace.getRoot().getProjects()));
			leftover.removeAll(Arrays.asList(ordered));
			unordered = (IProject[]) leftover.toArray(new IProject[leftover.size()]);
			int num = ordered.length + unordered.length;
			MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, Policy.bind("events.errors"), null);
			for (int i = 0; i < ordered.length; i++)
				if (ordered[i].isAccessible())
					basicBuild(ordered[i], trigger, status, Policy.subMonitorFor(monitor, Policy.totalWork / num));
			for (int i = 0; i < unordered.length; i++)
				if (unordered[i].isAccessible())
					basicBuild(unordered[i], trigger, status, Policy.subMonitorFor(monitor, Policy.totalWork / num));
			// if the status is not ok, throw an exception 
			if (!status.isOK())
				throw new ResourceException(status);
		} finally {
			building = false;
			deltaCache.flush();
		}
	} finally {
		monitor.done();
	}
}
public void build(IProject project, int trigger, IProgressMonitor monitor) throws CoreException {
	if (!canRun(trigger))
		return;
	try {
		building = true;
		MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, Policy.bind("events.errors"), null);
		basicBuild(project, trigger, status, monitor);
		if (!status.isOK())
			throw new ResourceException(status);
	} finally {
		building = false;
		deltaCache.flush();
	}
}
public void build(IProject project, int kind, String builderName, Map args, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String message = Policy.bind("events.building.1", project.getFullPath().toString());
		monitor.beginTask(message, 1);
		if (!canRun(kind))
			return;
		try {
			building = true;
			MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, Policy.bind("events.errors"), null);
			basicBuild(project, kind, builderName, args, status, Policy.subMonitorFor(monitor, 1));
			if (!status.isOK())
				throw new ResourceException(status);
		} finally {
			building = false;
		}
	} finally {
		monitor.done();
		deltaCache.flush();
	}
}
protected boolean canRun(int trigger) {
	return !building;
}
public void changing(IProject project) {
}
public void closing(IProject project) {
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
	/* nuke the old builder map */
//	setBuildersPersistentInfo(project, null);

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
		} else {
			// if the builder was instantiated, construct a memento with the important info
			info = new BuilderPersistentInfo();
			info.setProjectName(project.getName());
			info.setBuilderName(builderName);
			info.setLastBuildTree(((InternalBuilder) builder).getLastBuiltTree());
			info.setInterestingProjects(((InternalBuilder)builder).getInterestingProjects());
		}
		if (info != null)
			newInfos.put(builderName, info);
	}
	return newInfos;
}
protected String debugBuilder() {
	return currentBuilder == null ? "<no builder>" : currentBuilder.getClass().getName();
}
protected String debugProject() {
	if (currentBuilder== null)
		return "<no project>";
	return currentBuilder.getProject().getFullPath().toString();
}
public void deleting(IProject project) {
}
protected IncrementalProjectBuilder getBuilder(String builderName, IProject project) throws CoreException {
	Hashtable builders = getBuilders(project);
	IncrementalProjectBuilder result = (IncrementalProjectBuilder) builders.get(builderName);
	if (result != null)
		return result;
	result = instantiateBuilder(builderName, project);
	builders.put(builderName, result);
	((InternalBuilder) result).setProject(project);
	result.startupOnInitialize();
	return result;
}
/**
 * Returns a hashtable of all instantiated builders for the given project.
 * This hashtable maps String(builder name) -> Builder.
 */
protected Hashtable getBuilders(IProject project) {
	ProjectInfo info = (ProjectInfo) workspace.getResourceInfo(project.getFullPath(), false, false);
	Assert.isNotNull(info, Policy.bind("events.noProject", project.getName()));
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
/**
 * Returns the safe runnable instance for invoking a builder
 */
protected ISafeRunnable getSafeRunnable(final int trigger, final Map args, final MultiStatus status, final IProgressMonitor monitor) {
	return new ISafeRunnable() {
		public void run() throws Exception {
			IProject[] builders = currentBuilder.build(trigger, args, monitor);
			if (builders == null)
				builders = new IProject[0];
			currentBuilder.setInterestingProjects((IProject[]) builders.clone());
		}
		public void handleException(Throwable e) {
			if (e instanceof OperationCanceledException)
				throw (OperationCanceledException) e;
			//ResourceStats.buildException(e);
			// don't log the exception....it is already being logged in Platform#run
			if (e instanceof CoreException)
				status.add(((CoreException) e).getStatus());
			else {
				String pluginId = currentBuilder.getPluginDescriptor().getUniqueIdentifier();
				String message = e.getMessage();
				if (message == null)
					message = Policy.bind("events.unknown", e.getClass().getName(), currentBuilder.getClass().getName());
				status.add(new Status(IStatus.WARNING, pluginId, IResourceStatus.BUILD_FAILED, message, e));
			}
		}
	};
}
protected IResourceDelta getDelta(IProject project) {
	if (currentTree == null) {
		if (DEBUG_BUILD_FAILURE) 
			System.out.println("Build: no tree for delta " + debugBuilder() + " [" + debugProject() + "]");
		return null;
	}
	//check if this builder has indicated it cares about this project
	if (!isInterestingProject(project)) {
		if (DEBUG_BUILD_FAILURE) 
			System.out.println("Build: project not interesting for this builder " + debugBuilder() + " [" + debugProject() + "] " + project.getFullPath());
		return null;
	}
	//check if this project has changed
	if (currentDelta != null && currentDelta.findNodeAt(project.getFullPath()) == null) {
		//just return an empty delta rooted at this project
		return ResourceDeltaFactory.newEmptyDelta(project);
	}
	//now check against the cache
	IResourceDelta result = deltaCache.getDelta(project.getFullPath(), lastBuiltTree, currentTree);
	if (result != null)
		return result;

	long startTime = 0L;
	if (DEBUG_COMPUTE_DELTA) {
		startTime = System.currentTimeMillis();
		System.out.println("Computing delta for project: " + project.getName());
	}
	result = ResourceDeltaFactory.computeDelta(workspace, lastBuiltTree, currentTree, project.getFullPath(), false);
	deltaCache.cache(project.getFullPath(), lastBuiltTree, currentTree, result);
	if (DEBUG_BUILD_FAILURE && result == null) 
		System.out.println("Build: no delta " + debugBuilder() + " [" + debugProject() + "] " + project.getFullPath());
	if (DEBUG_COMPUTE_DELTA)
		System.out.println("Finished computing delta, time: " + (System.currentTimeMillis()-startTime) + "ms");
	return result;
}
/**
 * Hook for adding trace options and debug information at the start of a build.
 */
private void hookStartBuild(IncrementalProjectBuilder builder) {
	ResourceStats.startBuild(builder.getClass().getName());
	timeStamp = System.currentTimeMillis();
	System.out.println("Invoking builder: " + toString(builder));
}
/**
 * Hook for adding trace options and debug information at the end of a build.
 */
private void hookEndBuild(IncrementalProjectBuilder builder) {
	if (timeStamp == -1)
		return;		//builder wasn't called
	ResourceStats.endBuild();
	System.out.println("Builder finished: "  + toString(builder) + " time: " + (System.currentTimeMillis() - timeStamp) + "ms");
	timeStamp = -1;
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
	currentDelta = newTree.getDataTree().forwardDeltaWith(oldTree.getDataTree(), ResourceComparator.getComparator(false));
	
	//search for the builder's project
	if (currentDelta.findNodeAt(builder.getProject().getFullPath()) != null) {
		if (DEBUG_NEEDS_BUILD)
			System.out.println(toString(builder) + " needs building because of changes in: " + builder.getProject().getName());
		return true;
	}
	
	//search for builder's interesting projects
	IProject[] projects = builder.getInterestingProjects();	
	for (int i = 0; i < projects.length; i++) {
		if (currentDelta.findNodeAt(projects[i].getFullPath()) != null) {
			if (DEBUG_NEEDS_BUILD)
				System.out.println(toString(builder) + " needs building because of changes in: " + projects[i].getName());
			return true;
		}
	}
	return false;	
}
/**
 * Instantiates the builder with the given name.  If the builder or its plugin is missing, 
 * create a placeholder builder to takes its place.  This is needed to carry forward persistent
 * builder info, and to generate appropriate exceptions when somebody tries to invoke the builder.
 * This method NEVER returns null.
 */
protected IncrementalProjectBuilder instantiateBuilder(String builderName, IProject project) throws CoreException {
	try {
		IncrementalProjectBuilder builder = null;
		IExtension extension = Platform.getPluginRegistry().getExtension(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_BUILDERS, builderName);
		if (extension != null) {
			IConfigurationElement[] configs = extension.getConfigurationElements();
			if (configs.length != 0) {
				builder = (IncrementalProjectBuilder) configs[0].createExecutableExtension("run");
				((InternalBuilder) builder).setPluginDescriptor(configs[0].getDeclaringExtension().getDeclaringPluginDescriptor());
			}
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
	} catch (CoreException e) {
		throw new ResourceException(IResourceStatus.BUILD_FAILED, project.getFullPath(), Policy.bind("events.instantiate.0"), e);
	}
}
public void opening(IProject project) {
}
/**
 * Sets the builder map for the given project.  The builder map is
 * a Map mapping String(builder name) -> BuilderPersistentInfo.
 * The map includes entries for all builders that are
 * in the builder spec, and that have a last built state, even if they 
 * have not been instantiated this session.
 */
public void setBuildersPersistentInfo(IProject project, Map map) throws CoreException {
	project.setSessionProperty(K_BUILD_MAP, map);
}
public void shutdown(IProgressMonitor monitor) {
}
public void startup(IProgressMonitor monitor) {
}
/**
 * Returns a string representation of the given builder.  
 * For debugging purposes only.
 */
protected String toString(InternalBuilder builder) {
	String name = builder.getClass().getName();
	name = name.substring(name.lastIndexOf('.') + 1);
	return name + "(" + builder.getProject().getName() + ")";
}
}