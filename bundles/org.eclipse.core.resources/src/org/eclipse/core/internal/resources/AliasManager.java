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
package org.eclipse.core.internal.resources;

import java.util.*;

import org.eclipse.core.internal.events.ILifecycleListener;
import org.eclipse.core.internal.events.LifecycleEvent;
import org.eclipse.core.internal.localstore.FileSystemResourceManager;
//import org.eclipse.core.internal.utils.Assert;
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
 *  -  when projects are deleted, opened, closed, or moved 
 *  - when linked resources are created, deleted, or moved.
 */
public class AliasManager implements IManager, ILifecycleListener {
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
		 * Invoke the given doit for every resource whose location has the
		 * given location as a prefix.
		 */
		public void matchingPrefixDo(IPath prefix, Doit doit) {
			SortedMap matching;
			if (prefix.segmentCount() > 0) {
				//endPoint is the smallest possible path greater than the prefix that doesn't
				//match the prefix
				IPath endPoint = new Path(prefix.removeTrailingSeparator().toString() + "\0");//$NON-NLS-1$
				matching= map.subMap(prefix, endPoint);
			} else {
				matching = map;
			}
			for (Iterator it = matching.values().iterator(); it.hasNext();) {
				Object value = it.next();
				if (value == null)
					return;
				if (value instanceof List) {
					Iterator duplicates = ((List)value).iterator();
					while (duplicates.hasNext())
						doit.doit((IResource)duplicates.next());
				} else {
					doit.doit((IResource)value);
				}
			}
		}
		/**
		 * Invoke the given doit for every resource that matches the given
		 * location.
		 */
		public void matchingResourcesDo(IPath location, Doit doit) {
			Object value = map.get(location);
			if (value == null)
				return;
			if (value instanceof List) {
				Iterator duplicates = ((List)value).iterator();
				while (duplicates.hasNext())
					doit.doit((IResource)duplicates.next());
			} else {
				doit.doit((IResource)value);
			}
		}
		/**
		 * Calls the given doit with the project of every resource in the map
		 * whose location overlaps another resource in the map.
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
						doit.doit(((IResource)duplicates.next()).getProject());
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
							doit.doit(previousResource.getProject());
						if (currentResource != null)
							doit.doit(currentResource.getProject());
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

	class FindAliasesDoit implements Doit {
		private IPath searchPath;
		private int aliasType;
		public void doit(IResource match) {
			//don't record the resource we're computing aliases against as a match
			if (match.getFullPath().isPrefixOf(searchPath))
				return;
			IPath aliasPath = null;
			switch (match.getType()) {
				case IResource.PROJECT :
					//first check if there is a linked resource that blocks the project location
					if (suffix.segmentCount() > 0) {
						IResource testResource = ((IProject) match).findMember(suffix.segment(0));
						if (testResource != null && testResource.isLinked())
							return;
					}
					//there is an alias under this project
					aliasPath = match.getFullPath().append(suffix);
					break;
				case IResource.FOLDER :
					aliasPath = match.getFullPath().append(suffix);
					break;
				case IResource.FILE :
					if (suffix.segmentCount() == 0)
						aliasPath = match.getFullPath();
					break;
			}
			if (aliasPath != null)
				if (aliasType == IResource.FILE) {
					aliases.add(workspace.getRoot().getFile(aliasPath));
				} else {
					if (aliasPath.segmentCount() == 1)
						aliases.add(workspace.getRoot().getProject(aliasPath.lastSegment()));
					else
						aliases.add(workspace.getRoot().getFolder(aliasPath));
				}
		}
		/**
		 * Sets the resource that we are searching for aliases for.
		 */
		public void setSearchAlias(IResource aliasResource) {
			this.aliasType = aliasResource.getType();
			this.searchPath = aliasResource.getFullPath();
		}
	}
	public class AddToCollectionDoit implements Doit {
		Collection collection;
		public void setCollection(Collection collection) {
			this.collection = collection;
		}
		public void doit(IResource resource) {
			collection.add(resource);
		}
	}
	
	/**
	 * This maps IPath->IResource, where the path is an absolute file system
	 * location, and the resource contains the projects and/or linked resources
	 * that are rooted at that location.
	 */
	protected final LocationMap locationsMap = new LocationMap();
	
	/**
	 * The set of IProjects that have aliases.
	 */
	protected final Set aliasedProjects = new HashSet();
	
	/**
	 * The set of resources that have had structure changes that might
	 * invalidate the locations map or aliased projects set.  These will be
	 * updated incrementally on the next alias request.
	 */
	private final Set structureChanges = new HashSet();
	
	/**
	 * The total number of linked resources that exist in the workspace.  This
	 * count includes linked resources that don't currently have valid locations
	 * (due to an undefined path variable).
	 */
	private int linkedResourceCount = 0;
	
	/**
	 * A temporary set of aliases.  Used during computeAliases, but maintained
	 * as a field as an optimization to prevent recreating the set.
	 */
	protected final HashSet aliases = new HashSet();
	
	/**
	 * The Doit class used for finding aliases.
	 */
	private final FindAliasesDoit findAliases = new FindAliasesDoit();
	
	/**
	 * Doit convenience class for adding items to a list
	 */
	private final AddToCollectionDoit addToCollection = new AddToCollectionDoit();
	/**
	 * The suffix object is also used only during the computeAliases method.
	 * In this case it is a field because it is referenced from an inner class
	 * and we want to avoid creating a pointer array.  It is public to eliminate
	 * the need for synthetic accessor methods.
	 */
	public IPath suffix;
	
	/** the workspace */
	protected final Workspace workspace;

	public AliasManager(Workspace workspace) {
		this.workspace = workspace;
	}
	private void addToLocationsMap(IProject project) {
		IPath location = project.getLocation();
		if (location != null)
			locationsMap.add(location, project);
		try {
			IResource[] members = project.members();
			if (members != null)
				//look for linked resources
				for (int i = 0; i < members.length; i++)
					if (members[i].isLinked())
						addToLocationsMap(members[i]);
		} catch (CoreException e) {
			//skip inaccessible projects
		}
	}
	private void addToLocationsMap(IResource linkedResource) {
		IPath location = linkedResource.getLocation();
		if (location != null)
			if (locationsMap.add(location, linkedResource))
				linkedResourceCount++;
	}
	/**
	 * Builds the table of aliased projects from scratch.
	 */
	private void buildAliasedProjectsSet() {
		aliasedProjects.clear();
		//if there are no linked resources then there can't be any aliased projects
		if (linkedResourceCount <= 0) {
			//paranoid check -- count should never be below zero
//			Assert.isTrue(linkedResourceCount == 0, "Linked resource count below zero");//$NON-NLS-1$
			return;
		}
		//for every resource that overlaps another, marked its project as aliased
		addToCollection.setCollection(aliasedProjects);
		locationsMap.overLappingResourcesDo(addToCollection);
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
	/**
	 * Returns all aliases of the given resource, or null if there are none.
	 */
	public IResource[] computeAliases(final IResource resource, IPath location) {
		//nothing to do if we are or were in an alias-free workspace or project
		if (hasNoAliases(resource))
			return null;
		
		aliases.clear();
		internalComputeAliases(resource, location);
		int size = aliases.size();
		if (size == 0)
			return null;
		return (IResource[]) aliases.toArray(new IResource[size]);
	}
	/**
	 * Computes the aliases of the given resource at the given location, and
	 * adds them to the "aliases" collection.
	 */
	private void internalComputeAliases(IResource resource, IPath location) {
		IPath searchLocation = location == null ? resource.getLocation() : location;
		//if the location is invalid then there won't be any aliases to update
		if (searchLocation == null)
			return;

		suffix = Path.EMPTY;
		int segmentCount = searchLocation.segmentCount();
		findAliases.setSearchAlias(resource);
		/*
		 * Walk up the location segments for this resource, looking for a
		 * resource with a matching location.  All matches are then added to the
		 * "aliases" set.
		 */
		for (;;) {
			locationsMap.matchingResourcesDo(searchLocation, findAliases);
			if (--segmentCount <= 0)
				break;
			suffix = new Path(searchLocation.lastSegment()).append(suffix);
			searchLocation = searchLocation.removeLastSegments(1);
		}
	}
	/**
	 * Returns true if this resource is guaranteed to have no aliases, and false
	 * otherwise.
	 */
	private boolean hasNoAliases(final IResource resource) {
		//check if we're in an aliased project or workspace before updating structure changes.  In the 
		//deletion case, we need to know if the resource was in an aliased project *before* deletion.
		IProject project = resource.getProject();
		boolean noAliases = linkedResourceCount <= 0 || !aliasedProjects.contains(project);
		
		//now update any structure changes and check again if an update is needed
		if (!structureChanges.isEmpty()) {
			updateStructureChanges();
			noAliases &= linkedResourceCount <= 0 || !aliasedProjects.contains(project);
		}
		return noAliases;
	}
	/**
	 * Returns all aliases of this resource, and any aliases of subtrees of this
	 * resource.  Returns null if no aliases are found.
	 */
	private void computeDeepAliases(IResource resource, IPath location) {
		//if the location is invalid then there won't be any aliases to update
		if (location == null)
			return;
		//get the normal aliases (resources rooted in parent locations)
		internalComputeAliases(resource, location);
		//get all resources rooted below this resource's location
		addToCollection.setCollection(aliases);
		locationsMap.matchingPrefixDo(location, addToCollection);
		//if this is a project, get all resources rooted below links in this project
		if (resource.getType() == IResource.PROJECT) {
			try {
				IResource[] members = ((IProject)resource).members();
				for (int i = 0; i < members.length; i++) {
					if (members[i].isLinked()) {
						IPath linkLocation = members[i].getLocation();
						if (linkLocation != null)
							locationsMap.matchingPrefixDo(linkLocation, addToCollection);
					}
				}
			} catch (CoreException e) {
				//skip inaccessible projects
			}
		}
	}
	/**
	 * Returns the comparator to use when sorting the locations map.  Comparison
	 * is based on segments, so that paths with the most segments in common will
	 * always be adjacent.  This is equivalent to the natural order on the path
	 * strings, with the extra condition that the path separator is ordered
	 * before all other characters. (Ex: "/foo" < "/foo/zzz" < "/fooaaa").
	 */
	protected Comparator getComparator() {
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
	public void handleEvent(LifecycleEvent event) throws CoreException {
		/*
		 * We can't determine the end state for most operations because they may
		 * fail after we receive pre-notification.  In these cases, we remember
		 * the invalidated resources and recompute their state lazily on the
		 * next alias request.
		 */
		switch (event.kind) {
			case LifecycleEvent.PRE_PROJECT_CLOSE:
			case LifecycleEvent.PRE_PROJECT_DELETE:
				removeFromLocationsMap((IProject)event.resource);
				//fall through
			case LifecycleEvent.PRE_PROJECT_CREATE:
			case LifecycleEvent.PRE_PROJECT_OPEN:
				structureChanges.add(event.resource);
				break;
			case LifecycleEvent.PRE_LINK_DELETE:
				removeFromLocationsMap(event.resource);
				//fall through
			case LifecycleEvent.PRE_LINK_CREATE:
				structureChanges.add(event.resource);
				break;
			case LifecycleEvent.PRE_PROJECT_COPY:
			case LifecycleEvent.PRE_LINK_COPY:
				structureChanges.add(event.newResource);
				break;
			case LifecycleEvent.PRE_PROJECT_MOVE:
				removeFromLocationsMap((IProject)event.resource);
				structureChanges.add(event.newResource);
				break;
			case LifecycleEvent.PRE_LINK_MOVE:
				removeFromLocationsMap(event.resource);
				structureChanges.add(event.newResource);
				break;
		}
	}
	private void removeFromLocationsMap(IProject project) {
		//remove this project and all linked children from the location table
		IPath location = project.getLocation();
		if (location != null)
			locationsMap.remove(location, project);
		try {
			IResource[] children = project.members();
			if (children != null)
				for (int i = 0; i < children.length; i++)
					if (children[i].isLinked())
						removeFromLocationsMap(children[i]);
		} catch (CoreException e) {
			//ignore inaccessible projects
		}
	}
	private void removeFromLocationsMap(IResource linkedResource) {
		//this linked resource is being deleted
		IPath location = linkedResource.getLocation();
		if (location != null)
			if (locationsMap.remove(location, linkedResource))
				linkedResourceCount--;
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
		workspace.addLifecycleListener(this);
		buildLocationsMap();
		buildAliasedProjectsSet();
	}
	/**
	 * The file underlying the given resource has changed on disk.  Compute all
	 * aliases for this resource and update them.  This method will not attempt
	 * to incur any units of work on the given progress monitor, but it may
	 * update the subtask to reflect what aliases are being updated.
	 * @param resource the resource to compute aliases for
	 * @param location the file system location of the resource (passed as a
	 * parameter because in the project deletion case the resource is no longer
	 * accessible at time of update).
	 * @param whether to search for aliases on all children of the given
	 * resource.  Only depth ZERO and INFINITE are used.
	 */
	public void updateAliases(IResource resource, IPath location, int depth, IProgressMonitor monitor) throws CoreException {
		if (hasNoAliases(resource))
			return;
		aliases.clear();
		if (depth == IResource.DEPTH_ZERO)
			internalComputeAliases(resource, location);
		else
			computeDeepAliases(resource, location);
		if (aliases.size() == 0)
			return;
		FileSystemResourceManager localManager = workspace.getFileSystemManager();
		for (Iterator it = aliases.iterator(); it.hasNext();) {
			IResource alias = (IResource)it.next();
			monitor.subTask(Policy.bind("links.updatingDuplicate", alias.getFullPath().toString()));//$NON-NLS-1$
			localManager.refresh(alias, IResource.DEPTH_INFINITE, false, null);
		}
	}
	/**
	 * Process any structural changes that have occurred since the last alias
	 * request.
	 */
	private void updateStructureChanges() {
		boolean hadChanges = false;
		for (Iterator it = structureChanges.iterator(); it.hasNext();) {
			IResource resource = (IResource) it.next();
			if (!resource.exists())
				continue;
			hadChanges = true;
			if (resource.getType() == IResource.PROJECT)
				addToLocationsMap((IProject)resource);
			else 
				addToLocationsMap(resource);
		}
		structureChanges.clear();
		if (hadChanges)
			buildAliasedProjectsSet();
	}
}
