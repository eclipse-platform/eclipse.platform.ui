/*******************************************************************************
 * Copyright (c) 2015 Manumitting Technologies Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis (MTI) - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 472654
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class implements a topological sort. The implementation distinguishes between the objects to
 * be sorted from their corresponding dependency identifier, and properly tracks where multiple
 * objects have the same identifier. This is required for sorting Eclipse Extension Registry
 * extensions by their plug-in dependencies, as a plug-in may contribute multiple sets of
 * extensions.
 *
 * <p>
 * The implementation creates a dependency graph where the vertices represent the object identifiers
 * and the edges represent the required-relation between them. Subclasses must implement of
 * {@link #getRequirements(Object)} and {@link #getDependencies(Object)}. Although these are
 * normally symmetric operations (i.e., if <em>B</em> is a requirement of <em>A</em>, then
 * <em>A</em> is a dependent of <em>B</em>), one may be easier to compute than the other in some
 * contexts and so this class combines the results.
 * </p>
 *
 * <p>
 * Description of the algorithm:
 * </p>
 * <ol>
 * <li>Construct the dependency graph with {@link #getRequirements(Object)} and
 * {@link #getDependencies(Object)}. The out edges represent requirements, and in edges represent
 * dependencies. This graph may be disconnected if there are nodes with no dependencies amongst each
 * other.</li>
 * <li>Sort the list of identifiers by their out-degree, and then by in-degree. Ids with out-degree
 * 0 are roots; if there is no id with out-degree 0 then we have a cycle. We sort nodes with the
 * same out-degree by their in-degree to privilege nodes within the cycle from those that are
 * dependencies (e.g., consider: A &rarr; B, B &rarr; C, C &rarr; A, D &rarr; A; all have out-degree
 * 1, but A has in-degree 2, and A must be output before D).</li>
 * <li>While there are ids left to consider, take the id with lowest out-degree and add its objects
 * to the list. Remove the id from all of its dependents' required lists. This removal changes the
 * out-degree and may require that the id list be resorted since some of the dependents may now be
 * roots. Note that should we encounter a cycle (i.e., no nodes of degree 0), we process all the
 * bundles within the cycle, since any dependencies to any single ndoe within the cycle effectively
 * extends to all.</li>
 * </ol>
 *
 * @param <T>
 *            the type of objects being sorted
 * @param <ID>
 *            the type of the object identifiers; multiple objects of T may correspond to the same
 *            ID
 */
public abstract class TopologicalSort<T, ID> {
	private final Map<ID, Collection<T>> mappedObjects = new HashMap<>();
	// Captures the bundles that are listed as requirements for a particular bundle.
	private final Map<ID, Collection<ID>> requires = new HashMap<>();
	// Captures the bundles that list a particular bundle as a requirement
	private final Map<ID, Collection<ID>> depends = new HashMap<>();

	/**
	 * Return the identifier for the given object. The implementation properly tracks where multiple
	 * objects have the same identifier.
	 *
	 * @param object
	 *            the object
	 * @return the identifier
	 */
	protected abstract ID getId(T object);

	/**
	 * Return the list of IDs required by {@code id}. Implementors may choose to return {@code null}
	 * or an empty collection if this information is more easily computed by
	 * {@link #getDependencies(Object)}.
	 *
	 * @param id
	 *            the id
	 * @return the IDs required by {@code id}; may be {@code null}
	 */
	protected abstract Collection<ID> getRequirements(ID id);

	/**
	 * Return the list of IDs depended upon by {@code id}. Implementors may choose to return
	 * {@code null} or an empty collection if this information is more easily computed by
	 * {@link #getRequirements(Object)}.
	 *
	 * @param id
	 *            the id
	 * @return the IDs depended upon by {@code id}; may be {@code null}
	 */
	protected abstract Collection<ID> getDependencies(ID id);

	/**
	 * Sort the provided extensions by the dependencies of their contributors. Note that sorting is
	 * done in-place.
	 *
	 * @param objects
	 *            the objects to be sorted
	 * @return the objects in a topologically-sorted order
	 */
	public T[] sort(T[] objects) {
		if (objects.length <= 1) {
			return objects;
		}

		addAll(objects);
		return process(objects);
	}

