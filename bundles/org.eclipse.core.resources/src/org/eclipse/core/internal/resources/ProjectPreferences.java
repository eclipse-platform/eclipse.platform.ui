/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Represents a node in the Eclipse preference hierarchy which stores preference
 * values for projects.
 * 
 * @since 3.0
 */
public class ProjectPreferences extends EclipsePreferences {

	protected boolean isLoading = false;
	// cache
	private int segmentCount = 0;
	private String qualifier;
	private String projectName;
	private EclipsePreferences loadLevel;
	// cache which nodes have been loaded from disk
	private static Set loadedNodes = new HashSet();
	private static IResourceChangeListener listener = createListener();

	static {
		addListener();
	}

	/**
	 * Default constructor. Should only be called by #createExecutableExtension.
	 */
	public ProjectPreferences() {
		super(null, null);
	}

	private ProjectPreferences(IEclipsePreferences parent, String name) {
		super(parent, name);
		initialize();
	}

	/*
	 * Calculate and return the file system location for this preference node.
	 * Use the absolute path of the node to find out the project name so 
	 * we can get its location on disk.
	 * 
	 * NOTE: we cannot cache the location since it may change over the course
	 * of the project life-cycle.
	 */
	protected IPath getLocation() {
		if (projectName == null || qualifier == null)
			return null;
		IPath path = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName).getLocation();
		return computeLocation(path, qualifier);
	}

	private static IResourceChangeListener createListener() {
		final IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
			public boolean visit(IResourceDelta delta) throws CoreException {
				IPath path = delta.getFullPath();
				int count = path.segmentCount();

				// we only want to deal with changes in our specific subdir
				if (count < 2)
					return true;

				// check to see if we are the .settings directory
				if (count == 2) {
					String name = path.segment(1);
					return DEFAULT_PREFERENCES_DIRNAME.equals(name);
				}

				// shouldn't have to check this but do it just in case
				if (count > 3)
					return false;

				// if we made it this far we are inside /project/.settings and might
				// have a change to a preference file
				if (!PREFS_FILE_EXTENSION.equals(path.getFileExtension()))
					return false;

				String project = path.segment(0);
				String qualifier = path.removeFileExtension().lastSegment();
				IPath prefsPath = new Path(ProjectScope.SCOPE).append(project).append(qualifier);
				IEclipsePreferences node = Platform.getPreferencesService().getRootNode().node(prefsPath);
				try {
					node.sync();
				} catch (BackingStoreException e) {
					String message = "Exception syncing preferences for node: " + node.absolutePath();
					IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e);
					throw new CoreException(status);
				}

				// no more work to do
				return false;
			}
		};

		IResourceChangeListener result = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				if (event.getType() != IResourceChangeEvent.PRE_AUTO_BUILD)
					return;
				IResourceDelta delta = event.getDelta();
				if (delta == null)
					return;
				try {
					delta.accept(visitor);
				} catch (CoreException e) {
					String message = "Exception after processing file system change for possible preferences file.";
					IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e);
					ResourcesPlugin.getPlugin().getLog().log(status);
				}
			}
		};
		return result;
	}

	private static void addListener() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.PRE_AUTO_BUILD);
	}

	private static void removeListener() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
	}

	/*
	 * Parse this node's absolute path and initialize some cached values for
	 * later use.
	 */
	private void initialize() {
		// cache the segment count
		IPath path = new Path(absolutePath());
		segmentCount = path.segmentCount();
		if (segmentCount < 2)
			return;
		// cache the project name
		String scope = path.segment(0);
		if (ProjectScope.SCOPE.equals(scope))
			projectName = path.segment(1);
		// cache the qualifier
		if (segmentCount > 2)
			qualifier = path.segment(2);
	}

	protected boolean isAlreadyLoaded(IEclipsePreferences node) {
		return loadedNodes.contains(node.name());
	}

	protected void loaded() {
		loadedNodes.add(name());
	}

	/*
	 * Return the node at which these preferences are loaded/saved.
	 */
	protected IEclipsePreferences getLoadLevel() {
		if (loadLevel == null) {
			if (projectName == null || qualifier == null)
				return null;
			// Make it relative to this node rather than navigating to it from the root.
			// Walk backwards up the tree starting at this node.
			// This is important to avoid a chicken/egg thing on startup.
			EclipsePreferences node = this;
			for (int i = 3; i < segmentCount; i++)
				node = (EclipsePreferences) node.parent();
			loadLevel = node;
		}
		return loadLevel;
	}

	protected EclipsePreferences internalCreate(IEclipsePreferences nodeParent, String nodeName) {
		return new ProjectPreferences(nodeParent, nodeName);
	}
}