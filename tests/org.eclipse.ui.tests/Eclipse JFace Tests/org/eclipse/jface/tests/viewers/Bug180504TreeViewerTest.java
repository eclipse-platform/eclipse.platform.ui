/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.viewers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * @since 3.3
 *
 */
public class Bug180504TreeViewerTest extends ViewerTestCase {
	public class MyModel {
		public MyModel parent;

		public ArrayList child = new ArrayList();

		public int counter;

		public MyModel(int counter, MyModel parent) {
			this.parent = parent;
			this.counter = counter;
		}

		@Override
		public String toString() {
			String rv = "Item ";
			if (parent != null) {
				rv = parent.toString() + ".";
			}

			rv += counter;

			return rv;
		}
	}
	/**
	 * @param name
	 */
	public Bug180504TreeViewerTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		final TreeViewer treeViewer = new TreeViewer(parent, SWT.FULL_SELECTION);

		treeViewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public Object[] getElements(Object inputElement) {
				return ((MyModel) inputElement).child.toArray();
			}

			@Override
			public void dispose() {

			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {

			}

			@Override
			public Object[] getChildren(Object parentElement) {
				return getElements(parentElement);
			}

			@Override
			public Object getParent(Object element) {
				if (element == null) {
					return null;
				}

				return ((MyModel) element).parent;
			}

			@Override
			public boolean hasChildren(Object element) {
				return ((MyModel) element).child.size() > 0;
			}
		});

		treeViewer.setCellEditors(new CellEditor[] { new TextCellEditor(
				treeViewer.getTree()) });
		treeViewer.setColumnProperties(new String[] { "0" });
		treeViewer.setCellModifier(new ICellModifier() {
			@Override
			public boolean canModify(Object element, String property) {
				return true;
			}

			@Override
			public Object getValue(Object element, String property) {
				return "";
			}

			@Override
			public void modify(Object element, String property, Object value) {
				treeViewer.getControl().dispose();
			}

		});

		new TreeColumn(treeViewer.getTree(), SWT.NONE).setWidth(200);

		return treeViewer;
	}

	@Override
	protected void setUpModel() {
		// don't do anything here - we are not using the normal fModel and
		// fRootElement
	}

	@Override
	protected void setInput() {
		MyModel root = new MyModel(0, null);
		root.counter = 0;

		MyModel tmp;
		for (int i = 1; i < 100; i++) {
			tmp = new MyModel(i, root);
			root.child.add(tmp);
			for (int j = 1; j < i; j++) {
				tmp.child.add(new MyModel(j, tmp));
			}
		}

		getTreeViewer().setInput(root);
	}

	private TreeViewer getTreeViewer() {
		return (TreeViewer) fViewer;
	}

	public void testBug201002() {
		getTreeViewer().editElement(((MyModel)((MyModel)getTreeViewer().getInput()).child.get(90)).child.get(10), 0);
		Method m;
		try {
			m = ColumnViewer.class.getDeclaredMethod("applyEditorValue", new Class[0]);
			m.setAccessible(true);
			m.invoke(getTreeViewer(), new Object[0]);
		} catch (SecurityException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testBug180504CancleEditor() {
		getTreeViewer().editElement(((MyModel)((MyModel)getTreeViewer().getInput()).child.get(90)).child.get(10), 0);
		getTreeViewer().cancelEditing();
	}
}
