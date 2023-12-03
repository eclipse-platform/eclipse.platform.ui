/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433608
 *******************************************************************************/
package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TestVirtualTree extends TestTree {

	@Override
	public Viewer createViewer(Composite parent) {
		Tree tree = new Tree(parent, SWT.VIRTUAL);
		tree.addListener(SWT.SetData, new Listener() {
			private String getPosition(TreeItem item) {
				TreeItem parentItem = item.getParentItem();
				if (parentItem == null) {
					return "" + item.getParent().indexOf(item);
				}
				return getPosition(parentItem) + "." + parentItem.indexOf(item);
			}

			@Override
			public void handleEvent(Event event) {
				String position = getPosition((TreeItem) event.item);
				if (position.endsWith(".32")) {
					Thread.dumpStack();
				}
				System.out.println("updating " + position);
			}
		});
		TreeViewer viewer = new TreeViewer(tree);
		viewer.setContentProvider(new TestModelContentProvider());
		viewer.setUseHashlookup(true);

		if (fViewer2 == null) {
			fViewer2 = viewer;
		}
		return viewer;
	}

	public static void main(String[] args) {
		TestBrowser browser = new TestVirtualTree();
		if (args.length > 0 && args[0].equals("-twopanes")) {
			browser.show2Panes();
		}

		browser.setBlockOnOpen(true);
		browser.open(TestElement.createModel(3, 10));
	}

}
