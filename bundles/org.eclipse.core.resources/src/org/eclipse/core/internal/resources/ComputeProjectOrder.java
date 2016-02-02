/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Broadcom Corporation - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *     Mickael Istria (Red Hat Inc.) - Bug 488937
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.*;

/**
 * Implementation of a sort algorithm for computing the order of vertexes that are part
 * of a reference graph. This algorithm handles cycles in the graph in a reasonable way.
 * In 3.7 this class was enhanced to support computing order of a graph containing an
 * arbitrary type.
 *
 * @since 2.1
 */
class ComputeProjectOrder {

	/*
	 * Prevent class from being instantiated.
	 */
	private ComputeProjectOrder() {
		// not allowed
	}

	/**
	 * A directed graph. Once the vertexes and edges of the graph have been
	 * defined, the graph can be queried for the depth-first finish time of each
	 * vertex.
	 * <p>
	 * Ref: Cormen, Leiserson, and Rivest <it>Introduction to Algorithms</it>,
	 * McGraw-Hill, 1990. The depth-first search algorithm is in section 23.3.
	 * </p>
	 */
	private static class Digraph {
		/**
		 * struct-like object for representing a vertex along with various
		 * values computed during depth-first search (DFS).
		 */
		public static class Vertex {
			/**
			 * White is for marking vertexes as unvisited.
			 */
			public static final String WHITE = "white"; //$NON-NLS-1$

			/**
			 * Grey is for marking vertexes as discovered but visit not yet
			 * finished.
			 */
			public static final String GREY = "grey"; //$NON-NLS-1$

			/**
			 * Black is for marking vertexes as visited.
			 */
			public static final String BLACK = "black"; //$NON-NLS-1$

			/**
			 * Color of the vertex. One of <code>WHITE</code> (unvisited),
			 * <code>GREY</code> (visit in progress), or <code>BLACK</code>
			 * (visit finished). <code>WHITE</code> initially.
			 */
			public String color = WHITE;

			/**
			 * The DFS predecessor vertex, or <code>null</code> if there is no
			 * predecessor. <code>null</code> initially.
			 */
			public Vertex predecessor = null;

			/**
			 * Timestamp indicating when the vertex was finished (became BLACK)
			 * in the DFS. Finish times are between 1 and the number of
			 * vertexes.
			 */
			public int finishTime;

			/**
			 * The id of this vertex.
			 */
			public Object id;

			/**
			 * Ordered list of adjacent vertexes. In other words, "this" is the
			 * "from" vertex and the elements of this list are all "to"
			 * vertexes.
			 *
			 * Element type: <code>Vertex</code>
			 */
			public List<Vertex> adjacent = new ArrayList<>(3);

			/**
			 * Creates a new vertex with the given id.
			 *
			 * @param id the vertex id
			 */
			public Vertex(Object id) {
				this.id = id;
			}
		}

		/**
		 * Ordered list of all vertexes in this graph.
		 *
		 * Element type: <code>Vertex</code>
		 */
		private List<Vertex> vertexList = new ArrayList<>(100);

		/**
		 * Map from id to vertex.
		 *
		 * Key type: <code>Object</code>; value type: <code>Vertex</code>
		 */
		private Map<Object, Vertex> vertexMap = new HashMap<>(100);

		/**
		 * DFS visit time. Non-negative.
		 */
		private int time;

		/**
		 * Indicates whether the graph has been initialized. Initially
		 * <code>false</code>.
		 */
		private boolean initialized = false;

		/**
		 * Indicates whether the graph contains cycles. Initially
		 * <code>false</code>.
		 */
		private boolean cycles = false;

		/**
		 * Creates a new empty directed graph object.
		 * <p>
		 * After this graph's vertexes and edges are defined with
		 * <code>addVertex</code> and <code>addEdge</code>, call
		 * <code>freeze</code> to indicate that the graph is all there, and then
		 * call <code>idsByDFSFinishTime</code> to read off the vertexes ordered
		 * by DFS finish time.
		 * </p>
		 */
		public Digraph() {
			super();
		}

		/**
		 * Freezes this graph. No more vertexes or edges can be added to this
		 * graph after this method is called. Has no effect if the graph is
		 * already frozen.
		 */
		public void freeze() {
			if (!initialized) {
				initialized = true;
				// only perform depth-first-search once
				DFS();
			}
		}

