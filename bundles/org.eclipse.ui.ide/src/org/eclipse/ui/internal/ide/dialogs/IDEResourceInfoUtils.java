/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     David Black - bug 198091
 *     Mickael Istria (Red Hat Inc.) - Bug 486901
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.net.URI;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Utility class supporting common information required from resources.
 *
 * @since 3.2
 *
 */
public class IDEResourceInfoUtils {

	private static String BYTES_LABEL = IDEWorkbenchMessages.ResourceInfo_bytes;

	/**
	 * An empty string to reuse.
	 */
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private static String FILE_LABEL = IDEWorkbenchMessages.ResourceInfo_file;

	private static String FILE_NOT_EXIST_TEXT = IDEWorkbenchMessages.ResourceInfo_fileNotExist;

	private static String FILE_TYPE_FORMAT = IDEWorkbenchMessages.ResourceInfo_fileTypeFormat;

	private static String FOLDER_LABEL = IDEWorkbenchMessages.ResourceInfo_folder;

	private static String LINKED_FILE_LABEL = IDEWorkbenchMessages.ResourceInfo_linkedFile;

	private static String LINKED_FOLDER_LABEL = IDEWorkbenchMessages.ResourceInfo_linkedFolder;

	private static String VIRTUAL_FOLDER_LABEL = IDEWorkbenchMessages.ResourceInfo_virtualFolder;

	private static String VIRTUAL_FOLDER_TEXT = IDEWorkbenchMessages.ResourceInfo_isVirtualFolder;

	private static String MISSING_PATH_VARIABLE_TEXT = IDEWorkbenchMessages.ResourceInfo_undefinedPathVariable;

	private static String NOT_EXIST_TEXT = IDEWorkbenchMessages.ResourceInfo_notExist;

	private static String NOT_LOCAL_TEXT = IDEWorkbenchMessages.ResourceInfo_notLocal;

	private static String PROJECT_LABEL = IDEWorkbenchMessages.ResourceInfo_project;

	private static String UNKNOWN_LABEL = IDEWorkbenchMessages.ResourceInfo_unknown;

	/**
	 * Return whether or not the file called pathName exists.
	 *
	 * @param pathName
	 * @return boolean <code>true</code> if the file exists.
	 * @see IFileInfo#exists()
	 */
	public static boolean exists(String pathName) {
		IFileInfo info = getFileInfo(pathName);
		if (info == null) {
			return false;
		}
		return info.exists();
	}

	private static String getContentTypeString(IContentDescription description) {
		if (description != null) {
			IContentType contentType = description.getContentType();
			if (contentType != null) {
				return contentType.getName();
			}
		}
		return null;
	}

