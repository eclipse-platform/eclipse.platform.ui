/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeViewerTest extends AbstractTreeViewerTest {

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		fTreeViewer = new TreeViewer(parent);
		fTreeViewer.setContentProvider(new TestModelContentProvider());
		return fTreeViewer;
	}

	@Override
	protected int getItemCount() {
		TestElement first = fRootElement.getFirstChild();
		TreeItem ti = (TreeItem) fViewer.testFindItem(first);
		Tree tree = ti.getParent();
		return tree.getItemCount();
	}

	/**
	 * getItemCount method comment.
	 */
	@Override
	protected int getItemCount(TestElement element) {
		return element.getChildCount();
	}

	@Override
	protected String getItemText(int at) {
		Tree tree = (Tree) fTreeViewer.getControl();
		return tree.getItems()[at].getText();
	}

}
