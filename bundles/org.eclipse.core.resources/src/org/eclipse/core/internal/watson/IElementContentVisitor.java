package org.eclipse.core.internal.watson;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
/**
 * An interface for objects which can visit an element of
 * an element tree and access that element's node info.
 * @see ElementTreeIterator
 */
public interface IElementContentVisitor {
/** Visits a node (element).
 * <p> Note that <code>elementContents</code> is equal to
 * <code>tree.getElement(elementPath)</code> but takes no time.
 * @param tree the element tree being visited
 * @param elementPath the path of the node being visited on this call
 * @param elementContents the object at the node being visited on this call
 */
public void visitElement(ElementTree tree, IPath elementPath, Object elementContents);
}
