package org.eclipse.core.internal.events;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.watson.ElementTree;
import java.util.*;

/**
 * This class is the internal basis for all builders.  ISV developers should
 * not sublclass this class.
 *
 * @see BaseBuilder
 */

public abstract class InternalBuilder {
	
	private static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];
	
	private IProject project;
	private ElementTree oldState;
	private IPluginDescriptor pluginDescriptor;
	private IProject[] interestingProjects = EMPTY_PROJECT_ARRAY;
	
/**
 * 
 */
protected abstract IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException;
/**
 * Requests that this builder forget any state it may be retaining regarding
 * previously built states.  Typically this means that the next time the
 * builder runs, it will have to do a full build since it does not have
 * any state upon which to base an incremental build.
 */
protected void forgetLastBuiltState() {
	oldState = null;
}
protected IResourceDelta getDelta(IProject project) {
	return ((Workspace) project.getWorkspace()).getBuildManager().getDelta(project);
}
/* package */ final IProject[] getInterestingProjects() {
	return interestingProjects;
}
/* package */ final ElementTree getLastBuiltTree() {
	return oldState;
}
/* package */ final IPluginDescriptor getPluginDescriptor() {
	return pluginDescriptor;
}
/**
 * Returns the project for this builder
 */
protected IProject getProject() {
	return project;
}
/* package */ final void setInterestingProjects(IProject[] value) {
	interestingProjects = value;
}
/* package */ final void setLastBuiltTree(ElementTree value) {
	oldState = value;
}
/* package */ final void setPluginDescriptor(IPluginDescriptor value) {
	pluginDescriptor = value;
}
/**
 * Sets the project for which this builder operates.  
 *
 * @see #getProject
 */
/* package */ final void setProject(IProject value) {
	Assert.isTrue(project == null);
	project = value;
}
/**
 * 
 */
protected abstract void startupOnInitialize();
}
