/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.repo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.XMLWriter;

public class RepositoryRoot extends PlatformObject {

	public static final String[] DEFAULT_AUTO_REFRESH_FILES = { ".project", ".vcm_meta" }; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String DEFINED_MODULE_PREFIX = "module:"; //$NON-NLS-1$
	
	ICVSRepositoryLocation root;
	String name;
	// Map of String (remote folder path) -> Set (CVS tags)
	Map knownTags = new HashMap();
	// Map of String (remote folder path) -> Set (file paths that are project relative)
	Map autoRefreshFiles = new HashMap();
	// Map of String (module name) -> ICVSRemoteFolder (that is a defined module)
	Map modulesCache;
	
	public RepositoryRoot(ICVSRepositoryLocation root) {
		this.root = root;
	}
	
	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Method getRemoteFolder.
	 * @param path
	 * @param tag
	 * @return ICVSRemoteFolder
	 */
	public ICVSRemoteFolder getRemoteFolder(String path, CVSTag tag, IProgressMonitor monitor) throws CVSException {
		if (isDefinedModuleName(path)) {
			return getDefinedModule(getDefinedModuleName(path), tag, monitor);
		} else {
			return root.getRemoteFolder(path, tag);
		}
	}

	static boolean isDefinedModuleName(String path) {
		return path.startsWith(DEFINED_MODULE_PREFIX);
	}

	static String getDefinedModuleName(String path) {
		return path.substring(DEFINED_MODULE_PREFIX.length());
	}
	
	static String asDefinedModulePath(String path) {
		return DEFINED_MODULE_PREFIX + path;
	}
	
	/**
	 * Method getDefinedModule.
	 * @param path
	 * @param tag
	 * @param monitor
	 * @return ICVSRemoteFolder
	 */
	private ICVSRemoteFolder getDefinedModule(String path, CVSTag tag, IProgressMonitor monitor) throws CVSException {
		Map cache = getDefinedModulesCache(tag, monitor);
		ICVSRemoteFolder folder = (ICVSRemoteFolder)cache.get(path);
		if (folder != null) {
			folder = (ICVSRemoteFolder)folder.forTag(tag);
		}
		return folder;
	}
	
	private Map getDefinedModulesCache(CVSTag tag, IProgressMonitor monitor) throws CVSException {
		if (modulesCache == null) {
			modulesCache = new HashMap();
			try {
				ICVSRemoteResource[] folders = root.members(CVSTag.DEFAULT, true, monitor);
				for (int i = 0; i < folders.length; i++) {
					ICVSRemoteResource resource = folders[i];
					modulesCache.put(resource.getName(), resource);
				}
			} catch (CVSException e) {
				// we could't fetch the modules. Log the problem and continue
				CVSUIPlugin.log(e);
			}
		}
		return modulesCache;
	}
	
	public ICVSRemoteResource[] getDefinedModules(CVSTag tag, IProgressMonitor monitor) throws CVSException {
		Map cache = getDefinedModulesCache(tag, monitor);
		return (ICVSRemoteResource[]) cache.values().toArray(new ICVSRemoteResource[cache.size()]);
	}
	
	public static String getRemotePathFor(ICVSResource resource) throws CVSException {
		if (resource.isFolder()) {
			if (resource instanceof ICVSRemoteFolder) {
				ICVSRemoteFolder remoteFolder = (ICVSRemoteFolder) resource;
				if (remoteFolder.isDefinedModule()) {
					return asDefinedModulePath(remoteFolder.getName());
				}
			}
			FolderSyncInfo info = ((ICVSFolder)resource).getFolderSyncInfo();
			if (info == null)
				throw new CVSException(Policy.bind("RepositoryRoot.folderInfoMissing", resource.getName())); //$NON-NLS-1$
			return info.getRepository();
		} else {
			FolderSyncInfo info = resource.getParent().getFolderSyncInfo();
			if (info == null)
				throw new CVSException(Policy.bind("RepositoryRoot.folderInfoMissing", resource.getParent().getName())); //$NON-NLS-1$
			String path = new Path(info.getRepository()).append(resource.getName()).toString();
			return path;
		}
	}
	
