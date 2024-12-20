/*******************************************************************************
 * Copyright (c) 2024 DSA GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     DSA GmbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.BorderData;
import org.eclipse.swt.layout.BorderLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A simple example to illustrate how to preserve the expand/collapse state when
 * using a virtual viewer.
 */
public class Snippet070VirtualViewerWithExpandedState {
	public static class SimpleNode extends TreeNode {
		public SimpleNode(Object value) {
			super(value);
		}

		public int getChildCount() {
			SimpleNode[] children = getChildren();
			return children == null ? 0 : children.length;
		}

		public void setParentChildren(SimpleNode... children) {
			for (TreeNode child : children) {
				child.setParent(this);
			}
			setChildren(children);
		}

		@Override
		public SimpleNode[] getChildren() {
			return (SimpleNode[]) super.getChildren();
		}

		@Override
		public String toString() {
			return "[%s], %s".formatted(getClass().getSimpleName(), getValue());
		}
	}

	public static class LazyContentProvider implements ILazyTreeContentProvider {
		private final TreeViewer viewer;

		public LazyContentProvider(TreeViewer viewer) {
			this.viewer = viewer;
		}
		@Override
		public void updateElement(Object parent, int index) {
			SimpleNode[] children = ((SimpleNode) parent).getChildren();
			if (children == null) {
				// viewer.setHasChildren(parent, false);
			} else if (children.length > index) {
				SimpleNode child = children[index];
				viewer.replace(parent, index, child);
				viewer.setChildCount(child, child.getChildCount());
			} else {
				Platform.getLog(getClass()).warn("Invalid index value %d >= %d".formatted(index, children.length));
			}
		}

		@Override
		public void updateChildCount(Object element, int currentChildCount) {
			int newChildCount = ((SimpleNode) element).getChildCount();
			if (currentChildCount != newChildCount) {
				viewer.setChildCount(element, newChildCount);
			}
		}

		@Override
		public Object getParent(Object element) {
			return ((SimpleNode) element).getParent();
		}
	}

	static SimpleNode root;
	static SimpleNode node0;
	static SimpleNode node1;
	static SimpleNode node2;
	static SimpleNode node3;
	static SimpleNode node4;
	static SimpleNode node5;
	static SimpleNode node6;
	static SimpleNode node7;
	static SimpleNode node8;
	public static boolean flip = false;

	public static void flip() {
		flip = !flip;
		if (flip) {
			node0.setParentChildren(node1, node2, node8);
		} else {
			node0.setParentChildren(node2, node8);
		}
	}

	public static void main(String[] args) throws MalformedURLException {
		root = new SimpleNode("Invisible");
		node0 = new SimpleNode(0);
		node1 = new SimpleNode(1);
		node2 = new SimpleNode(2);
		node3 = new SimpleNode(3);
		node4 = new SimpleNode(4);
		node5 = new SimpleNode(5);
		node6 = new SimpleNode(6);
		node7 = new SimpleNode(7);
		node8 = new SimpleNode(8);

		root.setParentChildren(node0);
		node0.setParentChildren(node2, node8);
		node2.setParentChildren(node3, node4, node5);
		node5.setParentChildren(node6, node7);

		Shell shell = new Shell();
		shell.setSize(500, 155);
		shell.setLayout(new BorderLayout());

		TreeViewer viewer = new TreeViewer(shell, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.VIRTUAL);
		viewer.setContentProvider(new LazyContentProvider(viewer));
		viewer.setLabelProvider(new ColumnLabelProvider());
		viewer.getTree().setLayoutData(new BorderData(SWT.CENTER));
		viewer.setUseHashlookup(true);
		viewer.setInput(root);
		viewer.expandAll();

		ToolBar toolBar = new ToolBar(shell, SWT.NONE);
		toolBar.setLayoutData(new BorderData(SWT.TOP));

		ResourceManager resourceManager = JFaceResources.managerFor(shell);
		ImageDescriptor imageDescriptor = ImageDescriptor
				.createFromURL(URI.create("platform:/plugin/org.eclipse.pde.ui/icons/elcl16/refresh.png").toURL());
		Image image = resourceManager.create(imageDescriptor);
		ToolItem toolItem = new ToolItem(toolBar, SWT.PUSH);
		toolItem.setImage(image);
		toolItem.addSelectionListener(SelectionListener.widgetSelectedAdapter(event -> {
			flip();

			TreePath[] treePaths = viewer.getExpandedTreePaths();
			viewer.setInput(root);
			viewer.setExpandedTreePaths(treePaths);

			// This refresh is important, as it recalculates all tree items. Because the
			// viewer doesn't fit in the shell, some of those items remain virtual, which
			// then cause an exception, the next time this listener is executed.
			viewer.refresh();
		}));

		shell.open();
		while (!shell.isDisposed()) {
			shell.getDisplay().readAndDispatch();
		}
	}
}