		/**
		 * Defines a new vertex with the given id. The depth-first search is
		 * performed in the relative order in which vertexes were added to the
		 * graph.
		 *
		 * @param id the id of the vertex
		 * @exception IllegalArgumentException if the vertex id is
		 * already defined or if the graph is frozen
		 */
		public void addVertex(Object id) throws IllegalArgumentException {
			if (initialized) {
				throw new IllegalArgumentException();
			}
			Vertex vertex = new Vertex(id);
			Object existing = vertexMap.put(id, vertex);
			// nip problems with duplicate vertexes in the bud
			if (existing != null) {
				throw new IllegalArgumentException();
			}
			vertexList.add(vertex);
		}

		/**
		 * Adds a new directed edge between the vertexes with the given ids.
		 * Vertexes for the given ids must be defined beforehand with
		 * <code>addVertex</code>. The depth-first search is performed in the
		 * relative order in which adjacent "to" vertexes were added to a given
		 * "from" index.
		 *
		 * @param fromId the id of the "from" vertex
		 * @param toId the id of the "to" vertex
		 * @exception IllegalArgumentException if either vertex is undefined or
		 * if the graph is frozen
		 */
		public void addEdge(Object fromId, Object toId) throws IllegalArgumentException {
			if (initialized) {
				throw new IllegalArgumentException();
			}
			Vertex fromVertex = vertexMap.get(fromId);
			Vertex toVertex = vertexMap.get(toId);
			// nip problems with bogus vertexes in the bud
			if (fromVertex == null) {
				throw new IllegalArgumentException();
			}
			if (toVertex == null) {
				throw new IllegalArgumentException();
			}
			fromVertex.adjacent.add(toVertex);
		}

		/**
		 * Returns the ids of the vertexes in this graph ordered by depth-first
		 * search finish time. The graph must be frozen.
		 *
		 * @param increasing <code>true</code> if objects are to be arranged
		 * into increasing order of depth-first search finish time, and
		 * <code>false</code> if objects are to be arranged into decreasing
		 * order of depth-first search finish time
		 * @return the list of ids ordered by depth-first search finish time
		 * (element type: <code>Object</code>)
		 * @exception IllegalArgumentException if the graph is not frozen
		 */
		public List<Object> idsByDFSFinishTime(boolean increasing) {
			if (!initialized) {
				throw new IllegalArgumentException();
			}
			int len = vertexList.size();
			Object[] r = new Object[len];
			for (Iterator<Vertex> allV = vertexList.iterator(); allV.hasNext();) {
				Vertex vertex = allV.next();
				int f = vertex.finishTime;
				// note that finish times start at 1, not 0
				if (increasing) {
					r[f - 1] = vertex.id;
				} else {
					r[len - f] = vertex.id;
				}
			}
			return Arrays.asList(r);
		}

		/**
		 * Returns whether the graph contains cycles. The graph must be frozen.
		 *
		 * @return <code>true</code> if this graph contains at least one cycle,
		 * and <code>false</code> if this graph is cycle free
		 * @exception IllegalArgumentException if the graph is not frozen
		 */
		public boolean containsCycles() {
			if (!initialized) {
				throw new IllegalArgumentException();
			}
			return cycles;
		}

		/**
		 * Returns the non-trivial components of this graph. A non-trivial
		 * component is a set of 2 or more vertexes that were traversed
		 * together. The graph must be frozen.
		 *
		 * @return the possibly empty list of non-trivial components, where
		 * each component is an array of ids (element type:
		 * <code>Object[]</code>)
		 * @exception IllegalArgumentException if the graph is not frozen
		 */
		public List<Object[]> nonTrivialComponents() {
			if (!initialized) {
				throw new IllegalArgumentException();
			}
			// find the roots of each component
			// Map<Vertex,List<Object>> components
			Map<Vertex, List<Object>> components = new HashMap<>();
			for (Iterator<Vertex> it = vertexList.iterator(); it.hasNext();) {
				Vertex vertex = it.next();
				if (vertex.predecessor == null) {
					// this vertex is the root of a component
					// if component is non-trivial we will hit a child
				} else {
					// find the root ancestor of this vertex
					Vertex root = vertex;
					while (root.predecessor != null) {
						root = root.predecessor;
					}
					List<Object> component = components.get(root);
					if (component == null) {
						component = new ArrayList<>(2);
						component.add(root.id);
						components.put(root, component);
					}
					component.add(vertex.id);
				}
			}
			List<Object[]> result = new ArrayList<>(components.size());
			for (Iterator<List<Object>> it = components.values().iterator(); it.hasNext();) {
				List<Object> component = it.next();
				if (component.size() > 1) {
					result.add(component.toArray());
				}
			}
			return result;
		}

