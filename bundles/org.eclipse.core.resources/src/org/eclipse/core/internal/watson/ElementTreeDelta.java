/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.watson;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.internal.dtree.*;
import java.util.*;
/**
 * Describes the differences between two element trees.  
 * Specifically, an ElementTreeDelta describes the changes that 
 * have been made to the tree returned by getParent() to obtain the 
 * tree returned by getElementTree().
 *
 * ElementTreeDeltas are meant to be treated as light-weight query structures
 * with a relatively short lifespan.  Deltas cannot be serialized.
 *
 * ElementTreeDeltas support pluggable logic for delta calculation.  By
 * implementing the IElementComparator interface, the client can specify
 * the kind of change (addition, removal, etc) to any given element based
 * on the element's data.
 *
 * There are two ways of navigating ElementTreeDeltas.  First, one
 * can get a handle on an ElementDelta using the getElementDelta() method.
 * The ElementDelta can then be queried for its children, using a filter
 * interface called IDeltaFilter.  Clients can create implementations of
 * IDeltaFilter to navigate deltas based on various characteristics of
 * The delta elements.
 * @see IDeltaFilter
 * @see IElementComparator
 *
 * The second way of navigating element tree deltas is using the DeltaIterator.
 * The client provides an implementation of IDeltaVisitor that specifies
 * the code to be executed for each element of the delta.
 * @see DeltaIterator
 * @see IDeltaVisitor
 *
 * @see ElementTree.computeDeltaWith(...)
 */
public class ElementTreeDelta {

	protected IElementComparator comparator;
	protected DeltaDataTree deltaTree;
	protected ElementTree elementTree;
	protected ElementTree parent;

	/**
	 * Path of the root of the subtree that this delta is for.
	 */
	protected IPath rootPath;
/** 
 * Creates a delta describing the changes between the two given trees.
 */
ElementTreeDelta(ElementTree parent, ElementTree elementTree, IElementComparator comparator) {
	initialize(parent, elementTree, comparator);
	deltaTree = elementTree.getDataTree().compareWith(parent.getDataTree(), comparator).asReverseComparisonTree(comparator);
	rootPath = Path.ROOT;
}
/** 
 * Creates a delta describing the changes between the two given trees, starting
 * at the given path.
 */
ElementTreeDelta(ElementTree parent, ElementTree elementTree, IElementComparator comparator, IPath path) {
	initialize(parent, elementTree, comparator);
	deltaTree = parent.getDataTree().compareWith(elementTree.getDataTree(), comparator, path);
	rootPath = path;
}
/**
 * Destroys this delta and drops references to all trees deltas referenced herein.
 */
public void destroy() {
	comparator = null;
	deltaTree = null;
	elementTree = null;
	parent = null;
	rootPath = null;
}
/**
 * Returns deltas describing the children of the specified element that
 * match the given filter query. Must be called only when parentID 
 * represents a changed element, not an added or removed element.
 */
protected ElementDelta[] getAffectedElements(IPath parentID, IDeltaFilter filter) {
	IPath parentKey;
	if (parentID == null) {
		parentKey = deltaTree.rootKey();
	} else {
		parentKey = parentID;
	}
	Vector v = new Vector();
	IPath[] childKeys = deltaTree.getChildren(parentKey);
	
	for (int i = 0; i < childKeys.length; ++i) {
		IPath key = childKeys[i];

		/* return delta info based on user comparison */
		NodeComparison nodeComparison = (NodeComparison) deltaTree.getData(key);
		int userComparison = nodeComparison.getUserComparison();
		if (filter.includeElement(userComparison)) {
			v.addElement(new ElementDelta(this, rootPath.append(key), key, nodeComparison));
		}
	}

	ElementDelta[] result = new ElementDelta[v.size()];
	v.copyInto(result);
	return result;
}
/**
 * Returns the delta tree
 */
/*package*/ DeltaDataTree getDeltaTree() {
	return deltaTree;
}
/**
 * Returns the delta for the specified element, or null if the
 * element is not affected in this delta.
 */
public ElementDelta getElementDelta(IPath key) {

	if (key == null) {
		throw new IllegalArgumentException();
	}

	try {
		NodeComparison nodeComparison = (NodeComparison) deltaTree.getData(key);
		return new ElementDelta(this, rootPath.append(key), key, nodeComparison);
	}
	catch (ObjectNotFoundException e) {
		return null;
	}
}
/**
 * Returns the element tree which this delta describes,
 * when its changes are applied to the parent tree.
 * It is also referred to as the 'new' tree.
 */
public ElementTree getElementTree() {
	return elementTree;
}
/**
 * Returns the element tree that this delta is based on.
 * It is also referred to as the 'old' tree.
 */
public ElementTree getParent() {
	return parent;
}
/**
 * Return true if there are deltas describing affected children of the specified element.
 * Must be called only when parentID represents a changed element, not an added or removed element.
 */
protected boolean hasAffectedElements(IPath parentID, IDeltaFilter filter) {
	IPath parentKey = parentID == null ? deltaTree.rootKey() : parentID;

	IPath[] childKeys = deltaTree.getChildren(parentKey);
	for (int i = 0; i < childKeys.length; ++i) {
		NodeComparison nodeComparison = (NodeComparison) deltaTree.getData(childKeys[i]);
		if (filter.includeElement(nodeComparison.getUserComparison())) {
			return true;
		}
	}
	return false;
}
/** 
 * Initializes the tree delta
 */
private void initialize(ElementTree parent, ElementTree elementTree, IElementComparator comparator) {
	parent.immutable();
	elementTree.immutable();
	this.parent = parent;
	this.elementTree = elementTree;
	this.comparator = comparator;
}
}
