package org.eclipse.core.internal.dtree;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import java.io.DataInput;
import java.io.IOException;
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
		case AbstractDataTreeNode.T_COMPLETE_NODE:
		case AbstractDataTreeNode.T_DELTA_NODE:
			return true;
		case AbstractDataTreeNode.T_DELETED_NODE:
		case AbstractDataTreeNode.T_NO_DATA_DELTA_NODE:
		default:
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
	Object data = null;

	/* if not the root node */
	if (parentPath != null) {
		path = parentPath.append(name);
	} else {
		path = Path.ROOT;
	}
		
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
	AbstractDataTreeNode[] children = new AbstractDataTreeNode[childCount];
	for (int i = 0; i < childCount; i++) {
		children[i] = readNode(path);
	}

	/* create the appropriate node */
	switch (nodeType) {
		case AbstractDataTreeNode.T_COMPLETE_NODE:
			return new DataTreeNode(name, data, children);
		case AbstractDataTreeNode.T_DELTA_NODE:
			return new DataDeltaNode(name, data, children);
		case AbstractDataTreeNode.T_DELETED_NODE:
			return new DeletedNode(name);
		case AbstractDataTreeNode.T_NO_DATA_DELTA_NODE:
			return new NoDataDeltaNode(name, children);
		default:
			Assert.isTrue(false, Policy.bind("dtree.switchError"));
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
	byte b= input.readByte();
	int number= (b & 0xff); // not a no-op! converts unsigned byte to int

	if (number==0xff) { // magic escape value
		number= input.readInt();
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