		//		/**
		//		 * Performs a depth-first search of this graph and records interesting
		//		 * info with each vertex, including DFS finish time. Employs a recursive
		//		 * helper method <code>DFSVisit</code>.
		//		 * <p>
		//		 * Although this method is not used, it is the basis of the
		//		 * non-recursive <code>DFS</code> method.
		//		 * </p>
		//		 */
		//		private void recursiveDFS() {
		//			// initialize
		//			// all vertex.color initially Vertex.WHITE;
		//			// all vertex.predecessor initially null;
		//			time = 0;
		//			for (Iterator allV = vertexList.iterator(); allV.hasNext();) {
		//				Vertex nextVertex = (Vertex) allV.next();
		//				if (nextVertex.color == Vertex.WHITE) {
		//					DFSVisit(nextVertex);
		//				}
		//			}
		//		}
		//
		//		/**
		//		 * Helper method. Performs a depth first search of this graph.
		//		 *
		//		 * @param vertex the vertex to visit
		//		 */
		//		private void DFSVisit(Vertex vertex) {
		//			// mark vertex as discovered
		//			vertex.color = Vertex.GREY;
		//			List adj = vertex.adjacent;
		//			for (Iterator allAdjacent=adj.iterator(); allAdjacent.hasNext();) {
		//				Vertex adjVertex = (Vertex) allAdjacent.next();
		//				if (adjVertex.color == Vertex.WHITE) {
		//					// explore edge from vertex to adjVertex
		//					adjVertex.predecessor = vertex;
		//					DFSVisit(adjVertex);
		//				} else if (adjVertex.color == Vertex.GREY) {
		//                  // back edge (grey vertex means visit in progress)
		//                  cycles = true;
		//              }
		//			}
		//			// done exploring vertex
		//			vertex.color = Vertex.BLACK;
		//			time++;
		//			vertex.finishTime = time;
		//		}

