/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.events;

import org.eclipse.core.internal.resources.ICoreConstants;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.resources.IProject;

public class BuilderPersistentInfo {
	protected String projectName;
	protected String builderName;
	protected ElementTree lastBuildTree;
	protected IProject[] interestingProjects = ICoreConstants.EMPTY_PROJECT_ARRAY;

public void setProjectName(String name) {
	projectName = name;
}
public void setBuilderName(String name) {
	builderName = name;
}
public void setLastBuildTree(ElementTree tree) {
	lastBuildTree = tree;
}
public void setInterestingProjects(IProject[] projects) {
	interestingProjects = projects;
}
public String getProjectName() {
	return projectName;
}
public String getBuilderName() {
	return builderName;
}
public ElementTree getLastBuiltTree() {
	return lastBuildTree;
}
public IProject[] getInterestingProjects() {
	return interestingProjects;
}
}