	/** Place results in results and return results */
	private T[] process(T[] results) {
		buildDependencyGraph();

		// Sort nodes by out-degree ({@code requires}) and then by in-degree.
		// There are two situations: we have disconnected graphs
		// In case of a cycle, one of the nodes involved in the cycle should have
		// higher in-degree from some other non-cyclic node
		int resultsIndex = 0;
		List<ID> sortedByOutdegree = new ArrayList<>(requires.keySet());
		Comparator<ID> outdegreeSorter = new Comparator<ID>() {
			@Override
			public int compare(ID o1, ID o2) {
				assert requires.containsKey(o1) && requires.containsKey(o2);
				int comparison = requires.get(o1).size() - requires.get(o2).size();
				if (comparison == 0) {
					// Select the node whose removal would have the greatest effect
					return depends.get(o2).size() - depends.get(o1).size();
				}
				return comparison;
			}
		};
		Collections.sort(sortedByOutdegree, outdegreeSorter);

		while (!sortedByOutdegree.isEmpty()) {
			// don't sort unnecessarily: the current ordering is fine providing
			// item #0 still has no dependencies
			if (!requires.get(sortedByOutdegree.get(0)).isEmpty()) {
				Collections.sort(sortedByOutdegree, outdegreeSorter);
			}
			LinkedList<ID> cycleToBeDone = new LinkedList<>();
			cycleToBeDone.add(sortedByOutdegree.remove(0));
			while (!cycleToBeDone.isEmpty()) {
				ID bundleId = cycleToBeDone.removeFirst();
				assert depends.containsKey(bundleId) && requires.containsKey(bundleId);
				for (T ext : mappedObjects.get(bundleId)) {
					results[resultsIndex++] = ext;
				}
				// requires.get(bundleId) is empty unless there's a cycle;
				// we process all nodes within the cycle now
				for (ID reqId : requires.get(bundleId)) {
					cycleToBeDone.add(reqId);
					sortedByOutdegree.remove(reqId);
					depends.get(reqId).remove(bundleId);
				}
				requires.remove(bundleId);
				for (ID depId : depends.get(bundleId)) {
					requires.get(depId).remove(bundleId);
				}
				depends.remove(bundleId);
			}
		}
		return results;
	}

	/**
	 * @param objects
	 */
	private void addAll(T[] objects) {
		// first build up the list of object ids actually being considered
		for (T o : objects) {
			ID id = getId(o);
			Collection<T> exts = mappedObjects.get(id);
			if (exts == null) {
				mappedObjects.put(id, exts = new HashSet<>());
			}
			exts.add(o);
		}
	}

	private void buildDependencyGraph() {
		// reset
		requires.clear();
		depends.clear();
		for (ID id : mappedObjects.keySet()) {
			requires.put(id, new HashSet<ID>());
			depends.put(id, new HashSet<ID>());
		}

		// now populate the dependency graph
		for (ID subjectId : mappedObjects.keySet()) {
			assert requires.containsKey(subjectId) && depends.containsKey(subjectId);

			Collection<ID> requirements = getRequirements(subjectId);
			if (requirements != null) {
				for (ID requiredId : requirements) {
					assert !requiredId.equals(subjectId) : "self-cycles not supported"; //$NON-NLS-1$
					// ignore objects not being sorted
					if (!mappedObjects.containsKey(requiredId)) {
						continue;
					}
					// So requiredId is a requirement for subjectId
					// and subjectId is a dependent of requiredId
					depends.get(requiredId).add(subjectId);
					requires.get(subjectId).add(requiredId);
				}
			}

			Collection<ID> dependencies = getDependencies(subjectId);
			if (dependencies != null) {
				for (ID dependentId : dependencies) {
					assert !dependentId.equals(subjectId) : "self-cycles not supported"; //$NON-NLS-1$
					// ignore objects not being sorted
					if (!mappedObjects.containsKey(dependentId)) {
						continue;
					}
					// So dependentId is a dependency of subjectId
					// and subjectId is a requirement of dependentId
					requires.get(dependentId).add(subjectId);
					depends.get(subjectId).add(dependentId);
				}
			}
		}
	}
}
