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
import org.eclipse.core.internal.utils.Assert;
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
	private int segmentCount;
	private String qualifier;
	private IProject project;
	private IEclipsePreferences loadLevel;
	private IFile file;
	// cache which nodes have been loaded from disk
	protected static Set loadedNodes = new HashSet();
	private boolean isWriting;

	/**
	 * Default constructor. Should only be called by #createExecutableExtension.
	 */
	public ProjectPreferences() {
		super(null, null);
	}

	private ProjectPreferences(IEclipsePreferences parent, String name) {
		super(parent, name);
		// cache the segment count
		String path = absolutePath();
		segmentCount = getSegmentCount(path);
		if (segmentCount < 2)
			return;

		// cache the project name
		String projectName = getSegment(path, 1);
		if (projectName != null)
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		// cache the qualifier
		if (segmentCount > 2)
			qualifier = getSegment(path, 2);
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

	protected boolean isAlreadyLoaded(IEclipsePreferences node) {
		return loadedNodes.contains(node.absolutePath());
	}

	protected void loaded() {
		loadedNodes.add(absolutePath());
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
			file = getFile(project, qualifier);
		}
		return file;
	}

	static IFile getFile(IProject project, String qualifier) {
		return project.getFile(new Path(DEFAULT_PREFERENCES_DIRNAME).append(qualifier).addFileExtension(PREFS_FILE_EXTENSION));
	}

	static IFile getFile(IFolder folder, String qualifier) {
		Assert.isLegal(folder.getName().equals(DEFAULT_PREFERENCES_DIRNAME));
		return folder.getFile(new Path(qualifier).addFileExtension(PREFS_FILE_EXTENSION));
	}

	protected void save() throws BackingStoreException {
		IFile fileInWorkspace = getFile();
		if (fileInWorkspace == null) {
			if (Policy.DEBUG_PREFERENCES)
				Policy.debug("Not saving preferences since there is no file for node: " + absolutePath()); //$NON-NLS-1$
			return;
		}
		Properties table = convertToProperties(new Properties(), ""); //$NON-NLS-1$
		try {
			if (table.isEmpty()) {
				// nothing to save. delete existing file if one exists.
				if (fileInWorkspace.exists()) {
					if (Policy.DEBUG_PREFERENCES)
						Policy.debug("Deleting preference file: " + fileInWorkspace.getFullPath()); //$NON-NLS-1$
					if (fileInWorkspace.isReadOnly()) {
						IStatus status = fileInWorkspace.getWorkspace().validateEdit(new IFile[] {fileInWorkspace}, null);
						if (!status.isOK())
							throw new CoreException(status);
					}
					try {
						fileInWorkspace.delete(true, null);
					} catch (CoreException e) {
						String message = Policy.bind("preferences.deleteException", fileInWorkspace.getFullPath().toString()); //$NON-NLS-1$
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
			if (fileInWorkspace.exists()) {
				if (Policy.DEBUG_PREFERENCES)
					Policy.debug("Setting preference file contents for: " + fileInWorkspace.getFullPath()); //$NON-NLS-1$
				if (fileInWorkspace.isReadOnly()) {
					IStatus status = fileInWorkspace.getWorkspace().validateEdit(new IFile[] {fileInWorkspace}, null);
					if (!status.isOK())
						throw new CoreException(status);
				}
				// set the contents
				fileInWorkspace.setContents(input, IResource.KEEP_HISTORY, null);
			} else {
				// create the file
				IFolder folder = (IFolder) fileInWorkspace.getParent();
				if (!folder.exists()) {
					if (Policy.DEBUG_PREFERENCES)
						Policy.debug("Creating parent preference directory: " + folder.getFullPath()); //$NON-NLS-1$
					folder.create(IResource.NONE, true, null);
				}
				if (Policy.DEBUG_PREFERENCES)
					Policy.debug("Creating preference file: " + fileInWorkspace.getLocation()); //$NON-NLS-1$
				fileInWorkspace.create(input, IResource.NONE, null);
			}
		} catch (CoreException e) {
			String message = Policy.bind("preferences.saveProblems", fileInWorkspace.getFullPath().toString()); //$NON-NLS-1$
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
			input = new BufferedInputStream(localFile.getContents(true));
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
		convertFromProperties(fromDisk, true);
	}

	public static void updatePreferences(IFile file) throws CoreException {
		IPath path = file.getFullPath();
		// if we made it this far we are inside /project/.settings and might
		// have a change to a preference file
		if (!PREFS_FILE_EXTENSION.equals(path.getFileExtension()))
			return;

		String project = path.segment(0);
		String qualifier = path.removeFileExtension().lastSegment();
		Preferences root = Platform.getPreferencesService().getRootNode();
		Preferences node = root.node(ProjectScope.SCOPE).node(project).node(qualifier);
		String message = null;
		try {
			message = Policy.bind("preferences.syncException", node.absolutePath()); //$NON-NLS-1$
			if (!(node instanceof ProjectPreferences))
				return;
			ProjectPreferences projectPrefs = (ProjectPreferences) node;
			if (projectPrefs.isWriting)
				return;
			projectPrefs.load();
			// make sure that we generate the appropriate resource change events
			// if encoding settings have changed
			if (ResourcesPlugin.PI_RESOURCES.equals(qualifier))
				((Workspace) ResourcesPlugin.getWorkspace()).getCharsetManager().charsetPreferencesChanged(file.getProject());
		} catch (BackingStoreException e) {
			IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e);
			throw new CoreException(status);
		}
	}

	static void removeNode(Preferences node) throws CoreException {
		String message = Policy.bind("preferences.removeNodeException", node.absolutePath()); //$NON-NLS-1$
		try {
			node.removeNode();
		} catch (BackingStoreException e) {
			IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e);
			throw new CoreException(status);
		}
		String path = node.absolutePath();
		for (Iterator i = loadedNodes.iterator(); i.hasNext();) {
			String key = (String) i.next();
			if (key.startsWith(path))
				i.remove();
		}
	}

	/*
	 * The whole project has been removed so delete all of the project settings
	 */
	static void deleted(IProject project) throws CoreException {
		// The settings dir has been removed/moved so remove all project prefs
		// for the resource. We have to do this now because (since we aren't
		// synchronizing) there is short-circuit code that doesn't visit the
		// children.
		Preferences root = Platform.getPreferencesService().getRootNode();
		Preferences projectNode = root.node(ProjectScope.SCOPE).node(project.getName());
		// check if we need to notify the charset manager
		boolean hasResourcesSettings = getFile(project, ResourcesPlugin.PI_RESOURCES).exists();
		// remove the preferences
		removeNode(projectNode);
		// notifies the CharsetManager 		
		if (hasResourcesSettings)
			((Workspace) ResourcesPlugin.getWorkspace()).getCharsetManager().charsetPreferencesChanged(project);
	}

	static void deleted(IFile file) throws CoreException {
		IPath path = file.getFullPath();
		int count = path.segmentCount();
		if (count != 3)
			return;
		// check if we are in the .settings directory
		if (!EclipsePreferences.DEFAULT_PREFERENCES_DIRNAME.equals(path.segment(1)))
			return;
		Preferences root = Platform.getPreferencesService().getRootNode();
		String project = path.segment(0);
		String qualifier = path.removeFileExtension().lastSegment();
		Preferences projectNode = root.node(ProjectScope.SCOPE).node(project);
		try {
			if (!projectNode.nodeExists(qualifier))
				return;
		} catch (BackingStoreException e) {
			// ignore
		}
		// remove the preferences
		removeNode(projectNode.node(qualifier));
		// notifies the CharsetManager if needed
		if (qualifier.equals(ResourcesPlugin.PI_RESOURCES))
			((Workspace) ResourcesPlugin.getWorkspace()).getCharsetManager().charsetPreferencesChanged(file.getProject());
	}

	static void deleted(IResource resource) throws CoreException {
		switch (resource.getType()) {
			case IResource.FILE :
				deleted((IFile) resource);
				return;
			case IResource.FOLDER :
				deleted((IFolder) resource);
				return;
			case IResource.PROJECT :
				deleted((IProject) resource);
				return;
		}
	}

	static void deleted(IFolder folder) throws CoreException {
		IPath path = folder.getFullPath();
		int count = path.segmentCount();
		if (count != 2)
			return;
		// check if we are the .settings directory
		if (!EclipsePreferences.DEFAULT_PREFERENCES_DIRNAME.equals(path.segment(1)))
			return;
		Preferences root = Platform.getPreferencesService().getRootNode();
		// The settings dir has been removed/moved so remove all project prefs
		// for the resource.
		String project = path.segment(0);
		Preferences projectNode = root.node(ProjectScope.SCOPE).node(project);
		// check if we need to notify the charset manager
		boolean hasResourcesSettings = getFile(folder, ResourcesPlugin.PI_RESOURCES).exists();
		// remove the preferences
		removeNode(projectNode);
		// notifies the CharsetManager 		
		if (hasResourcesSettings)
			((Workspace) ResourcesPlugin.getWorkspace()).getCharsetManager().charsetPreferencesChanged(folder.getProject());
	}

	public void flush() throws BackingStoreException {
		isWriting = true;
		try {
			super.flush();
		} finally {
			isWriting = false;
		}
	}
}