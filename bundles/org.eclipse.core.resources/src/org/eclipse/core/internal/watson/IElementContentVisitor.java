package org.eclipse.core.internal.watson;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
