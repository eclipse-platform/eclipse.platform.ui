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
 * Interface for visiting the nodes of an element tree delta.
 * @see DeltaIterator
 */ 
public interface IDeltaVisitor {
/**
 * Visits an element in an element tree delta.  Returns true if the element's
 * children should be visited, and false otherwise.  The return value only
 * has signifance for a pre-order traversal.
 *
 * @param tree The Element tree delta being visited
 * @param path The path of the current element
 * @param oldData The element's data in the old tree
 * @param newData The element's data in the new tree
 * @param comparison The comparison value between the old and new tree
 */
public boolean visitElement(ElementTreeDelta tree, IPath path, Object oldData, Object newData, int comparison);
}
