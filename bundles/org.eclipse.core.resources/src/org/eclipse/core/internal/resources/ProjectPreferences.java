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

import java.io.*;
import java.util.*;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

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
	private IProject project;
	private EclipsePreferences loadLevel;
	private IFile file;
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
		// cache the segment count
		IPath path = new Path(absolutePath());
		segmentCount = path.segmentCount();
		if (segmentCount < 2)
			return;
		// cache the project name
		String scope = path.segment(0);
		if (ProjectScope.SCOPE.equals(scope))
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(1));
		// cache the qualifier
		if (segmentCount > 2)
			qualifier = path.segment(2);
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
		if (project == null || qualifier == null)
			return null;
		IPath path = project.getLocation();
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
					String message = Policy.bind("preferences.syncException", node.absolutePath()); //$NON-NLS-1$
					IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e);
					throw new CoreException(status);
				}

				// no more work to do
				return false;
			}
		};

		IResourceChangeListener result = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				switch (event.getType()) {
					case IResourceChangeEvent.PRE_BUILD :
						handleDelta(event);
						break;
					case IResourceChangeEvent.PRE_DELETE :
						handleProjectDelete(event);
						break;
				}
			}

			private void handleDelta(IResourceChangeEvent event) {
				IResourceDelta delta = event.getDelta();
				if (delta == null)
					return;
				try {
					delta.accept(visitor);
				} catch (CoreException e) {
					String message = Policy.bind("preferences.visitException"); //$NON-NLS-1$
					IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e);
					ResourcesPlugin.getPlugin().getLog().log(status);
				}
			}

			private void handleProjectDelete(IResourceChangeEvent event) {
				IResource resource = event.getResource();
				if (resource == null)
					return;
				Preferences scopeRoot = Platform.getPreferencesService().getRootNode().node(ProjectScope.SCOPE);
				try {
					if (!scopeRoot.nodeExists(resource.getName()))
						return;
					// delete the prefs
					scopeRoot.node(resource.getName()).removeNode();
				} catch (BackingStoreException e) {
					String message = Policy.bind("preferences.projectDeleteException", resource.getName()); //$NON-NLS-1$
					IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e);
					ResourcesPlugin.getPlugin().getLog().log(status);
				}
			}
		};
		return result;
	}

	private static void addListener() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_BUILD);
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
			if (project == null || qualifier == null)
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

	protected EclipsePreferences internalCreate(IEclipsePreferences nodeParent, String nodeName, Plugin context) {
		return new ProjectPreferences(nodeParent, nodeName);
	}

	private IFile getFile() {
		if (file == null) {
			if (project == null || qualifier == null)
				return null;
			file = project.getFile(new Path(DEFAULT_PREFERENCES_DIRNAME).append(qualifier).addFileExtension(PREFS_FILE_EXTENSION));
		}
		return file;
	}

	protected void save() throws BackingStoreException {
		IFile localFile = getFile();
		if (localFile == null) {
			if (Policy.DEBUG_PREFERENCES)
				Policy.debug("Not saving preferences since there is no file for node: " + absolutePath()); //$NON-NLS-1$
			return;
		}
		Properties table = convertToProperties(new Properties(), Path.EMPTY);
		if (table.isEmpty()) {
			// nothing to save. delete existing file if one exists.
			if (localFile.exists()) {
				if (Policy.DEBUG_PREFERENCES)
					Policy.debug("Deleting preference file: " + localFile.getFullPath()); //$NON-NLS-1$
				try {
					localFile.delete(true, null);
				} catch (CoreException e) {
					String message = Policy.bind("preferences.deleteException", localFile.getFullPath().toString()); //$NON-NLS-1$
					log(new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, IStatus.WARNING, message, null));
				}
			}
			return;
		}
		table.put(VERSION_KEY, VERSION_VALUE);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			table.store(output, null);
		} catch (IOException e) {
			String message = Policy.bind("preferences.saveProblems", absolutePath()); //$NON-NLS-1$
			log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, IStatus.ERROR, message, e));
			throw new BackingStoreException(message);
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				// ignore
			}
		}
		InputStream input = new BufferedInputStream(new ByteArrayInputStream(output.toByteArray()));
		try {
			if (localFile.exists()) {
				if (Policy.DEBUG_PREFERENCES)
					Policy.debug("Setting preference file contents for: " + localFile.getFullPath()); //$NON-NLS-1$
				// set the contents
				localFile.setContents(input, IResource.KEEP_HISTORY, null);
			} else {
				// create the file
				IFolder folder = (IFolder) localFile.getParent();
				if (!folder.exists()) {
					if (Policy.DEBUG_PREFERENCES)
						Policy.debug("Creating parent preference directory: " + folder.getFullPath()); //$NON-NLS-1$
					folder.create(IResource.NONE, true, null);
				}
				if (Policy.DEBUG_PREFERENCES)
					Policy.debug("Creating preference file: " + localFile.getLocation()); //$NON-NLS-1$
				localFile.create(input, IResource.NONE, null);
			}
		} catch (CoreException e) {
			String message = Policy.bind("preferences.saveProblems", localFile.getFullPath().toString()); //$NON-NLS-1$
			log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
			throw new BackingStoreException(message);
		}
	}

	protected void load() throws BackingStoreException {
		IFile localFile = getFile();
		if (localFile == null || !localFile.exists()) {
			if (Policy.DEBUG_PREFERENCES)
				Policy.debug("Unable to determine preference file or file does not exist for node: " + absolutePath()); //$NON-NLS-1$
			return;
		}
		if (Policy.DEBUG_PREFERENCES)
			Policy.debug("Loading preferences from file: " + localFile.getFullPath()); //$NON-NLS-1$
		Properties fromDisk = new Properties();
		InputStream input = null;
		try {
			input = new BufferedInputStream(localFile.getContents());
			fromDisk.load(input);
		} catch (CoreException e) {
			String message = Policy.bind("preferences.loadException", localFile.getFullPath().toString()); //$NON-NLS-1$
			log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
			throw new BackingStoreException(message);
		} catch (IOException e) {
			String message = Policy.bind("preferences.loadException", localFile.getFullPath().toString()); //$NON-NLS-1$
			log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
			throw new BackingStoreException(message);
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					// ignore
				}
		}
		convertFromProperties(fromDisk);
	}
}