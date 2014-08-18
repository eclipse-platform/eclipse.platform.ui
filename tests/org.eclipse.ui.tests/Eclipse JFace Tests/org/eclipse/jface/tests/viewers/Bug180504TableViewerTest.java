/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stefan Winkler <stefan@winklerweb.net> - Bug 242231
 ******************************************************************************/

package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @since 3.3
 *
 */
public class Bug180504TableViewerTest extends ViewerTestCase {

	/**
	 * @param name
	 */
	public Bug180504TableViewerTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		final TableViewer tableViewer = new TableViewer(parent, SWT.FULL_SELECTION);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setCellEditors(new CellEditor[] { new TextCellEditor(
				tableViewer.getTable()) });
		tableViewer.setColumnProperties(new String[] { "0" });
		tableViewer.setCellModifier(new ICellModifier() {
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
				tableViewer.getControl().dispose();
			}

		});

	    new TableColumn(tableViewer.getTable(), SWT.NONE).setWidth(200);

		return tableViewer;
	}

	@Override
	protected void setUpModel() {
		// don't do anything here - we are not using the normal fModel and
		// fRootElement
	}

	@Override
	protected void setInput() {
		String[] ar = new String[100];
		for( int i = 0; i < ar.length; i++ ) {
			ar[i] = i + "";
		}
		getTableViewer().setInput(ar);
	}

	private TableViewer getTableViewer() {
		return (TableViewer) fViewer;
	}

	public void testBug180504ApplyEditor() {
		getTableViewer().editElement(getTableViewer().getElementAt(0), 0);
		getTableViewer().applyEditorValue();
	}

	public void testBug180504CancleEditor() {
		getTableViewer().editElement(getTableViewer().getElementAt(0), 0);
		getTableViewer().cancelEditing();
	}
}
