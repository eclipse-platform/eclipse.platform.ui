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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * @since 3.3
 *
 */
public class Bug200558Test extends ViewerTestCase {

	/**
	 * @param name
	 */
	public Bug200558Test(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		final TreeViewer treeViewer = new TreeViewer(parent, SWT.FULL_SELECTION);
		treeViewer.setContentProvider(new ITreeContentProvider() {

			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return new Object[] { "item" };
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				return null;
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public boolean hasChildren(Object element) {
				return false;
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
				return "Test";
			}

			@Override
			public void modify(Object element, String property, Object value) {
			}

		});

	    new TreeColumn(treeViewer.getTree(), SWT.NONE);
	    new TreeColumn(treeViewer.getTree(), SWT.NONE).setWidth(100);

		return treeViewer;
	}

	@Override
	protected void setUpModel() {
		// don't do anything here - we are not using the normal fModel and
		// fRootElement
	}

	@Override
	protected void setInput() {
		getTreeViewer().setInput(new Object());
		getTreeViewer().getTree().getColumn(0).dispose();
	}

	private TreeViewer getTreeViewer() {
		return (TreeViewer) fViewer;
	}

	public void testBug200558() {
		getTreeViewer().editElement(getTreeViewer().getTree().getItem(0).getData(), 0);
		assertEquals("Test", ((Text)getTreeViewer().getCellEditors()[0].getControl()).getText());
	}
}
