/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Broadcom Corporation - build configurations
 *******************************************************************************/
package org.eclipse.core.internal.events;

import org.eclipse.core.internal.resources.ICoreConstants;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.resources.IProject;

public class BuilderPersistentInfo {
	protected String builderName;
	/**
	 * Index of this builder in the build spec. A value of -1 indicates
	 * that this index is unknown (it was not serialized in older workspace versions).
	 */
	private int buildSpecIndex = -1;
	protected IProject[] interestingProjects = ICoreConstants.EMPTY_PROJECT_ARRAY;
	protected ElementTree lastBuildTree;
	protected String projectName;
	protected String configId;

	public BuilderPersistentInfo(String projectName, String builderName, int buildSpecIndex) {
		this(projectName, null, builderName, buildSpecIndex);
	}

	public BuilderPersistentInfo(String projectName, String configId, String builderName, int buildSpecIndex) {
		this.projectName = projectName;
		this.configId = configId;
		this.builderName = builderName;
		this.buildSpecIndex = buildSpecIndex;
	}

	public String getBuilderName() {
		return builderName;
	}

	public int getBuildSpecIndex() {
		return buildSpecIndex;
	}

	/**
	 * @return the id of the configuration for which this information refers. 
	 * Will return null if the build command doesn't support configurations, or the 
	 * build persistent info has been loaded from a workspace without configurations.
	 */
	public String getConfigurationId() {
		return configId;
	}

	public IProject[] getInterestingProjects() {
		return interestingProjects;
	}

	public ElementTree getLastBuiltTree() {
		return lastBuildTree;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setConfigurationId(String configId) {
		this.configId = configId;
	}

	public void setInterestingProjects(IProject[] projects) {
		interestingProjects = projects;
	}

	public void setLastBuildTree(ElementTree tree) {
		lastBuildTree = tree;
	}
}
