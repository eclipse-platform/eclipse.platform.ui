/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.tests.views.properties.tabbed.model.Error;
import org.eclipse.ui.tests.views.properties.tabbed.model.File;
import org.eclipse.ui.tests.views.properties.tabbed.model.Folder;
import org.eclipse.ui.tests.views.properties.tabbed.model.Information;
import org.eclipse.ui.tests.views.properties.tabbed.model.Project;
import org.eclipse.ui.tests.views.properties.tabbed.model.Warning;

public class TestsViewContentProvider
    implements IStructuredContentProvider, ITreeContentProvider {

    /**
     * 
     */
    private final TestsView view;

    /**
     * @param view
     */
    TestsViewContentProvider(TestsView aView) {
        this.view = aView;
    }

    private TreeNode invisibleRoot;

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        //
    }

    public void dispose() {
        //
    }

    public Object[] getElements(Object parent) {
        if (parent.equals(this.view.getViewSite())) {
            if (invisibleRoot == null)
                initialize();
            return getChildren(invisibleRoot);
        }
        return getChildren(parent);
    }

    public Object getParent(Object child) {
        if (child instanceof TreeNode) {
            return ((TreeNode) child).getParent();
        }
        return null;
    }

    public Object[] getChildren(Object parent) {
        if (parent instanceof TreeNode) {
            return ((TreeNode) parent).getChildren();
        }
        return new Object[0];
    }

    public boolean hasChildren(Object parent) {
        if (parent instanceof TreeNode)
            return ((TreeNode) parent).hasChildren();
        return false;
    }

    /*
     * We will set up a dummy model to initialize tree heararchy. In a real
     * code, you will connect to a real model and expose its hierarchy.
     */
    void initialize() {
        TreeNode[] nodes = new TreeNode[] {
            new TreeNode(new Information("Informational Message One")),//$NON-NLS-1$
            new TreeNode(new Information("Informational Message Two")),//$NON-NLS-1$
            new TreeNode(new Error("Error Message One")),//$NON-NLS-1$
            new TreeNode(new Warning("Warning Message One")),//$NON-NLS-1$
            new TreeNode(new File("file.txt")),//$NON-NLS-1$
            new TreeNode(new File("another.txt")),//$NON-NLS-1$
            new TreeNode(new Folder("folder")),//$NON-NLS-1$
            new TreeNode(new Project("project"))};//$NON-NLS-1$
        invisibleRoot = new TreeNode(new Project(""));//$NON-NLS-1$
        invisibleRoot.setChildren(nodes);
    }

    public TreeNode getInvisibleRoot() {
        return invisibleRoot;
    }
}