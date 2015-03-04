/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River) - [108066] Project prefs marked dirty on read
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.*;
import java.util.*;
import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.internal.preferences.ExportedPreferences;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IExportedPreferences;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Represents a node in the Eclipse preference hierarchy which stores preference
 * values for projects.
 * 
 * @since 3.0
 */
public class ProjectPreferences extends EclipsePreferences {
	static final String PREFS_REGULAR_QUALIFIER = ResourcesPlugin.PI_RESOURCES;
	static final String PREFS_DERIVED_QUALIFIER = PREFS_REGULAR_QUALIFIER + ".derived"; //$NON-NLS-1$
	/**
	 * Cache which nodes have been loaded from disk
	 */
	protected static Set<String> loadedNodes = Collections.synchronizedSet(new HashSet<String>());
	private IFile file;
	private boolean initialized = false;
	/**
	 * Flag indicating that this node is currently reading values from disk,
	 * to avoid flushing during a read.
	 */
	private boolean isReading;
	/**
	 * Flag indicating that this node is currently writing values to disk,
	 * to avoid re-reading after the write completes.
	 */
	private boolean isWriting;
	private IEclipsePreferences loadLevel;
	private IProject project;
	private String qualifier;

	// cache
	private int segmentCount;

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
		ProjectPreferences projectNode = (ProjectPreferences) root.node(ProjectScope.SCOPE).node(project);
		// if the node isn't known then just return
		try {
			if (!projectNode.nodeExists(qualifier))
				return;
		} catch (BackingStoreException e) {
			// ignore
		}

		// clear the preferences
		clearNode(projectNode.node(qualifier));

		// notifies the CharsetManager if needed
		if (qualifier.equals(PREFS_REGULAR_QUALIFIER) || qualifier.equals(PREFS_DERIVED_QUALIFIER))
			preferencesChanged(file.getProject());
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
		boolean hasResourcesSettings = getFile(folder, PREFS_REGULAR_QUALIFIER).exists() || getFile(folder, PREFS_DERIVED_QUALIFIER).exists();
		// remove the preferences
		removeNode(projectNode);
		// notifies the CharsetManager 		
		if (hasResourcesSettings)
			preferencesChanged(folder.getProject());
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
		boolean hasResourcesSettings = getFile(project, PREFS_REGULAR_QUALIFIER).exists() || getFile(project, PREFS_DERIVED_QUALIFIER).exists();
		// remove the preferences
		removeNode(projectNode);
		// notifies the CharsetManager 		
		if (hasResourcesSettings)
			preferencesChanged(project);
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

	/*
	 * Return the preferences file for the given folder and qualifier.
	 */
	static IFile getFile(IFolder folder, String qualifier) {
		Assert.isLegal(folder.getName().equals(DEFAULT_PREFERENCES_DIRNAME));
		return folder.getFile(new Path(qualifier).addFileExtension(PREFS_FILE_EXTENSION));
	}

	/*
	 * Return the preferences file for the given project and qualifier.
	 */
	static IFile getFile(IProject project, String qualifier) {
		return project.getFile(new Path(DEFAULT_PREFERENCES_DIRNAME).append(qualifier).addFileExtension(PREFS_FILE_EXTENSION));
	}

	private static Properties loadProperties(IFile file) throws BackingStoreException {
		if (Policy.DEBUG_PREFERENCES)
			Policy.debug("Loading preferences from file: " + file.getFullPath()); //$NON-NLS-1$
		Properties result = new Properties();
		InputStream input = null;
		try {
			input = new BufferedInputStream(file.getContents(true));
			result.load(input);
		} catch (CoreException e) {
			String message = NLS.bind(Messages.preferences_loadException, file.getFullPath());
			log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
			throw new BackingStoreException(message);
		} catch (IOException e) {
			String message = NLS.bind(Messages.preferences_loadException, file.getFullPath());
			log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
			throw new BackingStoreException(message);
		} finally {
			FileUtil.safeClose(input);
		}
		return result;
	}

