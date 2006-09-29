/*******************************************************************************
 * Copyright (c) 2006 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Customized context menu based on TreeItem-Selection
 * 
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 *
 */
public class Snippet005TreeCustomMenu {
	private class MyContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			return ((MyModel) inputElement).child.toArray();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {

		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			if (element == null) {
				return null;
			}

			return ((MyModel) element).parent;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			return ((MyModel) element).child.size() > 0;
		}

	}

	public class MyModel {
		public MyModel parent;

		public ArrayList child = new ArrayList();

		public int counter;

		public MyModel(int counter, MyModel parent) {
			this.parent = parent;
			this.counter = counter;
		}

		public String toString() {
			String rv = "Item ";
			if (parent != null) {
				rv = parent.toString() + ".";
			}

			rv += counter;

			return rv;
		}
	}

	public Snippet005TreeCustomMenu(Shell shell) {
		final TreeViewer v = new TreeViewer(shell);
		v.setLabelProvider(new LabelProvider());
		v.setContentProvider(new MyContentProvider());
		v.setInput(createModel());

		final Action a = new Action("") {
		};
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);

		mgr.addMenuListener(new IMenuListener() {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
			 */
			public void menuAboutToShow(IMenuManager manager) {
				IStructuredSelection selection = (IStructuredSelection) v
						.getSelection();
				if (!selection.isEmpty()) {
					a.setText("Action for "
							+ ((MyModel) selection.getFirstElement())
									.toString());
					mgr.add(a);
				}
			}
		});
		v.getControl().setMenu(mgr.createContextMenu(v.getControl()));
	}

	private MyModel createModel() {

		MyModel root = new MyModel(0, null);
		root.counter = 0;

		MyModel tmp;
		for (int i = 1; i < 10; i++) {
			tmp = new MyModel(i, root);
			root.child.add(tmp);
			for (int j = 1; j < i; j++) {
				tmp.child.add(new MyModel(j, tmp));
			}
		}

		return root;
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new Snippet005TreeCustomMenu(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}