	/**
	 * Returns the root.
	 * @return ICVSRepositoryLocation
	 */
	public ICVSRepositoryLocation getRoot() {
		return root;
	}

	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Accept the tags for any remote path that represents a folder. However, for the time being,
	 * the given version tags are added to the list of known tags for the 
	 * remote ancestor of the resource that is a direct child of the remote root.
	 * 
	 * It is the reponsibility of the caller to ensure that the given remote path is valid.
	 */
	public void addTags(String remotePath, CVSTag[] tags) {	
		// Get the name to cache the version tags with
		String name = getCachePathFor(remotePath);
		
		// Make sure there is a table for the ancestor that holds the tags
		Set set = (Set)knownTags.get(name);
		if (set == null) {
			set = new HashSet();
			knownTags.put(name, set);
		}
		
		// Store the tag with the appropriate ancestor
		for (int i = 0; i < tags.length; i++) {
			set.add(tags[i]);
		}
	}
	
	/**
	 * Remove the given tags from the receiver
	 * @param remotePath
	 * @param tags
	 */
	public void removeTags(String remotePath, CVSTag[] tags) {	
		// Get the name to cache the version tags with
		String name = getCachePathFor(remotePath);
		
		// Make sure there is a table for the ancestor that holds the tags
		Set set = (Set)knownTags.get(name);
		if (set == null) {
			return;
		}
		
		// Store the tag with the appropriate ancestor
		for (int i = 0; i < tags.length; i++) {
			set.remove(tags[i]);
		}
	}
	
	/**
	 * Returns the absolute paths of the auto refresh files relative to the
	 * repository.
	 * 
	 * @return String[]
	 */
	public String[] getAutoRefreshFiles(String remotePath) {
		String name = getCachePathFor(remotePath);
		Set files = (Set)autoRefreshFiles.get(name);
		if (files == null || files.isEmpty()) {
			// convert the default relative file paths to full paths
			if (isDefinedModuleName(remotePath)) {
				return new String[0];
			}
			List result = new ArrayList();
			for (int i = 0; i < DEFAULT_AUTO_REFRESH_FILES.length; i++) {
				String relativePath = DEFAULT_AUTO_REFRESH_FILES[i];
				result.add(new Path(remotePath).append(relativePath).toString());
			}
			return (String[]) result.toArray(new String[result.size()]);
		} else {
			return (String[]) files.toArray(new String[files.size()]);
		}
	}
	
	/**
	 * Sets the auto refresh files for the given remote path to the given
	 * string values which are absolute file paths (relative to the receiver).
	 * 
	 * @param autoRefreshFiles The autoRefreshFiles to set
	 */
	public void setAutoRefreshFiles(String remotePath, String[] autoRefreshFiles) {
		Set newFiles = new HashSet(Arrays.asList(autoRefreshFiles));
		// Check to see if the auto-refresh files are the default files
		if (autoRefreshFiles.length == DEFAULT_AUTO_REFRESH_FILES.length) {
			boolean isDefault = true;
			for (int i = 0; i < DEFAULT_AUTO_REFRESH_FILES.length; i++) {
				String filePath = DEFAULT_AUTO_REFRESH_FILES[i];
				if (!newFiles.contains(new Path(remotePath).append(filePath).toString())) {
					isDefault = false;
					break;
				}
			}
			if (isDefault) {
				this.autoRefreshFiles.remove(getCachePathFor(remotePath));
				return;
			}
		}
		this.autoRefreshFiles.put(getCachePathFor(remotePath), newFiles);
	}

