package org.eclipse.core.internal.events;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.watson.ElementTree;
import java.util.*;

public class BuildManager implements ICoreConstants, IManager {
	protected Workspace workspace;
	protected boolean building = false;
	protected ElementTree currentTree;
	protected ElementTree lastBuiltTree;
	protected InternalBuilder currentBuilder;
	protected List createdDeltas = new ArrayList(5);
	protected boolean needNextTree;
public BuildManager(Workspace workspace) {
	this.workspace = workspace;
}
void basicBuild(final IProject project, final int trigger, final IncrementalProjectBuilder builder, final Map args, final MultiStatus status, final IProgressMonitor monitor) {
	try {
		// want to invoke some methods not accessible via IncrementalProjectBuilder
		currentBuilder = (InternalBuilder) builder;
		// Figure out which trees are involved based on the trigger and tree availabilty.
		// Also, grab a pointer to the current state before computing the delta
		// as this will be the last built state of the builder when we are done.
		lastBuiltTree = currentBuilder.getLastBuiltTree();
		boolean fullBuild = (trigger == IncrementalProjectBuilder.FULL_BUILD) || (lastBuiltTree == null);
		currentTree = fullBuild ? null : workspace.getElementTree();
		needNextTree = true;
		// The delta calculation closes the tree, but since we are currently
		// inside an operation, the tree should be open.
		workspace.newWorkingTree();
		try {
			// ResourceStats.startBuild(builderName);
			ISafeRunnable code = new ISafeRunnable() {
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
					// don't log the exception....it is already being logged in Workspace#run
					if (e instanceof CoreException)
						status.add(((CoreException) e).getStatus());
					else {
						String pluginId = currentBuilder.getPluginDescriptor().getUniqueIdentifier();
						String message = e.getMessage();
						if (message == null)
							message = Policy.bind("events.unknown", e.getClass().getName(), currentBuilder.getClass().getName());
						status.add(new Status(Status.WARNING, pluginId, IResourceStatus.INTERNAL_ERROR, message, e));
					}
				}
			};
			Platform.run(code);
		} finally {
			// ResourceStats.endBuild();
			// Always remember the current state as the last built state.
			// If the build went ok, commit it, otherwise abort.  Be sure to clean
			// up after ourselves.
			if (needNextTree) {
				ElementTree lastTree = workspace.getElementTree();
				lastTree.immutable();
				currentBuilder.setLastBuiltTree(lastTree);
			}
		}
	} finally {
		destroyDeltas();
		currentBuilder = null;
		currentTree = null;
		lastBuiltTree = null;
	}
}
void basicBuild(final IProject project, final int trigger, final MultiStatus status, final IProgressMonitor monitor) {
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
void basicBuild(IProject project, int trigger, String builderName, Map args, MultiStatus status, IProgressMonitor monitor) {
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
	basicBuild(project, trigger, builder, args, status, monitor);
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
	}
}
public boolean canRun(int trigger) {
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
	setBuildersPersistentInfo(project, null);

	ICommand[] buildCommands = ((Project) project).internalGetDescription().getBuildSpec(false);
	if (buildCommands.length == 0)
		return null;

	/* build the new map */
	Map newInfos = new HashMap(buildCommands.length * 2);
	Hashtable instantiatedBuilders = getBuilders(project);
	for (int i = 0; i < buildCommands.length; i++) {
		String builderName = buildCommands[i].getBuilderName();
		ElementTree tree = null;
		IncrementalProjectBuilder builder = (IncrementalProjectBuilder) instantiatedBuilders.get(builderName);
		if (builder != null) {
			tree = ((InternalBuilder) builder).getLastBuiltTree();
		} else {
			/* look in the old builder map */
			if (oldInfos != null) {
				BuilderPersistentInfo info = (BuilderPersistentInfo) oldInfos.get(builderName);
				tree = info.getLastBuiltTree();
			}
		}
		if (tree != null) {
			BuilderPersistentInfo info = new BuilderPersistentInfo();
			info.setProjectName(project.getName());
			info.setBuilderName(builderName);
			info.setLastBuildTree(tree);
			if (builder != null)
				info.setInterestingProjects(((InternalBuilder) builder).getInterestingProjects());
			else
				info.setInterestingProjects(ICoreConstants.EMPTY_PROJECT_ARRAY);
			newInfos.put(builderName, info);
		}
	}
	return newInfos;
}
public void deleting(IProject project) {
}
protected void destroyDeltas() {
	for (Iterator i = createdDeltas.iterator(); i.hasNext();) {
		((ResourceDelta) i.next()).destroy();
	}
	createdDeltas.clear();
}
public IncrementalProjectBuilder getBuilder(String builderName, IProject project) throws CoreException {
	Hashtable builders = getBuilders(project);
	IncrementalProjectBuilder result = (IncrementalProjectBuilder) builders.get(builderName);
	if (result != null)
		return result;
	result = (IncrementalProjectBuilder) instantiateBuilder(builderName, project);
	if (result == null)
		return null;
	builders.put(builderName, result);
	((InternalBuilder) result).setProject(project);
	((InternalBuilder) result).startupOnInitialize();
	return result;
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
 * Returns a hashtable of all instantiated builders for the given project.
 * This hashtable maps String(builder name) -> Builder.
 */
private Hashtable getBuilders(IProject project) {
	ProjectInfo info = (ProjectInfo) workspace.getResourceInfo(project.getFullPath(), false, false);
	Assert.isNotNull(info, Policy.bind("events.noProject", project.getName()));
	return info.getBuilders();
}
public IResourceDelta getDelta(IProject project) {
	if (currentTree == null)
		return null;
	IProject interestingProject = getInterestingProject(project);
	if (interestingProject == null)
		return null;

	IResourceDelta result = ResourceDeltaFactory.computeDelta(workspace, lastBuiltTree, currentTree, interestingProject.getFullPath(), false);
	createdDeltas.add(result);
	return result;
}
ResourceDelta getDelta(IProject project, IncrementalProjectBuilder builder, ElementTree currentTree) {
	return ResourceDeltaFactory.computeDelta(workspace, ((InternalBuilder)builder).getLastBuiltTree(), currentTree, project.getFullPath(), false);
}
private IProject getInterestingProject(IProject project) {
	if (project.equals(currentBuilder.getProject()))
		return project;
	IProject[] interestingProjects = currentBuilder.getInterestingProjects();
	for (int i = 0; i < interestingProjects.length; i++) {
		if (interestingProjects[i].equals(project)) {
			return project;
		}
	}
	return null;
}
protected IncrementalProjectBuilder instantiateBuilder(String builderName, IProject project) throws CoreException {
	IExtension extension = Platform.getPluginRegistry().getExtension(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_BUILDERS, builderName);
	if (extension == null)
		return null;
	IConfigurationElement[] configs = extension.getConfigurationElements();
	if (configs.length == 0)
		return null;
	try {
		IConfigurationElement config = configs[0];
		IncrementalProjectBuilder builder = (IncrementalProjectBuilder) config.createExecutableExtension("run");
		((InternalBuilder) builder).setPluginDescriptor(config.getDeclaringExtension().getDeclaringPluginDescriptor());

		/* get the map of builders to get the last built tree */
		Map infos = getBuildersPersistentInfo(project);
		if (infos != null) {
			BuilderPersistentInfo info = (BuilderPersistentInfo) infos.remove(builderName);
			ElementTree tree = info.getLastBuiltTree();
			if (tree != null) {
				((InternalBuilder) builder).setLastBuiltTree(tree);
			}
			((InternalBuilder) builder).setInterestingProjects(info.getInterestingProjects());
			/* delete the build map if it's now empty */
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
 * The given project is the destination of a copy or a move so the build manager
 * must fix the project reference inside each of its builders since they still
 * point to the source project.
 */
public void fixBuildersFor(IProject project) {
	ProjectInfo info = (ProjectInfo) workspace.getResourceInfo(project.getFullPath(), false, true);
	Hashtable builders = info.getBuilders();
	for (Enumeration e = builders.keys(); e.hasMoreElements(); ) {
		Object key = e.nextElement();
		InternalBuilder builder = (InternalBuilder) builders.get(key);
		builder.setProject(project);
	}
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
void basicBuild(IProject project, int trigger, ICommand[] commands, MultiStatus status, IProgressMonitor monitor) {
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
		}
	} finally {
		monitor.done();
	}
}
}