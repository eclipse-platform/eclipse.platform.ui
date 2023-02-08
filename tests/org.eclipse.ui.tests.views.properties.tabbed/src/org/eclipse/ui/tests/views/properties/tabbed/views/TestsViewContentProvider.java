/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.tests.views.properties.tabbed.model.Error;
import org.eclipse.ui.tests.views.properties.tabbed.model.File;
import org.eclipse.ui.tests.views.properties.tabbed.model.Folder;
import org.eclipse.ui.tests.views.properties.tabbed.model.Information;
import org.eclipse.ui.tests.views.properties.tabbed.model.Project;
import org.eclipse.ui.tests.views.properties.tabbed.model.Warning;

public class TestsViewContentProvider implements ITreeContentProvider {

	private final TestsView view;

	/**
	 * @param view
	 */
	TestsViewContentProvider(TestsView aView) {
		this.view = aView;
	}

	private TreeNode invisibleRoot;

	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		//
	}

	@Override
	public void dispose() {
		//
	}

	@Override
	public Object[] getElements(Object parent) {
		if (parent.equals(this.view.getViewSite())) {
			if (invisibleRoot == null)
				initialize();
			return getChildren(invisibleRoot);
		}
		return getChildren(parent);
	}

	@Override
	public Object getParent(Object child) {
		if (child instanceof TreeNode treeNode) {
			return treeNode.getParent();
		}
		return null;
	}

	@Override
	public Object[] getChildren(Object parent) {
		if (parent instanceof TreeNode treeNode) {
			return treeNode.getChildren();
		}
		return new Object[0];
	}

	@Override
	public boolean hasChildren(Object parent) {
		if (parent instanceof TreeNode treeNode)
			return treeNode.hasChildren();
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