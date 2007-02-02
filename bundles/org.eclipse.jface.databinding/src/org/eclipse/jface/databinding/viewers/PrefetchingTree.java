/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.databinding.viewers;



/* package */ class PrefetchingTree implements IPrefetchingTree {

    private static IPrefetchingTree instance;

    private PrefetchingTree() {
    }

    public boolean shouldPrefetch(Object parentNode) {
        return true;
    }
    
    /**
     * @param treeProvider
     * @return a prefetching tree
     */
    public static IPrefetchingTree getPrefetchingTree(Object treeProvider) {
        if (treeProvider instanceof IPrefetchingTree) {
            return (IPrefetchingTree)treeProvider;
        }
        if (instance == null) {
            instance = new PrefetchingTree();
        }
        return instance;
    }

}
