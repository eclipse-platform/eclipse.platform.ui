/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Breakpoints viewer.
 */
public class BreakpointsViewer extends CheckboxTreeViewer {

    /**
     * Constructs a new breakpoints viewer with the given tree.
     * 
     * @param tree
     */
    public BreakpointsViewer(Tree tree) {
        super(tree);
    }
    
    public Widget searchItem(Object element) {
        return findItem(element);
    }
    
    public void refreshItem(TreeItem item) {
        updateItem(item, item.getData());
    }

}
