/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 473427
 *     Mickael Istria (Red Hat Inc.) - Bug 488937
 *******************************************************************************/
package org.eclipse.core.internal.watson;

import java.io.*;
import java.util.*;
import org.eclipse.core.internal.dtree.*;
import org.eclipse.core.runtime.*;

/** <code>ElementTreeWriter</code> flattens an ElementTree
 * onto a data output stream.
 *
 * <p>This writer generates the most up-to-date format
 * of a saved element tree (cf. readers, which must usually also
 * deal with backward compatibility issues).  The flattened
 * representation always includes a format version number.
 *
 * <p>The writer has an <code>IElementInfoFactory</code>,
 * which it consults for writing element infos.
 *
 * <p>Element tree writers are thread-safe; several
 * threads may share a single writer.
 *
 */
public class ElementTreeWriter {
	/**
	 * The current format version number.
	 */
	public static final int CURRENT_FORMAT = 1;

	/**
	 * Constant representing infinite depth
	 */
	public static final int D_INFINITE = DataTreeWriter.D_INFINITE;

	/**
	 * For writing DeltaDataTrees
	 */
	protected DataTreeWriter dataTreeWriter;

	/**
	 * Constructs a new element tree writer that works for
	 * the given element info flattener.
	 */
	public ElementTreeWriter(final IElementInfoFlattener flattener) {

		/* wrap the IElementInfoFlattener in an IDataFlattener */
		IDataFlattener f = new IDataFlattener() {
			@Override
			public void writeData(IPath path, Object data, DataOutput output) throws IOException {
				// never write the root node of an ElementTree
				//because it contains the parent backpointer.
				if (!Path.ROOT.equals(path)) {
					flattener.writeElement(path, data, output);
				}
			}

			@Override
			public Object readData(IPath path, DataInput input) {
				return null;
			}
		};
		dataTreeWriter = new DataTreeWriter(f);
	}

	/**
	 * Sorts the given array of trees so that the following rules are true:
	 * 	 - The first tree has no parent
	 * 	 - No tree has an ancestor with a greater index in the array.
	 * If there are no missing parents in the given trees array, this means
	 * that in the resulting array, the i'th tree's parent will be tree i-1.
	 * The input tree array may contain duplicate trees.
	 * The sort order is written to the given output stream.
	 */
	protected ElementTree[] sortTrees(ElementTree[] trees, DataOutput output) throws IOException {

		/* the sorted list */
		int numTrees = trees.length;
		ElementTree[] sorted = new ElementTree[numTrees];
		int[] order = new int[numTrees];

		/* first build a table of ElementTree -> Vector of Integers(indices in trees array) */
		HashMap<ElementTree, List<Integer>> table = new HashMap<>(numTrees * 2 + 1);
		for (int i = 0; i < trees.length; i++) {
			List<Integer> indices = table.get(trees[i]);
			if (indices == null) {
				indices = new ArrayList<>();
				table.put(trees[i], indices);
			}
			indices.add(i);
		}

		/* find the oldest tree (a descendent of all other trees) */
		ElementTree oldest = trees[ElementTree.findOldest(trees)];

		/**
		 * Walk through the chain of trees from oldest to newest,
		 * adding them to the sorted list as we go.
		 */
		int i = numTrees - 1;
		while (i >= 0) {
			/* add all instances of the current oldest tree to the sorted list */
			List<Integer> indices = table.remove(oldest);
			for (Enumeration<Integer> e = Collections.enumeration(indices); e.hasMoreElements();) {
				Integer next = e.nextElement();
				sorted[i] = oldest;
				order[i] = next.intValue();
				i--;
			}
			if (i >= 0) {
				/* find the next tree in the list */
				ElementTree parent = oldest.getParent();
				while (table.get(parent) == null) {
					parent = parent.getParent();
				}
				oldest = parent;
			}
		}

		/* write the order array */
		for (i = 0; i < numTrees; i++) {
			writeNumber(order[i], output);
		}
		return sorted;
	}