		/**
		 * Performs a depth-first search of this graph and records interesting
		 * info with each vertex, including DFS finish time. Does not employ
		 * recursion.
		 */
		@SuppressWarnings({"unchecked"})
		private void DFS() {
			// state machine rendition of the standard recursive DFS algorithm
			int state;
			final int NEXT_VERTEX = 1;
			final int START_DFS_VISIT = 2;
			final int NEXT_ADJACENT = 3;
			final int AFTER_NEXTED_DFS_VISIT = 4;
			// use precomputed objects to avoid garbage
			final Integer NEXT_VERTEX_OBJECT = NEXT_VERTEX;
			final Integer AFTER_NEXTED_DFS_VISIT_OBJECT = AFTER_NEXTED_DFS_VISIT;
			// initialize
			// all vertex.color initially Vertex.WHITE;
			// all vertex.predecessor initially null;
			time = 0;
			// for a stack, append to the end of an array-based list
			List<Object> stack = new ArrayList<>(Math.max(1, vertexList.size()));
			Iterator<Vertex> allAdjacent = null;
			Vertex vertex = null;
			Iterator<Vertex> allV = vertexList.iterator();
			state = NEXT_VERTEX;
			nextStateLoop: while (true) {
				switch (state) {
					case NEXT_VERTEX :
						// on entry, "allV" contains vertexes yet to be visited
						if (!allV.hasNext()) {
							// all done
							break nextStateLoop;
						}
						Vertex nextVertex = allV.next();
						if (nextVertex.color == Vertex.WHITE) {
							stack.add(NEXT_VERTEX_OBJECT);
							vertex = nextVertex;
							state = START_DFS_VISIT;
							continue nextStateLoop;
						}
						//else
						state = NEXT_VERTEX;
						continue nextStateLoop;
					case START_DFS_VISIT :
						// on entry, "vertex" contains the vertex to be visited
						// top of stack is return code
						// mark the vertex as discovered
						vertex.color = Vertex.GREY;
						allAdjacent = vertex.adjacent.iterator();
						state = NEXT_ADJACENT;
						continue nextStateLoop;
					case NEXT_ADJACENT :
						// on entry, "allAdjacent" contains adjacent vertexes to
						// be visited; "vertex" contains vertex being visited
						if (allAdjacent.hasNext()) {
							Vertex adjVertex = allAdjacent.next();
							if (adjVertex.color == Vertex.WHITE) {
								// explore edge from vertex to adjVertex
								adjVertex.predecessor = vertex;
								stack.add(allAdjacent);
								stack.add(vertex);
								stack.add(AFTER_NEXTED_DFS_VISIT_OBJECT);
								vertex = adjVertex;
								state = START_DFS_VISIT;
								continue nextStateLoop;
							}
							if (adjVertex.color == Vertex.GREY) {
								// back edge (grey means visit in progress)
								cycles = true;
							}
							state = NEXT_ADJACENT;
							continue nextStateLoop;
						}
						//else done exploring vertex
						vertex.color = Vertex.BLACK;
						time++;
						vertex.finishTime = time;
						state = ((Integer) stack.remove(stack.size() - 1)).intValue();
						continue nextStateLoop;
					case AFTER_NEXTED_DFS_VISIT :
						// on entry, stack contains "vertex" and "allAjacent"
						vertex = (Vertex) stack.remove(stack.size() - 1);
						allAdjacent = (Iterator<Vertex>) stack.remove(stack.size() - 1);
						state = NEXT_ADJACENT;
						continue nextStateLoop;
				}
			}
		}

	}

	/**
	 * Data structure for holding the multi-part outcome of
	 * <code>ComputeVertexOrder.computeVertexOrder</code>.
	 */
	static final class VertexOrder {
		/**
		 * Creates an instance with the given values.
		 * @param vertexes initial value of <code>vertexes</code> field
		 * @param hasCycles initial value of <code>hasCycles</code> field
		 * @param knots initial value of <code>knots</code> field
		 */
		public VertexOrder(Object[] vertexes, boolean hasCycles, Object[][] knots) {
			this.vertexes = vertexes;
			this.hasCycles = hasCycles;
			this.knots = knots;
		}

		/**
		 * A list of vertexes ordered so as to honor the reference
		 * relationships between them wherever possible.
		 */
		public Object[] vertexes;
		/**
		 * <code>true</code> if any of the vertexes in <code>vertexes</code>
		 * are involved in non-trivial cycles in the reference graph.
		 */
		public boolean hasCycles;
		/**
		 * A list of knots in the reference graph. This list is empty if
		 * the reference graph does not contain cycles. If the reference graph
		 * contains cycles, each element is a knot of two or more vertexes that
		 * are involved in a cycle of mutually dependent references.
		 */
		public Object[][] knots;
	}

