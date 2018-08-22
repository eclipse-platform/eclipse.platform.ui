/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.rulers;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

/**
 * A directed acyclic graph. See http://en.wikipedia.org/wiki/Directed_acyclic_graph
 * @param <E> type of the vertices
 *
 * @since 3.3
 */
public final class DAG<E> {
	/**
	 * Multimap, supports <code>null</code> key, but not <code>null</code> values.
	 * @param <K> key type
	 * @param <V> values type
	 */
	private static final class MultiMap<K, V> {
		private final Map<K, Set<V>> fMap= new LinkedHashMap<>();

		/**
		 * Adds <code>val</code> to the values mapped to by <code>key</code>. If
		 * <code>val</code> is <code>null</code>, <code>key</code> is added to the key set of
		 * the multimap.
		 *
		 * @param key the key
		 * @param val the value
		 */
		public void put(K key, V val) {
			Set<V> values= fMap.get(key);
			if (values == null) {
				values= new LinkedHashSet<>();
				fMap.put(key, values);
			}
			if (val != null)
				values.add(val);
		}

		/**
		 * Returns all mappings for the given key, an empty set if there are no mappings.
		 *
		 * @param key the key
		 * @return the mappings for <code>key</code>
		 */
		public Set<V> get(K key) {
			Set<V> values= fMap.get(key);
			return values == null ? Collections.emptySet() : values;
		}

		public Set<K> keySet() {
			return fMap.keySet();
		}

		/**
		 * Removes all mappings for <code>key</code> and removes <code>key</code> from the key
		 * set.
		 *
		 * @param key the key to remove
		 * @return the removed mappings
		 */
		public Set<V> removeAll(K key) {
			Set<V> values= fMap.remove(key);
			return values == null ? Collections.emptySet() : values;
		}

		/**
		 * Removes a mapping from the multimap, but does not remove the <code>key</code> from the
		 * key set.
		 *
		 * @param key the key
		 * @param val the value
		 */
		public void remove(K key, V val) {
			Set<V> values= fMap.get(key);
			if (values != null)
				values.remove(val);
		}

		@Override
		public String toString() {
			return fMap.toString();
		}
	}

	private final MultiMap<E, E> fOut= new MultiMap<>();
	private final MultiMap<E, E> fIn= new MultiMap<>();

	/**
	 * Adds a directed edge from <code>origin</code> to <code>target</code>. The vertices are not
	 * required to exist prior to this call - if they are not currently contained by the graph, they are
	 * automatically added.
	 *
	 * @param origin the origin vertex of the dependency
	 * @param target the target vertex of the dependency
	 * @return <code>true</code> if the edge was added, <code>false</code> if the
	 *         edge was not added because it would have violated the acyclic nature of the
	 *         receiver.
	 */
	public boolean addEdge(E origin, E target) {
		Assert.isLegal(origin != null);
		Assert.isLegal(target != null);

		if (hasPath(target, origin))
			return false;

		fOut.put(origin, target);
		fOut.put(target, null);
		fIn.put(target, origin);
		fIn.put(origin, null);
		return true;
	}

	/**
	 * Adds a vertex to the graph. If the vertex does not exist prior to this call, it is added with
	 * no incoming or outgoing edges. Nothing happens if the vertex already exists.
	 *
	 * @param vertex the new vertex
	 */
	public void addVertex(E vertex) {
		Assert.isLegal(vertex != null);
		fOut.put(vertex, null);
		fIn.put(vertex, null);
	}

	/**
	 * Removes a vertex and all its edges from the graph.
	 *
	 * @param vertex the vertex to remove
	 */
	public void removeVertex(E vertex) {
		Set<E> targets= fOut.removeAll(vertex);
		for (Iterator<E> it= targets.iterator(); it.hasNext();)
			fIn.remove(it.next(), vertex);
		Set<E> origins= fIn.removeAll(vertex);
		for (Iterator<E> it= origins.iterator(); it.hasNext();)
			fOut.remove(it.next(), vertex);
	}

	/**
	 * Returns the sources of the receiver. A source is a vertex with no incoming edges. The
	 * returned set's iterator traverses the nodes in the order they were added to the graph.
	 *
	 * @return the sources of the receiver
	 */
	public Set<E> getSources() {
		return computeZeroEdgeVertices(fIn);
	}

	/**
	 * Returns the sinks of the receiver. A sink is a vertex with no outgoing edges. The returned
	 * set's iterator traverses the nodes in the order they were added to the graph.
	 *
	 * @return the sinks of the receiver
	 */
	public Set<E> getSinks() {
		return computeZeroEdgeVertices(fOut);
	}

	private static <T> Set<T> computeZeroEdgeVertices(MultiMap<T, T> map) {
		Set<T> candidates= map.keySet();
		Set<T> roots= new LinkedHashSet<>(candidates.size());
		for (Iterator<T> it= candidates.iterator(); it.hasNext();) {
			T candidate= it.next();
			if (map.get(candidate).isEmpty())
				roots.add(candidate);
		}
		return roots;
	}

	/**
	 * Returns the direct children of a vertex. The returned {@link Set} is unmodifiable.
	 *
	 * @param vertex the parent vertex
	 * @return the direct children of <code>vertex</code>
	 */
	public Set<E> getChildren(E vertex) {
		return Collections.unmodifiableSet(fOut.get(vertex));
	}

	private boolean hasPath(E start, E end) {
		// break condition
		if (start == end)
			return true;

		Set<E> children= fOut.get(start);
		for (Iterator<E> it= children.iterator(); it.hasNext();)
			// recursion
			if (hasPath(it.next(), end))
				return true;
		return false;
	}

	@Override
	public String toString() {
		return "Out: " + fOut.toString() + " In: " + fIn.toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}