	private static void preferencesChanged(IProject project) {
		Workspace workspace = ((Workspace) ResourcesPlugin.getWorkspace());
		workspace.getCharsetManager().projectPreferencesChanged(project);
		workspace.getContentDescriptionManager().projectPreferencesChanged(project);
	}

	private static void read(ProjectPreferences node, IFile file) throws BackingStoreException, CoreException {
		if (file == null || !file.exists()) {
			if (Policy.DEBUG_PREFERENCES)
				Policy.debug("Unable to determine preference file or file does not exist for node: " + node.absolutePath()); //$NON-NLS-1$
			return;
		}
		Properties fromDisk = loadProperties(file);
		// no work to do
		if (fromDisk.isEmpty())
			return;
		// create a new node to store the preferences in.
		IExportedPreferences myNode = (IExportedPreferences) ExportedPreferences.newRoot().node(node.absolutePath());
		convertFromProperties((EclipsePreferences) myNode, fromDisk, false);
		//flag that we are currently reading, to avoid unnecessary writing
		boolean oldIsReading = node.isReading;
		node.isReading = true;
		try {
			Platform.getPreferencesService().applyPreferences(myNode);
		} finally {
			node.isReading = oldIsReading;
		}
	}

	static void removeNode(Preferences node) throws CoreException {
		String message = NLS.bind(Messages.preferences_removeNodeException, node.absolutePath());
		try {
			node.removeNode();
		} catch (BackingStoreException e) {
			IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e);
			throw new CoreException(status);
		}
		removeLoadedNodes(node);
	}

	static void clearNode(Preferences node) throws CoreException {
		// if the underlying properties file was deleted, clear the values and remove
		// it from the list of loaded nodes, keep the node as it might still be referenced
		try {
			clearAll(node);
		} catch (BackingStoreException e) {
			String message = NLS.bind(Messages.preferences_clearNodeException, node.absolutePath());
			IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e);
			throw new CoreException(status);
		}
		removeLoadedNodes(node);
	}

	private static void clearAll(Preferences node) throws BackingStoreException {
		node.clear();
		String[] names = node.childrenNames();
		for (int i = 0; i < names.length; i++) {
			clearAll(node.node(names[i]));
		}
	}

	private static void removeLoadedNodes(Preferences node) {
		String path = node.absolutePath();
		synchronized (loadedNodes) {
			for (Iterator<String> i = loadedNodes.iterator(); i.hasNext();) {
				String key = i.next();
				if (key.startsWith(path))
					i.remove();
			}
		}
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
			message = NLS.bind(Messages.preferences_syncException, node.absolutePath());
			if (!(node instanceof ProjectPreferences))
				return;
			ProjectPreferences projectPrefs = (ProjectPreferences) node;
			if (projectPrefs.isWriting)
				return;
			read(projectPrefs, file);
			// Bug 108066: In case the node had existed before it was updated from
			// file, the read() operation marks it dirty. Override the dirty flag
			// since we know that the node is expected to be in sync with the file.
			projectPrefs.dirty = false;

			// make sure that we generate the appropriate resource change events
			// if encoding settings have changed
			if (PREFS_REGULAR_QUALIFIER.equals(qualifier) || PREFS_DERIVED_QUALIFIER.equals(qualifier))
				preferencesChanged(file.getProject());
		} catch (BackingStoreException e) {
			IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e);
			throw new CoreException(status);
		}
	}

	/**
	 * Default constructor. Should only be called by #createExecutableExtension.
	 */
	public ProjectPreferences() {
		super(null, null);
	}

	private ProjectPreferences(EclipsePreferences parent, String name) {
		super(parent, name);

		// cache the segment count
		String path = absolutePath();
		segmentCount = getSegmentCount(path);

		if (segmentCount == 1)
			return;

		// cache the project name
		String projectName = getSegment(path, 1);
		if (projectName != null)
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		// cache the qualifier
		if (segmentCount > 2)
			qualifier = getSegment(path, 2);
	}

	@Override
	public String[] childrenNames() throws BackingStoreException {
		// illegal state if this node has been removed
		checkRemoved();
		initialize();
		silentLoad();
		return super.childrenNames();
	}

	@Override
	public void clear() {
		// illegal state if this node has been removed
		checkRemoved();
		silentLoad();
		super.clear();
	}

	/*
	 * Figure out what the children of this node are based on the resources
	 * that are in the workspace.
	 */
	private String[] computeChildren() {
		if (project == null)
			return EMPTY_STRING_ARRAY;
		IFolder folder = project.getFolder(DEFAULT_PREFERENCES_DIRNAME);
		if (!folder.exists())
			return EMPTY_STRING_ARRAY;
		IResource[] members = null;
		try {
			members = folder.members();
		} catch (CoreException e) {
			return EMPTY_STRING_ARRAY;
		}
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < members.length; i++) {
			IResource resource = members[i];
			if (resource.getType() == IResource.FILE && PREFS_FILE_EXTENSION.equals(resource.getFullPath().getFileExtension()))
				result.add(resource.getFullPath().removeFileExtension().lastSegment());
		}
		return result.toArray(EMPTY_STRING_ARRAY);
	}

	@Override
	public void flush() throws BackingStoreException {
		if (isReading)
			return;
		isWriting = true;
		try {
			// call the internal method because we don't want to be synchronized, we will do that ourselves later.
			IEclipsePreferences toFlush = super.internalFlush();
			//if we aren't at the right level, then flush the appropriate node
			if (toFlush != null)
				toFlush.flush();
		} finally {
			isWriting = false;
		}
	}

	private IFile getFile() {
		if (file == null) {
			if (project == null || qualifier == null)
				return null;
			file = getFile(project, qualifier);
		}
		return file;
	}

	/*
	 * Return the node at which these preferences are loaded/saved.
	 */
	@Override
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

	/*
	 * Calculate and return the file system location for this preference node.
	 * Use the absolute path of the node to find out the project name so 
	 * we can get its location on disk.
	 * 
	 * NOTE: we cannot cache the location since it may change over the course
	 * of the project life-cycle.
	 */
	@Override
	protected IPath getLocation() {
		if (project == null || qualifier == null)
			return null;
		IPath path = project.getLocation();
		return computeLocation(path, qualifier);
	}

	@Override
	protected EclipsePreferences internalCreate(EclipsePreferences nodeParent, String nodeName, Object context) {
		return new ProjectPreferences(nodeParent, nodeName);
	}

	@Override
	protected String internalGet(String key) {
		// throw NPE if key is null
		if (key == null)
			throw new NullPointerException();
		// illegal state if this node has been removed
		checkRemoved();
		silentLoad();
		return super.internalGet(key);
	}

	@Override
	protected String internalPut(String key, String newValue) {
		// illegal state if this node has been removed
		checkRemoved();
		silentLoad();
		if ((segmentCount == 3) && PREFS_REGULAR_QUALIFIER.equals(qualifier) && (project != null)) {
			if (ResourcesPlugin.PREF_SEPARATE_DERIVED_ENCODINGS.equals(key)) {
				CharsetManager charsetManager = ((Workspace) ResourcesPlugin.getWorkspace()).getCharsetManager();
				if (Boolean.parseBoolean(newValue))
					charsetManager.splitEncodingPreferences(project);
				else
					charsetManager.mergeEncodingPreferences(project);
			}
		}
		return super.internalPut(key, newValue);
	}

	private void initialize() {
		if (segmentCount != 2)
			return;

		// if already initialized, then skip this initialization
		if (initialized)
			return;

		// initialize the children only if project is opened
		if (project.isOpen()) {
			try {
				synchronized (this) {
					List<String> addedNames = Arrays.asList(internalChildNames());
					String[] names = computeChildren();
					// add names only for nodes that were not added previously
					for (int i = 0; i < names.length; i++)
						if (!addedNames.contains(names[i]))
							addChild(names[i], null);
				}
			} finally {
				// mark as initialized so that subsequent project opening will not initialize preferences again
				initialized = true;
			}
		}
	}

	@Override
	protected boolean isAlreadyLoaded(IEclipsePreferences node) {
		return loadedNodes.contains(node.absolutePath());
	}

	@Override
	public String[] keys() {
		// illegal state if this node has been removed
		checkRemoved();
		silentLoad();
		return super.keys();
	}

	@Override
	protected void load() throws BackingStoreException {
		load(true);
	}

	private void load(boolean reportProblems) throws BackingStoreException {
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
			convertFromProperties(this, fromDisk, true);
			loadedNodes.add(absolutePath());
		} catch (CoreException e) {
			if (reportProblems) {
				String message = NLS.bind(Messages.preferences_loadException, localFile.getFullPath());
				log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
				throw new BackingStoreException(message);
			}
		} catch (IOException e) {
			if (reportProblems) {
				String message = NLS.bind(Messages.preferences_loadException, localFile.getFullPath());
				log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
				throw new BackingStoreException(message);
			}
		} finally {
			FileUtil.safeClose(input);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.internal.preferences.EclipsePreferences#nodeExists(java.lang.String)
	 * 
	 * If we are at the /project node and we are checking for the existence of a child, we
	 * want special behaviour. If the child is a single segment name, then we want to
	 * return true if the node exists OR if a project with that name exists in the workspace.
	 */
	@Override
	public boolean nodeExists(String path) throws BackingStoreException {
		// short circuit for checking this node
		if (path.length() == 0)
			return !removed;
		// illegal state if this node has been removed.
		// do this AFTER checking for the empty string.
		checkRemoved();
		initialize();
		silentLoad();
		if (segmentCount != 1)
			return super.nodeExists(path);
		if (path.length() == 0)
			return super.nodeExists(path);
		if (path.charAt(0) == IPath.SEPARATOR)
			return super.nodeExists(path);
		if (path.indexOf(IPath.SEPARATOR) != -1)
			return super.nodeExists(path);
		// if we are checking existance of a single segment child of /project, base the answer on
		// whether or not it exists in the workspace.
		return ResourcesPlugin.getWorkspace().getRoot().getProject(path).exists() || super.nodeExists(path);
	}

	@Override
	public void remove(String key) {
		// illegal state if this node has been removed
		checkRemoved();
		silentLoad();
		super.remove(key);
		if ((segmentCount == 3) && PREFS_REGULAR_QUALIFIER.equals(qualifier) && (project != null)) {
			if (ResourcesPlugin.PREF_SEPARATE_DERIVED_ENCODINGS.equals(key)) {
				CharsetManager charsetManager = ((Workspace) ResourcesPlugin.getWorkspace()).getCharsetManager();
				if (ResourcesPlugin.DEFAULT_PREF_SEPARATE_DERIVED_ENCODINGS)
					charsetManager.splitEncodingPreferences(project);
				else
					charsetManager.mergeEncodingPreferences(project);
			}
		}
	}

	@Override
	protected void save() throws BackingStoreException {
		final IFile fileInWorkspace = getFile();
		if (fileInWorkspace == null) {
			if (Policy.DEBUG_PREFERENCES)
				Policy.debug("Not saving preferences since there is no file for node: " + absolutePath()); //$NON-NLS-1$
			return;
		}
		final String finalQualifier = qualifier;
		final BackingStoreException[] bse = new BackingStoreException[1];
		try {
			IWorkspaceRunnable operation = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						Properties table = convertToProperties(new SortedProperties(), ""); //$NON-NLS-1$
						// nothing to save. delete existing file if one exists.
						if (table.isEmpty()) {
							if (fileInWorkspace.exists()) {
								if (Policy.DEBUG_PREFERENCES)
									Policy.debug("Deleting preference file: " + fileInWorkspace.getFullPath()); //$NON-NLS-1$
								if (fileInWorkspace.isReadOnly()) {
									IStatus status = fileInWorkspace.getWorkspace().validateEdit(new IFile[] {fileInWorkspace}, IWorkspace.VALIDATE_PROMPT);
									if (!status.isOK())
										throw new CoreException(status);
								}
								try {
									fileInWorkspace.delete(true, null);
								} catch (CoreException e) {
									String message = NLS.bind(Messages.preferences_deleteException, fileInWorkspace.getFullPath());
									log(new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, IStatus.WARNING, message, null));
								}
							}
							return;
						}
						table.put(VERSION_KEY, VERSION_VALUE);
						// print the table to a string and remove the timestamp that Properties#store always adds
						String s = removeTimestampFromTable(table);
						String systemLineSeparator = System.getProperty("line.separator"); //$NON-NLS-1$
						String fileLineSeparator = FileUtil.getLineSeparator(fileInWorkspace);
						if (!systemLineSeparator.equals(fileLineSeparator))
							s = s.replaceAll(systemLineSeparator, fileLineSeparator);
						InputStream input = new BufferedInputStream(new ByteArrayInputStream(s.getBytes("UTF-8"))); //$NON-NLS-1$
						if (fileInWorkspace.exists()) {
							if (Policy.DEBUG_PREFERENCES)
								Policy.debug("Setting preference file contents for: " + fileInWorkspace.getFullPath()); //$NON-NLS-1$
							if (fileInWorkspace.isReadOnly()) {
								IStatus status = fileInWorkspace.getWorkspace().validateEdit(new IFile[] {fileInWorkspace}, IWorkspace.VALIDATE_PROMPT);
								if (!status.isOK()) {
									input.close();
									throw new CoreException(status);
								}
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
						if (PREFS_DERIVED_QUALIFIER.equals(finalQualifier))
							fileInWorkspace.setDerived(true, null);
					} catch (BackingStoreException e) {
						bse[0] = e;
					} catch (IOException e) {
						String message = NLS.bind(Messages.preferences_saveProblems, fileInWorkspace.getFullPath());
						log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
						bse[0] = new BackingStoreException(message);
					}
				}
			};
			//don't bother with scheduling rules if we are already inside an operation
			try {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				if (((Workspace) workspace).getWorkManager().isLockAlreadyAcquired()) {
					operation.run(null);
				} else {
					IResourceRuleFactory factory = workspace.getRuleFactory();
					// we might: delete the file, create the .settings folder, create the file, modify the file, or set derived flag for the file.
					ISchedulingRule rule = MultiRule.combine(new ISchedulingRule[] {factory.deleteRule(fileInWorkspace), factory.createRule(fileInWorkspace.getParent()), factory.modifyRule(fileInWorkspace), factory.derivedRule(fileInWorkspace)});
					workspace.run(operation, rule, IResource.NONE, null);
					if (bse[0] != null)
						throw bse[0];
				}
			} catch (OperationCanceledException e) {
				throw new BackingStoreException(Messages.preferences_operationCanceled);
			}
		} catch (CoreException e) {
			String message = NLS.bind(Messages.preferences_saveProblems, fileInWorkspace.getFullPath());
			log(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IStatus.ERROR, message, e));
			throw new BackingStoreException(message);
		}
	}

	private void silentLoad() {
		ProjectPreferences node = (ProjectPreferences) getLoadLevel();
		if (node == null)
			return;
		if (isAlreadyLoaded(node) || node.isLoading())
			return;
		try {
			node.setLoading(true);
			node.load(false);
		} catch (BackingStoreException e) {
			// will not happen, all exceptions are swallowed by load(false)
		} finally {
			node.setLoading(false);
		}
	}
}
