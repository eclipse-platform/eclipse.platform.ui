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
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.*;
import org.eclipse.core.internal.utils.Policy;
import java.io.*;
import java.util.*;

public class WorkspaceTreeReader {
	protected Workspace workspace;

public WorkspaceTreeReader(Workspace workspace) {
	this.workspace = workspace;
}
public void readTree(DataInputStream input, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	String message;
	try {
		message = Policy.bind("resources.reading"); //$NON-NLS-1$
		monitor.beginTask(message, Policy.totalWork);
		readWorkspaceFields(input, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));

		HashMap savedStates = new HashMap(20);
		List pluginsToBeLinked = new ArrayList(20);
		readPluginsSavedStates(input, savedStates, pluginsToBeLinked, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));
		workspace.getSaveManager().setPluginsSavedState(savedStates);

		List buildersToBeLinked = new ArrayList(20);
		readBuildersPersistentInfo(input, buildersToBeLinked, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));

		ElementTree[] trees = readTrees(Path.ROOT, input, Policy.subMonitorFor(monitor, Policy.opWork * 40 / 100));
		linkPluginsSavedStateToTrees(pluginsToBeLinked, trees, Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));
		linkBuildersToTrees(buildersToBeLinked, trees, pluginsToBeLinked.size(), Policy.subMonitorFor(monitor, Policy.opWork * 10 / 100));

	} catch (IOException e) {
		message = Policy.bind("resources.readWorkspaceTree"); //$NON-NLS-1$
		throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, null, message, e);
	} finally {
		monitor.done();
	}
}
protected void readWorkspaceFields(DataInputStream input, IProgressMonitor monitor) throws IOException, CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		// read the node id 
		workspace.nextNodeId = input.readLong();
		// read the modification stamp
		workspace.nextModificationStamp = input.readLong();
		// read the next marker id
		workspace.nextMarkerId = input.readLong();
		// read the synchronizer's registered sync partners
		 ((Synchronizer) workspace.getSynchronizer()).readPartners(input);
	} finally {
		monitor.done();
	}
}
protected void readPluginsSavedStates(DataInputStream input, HashMap savedStates, List plugins, IProgressMonitor monitor) throws IOException, CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		int stateCount = input.readInt();
		for (int i = 0; i < stateCount; i++) {
			String pluginId = input.readUTF();
			SavedState state = new SavedState(workspace, pluginId, null, null);
			savedStates.put(pluginId, state);
			plugins.add(state);
		}
	} finally {
		monitor.done();
	}
}
protected void readBuildersPersistentInfo(DataInputStream input, List builders, IProgressMonitor monitor) throws IOException, CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		int builderCount = input.readInt();
		for (int i = 0; i < builderCount; i++) {
			BuilderPersistentInfo info = new BuilderPersistentInfo();
			info.setProjectName(input.readUTF());
			info.setBuilderName(input.readUTF());
			builders.add(info);
		}
	} finally {
		monitor.done();
	}
}
/**
 * Read trees from disk and link them to the workspace tree.
 */