	/**
	 * Sorts the given list of vertexes in a manner that honors the given
	 * reference relationships between them. That is, if A references
	 * B, then the resulting order will list B before A if possible.
	 * For graphs that do not contain cycles, the result is the same as a conventional
	 * topological sort. For graphs containing cycles, the order is based on
	 * ordering the strongly connected components of the graph. This has the
	 * effect of keeping each knot of vertexes together without otherwise
	 * affecting the order of vertexes not involved in a cycle. For a graph G,
	 * the algorithm performs in O(|G|) space and time.
	 * <p>
	 * When there is an arbitrary choice, vertexes are ordered as supplied.
	 * If there are no constraints on the order of the vertexes, they are returned
	 * in the reverse order of how they are supplied.
	 * </p>
	 * <p> Ref: Cormen, Leiserson, and Rivest <it>Introduction to
	 * Algorithms</it>, McGraw-Hill, 1990. The strongly-connected-components
	 * algorithm is in section 23.5.
	 * </p>
	 *
	 * @param vertexes a list of vertexes
	 * @param references a list of pairs [A,B] meaning that A references B
	 * @return an object describing the resulting order
	 */
	static VertexOrder computeVertexOrder(SortedSet<? extends Object> vertexes, List<? extends Object[]> references) {

		// Step 1: Create the graph object.
		final Digraph g1 = new Digraph();
		// add vertexes
		for (Iterator<?> it = vertexes.iterator(); it.hasNext();) {
			g1.addVertex(it.next());
		}
		// add edges
		for (Iterator<? extends Object[]> it = references.iterator(); it.hasNext();) {
			Object[] ref = it.next();
			Object p = ref[0];
			Object q = ref[1];
			// p has a reference to q
			// therefore create an edge from q to p
			// to cause q to come before p in eventual result
			g1.addEdge(q, p);
		}
		g1.freeze();

		// Step 2: Create the transposed graph. This time, define the vertexes
		// in decreasing order of depth-first finish time in g1
		// interchange "to" and "from" to reverse edges from g1
		final Digraph g2 = new Digraph();
		// add vertexes
		List<Object> resortedVertexes = g1.idsByDFSFinishTime(false);
		for (Iterator<Object> it = resortedVertexes.iterator(); it.hasNext();) {
			g2.addVertex(it.next());
		}
		// add edges
		for (Iterator<? extends Object[]> it = references.iterator(); it.hasNext();) {
			Object[] ref = it.next();
			Object p = ref[0];
			Object q = ref[1];
			// p has a reference to q
			// therefore create an edge from p to q
			// N.B. this is the reverse of step 1
			g2.addEdge(p, q);
		}
		g2.freeze();

		// Step 3: Return the vertexes in increasing order of depth-first finish
		// time in g2
		List<Object> sortedVertexList = g2.idsByDFSFinishTime(true);
		Object[] orderedVertexes = new Object[sortedVertexList.size()];
		sortedVertexList.toArray(orderedVertexes);
		Object[][] knots;
		boolean hasCycles = g2.containsCycles();
		if (hasCycles) {
			List<Object[]> knotList = g2.nonTrivialComponents();
			knots = new Object[knotList.size()][];
			knotList.toArray(knots);
		} else {
			knots = new Object[][] {};
		}
		return new VertexOrder(orderedVertexes, hasCycles, knots);
	}

	static interface VertexFilter {
		boolean matches(Object vertex);
	}

	/**
	 * Given a VertexOrder and a VertexFilter, remove all vertexes
	 * matching the filter from the ordering.
	 */
	static VertexOrder filterVertexOrder(VertexOrder order, VertexFilter filter) {
		// Optimize common case where nothing is to be filtered
		// and cache the results of applying the filter
		int filteredCount = 0;
		boolean[] filterMatches = new boolean[order.vertexes.length];
		for (int i = 0; i < order.vertexes.length; i++) {
			filterMatches[i] = filter.matches(order.vertexes[i]);
			if (filterMatches[i])
				filteredCount++;
		}

		// No vertexes match the filter, so return the order unmodified
		if (filteredCount == 0) {
			return order;
		}

		// Otherwise we need to eliminate mention of vertexes matching the filter
		// from the list of vertexes
		Object[] reducedVertexes = new Object[order.vertexes.length - filteredCount];
		for (int i = 0, j = 0; i < order.vertexes.length; i++) {
			if (!filterMatches[i]) {
				reducedVertexes[j] = order.vertexes[i];
				j++;
			}
		}

		// and from the knots list
		List<Object[]> reducedKnots = new ArrayList<>(order.knots.length);
		for (int i = 0; i < order.knots.length; i++) {
			Object[] knot = order.knots[i];
			List<Object> knotList = new ArrayList<>(knot.length);
			for (int j = 0; j < knot.length; j++) {
				Object vertex = knot[j];
				if (!filter.matches(vertex)) {
					knotList.add(vertex);
				}
			}
			// Keep knots containing 2 or more vertexes in the specified subset
			if (knotList.size() > 1) {
				reducedKnots.add(knotList.toArray());
			}
		}

		return new VertexOrder(reducedVertexes, reducedKnots.size() > 0, reducedKnots.toArray(new Object[reducedKnots.size()][]));
	}
}
