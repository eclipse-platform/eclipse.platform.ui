/**********************************************************************
 * Copyright (c) 2002, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.watson;

import org.eclipse.core.runtime.IPath;

/**
 * An interface for objects which can visit an element of an element tree and
 * access that element's node info, along with the element's path
 * @see ElementTreeIterator
 */
public interface IElementPathContentVisitor {
/** Visits a node (element).
 * <p> Note that <code>elementContents</code> is equal to<code>tree.
 * getElement(elementPath)</code> but takes no time.
 * @param tree the element tree being visited
 * @param elementContents the object at the node being visited on this call
 * @param path the path of the object being visited
 * @return true if this element's children should be visited, and false
 * otherwise.
 */
public boolean visitElement(ElementTree tree, IPath path, Object elementContents);
}

