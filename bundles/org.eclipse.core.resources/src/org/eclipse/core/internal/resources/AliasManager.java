/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.resources;

import java.util.*;

import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;

/**
 * An alias is a resource that occupies the same file system location as another
 * resource in the workspace.  When a resource is modified in a way that affects
 * the file on disk, all aliases need to be updated.  This class is used to
 * maintain data structures for quickly computing the set of aliases for a given
 * resource, and for efficiently updating all aliases when a resource changes on
 * disk.
 * 
 * The approach for computing aliases is optimized for alias-free workspaces and
 * alias-free projects.  That is, if the workspace contains no aliases, then
 * updating should be very quick.  If a resource is changed in a project that
 * contains no aliases, it should also be very fast.
 * 
 * The data structures maintained by the alias manager can be seen as a cache,
 * that is, they store no information that cannot be recomputed from other
 * available information.  On shutdown, the alias manager discards all state; on
 * startup, the alias manager eagerly rebuilds its state.  The reasoning is
 * that it's better to incur this cost on startup than on the first attempt to
 * modify a resource.  After startup, the state is updated incrementally on the
 * following occasions: 
 *  - when projects are created, deleted, opened, closed, or moved 
 *  - when linked resources are created, deleted, or moved.
 */
public class AliasManager implements IManager {
	/**
	 * Maintains a mapping of IPath->IResource, such that multiple resources
	 * mapped from the same path are tolerated.
	 */
	class LocationMap {
		/**
		 * Map of IPath->IResource OR IPath->ArrayList of (IResource)
		 */
		private final SortedMap map = new TreeMap(getComparator());
		/**
		 * Adds the given resource to the map, keyed by the given location.
		 * Returns true if a new entry was added, and false otherwise.
		 */
		public boolean add(IPath location, IResource resource) {
			Object oldValue = map.get(location);
			if (oldValue == null) {
				map.put(location, resource);
				return true;
			}
			if (oldValue instanceof IResource) {
				if (resource.equals(oldValue))
					return false;//duplicate
				ArrayList newValue = new ArrayList(2);
				newValue.add(oldValue);
				newValue.add(resource);
				map.put(location, newValue);
				return true;
			}
			ArrayList list = (ArrayList)oldValue;
			if (list.contains(resource))
				return false;//duplicate
			list.add(resource);
			return true;
		}
		/**
		 * Method clear.
		 */
		public void clear() {
			map.clear();
		}
		/**
		 * Calls the given doit with every resource in the map whose location
		 * overlaps another resource in the map.
		 */
		public void overLappingResourcesDo(Doit doit) {
			Iterator entries = map.entrySet().iterator();
			IPath previousPath = null;
			IResource previousResource = null;
			while (entries.hasNext()) {
				Map.Entry current = (Map.Entry)entries.next();
				//value is either single resource or List of resources
				IPath currentPath = (IPath)current.getKey();
				IResource currentResource = null;
				Object value = current.getValue();
				if (value instanceof List) {
					//if there are several then they're all overlapping
					Iterator duplicates = ((List)value).iterator();
					while (duplicates.hasNext())
						doit.doit((IResource)duplicates.next());
				} else {
					//value is a single resource
					currentResource = (IResource)value;
				}
				if (previousPath != null) {
					//check for overlap with previous
					//Note: previous is always shorter due to map sorting rules
					if (previousPath.isPrefixOf(currentPath)) {
						//resources will be null if they were in a list, in which case 
						//they've already been passed to the doit
						if (previousResource != null)
							doit.doit(previousResource);
						if (currentResource != null)
							doit.doit(currentResource);
					}
				}
				previousPath = currentPath;
				previousResource = currentResource;
			}
		}
		/**
		 * Removes the given location from the map.  Returns true if anything
		 * was actually removed, and false otherwise.
		 */
		public boolean remove(IPath location, IResource resource) {
			Object oldValue = map.get(location);
			if (oldValue == null)
				return false;
			if (oldValue instanceof IResource) {
				if (resource.equals(oldValue)) {
					map.remove(location);
					return true;
				}
				return false;
			}
			ArrayList list = (ArrayList)oldValue;
			boolean wasRemoved = list.remove(resource);
			if (list.size() == 0)
				map.remove(location);
			return wasRemoved;
		}
	}
	interface Doit {
		public void doit(IResource resource);
	}
	
	/**
	 * This maps IPath->IResource, where the path is an absolute file system
	 * location, and the resource contains the projects and/or linked resources
	 * that are rooted at that location.
	 */
	private final LocationMap locationsMap = new LocationMap();
	
	/**
	 * The set of IProjects that have aliases.
	 */
	private final Set aliasedProjects = new HashSet();
	
	/**
	 * The total number of linked resources that exist in the workspace.  This
	 * count includes linked resources that don't currently have valid locations
	 * (due to an undefined path variable).
	 */
	private int linkedResourceCount = 0;
	
	/** the workspace */
	private final Workspace workspace;

