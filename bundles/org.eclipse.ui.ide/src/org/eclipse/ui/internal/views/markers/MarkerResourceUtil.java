/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceMappingContext;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.internal.ide.Policy;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;
import org.eclipse.ui.views.tasklist.ITaskListResourceAdapter;

/**
 * A Resource helper class for the markers view code.
 * 
 * @author hitesh soliwal
 * @since 3.6
 * 
 */
class MarkerResourceUtil {

	static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];

	/**
	 * Optimally gets the resources applicable to the current state of filters,
	 * the smaller the resources and more specific they are the less the
	 * filtering we have to do during processing.
	 * 
	 * @return collection of resource we want to collect markers for, taking
	 *         various enabled filters into account.
	 */
	static Set computeResources(IResource[] selectedResources,
			Collection enabledFilters, boolean andFilters) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		if (enabledFilters==null||enabledFilters.size() == 0) {
			HashSet set = new HashSet(1);
			set.add(root);
			return set;
		}
		Set resourceSet = andFilters ? getResourcesFiltersAnded(enabledFilters,
				selectedResources, root) : getResourcesFiltersOred(
				enabledFilters, selectedResources, root);
		
		//remove duplicates
		return trim2ParentResources(root, resourceSet);
	}

	/**
	 * (optimization as side-effect): Compute common parents, if any. Remove further
	 * duplicates. We collect markers with a flag of DEPTH_INFINITE; so,
	 * effectively the children of a resource are also its duplicates.
	 * 
	 * @param root
	 * @param resourceSet
	 * @return set
	 */
	static Set trim2ParentResources(IWorkspaceRoot root, Set resourceSet) {
		if (resourceSet.isEmpty() || resourceSet.size() == 1) {
			return resourceSet;
		}
		if (resourceSet.contains(root)) {
			resourceSet.clear();
			resourceSet.add(root);
			return resourceSet;
		}
		Object[] clones = resourceSet.toArray();
		for (int i = 0; i < clones.length; i++) {
			IResource resource = (IResource) clones[i];
			Iterator iterator = resourceSet.iterator();
			while (iterator.hasNext()) {
				IResource resToRemove = (IResource) iterator.next();
				if (resToRemove.equals(root)) {
					resourceSet.clear();
					resourceSet.add(root);
					return resourceSet;
				}
				if (resource.equals(resToRemove)) {
					continue;
				}
				if (resource.getFullPath()
						.isPrefixOf(resToRemove.getFullPath())) {
					iterator.remove();
				}
			}
		}
		return resourceSet;
	}

	/**
	 * Since every filter provides it's on set of resources, instead of
	 * computing markers on filters individually and then ORing the markers; we
	 * would save a good amount of system-resources if we ORed them before
	 * gathering phase,removing duplicates.
	 * 
	 * @param enabledFilters
	 * @param root
	 * @return set
	 */
	static Set getResourcesFiltersOred(Collection enabledFilters,
			IResource[] selectedResources, IWorkspaceRoot root) {
		if (enabledFilters==null||enabledFilters.size() == 0) {
			HashSet set = new HashSet(1);
			set.add(root);
			return set;
		}
		Set resourceSet = new HashSet();
		Iterator filtersIterator = enabledFilters.iterator();
		while (filtersIterator.hasNext()) {
			MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) filtersIterator
					.next();
			Set set = getResourcesForFilter(group, selectedResources, root);
			resourceSet.addAll(set);
			if (resourceSet.contains(root)) {
				set = new HashSet(1);
				set.add(root);
				return set;
			}
		}
		return resourceSet;
	}

	/**
	 * The method may look long and a little time-consuming, but it actually
	 * performs a short-circuit AND operation on the resources, and therefore
	 * quick.
	 * 
	 * Note: This is an optimization; we could have ORed the resources instead.
	 * Let us say, for example, we had a filter of workspace-scope(ANY), and
	 * others of scope on Selected element and maybe others.Now, if we computed
	 * markers by ORing the resources, then we would compute markers for the
	 * entire Workspace. The filters would anyways keep only the markers that
	 * are an intersection of all resources (filter by scope) .In other words,
	 * we spend more system-resources in both gathering and filtering.If we
	 * ANDed the scopes(resources) we'd, save a good amount of system-resources
	 * in both phases.
	 * 
	 * @param enabledFilters
	 * @param selectedResources
	 * @param root
	 * @return set
	 */
	static Set getResourcesFiltersAnded(Collection enabledFilters,
			IResource[] selectedResources, IWorkspaceRoot root) {
		if (enabledFilters==null||enabledFilters.size() == 0) {
			HashSet set = new HashSet(1);
			set.add(root);
			return set;
		}
		Set resourceSet = new HashSet();
		
		Iterator filtersIterator = enabledFilters.iterator();
		Set removeMain = new HashSet();
		while (filtersIterator.hasNext()) {
			MarkerFieldFilterGroup group = (MarkerFieldFilterGroup) filtersIterator
					.next();
			Set set = getResourcesForFilter(group, selectedResources, root);
			if (resourceSet.isEmpty()) {
				// first time
				resourceSet.addAll(set);
			} else {
				Iterator resIterator = resourceSet.iterator();
				while (resIterator.hasNext()) {
					boolean remove = true;
					IResource mainRes = (IResource) resIterator.next();
					Iterator iterator = set.iterator();
					while (iterator.hasNext() && remove) {
						IResource grpRes = (IResource) iterator.next();
						remove = !grpRes.equals(mainRes);
						if (remove
								&& grpRes.getFullPath().isPrefixOf(
										mainRes.getFullPath())) {
							remove = false;
						} else if (remove
								&& mainRes.getFullPath().isPrefixOf(
										grpRes.getFullPath())) {
							remove = false;
							removeMain.add(mainRes);
						}
					}
					if (remove) {
						resIterator.remove();
					}
				}
				Iterator iterator = set.iterator();
				while (iterator.hasNext()) {
					boolean remove = true;
					IResource grpRes = (IResource) iterator.next();
					resIterator = resourceSet.iterator();
					while (resIterator.hasNext()&&remove) {
						IResource mainRes = (IResource) resIterator.next();
						remove = !grpRes.equals(mainRes);
						if (remove
								&& mainRes.getFullPath().isPrefixOf(
										grpRes.getFullPath())) {
							remove = false;
						}
					}
					if (remove) {
						iterator.remove();
					}
				}
				resourceSet.addAll(set);
				resourceSet.removeAll(removeMain);
				removeMain.clear();
				if (resourceSet.isEmpty()) {
					// if the And between two is empty
					// then its empty for all
					return resourceSet;
				}
			}
		}
		return resourceSet;
	}

	/**
	 * Get the resources indicated by the filter's scope.
	 *  
	 * @param group
	 * @param selectedResources
	 * @param root
	 */
	static Set getResourcesForFilter(MarkerFieldFilterGroup group,
			IResource[] selectedResources, IWorkspaceRoot root) {
		HashSet resourceSet = new HashSet();
		switch (group.getScope()) {
		case MarkerFieldFilterGroup.ON_ANY: {
			resourceSet.add(root);
			break;
		}
		case MarkerFieldFilterGroup.ON_SELECTED_ONLY:
		case MarkerFieldFilterGroup.ON_SELECTED_AND_CHILDREN: {
			for (int i = 0; i < selectedResources.length; i++) {
				resourceSet.add(selectedResources[i]);
			}
			break;
		}
		case MarkerFieldFilterGroup.ON_ANY_IN_SAME_CONTAINER: {
			IResource[] resources = getProjects(selectedResources);
			for (int i = 0; i < resources.length; i++) {
				resourceSet.add(resources[i]);
			}
			break;
		}
		case MarkerFieldFilterGroup.ON_WORKING_SET: {
			group.refresh();
			IResource[] resources = group.getResourcesInWorkingSet();
			for (int i = 0; i < resources.length; i++) {
				resourceSet.add(resources[i]);
			}
			break;
		}
		}
		return resourceSet;
	}

	/**
	 * Returns the set of projects that contain the given set of resources.
	 * 
	 * @param resources
	 * @return IProject[]
	 */
	static IProject[] getProjects(IResource[] resources) {
		if (resources == null)
			return EMPTY_PROJECT_ARRAY;

		Collection projects = getProjectsAsCollection(resources);

		return (IProject[]) projects.toArray(new IProject[projects.size()]);
	}

	/**
	 * Return the projects for the elements.
	 * 
	 * @param elements
	 *            collection of IResource or IResourceMapping
	 * @return Collection of IProject
	 */
	static Collection getProjectsAsCollection(Object[] elements) {
		HashSet projects = new HashSet();

		for (int idx = 0; idx < elements.length; idx++) {
			if (elements[idx] instanceof IResource) {
				projects.add(((IResource) elements[idx]).getProject());
			} else {
				IProject[] mappingProjects = (((ResourceMapping) elements[idx])
						.getProjects());
				for (int i = 0; i < mappingProjects.length; i++) {
					projects.add(mappingProjects[i]);
				}
			}

		}

		return projects;
	}

	/**
	 * Add the resources in resourceMapping to the resourceCollection
	 * 
	 * @param resourceCollection
	 * @param resourceMapping
	 */
	static void addResources(Collection resourceCollection,
			ResourceMapping resourceMapping) {

		try {
			ResourceTraversal[] traversals = resourceMapping.getTraversals(
					ResourceMappingContext.LOCAL_CONTEXT,
					new NullProgressMonitor());
			for (int i = 0; i < traversals.length; i++) {
				ResourceTraversal traversal = traversals[i];
				IResource[] result = traversal.getResources();
				for (int j = 0; j < result.length; j++) {
					resourceCollection.add(result[j]);
				}
			}
		} catch (CoreException e) {
			Policy.handle(e);
		}
	}
	
	/**
	 * Adapts an object to a resource or resource mapping;
	 * If the object cannot be adapted,it return null.
	 * The method uses {@link Util#getAdapter(Object, Class)}
	 * The scheme for adapting follows the sequence
	 * 		If instance of {@link IAdaptable}, query ITaskListResourceAdapter
	 * 			if available for the object.
	 * 		Try to adapt to an IResource
	 * 		Try to adapt to an IFile
	 * 		Finally try adapting to a ResourceMapping
	 * 
	 * @param object
	 */
	static Object adapt2ResourceElement(Object object) {
		IResource resource = null;
		if (object instanceof IAdaptable) {
			Object adapter = Util.getAdapter(object,
					ITaskListResourceAdapter.class);
			if (adapter != null) {
				resource = ((ITaskListResourceAdapter) adapter)
						.getAffectedResource((IAdaptable) object);
			}
		}
		if (resource == null) {
			resource = (IResource) Util.getAdapter(object, IResource.class);
		}
		if (resource == null) {
			resource = (IResource) Util.getAdapter(object, IFile.class);
		}
		if (resource == null) {
			Object mapping = Util.getAdapter(object, ResourceMapping.class);
			if (mapping != null) {
				return mapping;
			}
		} else {
			return resource;
		}
		return null;
	}

	/**
	 * Gets all sub-type id(s) including self, for the list of marker typeIds
	 * 
	 * @param typeIds
	 */
	static String[] getAllSubTypesIds(String[] typeIds) {
		HashSet set = getAllSubTypes(typeIds);
		return toTypeStrings(set);
	}

	/**
	 * Gets all sub-types {@link MarkerType} including self for the list of
	 * marker typeIds
	 * 
	 * @param typeIds
	 */
	static HashSet getAllSubTypes(String[] typeIds) {
		HashSet set = new HashSet();
		MarkerTypesModel typesModel = MarkerTypesModel.getInstance();
		for (int i = 0; i < typeIds.length; i++) {
			MarkerType type = typesModel.getType(typeIds[i]);
			set.add(type);
			MarkerType[] subs = type.getAllSubTypes();
			for (int j = 0; j < subs.length; j++) {
				set.add(subs[j]);
			}
		}
		return set;
	}

	/**
	 * Gets mutually exclusive super-types ids for the list of
	 * marker typeIds
	 * 
	 * @param typeIds
	 */
	static String[] getMutuallyExclusiveSupersIds(String[] typeIds) {
		HashSet set = getMutuallyExclusiveSupers(typeIds);
		return toTypeStrings(set);
	}

	/**
	 * Gets mutually exclusive super-types {@link MarkerType} for the list of
	 * marker typeIds
	 * 
	 * @param typeIds
	 */
	static HashSet getMutuallyExclusiveSupers(String[] typeIds) {
		HashSet set = new HashSet();
		MarkerTypesModel typesModel = MarkerTypesModel.getInstance();
		for (int i = 0; i < typeIds.length; i++) {
			MarkerType type = typesModel.getType(typeIds[i]);
			set.add(type);
		}
		for (int i = 0; i < typeIds.length; i++) {
			MarkerType type = typesModel.getType(typeIds[i]);
			MarkerType[] subs = type.getAllSubTypes();
			HashSet subsOnly = new HashSet(Arrays.asList(subs));
			subsOnly.remove(type);
			set.removeAll(subsOnly);
		}
		return set;
	}

	/**
	 * Converts a collection of {@link MarkerType} into an array of marker
	 * typeIds
	 * 
	 * @param collection
	 */
	private static String[] toTypeStrings(Collection collection) {
		HashSet ids = new HashSet();
		Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			MarkerType type = (MarkerType) iterator.next();
			ids.add(type.getId());
		}
		return (String[]) ids.toArray(new String[ids.size()]);
	}
}
