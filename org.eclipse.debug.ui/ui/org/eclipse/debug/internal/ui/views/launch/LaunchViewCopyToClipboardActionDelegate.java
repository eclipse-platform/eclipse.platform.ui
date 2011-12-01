/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.debug.internal.ui.viewers.model.InternalTreeModelViewer;
import org.eclipse.debug.internal.ui.viewers.model.VirtualCopyToClipboardActionDelegate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * @since 3.5
 */
public class LaunchViewCopyToClipboardActionDelegate extends VirtualCopyToClipboardActionDelegate {

    protected TreeItem[] getSelectedItems(TreeModelViewer clientViewer) {
        LaunchView view = (LaunchView)getView();
        if (view.isBreadcrumbVisible()) {
            ISelection selection = getSelection();
            if (selection instanceof ITreeSelection && getViewer() instanceof InternalTreeModelViewer) {
                TreePath path = TreePath.EMPTY;
                if (!selection.isEmpty()) {
                    path = ((ITreeSelection)selection).getPaths()[0];
                }
                return getSelectedItemsInTreeViewer((TreeModelViewer)getViewer(), path);
            }
            return new TreeItem[0];
        } else {
            return super.getSelectedItems(clientViewer);
        }
    }
    
    private TreeItem[] getSelectedItemsInTreeViewer(TreeModelViewer viewer, TreePath path) {
        Widget item = viewer.findItem(path);
        if (item instanceof TreeItem) {
            return new TreeItem[] { (TreeItem)item };
        } else if (item instanceof Tree) {
            return ((Tree)item).getItems();
        } 
        return new TreeItem[0];
    }
}
