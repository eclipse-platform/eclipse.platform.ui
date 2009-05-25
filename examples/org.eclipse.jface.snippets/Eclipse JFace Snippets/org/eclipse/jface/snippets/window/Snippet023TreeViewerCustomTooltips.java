/*******************************************************************************
 * Copyright (c) 2006, 2007 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.snippets.window;

import java.util.ArrayList;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A simple TreeViewer to demonstrate how custom tooltips could be created
 * easily. This is an extended version from
 * http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet125.java
 * 
 * This code is for users pre 3.3 others could use newly added tooltip support in
 * {@link CellLabelProvider}
 * 
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 * 
 */
public class Snippet023TreeViewerCustomTooltips {
	private class MyContentProvider implements ITreeContentProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return ((MyModel) inputElement).child.toArray();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 *      java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			if (element == null) {
				return null;
			}

			return ((MyModel) element).parent;
		}

		/*
		 * (non-Javadoc)
		 * 
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

	public Snippet023TreeViewerCustomTooltips(Shell shell) {
		final TreeViewer v = new TreeViewer(shell);
		v.setLabelProvider(new LabelProvider());
		v.setContentProvider(new MyContentProvider());
		v.setInput(createModel());
		v.getTree().setToolTipText("");

		final Listener labelListener = new Listener () {
			public void handleEvent (Event event) {
				Label label = (Label)event.widget;
				Shell shell = label.getShell ();
				switch (event.type) {
					case SWT.MouseDown:
						Event e = new Event ();
						e.item = (TreeItem) label.getData ("_TABLEITEM");
						// Assuming table is single select, set the selection as if
						// the mouse down event went through to the table
						v.getTree().setSelection (new TreeItem [] {(TreeItem) e.item});
						v.getTree().notifyListeners (SWT.Selection, e);
						shell.dispose ();
						v.getTree().setFocus();
						break;
					case SWT.MouseExit:
						shell.dispose ();
						break;
				}
			}
		};
		
		Listener treeListener = new Listener () {
			Shell tip = null;
			Label label = null;
			public void handleEvent (Event event) {
				switch (event.type) {
					case SWT.Dispose:
					case SWT.KeyDown:
					case SWT.MouseMove: {
						if (tip == null) break;
						tip.dispose ();
						tip = null;
						label = null;
						break;
					}
					case SWT.MouseHover: {
						Point coords = new Point(event.x, event.y);
						TreeItem item = v.getTree().getItem(coords);
						if (item != null) {
							int columns = v.getTree().getColumnCount();

							for (int i = 0; i < columns || i == 0; i++) {
								if (item.getBounds(i).contains(coords)) {
									if (tip != null  && !tip.isDisposed ()) tip.dispose ();
									tip = new Shell (v.getTree().getShell(), SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
									tip.setBackground (v.getTree().getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
									FillLayout layout = new FillLayout ();
									layout.marginWidth = 2;
									tip.setLayout (layout);
									label = new Label (tip, SWT.NONE);
									label.setForeground (v.getTree().getDisplay().getSystemColor (SWT.COLOR_INFO_FOREGROUND));
									label.setBackground (v.getTree().getDisplay().getSystemColor (SWT.COLOR_INFO_BACKGROUND));
									label.setData ("_TABLEITEM", item);
									label.setText ("Tooltip: " + item.getData()+ " => Column: " + i);
									label.addListener (SWT.MouseExit, labelListener);
									label.addListener (SWT.MouseDown, labelListener);
									Point size = tip.computeSize (SWT.DEFAULT, SWT.DEFAULT);
									Rectangle rect = item.getBounds (i);
									Point pt = v.getTree().toDisplay (rect.x, rect.y);
									tip.setBounds (pt.x, pt.y, size.x, size.y);
									tip.setVisible (true);
									break;
								}
							}
						}
					}
				}
			}
		};
		v.getTree().addListener (SWT.Dispose, treeListener);
		v.getTree().addListener (SWT.KeyDown, treeListener);
		v.getTree().addListener (SWT.MouseMove, treeListener);
		v.getTree().addListener (SWT.MouseHover, treeListener);
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
		new Snippet023TreeViewerCustomTooltips(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}
