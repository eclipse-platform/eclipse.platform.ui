/*******************************************************************************
 * Copyright (c) 2026 Aleksandar Kurtakov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.junit.jupiter.api.Test;

/**
 * Verifies that when an {@link EditingSupport} {@code getValue(Object)} returns
 * a value incompatible with the {@link CellEditor} attached to the column,
 * there is enough context (column index, element, value type) to help finding
 * the cause.
 */
public class EditingSupportValueMismatchTest extends ViewerTestCase {

	private TableViewer tableViewer;

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.NONE);
		tableViewer.setContentProvider(new TestModelContentProvider());

		TableViewerColumn tableColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		tableColumn.setEditingSupport(new EditingSupport(tableViewer) {

			@Override
			protected void setValue(Object element, Object value) {
				// not reached in this test
			}

			@Override
			protected Object getValue(Object element) {
				// Return a non-String value for TextCellEditor, which expects a String.
				return Integer.valueOf(42);
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(tableViewer.getTable());
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});
		tableColumn.setLabelProvider(new ColumnLabelProvider());
		return tableViewer;
	}

	@Test
	public void testMismatchProducesDetailedException() {
		TestElement testElement = fRootElement.getChildAt(0);

		AssertionFailedException ex = assertThrows(AssertionFailedException.class,
				() -> tableViewer.editElement(testElement, 0));

		String message = ex.getMessage();
		assertNotNull(message);
		assertTrue(message.contains("TextCellEditor"), "Name the cell editor: " + message);
		assertTrue(message.contains("column index 0"), "Report the column index: " + message);
		assertTrue(message.contains(Integer.class.getName()), "Report the offending value type: " + message);
		assertTrue(message.contains("Cause:"), "Original assertion message: " + message);
	}
}
