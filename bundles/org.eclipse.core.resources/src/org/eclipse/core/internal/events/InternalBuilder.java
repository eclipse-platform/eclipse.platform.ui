/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.events;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.watson.ElementTree;
import java.util.*;

/**
 * This class is the internal basis for all builders.  ISV developers should
 * not subclass this class.
 *
 * @see BaseBuilder
 */
public abstract class InternalBuilder {
	/**
	 * Human readable builder name for progress reporting.
	 */
	private String label;
	private IProject project;
	private String natureId;
	private ElementTree oldState;
	private IPluginDescriptor pluginDescriptor;
	private IProject[] interestingProjects = ICoreConstants.EMPTY_PROJECT_ARRAY;
	private boolean forgetStateRequested = false;
	
/**
 * 
 */
protected abstract IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException;
/**
 * Clears the request to forget last built states.
 */
void clearForgetLastBuiltState() {
	forgetStateRequested = false;
}
/**
 * @see IncrementalProjectBuilder#forgetLastBuiltState
 */
protected void forgetLastBuiltState() {
	oldState = null;
	forgetStateRequested = true;
}
protected IResourceDelta getDelta(IProject project) {
	return ((Workspace) project.getWorkspace()).getBuildManager().getDelta(project);
}
/* package */ final IProject[] getInterestingProjects() {
	return interestingProjects;
}
/* package */ final String getLabel() {
	return label;
}
/* package */ final ElementTree getLastBuiltTree() {
	return oldState;
}
/**
 * Returns the ID of the nature that owns this builder.  Returns null
 * if the builder does not belong to a nature.
 */
/* package */ final String getNatureId() {
	return natureId;
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
/* package */ final void setNatureId(String id) {
	this.natureId = id;
}
/* package */ final void setPluginDescriptor(IPluginDescriptor value) {
	pluginDescriptor = value;
}
/* package */ final void setLabel(String value) {
	this.label = value;
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
protected abstract void startupOnInitialize();
/**
 * Returns true if the builder requested that its last built state be forgetten,
 * and false otherwise.
 */
boolean wasForgetStateRequested() {
	return forgetStateRequested;
}
}
