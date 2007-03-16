/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.internal.viewers;

/**
 * @since 3.3
 *
 */
public interface IPrefetchingTree {
    /**
     * Returns true if and only if the content provider should
     * try to prefetch the children of the given node.
     * Prefetching uses unused CPU cycles to fetch the children 
     * of visible nodes so that they expand faster. This will 
     * generally cause the application to run faster so should 
     * usually be enabled. 
     * <p> 
     * In some circumstances computing the children of a node may 
     * require network resources that need to be conserved, so 
     * prefetching can be explicitly disabled these nodes. This
     * means that the user will need to wait for a "pending" node
     * every time they expand the parent node.
     * </p> 
     * 
     * @param parentNode
     * @return true iff the children should be eagerly fetched
     */
    boolean shouldPrefetch(Object parentNode);
}
