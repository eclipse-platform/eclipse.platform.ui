/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.internal.events.BuilderPersistentInfo;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Reads version 2 of the workspace tree file format. 
 * 
 * This version differs from version 1 in the amount of information that is persisted
 * for each builder. Version 1 only stored builder names and trees. Version
 * 2 stores builder names, project names, trees, and interesting projects for
 * each builder.
 */
public class WorkspaceTreeReader_2 extends WorkspaceTreeReader_1 {

	public WorkspaceTreeReader_2(Workspace workspace) {
		super(workspace);
	}

	protected int getVersion() {
		return ICoreConstants.WORKSPACE_TREE_VERSION_2;
	}

	/*
	 * overwritten from WorkspaceTreeReader_1
	 */
	protected void readBuildersPersistentInfo(IProject project, DataInputStream input, List builders, IProgressMonitor monitor) throws IOException {
		monitor = Policy.monitorFor(monitor);
		try {
			int builderCount = input.readInt();
			for (int i = 0; i < builderCount; i++) {
				BuilderPersistentInfo info = readBuilderInfo(project, input, i);
				// read interesting projects
				int n = input.readInt();
				IProject[] projects = new IProject[n];
				for (int j = 0; j < n; j++)
					projects[j] = workspace.getRoot().getProject(input.readUTF());
				info.setInterestingProjects(projects);
				builders.add(info);
			}
		} finally {
			monitor.done();
		}
	}

	/*
	 * overwritten from WorkspaceTreeReader_1
	 */
	public void readTree(IProject project, DataInputStream input, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		String message;
		try {
			message = Messages.resources_reading;
			monitor.beginTask(message, 10);

			/* read in the builder infos */
			List infos = new ArrayList(5);
			readBuildersPersistentInfo(project, input, infos, Policy.subMonitorFor(monitor, 1));

			/* read and link the trees */
			ElementTree[] trees = readTrees(project.getFullPath(), input, Policy.subMonitorFor(monitor, 8));

			/* map builder names to trees */
			linkBuildersToTrees(infos, trees, 0, Policy.subMonitorFor(monitor, 1));

		} catch (IOException e) {
			message = Messages.resources_readProjectTree;
			throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, null, message, e);
		} finally {
			monitor.done();
		}
	}
}
