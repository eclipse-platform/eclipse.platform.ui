/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.watson;

import java.io.DataInput;
import java.io.IOException;
import org.eclipse.core.internal.dtree.DeltaDataTree;

/** <code>ElementTreeReader_1</code> is an implementation
 * of the <code>ElementTreeReader</code> for format version 1.
 *
 * <p>Instances of this reader read only format 1
 * of a saved element tree (they do not deal with
 * compatibility issues).
 *
 * @see ElementTreeReader
 */
/* package */class ElementTreeReaderImpl_1 extends ElementTreeReader {

	/** 
	 * Constructs a new element tree reader that works for
	 * the given element info factory.
	 */
	ElementTreeReaderImpl_1(IElementInfoFlattener factory) {
		super(factory);
	}

	/**
	 * Reads an element tree delta from the input stream, and
	 * reconstructs it as a delta on the given tree.
	 */
	public ElementTree readDelta(ElementTree parentTree, DataInput input) throws IOException {
		DeltaDataTree complete = parentTree.getDataTree();
		DeltaDataTree delta = dataTreeReader.readTree(complete, input);

		//if the delta is empty, just return the parent
		if (delta.isEmptyDelta())
			return parentTree;

		ElementTree tree = new ElementTree(delta);

		//copy the user data forward
		IElementTreeData data = parentTree.getTreeData();
		if (data != null) {
			tree.setTreeData((IElementTreeData) data.clone());
		}

		//make the underlying data tree immutable
		//can't call immutable() on the ElementTree because
		//this would attempt to reroot.
		delta.immutable();
		return tree;
	}

	/**
	 * Reads a chain of ElementTrees from the given input stream.
	 * @return A chain of ElementTrees, where the first tree in the list is
	 * complete, and all other trees are deltas on the previous tree in the list.
	 */
	public ElementTree[] readDeltaChain(DataInput input) throws IOException {
		/* read the number of trees */
		int treeCount = readNumber(input);
		ElementTree[] results = new ElementTree[treeCount];

		if (treeCount <= 0) {
			return results;
		}

		/* read the sort order */
		int[] order = new int[treeCount];
		for (int i = 0; i < treeCount; i++) {
			order[i] = readNumber(input);
		}

		/* read the complete tree */
		results[order[0]] = super.readTree(input);

		/* reconstitute each of the remaining trees from their written deltas */
		for (int i = 1; i < treeCount; i++) {
			results[order[i]] = super.readDelta(results[order[i - 1]], input);
		}

		return results;
	}

	/** Part of <code>ElementTreeReader</code> interface.
	 * @see ElementTreeReader
	 */
	public ElementTree readTree(DataInput input) throws IOException {

		/* The format version number has already been consumed
		 * by ElementTreeReader#readFrom.
		 */
		ElementTree result = new ElementTree(dataTreeReader.readTree(null, input));
		return result;
	}
}
