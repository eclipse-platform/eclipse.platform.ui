/*******************************************************************************
 * Copyright (c) 2014 Stefan Winkler and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Winkler - initial API and implementation (bug 242231)
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.junit.Test;

/**
 * This testcase for Bug 242231 checks that applyEditorValue(), which has been
 * made into a public method, works as advertised, namely that it closes an open
 * cell editor and sets its value to the model.
 *
 * @since 3.5
 */
public class Bug242231Test extends ViewerTestCase {

	protected static final String[] COMBO_ITEMS = new String[] { "default value", "some value", "value changed" };
	private TableViewer tableViewer;

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.NONE);
		tableViewer.setContentProvider(new TestModelContentProvider());

		TableViewerColumn tableColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		tableColumn.setEditingSupport(getEditingSupport());
		tableColumn.setLabelProvider(new ColumnLabelProvider());
		return tableViewer;
	}

	/**
	 * @return editing support for the test
	 */
	private EditingSupport getEditingSupport() {
		return new EditingSupport(tableViewer) {

			@Override
			protected void setValue(Object element, Object value) {
				((TestElement) element).setLabel("value set");
			}

			@Override
			protected Object getValue(Object element) {
				return 0;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new ComboBoxCellEditor(tableViewer.getControl().getParent(), COMBO_ITEMS);
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		};
	}

	@Test
	public void testBug242231() {
		// get a test element from the model and set the label to a known value
		TestElement testElement = fRootElement.getChildAt(0);
		testElement.setLabel("default value");

		// open the cell editor
		tableViewer.editElement(testElement, 0);

		// apply the editor value
		tableViewer.applyEditorValue();

		// check that the new value has been set to the model
		assertEquals("value set", testElement.getLabel());
	}
}
