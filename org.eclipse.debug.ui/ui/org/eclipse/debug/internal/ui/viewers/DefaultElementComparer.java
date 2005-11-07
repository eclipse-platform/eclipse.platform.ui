/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.jface.viewers.IElementComparer;

/**
 * Default comparator used to compare individual elements in a tree path.
 *
 * @since 3.2
 * @see org.eclipse.debug.internal.ui.treeviewer.TreePath
 */
public class DefaultElementComparer implements IElementComparer {
    
    public static final DefaultElementComparer INSTANCE= new DefaultElementComparer();
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IElementComparer#equals(java.lang.Object, java.lang.Object)
     */
    public boolean equals(Object a, Object b) {
        return a.equals(b);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IElementComparer#hashCode(java.lang.Object)
     */
    public int hashCode(Object element) {
        return element.hashCode();
    }
}