	/**
	 * Return the value for the date String for the timestamp of the supplied
	 * resource.
	 *
	 * @param resource
	 *            The resource to query
	 * @return String
	 */
	public static String getDateStringValue(IResource resource) {
		if (!resource.isLocal(IResource.DEPTH_ZERO)) {
			return NOT_LOCAL_TEXT;
		}

		// don't access the file system for closed projects (bug 151089)
		if (!isProjectAccessible(resource)) {
			return UNKNOWN_LABEL;
		}

		URI location = resource.getLocationURI();
		if (location == null) {
			if (resource.isLinked()) {
				return MISSING_PATH_VARIABLE_TEXT;
			}
			return NOT_EXIST_TEXT;
		}

		IFileInfo info = getFileInfo(location);
		if (info == null) {
			return UNKNOWN_LABEL;
		}

		if (info.exists()) {
			DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG,
					DateFormat.MEDIUM);
			return format.format(new Date(info.getLastModified()));
		}
		return NOT_EXIST_TEXT;
	}

	/**
	 * Return the fileInfo at pathName or <code>null</code> if the format is
	 * invalid or if the file info cannot be determined.
	 *
	 * @param pathName
	 * @return IFileInfo or <code>null</code>
	 */
	public static IFileInfo getFileInfo(IPath pathName) {
		IFileStore store = getFileStore(pathName.toFile().toURI());
		if (store == null) {
			return null;
		}
		return store.fetchInfo();
	}

	/**
	 * Return the fileInfo at pathName or <code>null</code> if the format is
	 * invalid or if the file info cannot be determined.
	 *
	 * @param pathName
	 * @return IFileInfo or <code>null</code>
	 */
	public static IFileInfo getFileInfo(String pathName) {
		IFileStore store = getFileStore(pathName);
		if (store == null) {
			return null;
		}
		return store.fetchInfo();
	}

	/**
	 * Return the fileInfo for location. Return <code>null</code> if there is
	 * a CoreException looking it up
	 *
	 * @param location
	 * @return String or <code>null</code>
	 */
	public static IFileInfo getFileInfo(URI location) {
		if (location.getScheme() == null)
			return null;
		IFileStore store = getFileStore(location);
		if (store == null) {
			return null;
		}
		return store.fetchInfo();
	}

	/**
	 * Get the file store for the local file system path.
	 *
	 * @param string
	 * @return IFileStore or <code>null</code> if there is a
	 *         {@link CoreException}.
	 */
	public static IFileStore getFileStore(String string) {
		IPath location = IPath.fromOSString(string);
		//see if there is an existing resource at that location that might have a different file store
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(location);
		if (file != null) {
			return getFileStore(file.getLocationURI());
		}
		return getFileStore(location.toFile().toURI());
	}

	/**
	 * Get the file store for the URI.
	 *
	 * @param uri
	 * @return IFileStore or <code>null</code> if there is a
	 *         {@link CoreException}.
	 */
	public static IFileStore getFileStore(URI uri) {
		try {
			return EFS.getStore(uri);
		} catch (CoreException e) {
			log(e);
			return null;
		}
	}

	/**
	 * Get the location of a resource
	 *
	 * @param resource
	 * @return String the text to display the location
	 */
	public static String getLocationText(IResource resource) {
		if (resource.isVirtual())
			return VIRTUAL_FOLDER_TEXT;
		if (!resource.isLocal(IResource.DEPTH_ZERO)) {
			return NOT_LOCAL_TEXT;
		}

		URI resolvedLocation = resource.getLocationURI();
		URI location = resolvedLocation;
		boolean isLinked = resource.isLinked();
		if (isLinked) {
			location = resource.getRawLocationURI();
		}
		if (location == null) {
			return NOT_EXIST_TEXT;
		}

		if (resolvedLocation.getScheme() == null)
			return location.toString();

		IFileStore store = getFileStore(resolvedLocation);
		// don't access the file system for closed projects (bug 151089)
		boolean isPathVariable = isPathVariable(resource);
		if (isProjectAccessible(resource) && resolvedLocation != null
				&& !isPathVariable) {
			// No path variable used. Display the file not exist message
			// in the location. Fixes bug 33318.
			if (store == null) {
				return UNKNOWN_LABEL;
			}
			if (!store.fetchInfo().exists()) {
				return NLS.bind(FILE_NOT_EXIST_TEXT, store.toString());
			}
		}
		if (isLinked && isPathVariable) {
			String tmp = URIUtil.toPath(resource.getRawLocationURI()).toOSString();
			return resource.getPathVariableManager().convertToUserEditableFormat(tmp, true);
		}
		if (store != null) {
			return store.toString();
		}
		return location.toString();
	}

	/**
	 * Get the resolved location of a resource. This resolves path variables if
	 * present in the resource path.
	 *
	 * @param resource
	 * @return String
	 */
	public static String getResolvedLocationText(IResource resource) {
		if (!resource.isLocal(IResource.DEPTH_ZERO)) {
			return NOT_LOCAL_TEXT;
		}

		URI location = resource.getLocationURI();
		if (location == null) {
			if (resource.isLinked()) {
				return MISSING_PATH_VARIABLE_TEXT;
			}

			return NOT_EXIST_TEXT;
		}

		if (location.getScheme() == null)
			return UNKNOWN_LABEL;

		IFileStore store = getFileStore(location);
		if (store == null) {
			return UNKNOWN_LABEL;
		}

		// don't access the file system for closed projects (bug 151089)
		if (isProjectAccessible(resource) && !store.fetchInfo().exists()) {
			return NLS.bind(FILE_NOT_EXIST_TEXT, store.toString());
		}

		return store.toString();
	}

	/**
	 * Return a String that indicates the size of the supplied file.
	 *
	 * @param resource
	 * @return String
	 */
	public static String getSizeString(IResource resource) {
		if (resource.getType() != IResource.FILE) {
			return ""; //$NON-NLS-1$
		}

		IFile file = (IFile) resource;
		if (!file.isLocal(IResource.DEPTH_ZERO)) {
			return NOT_LOCAL_TEXT;
		}

		URI location = file.getLocationURI();
		if (location == null) {
			if (file.isLinked()) {
				return MISSING_PATH_VARIABLE_TEXT;
			}

			return NOT_EXIST_TEXT;
		}

		IFileInfo info = getFileInfo(location);
		if (info == null) {
			return UNKNOWN_LABEL;
		}

		if (info.exists()) {
			return NLS.bind(BYTES_LABEL, NumberFormat.getInstance().format(info.getLength()));
		}

		return NOT_EXIST_TEXT;
	}

	/**
	 * Get the string that identifies the type of this resource.
	 *
	 * @param resource
	 * @param description
	 * @return String
	 */
	public static String getTypeString(IResource resource,
			IContentDescription description) {

		if (resource.getType() == IResource.FILE) {
			if (resource.isLinked()) {
				return LINKED_FILE_LABEL;
			}

			if (resource instanceof IFile) {
				String contentType = getContentTypeString(description);
				if (contentType != null) {
					return MessageFormat.format(FILE_TYPE_FORMAT, contentType );
				}
			}
			return FILE_LABEL;
		}

		if (resource.getType() == IResource.FOLDER) {
			if (resource.isVirtual()) {
				return VIRTUAL_FOLDER_LABEL;
			}
			if (resource.isLinked()) {
				return LINKED_FOLDER_LABEL;
			}

			return FOLDER_LABEL;
		}

		if (resource.getType() == IResource.PROJECT) {
			return PROJECT_LABEL;
		}

		// Should not be possible
		return UNKNOWN_LABEL;
	}

	/**
	 * Returns whether the given resource is a linked resource bound to a path
	 * variable.
	 *
	 * @param resource
	 *            resource to test
	 * @return boolean <code>true</code> the given resource is a linked
	 *         resource bound to a path variable. <code>false</code> the given
	 *         resource is either not a linked resource or it is not using a
	 *         path variable.
	 */
	private static boolean isPathVariable(IResource resource) {
		if (!resource.isLinked()) {
			return false;
		}

		URI resolvedLocation = resource.getLocationURI();
		if (resolvedLocation == null) {
			// missing path variable
			return true;
		}
		URI rawLocation = resource.getRawLocationURI();
		if (resolvedLocation.equals(rawLocation)) {
			return false;
		}

		return true;
	}

	/**
	 * Returns whether the resource's project is available
	 */
	private static boolean isProjectAccessible(IResource resource) {
		IProject project = resource.getProject();
		return project != null && project.isAccessible();
	}

	/**
	 * Return the file stores that are a child of store that the filter accepts.
	 *
	 * @param store
	 * @param fileFilter
	 * @param monitor
	 * @return IFileStore[]
	 */
	public static IFileStore[] listFileStores(IFileStore store,
			IFileStoreFilter fileFilter, IProgressMonitor monitor) {
		ArrayList<IFileStore> result = new ArrayList<>();
		IFileStore[] children;
		try {
			children = store.childStores(EFS.NONE, monitor);
		} catch (CoreException e) {
			log(e);
			return new IFileStore[0];
		}
		for (IFileStore fileStore : children) {
			if (fileFilter.accept(fileStore)) {
				result.add(fileStore);
			}
		}
		IFileStore[] stores = new IFileStore[result.size()];
		result.toArray(stores);
		return stores;
	}

	/**
	 * Log the CoreException
	 * @param e
	 */
	private static void log(CoreException e) {
		StatusManager.getManager().handle(e, IDEWorkbenchPlugin.IDE_WORKBENCH);
	}

}
