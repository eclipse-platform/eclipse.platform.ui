/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.events;

import java.util.Map;
import org.eclipse.core.internal.resources.ICoreConstants;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * This class is the internal basis for all builders. Plugin developers should not
 * subclass this class.
 * 
 * @see IncrementalProjectBuilder
 */
public abstract class InternalBuilder {
	/**
	 * Hold a direct reference to the build manager as an optimization.
	 * This will be initialized by BuildManager when it is constructed.
	 */
	static BuildManager buildManager;
	private ICommand command;
	private boolean forgetStateRequested = false;
	private IProject[] interestingProjects = ICoreConstants.EMPTY_PROJECT_ARRAY;
	/**
	 * Human readable builder name for progress reporting.
	 */
	private String label;
	private String natureId;
	private ElementTree oldState;
	/**
	 * The symbolic name of the plugin that defines this builder
	 */
	private String pluginId;
	private IProject project;
	
	/**
	 * The value of the callOnEmptyDelta builder extension attribute.
	 */
	private boolean callOnEmptyDelta = false;

	/*
	 *  @see IncrementalProjectBuilder#build
	 */
	protected abstract IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns the value of the callOnEmptyDelta builder extension attribute.
	 */
	final boolean callOnEmptyDelta() {
		return callOnEmptyDelta;
	}
	/*
	 * @see IncrementalProjectBuilder
	 */
	protected abstract void clean(IProgressMonitor monitor) throws CoreException;

	/**
	 * Clears the request to forget last built states.
	 */
	final void clearForgetLastBuiltState() {
		forgetStateRequested = false;
	}

	/*
	 * @see IncrementalProjectBuilder#forgetLastBuiltState
	 */
	protected void forgetLastBuiltState() {
		oldState = null;
		forgetStateRequested = true;
	}

	/*
	 * @see IncrementalProjectBuilder#getCommand
	 */
	protected ICommand getCommand() {
		return (ICommand)((BuildCommand)command).clone();
	}
	
	/*
	 * @see IncrementalProjectBuilder#forgetLastBuiltState
	 */
	protected IResourceDelta getDelta(IProject aProject) {
		return buildManager.getDelta(aProject);
	}

	final IProject[] getInterestingProjects() {
		return interestingProjects;
	}

	final String getLabel() {
		return label;
	}

	final ElementTree getLastBuiltTree() {
		return oldState;
	}

	/**
	 * Returns the ID of the nature that owns this builder. Returns null if the
	 * builder does not belong to a nature.
	 */
	final String getNatureId() {
		return natureId;
	}

	final String getPluginId() {
		return pluginId;
	}

	/**
	 * Returns the project for this builder
	 */
	protected IProject getProject() {
		return project;
	}

	/*
	 * @see IncrementalProjectBuilder#hasBeenBuilt
	 */
	protected boolean hasBeenBuilt(IProject aProject) {
		return buildManager.hasBeenBuilt(aProject);
	}

	/*
	 * @see IncrementalProjectBuilder#isInterrupted
	 */
	public boolean isInterrupted() {
		return buildManager.autoBuildJob.isInterrupted();
	}

	/*
	 * @see IncrementalProjectBuilder#needRebuild
	 */
	protected void needRebuild() {
		buildManager.requestRebuild();
	}
	
	final void setCallOnEmptyDelta(boolean value) {
		this.callOnEmptyDelta = value;
	}

	final void setCommand(ICommand value) {
		this.command = value;
	}
	
	final void setInterestingProjects(IProject[] value) {
		interestingProjects = value;
	}

	final void setLabel(String value) {
		this.label = value;
	}

	final void setLastBuiltTree(ElementTree value) {
		oldState = value;
	}

	final void setNatureId(String id) {
		this.natureId = id;
	}

	final void setPluginId(String value) {
		pluginId = value;
	}

	/**
	 * Sets the project for which this builder operates.
	 * @see #getProject()
	 */
	final void setProject(IProject value) {
		Assert.isTrue(project == null);
		project = value;
	}

	/*
	 * @see IncrementalProjectBuilder#startupOnInitialize
	 */
	protected abstract void startupOnInitialize();

	/**
	 * Returns true if the builder requested that its last built state be
	 * forgetten, and false otherwise.
	 */
	final boolean wasForgetStateRequested() {
		return forgetStateRequested;
	}
}
