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
