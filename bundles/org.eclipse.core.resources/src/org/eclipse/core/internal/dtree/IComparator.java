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
package org.eclipse.core.internal.dtree;

/**
 * An interface for comparing two data tree objects.  Provides information
 * on how an object has changed from one tree to another.
 */
public interface IComparator {
/**
 * Returns an integer describing the changes between two data objects
 * in a data tree.  The first three bits of the returned integer are 
 * used during calculation of delta trees.  The remaining bits can be 
 * assigned any meaning that is useful to the client.  If there is no 
 * change in the two data objects, this method must return 0.
 *
 * @see NodeComparison
 */
int compare(Object o1, Object o2);
}
