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
package org.eclipse.core.internal.dtree;

import java.io.DataInput;
import java.io.IOException;
import org.eclipse.core.internal.utils.Messages;
import org.eclipse.core.runtime.*;

/**
 * Class used for reading a single data tree (no parents) from an input stream
 */
public class DataTreeReader {
	/**
	 * Callback for reading tree data
	 */
	protected IDataFlattener flatener;

	/**
	 * The stream to read the tree from
	 */
	protected DataInput input;

	/**
	 * Creates a new DeltaTreeReader.
	 */
	public DataTreeReader(IDataFlattener f) {
		flatener = f;
	}

	/**
	 * Returns true if the given node type has data.
	 */
	protected boolean hasData(int nodeType) {
		switch (nodeType) {
			case AbstractDataTreeNode.T_COMPLETE_NODE :
			case AbstractDataTreeNode.T_DELTA_NODE :
				return true;
			case AbstractDataTreeNode.T_DELETED_NODE :
			case AbstractDataTreeNode.T_NO_DATA_DELTA_NODE :
			default :
				return false;
		}
	}

	/**
	 * Reads a node from the given input stream
	 */
	protected AbstractDataTreeNode readNode(IPath parentPath) throws IOException {
		/* read the node name */
		String name = input.readUTF();

		/* read the node type */
		int nodeType = readNumber();

		/* maybe read the data */
		IPath path;

		/* if not the root node */
		if (parentPath != null) {
			path = parentPath.append(name);
		} else {
			path = Path.ROOT;
		}

		Object data = null;
		if (hasData(nodeType)) {

			/* read flag indicating if the data is null */
			int dataFlag = readNumber();
			if (dataFlag != 0) {
				data = flatener.readData(path, input);
			}
		}

		/* read the number of children */
		int childCount = readNumber();

		/* read the children */
		AbstractDataTreeNode[] children;
		if (childCount == 0) {
			children = AbstractDataTreeNode.NO_CHILDREN;
		} else {
			children = new AbstractDataTreeNode[childCount];
			for (int i = 0; i < childCount; i++) {
				children[i] = readNode(path);
			}
		}

		/* create the appropriate node */
		switch (nodeType) {
			case AbstractDataTreeNode.T_COMPLETE_NODE :
				return new DataTreeNode(name, data, children);
			case AbstractDataTreeNode.T_DELTA_NODE :
				return new DataDeltaNode(name, data, children);
			case AbstractDataTreeNode.T_DELETED_NODE :
				return new DeletedNode(name);
			case AbstractDataTreeNode.T_NO_DATA_DELTA_NODE :
				return new NoDataDeltaNode(name, children);
			default :
				Assert.isTrue(false, Messages.dtree_switchError);
				return null;
		}
	}

	/** 
	 * Reads an integer stored in compact format.  Numbers between
	 * 0 and 254 inclusive occupy 1 byte; other numbers occupy 5 bytes,
	 * the first byte being 0xff and the next 4 bytes being the standard
	 * representation of an int.
	 */
	protected int readNumber() throws IOException {
		byte b = input.readByte();
		int number = (b & 0xff); // not a no-op! converts unsigned byte to int

		if (number == 0xff) { // magic escape value
			number = input.readInt();
		}
		return number;
	}

	/**
	 * Reads a DeltaDataTree from the given input stream
	 */
	public DeltaDataTree readTree(DeltaDataTree parent, DataInput input) throws IOException {
		this.input = input;
		AbstractDataTreeNode root = readNode(Path.ROOT);
		return new DeltaDataTree(root, parent);
	}
}
