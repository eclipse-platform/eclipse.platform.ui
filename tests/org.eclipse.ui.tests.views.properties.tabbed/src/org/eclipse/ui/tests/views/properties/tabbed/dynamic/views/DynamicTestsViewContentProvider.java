/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.dynamic.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.tests.views.properties.tabbed.dynamic.model.DynamicTestsElement;

/**
 * A content provider for nodes in the tree of the dynamic tests view.
 *
 * @author Anthony Hunter
 */
public class DynamicTestsViewContentProvider implements
		IStructuredContentProvider, ITreeContentProvider {

	private DynamicTestsTreeNode invisibleRoot;

	private final DynamicTestsView view;

	public DynamicTestsViewContentProvider(DynamicTestsView dynamicTestsView) {
		super();
		this.view = dynamicTestsView;
	}

	@Override
	public void dispose() {
		//
	}

	@Override
	public Object[] getChildren(Object parent) {
		if (parent instanceof DynamicTestsTreeNode) {
			return ((DynamicTestsTreeNode) parent).getChildren();
		}
		return new Object[0];
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

	public DynamicTestsTreeNode getInvisibleRoot() {
		return invisibleRoot;
	}

	@Override
	public Object getParent(Object child) {
		if (child instanceof DynamicTestsTreeNode) {
			return ((DynamicTestsTreeNode) child).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object parent) {
		if (parent instanceof DynamicTestsTreeNode)
			return ((DynamicTestsTreeNode) parent).hasChildren();
		return false;
	}

	public void initialize() {
		String elements[] = new String[] { "icons/black_triangle.gif",//$NON-NLS-1$
				"icons/blue_circle.gif",//$NON-NLS-1$
				"icons/blue_square.gif",//$NON-NLS-1$
				"icons/blue_triangle.gif",//$NON-NLS-1$
				"icons/green_circle.gif",//$NON-NLS-1$
				"icons/green_square.gif",//$NON-NLS-1$
				"icons/green_triangle.gif",//$NON-NLS-1$
				"icons/red_circle.gif",//$NON-NLS-1$
				"icons/red_square.gif",//$NON-NLS-1$
				"icons/red_star.gif",//$NON-NLS-1$
				"icons/red_triangle.gif" };//$NON-NLS-1$
		DynamicTestsTreeNode[] nodes = new DynamicTestsTreeNode[elements.length];
		for (int i = 0; i < elements.length; i++) {
			DynamicTestsElement dynamicTestsElement = new DynamicTestsElement(
					view, elements[i]);
			nodes[i] = new DynamicTestsTreeNode(dynamicTestsElement);
		}
		invisibleRoot = new DynamicTestsTreeNode(null);
		invisibleRoot.setChildren(nodes);
	}

	@Override
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		//
	}
}