/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.internal.builders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;
import java.util.function.Predicate;
import junit.framework.JUnit4TestAdapter;
import org.eclipse.core.internal.resources.ComputeProjectOrder;
import org.eclipse.core.internal.resources.ComputeProjectOrder.Digraph;
import org.eclipse.core.internal.resources.ComputeProjectOrder.Digraph.Edge;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 *
 */
@RunWith(JUnit4.class)
public class ComputeProjectOrderTest {
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(ComputeProjectOrderTest.class);
	}

	@Test
	public void testComputeVertexOrderDuration() {
		final Digraph<Object> digraph = new Digraph<>(Object.class);
		for (int i = 0; i < 320; i++) {
			Collection<Object> existingVertexes = new HashSet<>(digraph.vertexMap.keySet());
			Object newVertex = new Object();
			digraph.addVertex(newVertex);
			existingVertexes.forEach(existingVertex -> digraph.addEdge(existingVertex, newVertex));
		}
		long timestamp = System.currentTimeMillis();
		digraph.freeze();
		ComputeProjectOrder.computeVertexOrder(digraph, Object.class);
		long duration = System.currentTimeMillis() - timestamp;
		assertTrue(duration < 1000);
	}

	@Test
	public void testFilterDigraphDuration() {
		final Digraph<Object> digraph = new Digraph<>(Object.class);
		Object initialVertex = new Object();
		digraph.addVertex(initialVertex);
		for (int i = 0; i < 320; i++) {
			Collection<Object> existingVertexes = new HashSet<>(digraph.vertexMap.keySet());
			Object newVertex = new Object();
			digraph.addVertex(newVertex);
			existingVertexes.forEach(existingVertex -> digraph.addEdge(existingVertex, newVertex));
		}
		// keep everything
		long timestamp = System.currentTimeMillis();
		Digraph<Object> filtered = ComputeProjectOrder.buildFilteredDigraph(digraph, o -> false, Object.class);
		assertEquals(digraph.vertexMap.keySet(), filtered.vertexMap.keySet());
		long duration = System.currentTimeMillis() - timestamp;
		assertTrue(duration < 1000);
		// keep only 1 element
		timestamp = System.currentTimeMillis();
		filtered = ComputeProjectOrder.buildFilteredDigraph(digraph, o -> o != initialVertex, Object.class);
		assertEquals(Collections.singleton(initialVertex), filtered.vertexMap.keySet());
		duration = System.currentTimeMillis() - timestamp;
		assertTrue(duration < 1000);
	}

	@Test
	public void testFilterDigraphBasic() {
		final Digraph<Object> digraph = new Digraph<>(Object.class);
		Object a = new Object();
		Object b = new Object();
		Object c = new Object();
		digraph.addVertex(a);
		digraph.addVertex(b);
		digraph.addEdge(a, b);
		digraph.addVertex(c);
		digraph.addEdge(b, c);
		//
		Digraph<Object> filtered = ComputeProjectOrder.buildFilteredDigraph(digraph, Predicate.isEqual(b), Object.class);
		Set<Object> filteredVertexes = new HashSet<>(2, (float) 1.);
		filteredVertexes.add(a);
		filteredVertexes.add(c);
		assertEquals(filteredVertexes, filtered.vertexMap.keySet());
		Collection<Edge<Object>> filteredEdges = filtered.getEdges();
		assertEquals(Collections.singleton(new Edge<>(a, c)), filteredEdges);
	}
}