	/**
	 * Writes the delta describing the changes that have to be made
	 * to newerTree to obtain olderTree.
	 *
	 * @param path The path of the subtree to write.  All nodes on the path above
	 *  the subtree are represented as empty nodes.
	 * @param depth The depth of the subtree to write.  A depth of zero writes a
	 *  single node, and a depth of D_INFINITE writes the whole subtree.
	 * @param output The stream to write the subtree to.
	 */
	public void writeDelta(ElementTree olderTree, ElementTree newerTree, IPath path, int depth, final DataOutput output, IElementComparator comparator) throws IOException {

		/* write the version number */
		writeNumber(CURRENT_FORMAT, output);

		/**
		 * Note that in current ElementTree usage, the newest
		 * tree is the complete tree, and older trees are just
		 * deltas on the new tree.
		 */
		DeltaDataTree completeTree = newerTree.getDataTree();
		DeltaDataTree derivedTree = olderTree.getDataTree();
		DeltaDataTree deltaToWrite = null;

		deltaToWrite = completeTree.forwardDeltaWith(derivedTree, comparator);

		Assert.isTrue(deltaToWrite.isImmutable());
		dataTreeWriter.writeTree(deltaToWrite, path, depth, output);
	}

	/**
	 * Writes an array of ElementTrees to the given output stream.
	 * @param trees A chain of ElementTrees, where on tree in the list is
	 * complete, and all other trees are deltas on the previous tree in the list.
	 * @param path The path of the subtree to write.  All nodes on the path above
	 *  the subtree are represented as empty nodes.
	 * @param depth The depth of the subtree to write.  A depth of zero writes a
	 *  single node, and a depth of D_INFINITE writes the whole subtree.
	 * @param output The stream to write the subtree to.

	 */
	public void writeDeltaChain(ElementTree[] trees, IPath path, int depth, DataOutput output, IElementComparator comparator) throws IOException {
		/* Write the format version number */
		writeNumber(CURRENT_FORMAT, output);

		/* Write the number of trees */
		int treeCount = trees.length;
		writeNumber(treeCount, output);

		if (treeCount <= 0) {
			return;
		}

		/**
		 * Sort the trees in ancestral order,
		 * which writes the tree order to the output
		 */
		ElementTree[] sortedTrees = sortTrees(trees, output);

		/* Write the complete tree */
		writeTree(sortedTrees[0], path, depth, output);

		/* Write the deltas for each of the remaining trees */
		for (int i = 1; i < treeCount; i++) {
			writeDelta(sortedTrees[i], sortedTrees[i - 1], path, depth, output, comparator);
		}
	}

	/**
	 * Writes an integer in a compact format biased towards
	 * small non-negative numbers. Numbers between
	 * 0 and 254 inclusive occupy 1 byte; other numbers occupy 5 bytes.
	 */
	protected void writeNumber(int number, DataOutput output) throws IOException {
		if (number >= 0 && number < 0xff) {
			output.writeByte(number);
		} else {
			output.writeByte(0xff);
			output.writeInt(number);
		}
	}

	/**
	 * Writes all or some of an element tree to an output stream.
	 * This always writes the most current version of the element tree
	 * file format, whereas the reader supports multiple versions.
	 *
	 * @param tree The tree to write
	 * @param path The path of the subtree to write.  All nodes on the path above
	 *  the subtree are represented as empty nodes.
	 * @param depth The depth of the subtree to write.  A depth of zero writes a
	 *  single node, and a depth of D_INFINITE writes the whole subtree.
	 * @param output The stream to write the subtree to.
	 */
	public void writeTree(ElementTree tree, IPath path, int depth, final DataOutput output) throws IOException {

		/* Write the format version number. */
		writeNumber(CURRENT_FORMAT, output);

		/* This actually just copies the root node, which is what we want */
		DeltaDataTree subtree = new DeltaDataTree(tree.getDataTree().copyCompleteSubtree(Path.ROOT));

		dataTreeWriter.writeTree(subtree, path, depth, output);
	}
}
