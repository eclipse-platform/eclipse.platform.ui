package org.eclipse.core.internal.resources;
/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.internal.localstore.FileSystemResourceManager;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

/**
 * Detects and responds to changes in project description files
 */
public class ProjectDescriptionChangeListener implements IResourceChangeListener {
	protected Workspace workspace;
	protected static final IPath filePath = Path.EMPTY.append(FileSystemResourceManager.F_PROJECT);
/**
 * Creates a new ProjectDescriptionChangeListener.
 */
public ProjectDescriptionChangeListener(Workspace workspace) {
	this.workspace = workspace;
}
/**
 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
 */
public void resourceChanged(IResourceChangeEvent event) {
	IResourceDelta delta = event.getDelta();
	//get deltas for the projects
	IResourceDelta[] projectDeltas = delta.getAffectedChildren();
	for (int i = 0; i < projectDeltas.length; i++) {
		//if the project description has changed, then copy in memory always wins
		if ((projectDeltas[i].getFlags() & IResourceDelta.DESCRIPTION) != 0)
			continue;
		IProject project = (IProject)projectDeltas[i].getResource();
		//if the project is being closed or deleted, we don't need to update description
		if (projectDeltas[i].getKind() == IResourceDelta.REMOVED || !project.isAccessible())
			continue;
		IResourceDelta childDelta = projectDeltas[i].findMember(filePath);
		if (childDelta == null)
			continue;
		//update the project description
		try {
			((Project)project).updateDescription();
		} catch (CoreException e) {
			//This error should be propagated to the user
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
		}
	}
}
}