	public AliasManager(Workspace workspace) {
		this.workspace = workspace;
	}
	private void addToLocationsMap(IProject project) {
		IPath location = project.getLocation();
		if (location != null)
			locationsMap.add(location, project);
		IResource[] members = null;
		try {
			members = project.members();
		} catch (CoreException e) {
			//skip inaccessible projects
		}
		if (members != null) {
			//look for linked resources
			for (int j = 0; j < members.length; j++) {
				if (members[j].isLinked()) {
					location = members[j].getLocation();
					if (location != null)
						if (locationsMap.add(location, members[j]))
							linkedResourceCount++;
				}
			}
		}
	}

	/**
	 * Builds the table of aliased projects from scratch.
	 */
	private void buildAliasedProjectsSet() {
		aliasedProjects.clear();
		//if there are no linked resources then there can't be any aliased projects
		if (linkedResourceCount <= 0) {
			//paranoid check -- count should never be below zero
			Assert.isTrue(linkedResourceCount == 0, "Linked resource count below zero");//$NON-NLS-1$
			return;
		}
		//for every resource that overlaps another, marked its project as aliased
		locationsMap.overLappingResourcesDo(new Doit() {
			public void doit(IResource resource) {
				aliasedProjects.add(resource.getProject());
			}
		});
	}
		/**
		 * Builds the table of resource locations from scratch.  Also computes an
		 * initial value for the linked resource counter.
		 */
	private void buildLocationsMap() {
		locationsMap.clear();
		linkedResourceCount = 0;
		//build table of IPath (file system location) -> IResource (project or linked resource)
		IProject[] projects = workspace.getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			addToLocationsMap(projects[i]);
		}
	}
	public void changing(IProject project) {
	}
	public void closing(IProject project) {
		//same as deleting for purposes of alias data
		deleting(project);
	}
	public void deleting(IProject project) {
		//remove this project and all linked children from the location table
		IPath location = project.getLocation();
		if (location != null)
			locationsMap.remove(location, project);
		IResource[] children = null;
		try {
			children = project.members();
		} catch (CoreException e) {
			//ignore inaccessible projects
		}
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				if (children[i].isLinked()) {
					deleting(children[i]);
				}
			}
		}
		//rebuild the set of aliased projects from scratch
		buildAliasedProjectsSet();
	}
	public void deleting(IResource linkedResource) {
		//this linked resource is being deleted
		IPath location = linkedResource.getLocation();
		if (location != null)
			if (locationsMap.remove(location, linkedResource));
				linkedResourceCount--;
	}
	/**
	 * Returns the comparator to use when sorting the locations map.  Comparison
	 * is based on segments, so that paths with the most segments in common will
	 * always be adjacent.  This is equivalent to the natural order on the path
	 * strings, with the extra condition that the path separator is ordered
	 * before all other characters. (Ex: "/foo" < "/foo/zzz" < "/fooaaa").
	 */
	private Comparator getComparator() {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				IPath path1 = (IPath) o1;
				IPath path2 = (IPath) o2;
				int segmentCount1 = path1.segmentCount();
				int segmentCount2 = path2.segmentCount();
				for (int i = 0; (i < segmentCount1) && (i < segmentCount2); i++) {
					String segment1 = path1.segment(i);
					String segment2 = path2.segment(i);
					int compare = segment1.compareTo(segment2);
					if (compare != 0)
						return compare;
				}
				//all segments are equal, so they are the same if they have the same number of segments
				return segmentCount1-segmentCount2;
			}
		};
	}
	public void opening(IProject project) {
		addToLocationsMap(project);
		buildAliasedProjectsSet();
	}

	/**
	 * @see IManager#shutdown
	 */
	public void shutdown(IProgressMonitor monitor) throws CoreException {
	}
	
	/**
	 * @see IManager#startup
	 */
	public void startup(IProgressMonitor monitor) throws CoreException {
		buildLocationsMap();
		buildAliasedProjectsSet();
	}
	/**
	 * The file underlying the given resource has changed on disk.  Compute all
	 * aliases for this resource and update them.  This method will not attempt
	 * to incur any units of work on the given progress monitor, but it may
	 * update the subtask to reflect what aliases are being updated.
	 */
	public void updateAliases(IResource resource, IProgressMonitor monitor) throws CoreException {
		//nothing to do if we're in an alias-free workspace or project
		if (linkedResourceCount <= 0 || !aliasedProjects.contains(resource.getProject()))
			return;
		IResource[] aliases = computeAliases(resource);
		if (aliases == null)
			return;
		for (int i = 0; i < aliases.length; i++) {
			monitor.subTask(Policy.bind("links.updatingDuplicate", resource.getFullPath().toString()));//$NON-NLS-1$
			aliases[i].refreshLocal(IResource.DEPTH_INFINITE, null);
		}
	}
	/**
	 * Returns all aliases of the given resource, or null if there are none.
	 * This list will NOT include the resource itself.
	 */
	private IResource[] computeAliases(IResource resource) {
		// todo
		return null;
	}
}