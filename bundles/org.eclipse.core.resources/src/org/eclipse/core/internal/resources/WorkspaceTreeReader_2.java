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
package org.eclipse.core.internal.resources;

import org.eclipse.core.internal.events.BuilderPersistentInfo;
import org.eclipse.core.internal.watson.ElementTree;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class WorkspaceTreeReader_2 extends WorkspaceTreeReader {

public WorkspaceTreeReader_2(Workspace workspace) {
	super(workspace);
}
protected int getVersion() {
	return ICoreConstants.WORKSPACE_TREE_VERSION_2;
}
protected void readBuildersPersistentInfo(DataInputStream input, List builders, IProgressMonitor monitor) throws IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		int builderCount = input.readInt();
		for (int i = 0; i < builderCount; i++) {
			BuilderPersistentInfo info = new BuilderPersistentInfo();
			info.setProjectName(input.readUTF());
			info.setBuilderName(input.readUTF());
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
public void readTree(IProject project, DataInputStream input, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	String message;
	try {
		message = Policy.bind("resources.reading"); //$NON-NLS-1$
		monitor.beginTask(message, 10);

		/* read in the list of builder names */
		List infos = new ArrayList(5);
		readBuildersPersistentInfo(input, infos, Policy.subMonitorFor(monitor, 1));
		for (Iterator it = infos.iterator(); it.hasNext();) {
			// Slam project name in. It might happen that the project was moved
			// and we have the wrong name in the file.
			BuilderPersistentInfo info = (BuilderPersistentInfo) it.next();
			info.setProjectName(project.getName());
		}

		/* read and link the trees */
		ElementTree[] trees = readTrees(project.getFullPath(), input, Policy.subMonitorFor(monitor, 8));

		/* map builder names to trees */
		linkBuildersToTrees(infos, trees, 0, Policy.subMonitorFor(monitor, 1));

	} catch (IOException e) {
		message = Policy.bind("resources.readProjectTree"); //$NON-NLS-1$
		throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, null, message, e);
	} finally {
		monitor.done();
	}
}
}