	/**
	 * Fetches tags from auto-refresh files.
	 */
	public void refreshDefinedTags(String remotePath, boolean replace, IProgressMonitor monitor) throws TeamException {
		String[] filesToRefresh = getAutoRefreshFiles(remotePath);
		monitor.beginTask(null, filesToRefresh.length * 10); //$NON-NLS-1$
		try {
			List tags = new ArrayList();
			for (int i = 0; i < filesToRefresh.length; i++) {
				ICVSRemoteFile file = root.getRemoteFile(filesToRefresh[i], CVSTag.DEFAULT);
				tags.addAll(Arrays.asList(fetchTags(file, Policy.subMonitorFor(monitor, 5))));
			}
			if (!tags.isEmpty()) {
				addTags(remotePath, (CVSTag[]) tags.toArray(new CVSTag[tags.size()]));
			}
		} finally {
			monitor.done();
		}
	}
	
	/*
	 * Method clearTags.
	 */
	private void clearTags(String remotePath) {
		String name = getCachePathFor(remotePath);
		knownTags.remove(name);
	}
	
	/**
	 * Returns Branch and Version tags for the given files
	 */	
	private CVSTag[] fetchTags(ICVSRemoteFile file, IProgressMonitor monitor) throws TeamException {
		try {
			Set tagSet = new HashSet();
			ILogEntry[] entries = file.getLogEntries(monitor);
			for (int j = 0; j < entries.length; j++) {
				CVSTag[] tags = entries[j].getTags();
				for (int k = 0; k < tags.length; k++) {
					tagSet.add(tags[k]);
				}
			}
			return (CVSTag[])tagSet.toArray(new CVSTag[0]);
		} catch (TeamException e) {
			IStatus status = e.getStatus();
			if (status.getCode() == CVSStatus.SERVER_ERROR && status.isMultiStatus()) {
				IStatus[] children = status.getChildren();
				if (children.length == 1 && children[0].getCode() == CVSStatus.DOES_NOT_EXIST) {
					return new CVSTag[0];
				}
			}
			throw e;
		}
	}
	
	private String getCachePathFor(String remotePath) {
		String root = new Path(remotePath).segment(0);
		if (isDefinedModuleName(remotePath)) {
			return asDefinedModulePath(root);
		}
		return root;
	}
	