protected ElementTree[] readTrees(IPath root, DataInputStream input, IProgressMonitor monitor) throws IOException, CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String message = Policy.bind("resources.reading"); //$NON-NLS-1$
		monitor.beginTask(message, 4);
		ElementTreeReader treeReader = new ElementTreeReader(workspace.getSaveManager());
		ElementTree[] trees = treeReader.readDeltaChain(input);
		monitor.worked(3);
		if (root.isRoot()) {
			//Don't need to link because we're reading the whole workspace.
			//The last tree in the chain is the complete tree.
			ElementTree newTree = trees[trees.length-1];
			newTree.setTreeData(workspace.tree.getTreeData());
			workspace.tree = newTree;
		} else {
			//splice the restored tree into the current set of trees
			workspace.linkTrees(root, trees);
		}
		monitor.worked(1);
		return trees;
	} finally {
		monitor.done();
	}
}
protected void linkPluginsSavedStateToTrees(List states, ElementTree[] trees, IProgressMonitor monitor) {
	monitor = Policy.monitorFor(monitor);
	try {
		for (int i = 0; i < states.size(); i++) {
			SavedState state = (SavedState) states.get(i);
			// If the tree is too old (depends on the policy), the plug-in should not
			// get it back as a delta. It is expensive to maintain this information too long.
			if (!workspace.getSaveManager().isOldPluginTree(state.pluginId))
				state.oldTree = trees[i];
		}
	} finally {
		monitor.done();
	}
}
protected void linkBuildersToTrees(List buildersToBeLinked, ElementTree[] trees, int index, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		HashMap infos = null;
		String projectName = null;
		for (int i = 0; i < buildersToBeLinked.size(); i++) {
			BuilderPersistentInfo info = (BuilderPersistentInfo) buildersToBeLinked.get(i);
			if (!info.getProjectName().equals(projectName)) {
				if (infos != null) { // if it is not the first iteration
					IProject project = workspace.getRoot().getProject(projectName);
					workspace.getBuildManager().setBuildersPersistentInfo(project, infos);
				}
				projectName = info.getProjectName();
				infos = new HashMap(5);
			}
			info.setLastBuildTree(trees[index++]);
			infos.put(info.getBuilderName(), info);
		}
		if (infos != null) {
			IProject project = workspace.getRoot().getProject(projectName);
			workspace.getBuildManager().setBuildersPersistentInfo(project, infos);
		}
	} finally {
		monitor.done();
	}
}
public ElementTree readSnapshotTree(DataInputStream input, ElementTree complete, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	String message;
	try {
		message = Policy.bind("resources.readingSnap"); //$NON-NLS-1$
		monitor.beginTask(message, Policy.totalWork);
		ElementTreeReader reader = new ElementTreeReader(workspace.getSaveManager());
		while (input.available() > 0) {
			readWorkspaceFields(input, Policy.subMonitorFor(monitor, Policy.totalWork / 2));
			complete = reader.readDelta(complete, input);
			try {
				// make sure each snapshot is read by the correct reader
				int version = input.readInt();
				if (version != getVersion())
					return getReader(workspace, version).readSnapshotTree(input, complete, monitor);
			} catch (EOFException e) {
				break;
			}
		}
		return complete;
	} catch (IOException e) {
		message = Policy.bind("resources.readWorkspaceSnap"); //$NON-NLS-1$
		throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, null, message, e);
	} finally {
		monitor.done();
	}
}
public static WorkspaceTreeReader getReader(Workspace workspace, int version) {
	switch (version) {
		case ICoreConstants.WORKSPACE_TREE_VERSION_1:
			return new WorkspaceTreeReader(workspace);
		case ICoreConstants.WORKSPACE_TREE_VERSION_2:
			return new WorkspaceTreeReader_2(workspace);
		default:
			// The following class should be
			// removed soon. See comments in WorkspaceTreeReader_0.
			return new WorkspaceTreeReader_0(workspace);
	}
}
protected int getVersion() {
	return ICoreConstants.WORKSPACE_TREE_VERSION_1;
}
public void readTree(IProject project, DataInputStream input, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	String message;
	try {
		message = Policy.bind("resources.reading"); //$NON-NLS-1$
		monitor.beginTask(message, 10);
		/* read the number of builders */
		int numBuilders = input.readInt();

		/* read in the list of builder names */
		String[] builderNames = new String[numBuilders];
		for (int i = 0; i < numBuilders; i++) {
			String builderName = input.readUTF();
			builderNames[i] = builderName;
		}
		monitor.worked(1);

		/* read and link the trees */
		ElementTree[] trees = readTrees(project.getFullPath(), input, Policy.subMonitorFor(monitor, 8));

		/* map builder names to trees */
		if (numBuilders > 0) {
			Map infos = new HashMap(trees.length * 2 + 1);
			for (int i = 0; i < numBuilders; i++) {
				BuilderPersistentInfo info = new BuilderPersistentInfo();
				info.setBuilderName(builderNames[i]);
				info.setProjectName(project.getName());
				info.setLastBuildTree(trees[i]);
				infos.put(builderNames[i], info);
			}
			workspace.getBuildManager().setBuildersPersistentInfo(project, infos);
		}
		monitor.worked(1);

	} catch (IOException e) {
		message = Policy.bind("readProjectTree"); //$NON-NLS-1$
		throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, null, message, e);
	} finally {
		monitor.done();
	}
}
}