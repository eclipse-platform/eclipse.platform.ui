package org.eclipse.core.tests.resources.regression;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
/**
 * A builder that does not do anything.
 */
public class SimpleBuilder extends IncrementalProjectBuilder {

	protected int triggerForLastBuild;
	protected static SimpleBuilder instance;

	/** contants */
	public static final String BUILDER_ID = "org.eclipse.core.tests.resources.simplebuilder";
public SimpleBuilder() {
	super();
	instance = this;
}
protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
	triggerForLastBuild = kind;
	return null;
}
public static SimpleBuilder getInstance() {
	return instance;
}
/**
 * 
 */
protected void startupOnInitialize() {}
public boolean wasAutoBuild() {
	return triggerForLastBuild == IncrementalProjectBuilder.AUTO_BUILD;
}
public boolean wasFullBuild() {
	return triggerForLastBuild == IncrementalProjectBuilder.FULL_BUILD;
}
public boolean wasIncrementalBuild() {
	return triggerForLastBuild == IncrementalProjectBuilder.INCREMENTAL_BUILD;
}
}