	/**
	 * Write out the state of the receiver as XML on the given XMLWriter.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	public void writeState(XMLWriter writer) throws IOException {

		HashMap attributes = new HashMap();

		attributes.clear();
		attributes.put(RepositoriesViewContentHandler.ID_ATTRIBUTE, root.getLocation());
		String programName = ((CVSRepositoryLocation)root).getRemoteCVSProgramName();
		if (!programName.equals(CVSRepositoryLocation.DEFAULT_REMOTE_CVS_PROGRAM_NAME)) {
			attributes.put(RepositoriesViewContentHandler.REPOSITORY_PROGRAM_NAME_ATTRIBUTE, programName);
		}
		if (name != null) {
			attributes.put(RepositoriesViewContentHandler.NAME_ATTRIBUTE, name);
		}
		String readLocation = ((CVSRepositoryLocation)root).getReadLocation();
		if (readLocation != null) {
			attributes.put(RepositoriesViewContentHandler.READ_ID_ATTRIBUTE, readLocation);
		}
		String writeLocation = ((CVSRepositoryLocation)root).getWriteLocation();
		if (writeLocation != null) {
			attributes.put(RepositoriesViewContentHandler.WRITE_ID_ATTRIBUTE, writeLocation);
		}
		
		writer.startTag(RepositoriesViewContentHandler.REPOSITORY_TAG, attributes, true);
		
		// Gather all the modules that have tags and/or auto-refresh files
		

		// for each module, write the moduel, tags and auto-refresh files.
		String[] paths = getKnownRemotePaths();
		for (int i = 0; i < paths.length; i++) {
			String path = paths[i];
			attributes.clear();
			String name = path;
			if (isDefinedModuleName(path)) {
				name = getDefinedModuleName(path);
				attributes.put(RepositoriesViewContentHandler.TYPE_ATTRIBUTE, RepositoriesViewContentHandler.DEFINED_MODULE_TYPE);
			}
			attributes.put(RepositoriesViewContentHandler.PATH_ATTRIBUTE, name);
			writer.startTag(RepositoriesViewContentHandler.MODULE_TAG, attributes, true);
			Set tagSet = (Set)knownTags.get(path);
			if (tagSet != null) {
				Iterator tagIt = tagSet.iterator();
				while (tagIt.hasNext()) {
					CVSTag tag = (CVSTag)tagIt.next();
					attributes.clear();
					attributes.put(RepositoriesViewContentHandler.NAME_ATTRIBUTE, tag.getName());
					attributes.put(RepositoriesViewContentHandler.TYPE_ATTRIBUTE, RepositoriesViewContentHandler.TAG_TYPES[tag.getType()]);
					writer.startAndEndTag(RepositoriesViewContentHandler.TAG_TAG, attributes, true);
				}
			}
			Set refreshSet = (Set)autoRefreshFiles.get(path);
			if (refreshSet != null) {
				Iterator filenameIt = refreshSet.iterator();
				while (filenameIt.hasNext()) {
					String filename = (String)filenameIt.next();
					attributes.clear();
					attributes.put(RepositoriesViewContentHandler.FULL_PATH_ATTRIBUTE, filename);
					writer.startAndEndTag(RepositoriesViewContentHandler.AUTO_REFRESH_FILE_TAG, attributes, true);
				}
			}
			writer.endTag(RepositoriesViewContentHandler.MODULE_TAG);
		}
		writer.endTag(RepositoriesViewContentHandler.REPOSITORY_TAG);
	}
	
	/**
	 * Method getKnownTags.
	 * @param remotePath
	 * @return CVSTag[]
	 */
	public CVSTag[] getKnownTags(String remotePath) {
		Set tagSet = (Set)knownTags.get(getCachePathFor(remotePath));
		if (tagSet == null) return new CVSTag[0];
		return (CVSTag[]) tagSet.toArray(new CVSTag[tagSet.size()]);
	}
	
	public String[] getKnownRemotePaths() {
		Set paths = new HashSet();
		paths.addAll(knownTags.keySet());
		paths.addAll(autoRefreshFiles.keySet());
		return (String[]) paths.toArray(new String[paths.size()]);
	}
	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (ICVSRepositoryLocation.class.equals(adapter)) return getRoot();
		return super.getAdapter(adapter);
	}

	public ICVSRemoteResource[] filterResources(ICVSRemoteResource[] resource) {
		List result = new ArrayList();
		for (int i = 0; i < resource.length; i++) {
			ICVSRemoteResource remoteResource = resource[i];
			if (remoteResource instanceof ICVSRemoteFolder) {
				ICVSRemoteFolder folder = (ICVSRemoteFolder) remoteResource;
				if (tagIsKnown(remoteResource)) {
					result.add(folder);
				}
			}
		}
		return (ICVSRemoteResource[]) result.toArray(new ICVSRemoteResource[result.size()]);
	}
	
	/**
	 * Method tagIsKnown.
	 * @param remoteResource
	 * @return boolean
	 */
	public boolean tagIsKnown(ICVSRemoteResource remoteResource) {
		if (remoteResource instanceof ICVSRemoteFolder) {
			ICVSRemoteFolder folder = (ICVSRemoteFolder) remoteResource;
			String path = getCachePathFor(folder.getRepositoryRelativePath());
			CVSTag[] tags = getKnownTags(path);
			CVSTag tag = folder.getTag();
			for (int i = 0; i < tags.length; i++) {
				CVSTag knownTag = tags[i];
				if (knownTag.equals(tag)) return true;
			}
		}
		return false;
	}
	
	/**
	 * This method is invoked whenever the refresh button in the
	 * RepositoriesView is pressed.
	 */
	void clearCache() {
		modulesCache = null;
	}

	/**
	 * Sets the root.
	 * @param root The root to set
	 */
	void setRepositoryLocation(ICVSRepositoryLocation root) {
		this.root = root;
	}

}
