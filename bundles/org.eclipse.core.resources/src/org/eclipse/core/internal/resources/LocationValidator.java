/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.resources;

import java.net.URI;
import org.eclipse.core.filesystem.IFileStoreConstants;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

/**
 * This class implements the various path, URI, and name validation methods
 * in the workspace API
 */
public class LocationValidator {
	private final Workspace workspace;

	public LocationValidator(Workspace workspace) {
		this.workspace = workspace;
	}

	/**
	 * Returns true if the given file system locations overlap. If "bothDirections" is true,
	 * this means they are the same, or one is a proper prefix of the other.  If "bothDirections"
	 * is false, this method only returns true if the locations are the same, or the first location
	 * is a prefix of the second.  Returns false if the locations do not overlap
	 * Does the right thing with respect to case insensitive platforms.
	 */
	protected boolean isOverlapping(IPath location1, IPath location2, boolean bothDirections) {
		IPath one = location1;
		IPath two = location2;
		// If we are on a case-insensitive file system then convert to all lower case.
		if (!Workspace.caseSensitive) {
			one = new Path(location1.toOSString().toLowerCase());
			two = new Path(location2.toOSString().toLowerCase());
		}
		return one.isPrefixOf(two) || (bothDirections && two.isPrefixOf(one));
	}

	/**
	 * Returns true if the given file system locations overlap. If "bothDirections" is true,
	 * this means they are the same, or one is a proper prefix of the other.  If "bothDirections"
	 * is false, this method only returns true if the locations are the same, or the first location
	 * is a prefix of the second.  Returns false if the locations do not overlap
	 */
	protected boolean isOverlapping(URI location1, URI location2, boolean bothDirections) {
		if (location1.equals(location2))
			return true;
		String scheme1 = location1.getScheme();
		String scheme2 = location2.getScheme();
		if (!scheme1.equals(scheme2))
			return false;
		if (IFileStoreConstants.SCHEME_FILE.equals(scheme1) && IFileStoreConstants.SCHEME_FILE.equals(scheme2))
			return isOverlapping(new Path(location1.getSchemeSpecificPart()), new Path(location2.getSchemeSpecificPart()), bothDirections);
		String string1 = location1.toString();
		String string2 = location2.toString();
		return string1.startsWith(string2) || (bothDirections && string2.startsWith(string1));
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validateLinkLocation(IResource, IPath)
	 */
	public IStatus validateLinkLocation(IResource resource, IPath unresolvedLocation) {
		String message;
		//check if resource linking is disabled
		if (ResourcesPlugin.getPlugin().getPluginPreferences().getBoolean(ResourcesPlugin.PREF_DISABLE_LINKING)) {
			message = NLS.bind(Messages.links_workspaceVeto, resource.getName());
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, resource.getFullPath(), message);
		}
		//check that the resource has a project as its parent
		IContainer parent = resource.getParent();
		if (parent == null || parent.getType() != IResource.PROJECT) {
			message = NLS.bind(Messages.links_parentNotProject, resource.getName());
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, resource.getFullPath(), message);
		}
		if (!parent.isAccessible()) {
			message = NLS.bind(Messages.links_parentNotAccessible, resource.getFullPath());
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, resource.getFullPath(), message);
		}
		IPath location = workspace.getPathVariableManager().resolvePath(unresolvedLocation);
		//check nature veto
		String[] natureIds = ((Project) parent).internalGetDescription().getNatureIds();

		IStatus result = workspace.getNatureManager().validateLinkCreation(natureIds);
		if (!result.isOK())
			return result;
		//check team provider veto
		if (resource.getType() == IResource.FILE)
			result = workspace.getTeamHook().validateCreateLink((IFile) resource, IResource.NONE, location);
		else
			result = workspace.getTeamHook().validateCreateLink((IFolder) resource, IResource.NONE, location);
		if (!result.isOK())
			return result;
		if (location.isEmpty()) {
			message = Messages.links_noPath;
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, resource.getFullPath(), message);
		}
		//check the standard path name restrictions
		int segmentCount = location.segmentCount();
		for (int i = 0; i < segmentCount; i++) {
			result = validateName(location.segment(i), resource.getType());
			if (!result.isOK())
				return result;
		}
		//if the location doesn't have a device, see if the OS will assign one
		if (location.isAbsolute() && location.getDevice() == null)
			location = new Path(location.toFile().getAbsolutePath());
		// test if the given location overlaps the platform metadata location
		IPath testLocation = workspace.getMetaArea().getLocation();
		if (isOverlapping(location, testLocation, true)) {
			message = NLS.bind(Messages.links_invalidLocation, location.toOSString());
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, resource.getFullPath(), message);
		}
		//test if the given path overlaps the location of the given project
		testLocation = resource.getProject().getLocation();
		if (testLocation != null && isOverlapping(location, testLocation, false)) {
			message = NLS.bind(Messages.links_locationOverlapsProject, location.toOSString());
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, resource.getFullPath(), message);
		}
		//warnings (all errors must be checked before all warnings)
		//check that the location is absolute
		if (!location.isAbsolute()) {
			//we know there is at least one segment, because of previous isEmpty check
			message = NLS.bind(Messages.pathvar_undefined, location.toOSString(), location.segment(0));
			return new ResourceStatus(IResourceStatus.VARIABLE_NOT_DEFINED_WARNING, resource.getFullPath(), message);
		}
		// Iterate over each known project and ensure that the location does not
		// conflict with any project locations or linked resource locations
		IProject[] projects = workspace.getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			// since we are iterating over the project in the workspace, we
			// know that they have been created before and must have a description
			IProjectDescription desc = ((Project) project).internalGetDescription();
			testLocation = desc.getLocation();
			if (testLocation != null && isOverlapping(location, testLocation, true)) {
				message = NLS.bind(Messages.links_overlappingResource, location.toOSString());
				return new ResourceStatus(IResourceStatus.OVERLAPPING_LOCATION, resource.getFullPath(), message);
			}
			//iterate over linked resources and check for overlap
			if (!project.isOpen())
				continue;
			IResource[] children = null;
			try {
				children = project.members();
			} catch (CoreException e) {
				//ignore projects that cannot be accessed
			}
			if (children == null)
				continue;
			for (int j = 0; j < children.length; j++) {
				if (children[j].isLinked()) {
					testLocation = children[j].getLocation();
					if (testLocation != null && isOverlapping(location, testLocation, true)) {
						message = NLS.bind(Messages.links_overlappingResource, location.toOSString());
						return new ResourceStatus(IResourceStatus.OVERLAPPING_LOCATION, resource.getFullPath(), message);
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validateName(String, int)
	 */
	public IStatus validateName(String segment, int type) {
		String message;

		/* segment must not be null */
		if (segment == null) {
			message = Messages.resources_nameNull;
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		// cannot be an empty string
		if (segment.length() == 0) {
			message = Messages.resources_nameEmpty;
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		/* test invalid characters */
		char[] chars = OS.INVALID_RESOURCE_CHARACTERS;
		for (int i = 0; i < chars.length; i++)
			if (segment.indexOf(chars[i]) != -1) {
				message = NLS.bind(Messages.resources_invalidCharInName, String.valueOf(chars[i]), segment);
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
			}

		/* test invalid OS names */
		if (!OS.isNameValid(segment)) {
			message = NLS.bind(Messages.resources_invalidName, segment);
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Validates that the given workspace path is valid for the given type.  If 
	 * <code>lastSegmentOnly</code> is true, it is assumed that all segments except
	 * the last one have previously been validated.  This is an optimization for validating
	 * a leaf resource when it is known that the parent exists (and thus its parent path
	 * must already be valid).
	 */
	public IStatus validatePath(IPath path, int type, boolean lastSegmentOnly) {
		String message;

		/* path must not be null */
		if (path == null) {
			message = Messages.resources_pathNull;
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		/* path must not have a device separator */
		if (path.getDevice() != null) {
			message = NLS.bind(Messages.resources_invalidCharInPath, String.valueOf(IPath.DEVICE_SEPARATOR), path);
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		/* path must not be the root path */
		if (path.isRoot()) {
			message = Messages.resources_invalidRoot;
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		/* path must be absolute */
		if (!path.isAbsolute()) {
			message = NLS.bind(Messages.resources_mustBeAbsolute, path);
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		/* validate segments */
		int numberOfSegments = path.segmentCount();
		if ((type & IResource.PROJECT) != 0) {
			if (numberOfSegments == ICoreConstants.PROJECT_SEGMENT_LENGTH) {
				return validateName(path.segment(0), IResource.PROJECT);
			} else if (type == IResource.PROJECT) {
				message = NLS.bind(Messages.resources_projectPath, path);
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
			}
		}
		if ((type & (IResource.FILE | IResource.FOLDER)) != 0) {
			if (numberOfSegments < ICoreConstants.MINIMUM_FILE_SEGMENT_LENGTH) {
				message = NLS.bind(Messages.resources_resourcePath, path);
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
			}
			int fileFolderType = type &= ~IResource.PROJECT;
			int segmentCount = path.segmentCount();
			if (lastSegmentOnly)
				return validateName(path.segment(segmentCount - 1), fileFolderType);
			IStatus status = validateName(path.segment(0), IResource.PROJECT);
			if (!status.isOK())
				return status;
			// ignore first segment (the project)
			for (int i = 1; i < segmentCount; i++) {
				status = validateName(path.segment(i), fileFolderType);
				if (!status.isOK())
					return status;
			}
			return Status.OK_STATUS;
		}
		message = NLS.bind(Messages.resources_invalidPath, path);
		return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validatePath(String, int)
	 */
	public IStatus validatePath(String path, int type) {
		/* path must not be null */
		if (path == null) {
			String message = Messages.resources_pathNull;
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}
		return validatePath(Path.fromOSString(path), type, false);
	}

	public IStatus validateProjectLocation(IProject context, IPath unresolvedLocation) {
		// the default default is ok for all projects
		if (unresolvedLocation == null)
			return Status.OK_STATUS;
		IPath location = workspace.getPathVariableManager().resolvePath(unresolvedLocation);
		//check that the location is absolute
		if (!location.isAbsolute()) {
			String message;
			if (location.segmentCount() > 0)
				message = NLS.bind(Messages.pathvar_undefined, location.toString(), location.segment(0));
			else
				message = Messages.links_noPath;
			return new ResourceStatus(IResourceStatus.VARIABLE_NOT_DEFINED, null, message);
		}
		return validateProjectLocationURI(context, FileUtil.toURI(location));
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validateProjectLocation(IProject, URI)
	 */
	public IStatus validateProjectLocationURI(IProject context, URI unresolvedLocation) {
		String message;
		// the default default is ok for all projects
		if (unresolvedLocation == null)
			return Status.OK_STATUS;
		//check the standard path name restrictions
		URI location = workspace.getPathVariableManager().resolveURI(unresolvedLocation);
		if (IFileStoreConstants.SCHEME_FILE.equals(location.getScheme())) {
			IPath pathPart = new Path(location.getSchemeSpecificPart());
			int segmentCount = pathPart.segmentCount();
			for (int i = 0; i < segmentCount; i++) {
				IStatus result = validateName(pathPart.segment(i), IResource.PROJECT);
				if (!result.isOK())
					return result;
			}
		}
		//check that the location is absolute
		if (!location.isAbsolute()) {
			IPath pathPart = new Path(location.getSchemeSpecificPart());
			if (pathPart.segmentCount() > 0)
				message = NLS.bind(Messages.pathvar_undefined, location.toString(), pathPart.segment(0));
			else
				message = Messages.links_noPath;
			return new ResourceStatus(IResourceStatus.VARIABLE_NOT_DEFINED, null, message);
		}
		// test if the given location overlaps the default default location
		IPath defaultDefaultLocation = Platform.getLocation();
		if (isOverlapping(location, defaultDefaultLocation.toFile().toURI(), true)) {
			message = NLS.bind(Messages.resources_overlapWorkspace, location.toString(), defaultDefaultLocation.toOSString());
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}
		// Iterate over each known project and ensure that the location does not
		// conflict with any of their already defined locations.
		IProject[] projects = workspace.getRoot().getProjects();
		for (int j = 0; j < projects.length; j++) {
			IProject project = projects[j];
			// since we are iterating over the project in the workspace, we
			// know that they have been created before and must have a description
			IProjectDescription desc = ((Project) project).internalGetDescription();
			URI definedLocalLocation = desc.getLocationURI();
			// if the project uses the default location then continue
			if (definedLocalLocation == null)
				continue;
			//tolerate locations being the same if this is the project being tested
			if (project.equals(context) && definedLocalLocation.equals(location))
				continue;
			if (isOverlapping(location, definedLocalLocation, true)) {
				message = NLS.bind(Messages.resources_overlapProject, location.toString(), project.getName());
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
			}
		}
		//if this project exists and has linked resources, the project location cannot overlap
		//the locations of any linked resources in that project
		if (context.exists() && context.isOpen()) {
			IResource[] children = null;
			try {
				children = context.members();
			} catch (CoreException e) {
				//ignore projects that cannot be accessed
			}
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					if (children[i].isLinked()) {
						URI testLocation = children[i].getLocationURI();
						if (testLocation != null && isOverlapping(testLocation, location, false)) {
							message = NLS.bind(Messages.links_locationOverlapsLink, location.toString());
							return new ResourceStatus(IResourceStatus.OVERLAPPING_LOCATION, context.getFullPath(), message);
						}
					}
				}
			}
		}
		return Status.OK_STATUS;
	}
}