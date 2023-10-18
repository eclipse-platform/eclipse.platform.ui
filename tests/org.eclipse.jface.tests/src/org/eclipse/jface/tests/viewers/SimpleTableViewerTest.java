/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.jface.tests.viewers;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.tests.harness.util.Mocks;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class SimpleTableViewerTest extends ViewerTestCase {

	private TableViewer tableViewer;

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		tableViewer = new TableViewer(parent);
		tableViewer.setContentProvider(new TestModelContentProvider());
		return tableViewer;
	}

	@Test
	public void testNullLabel() {
		tableViewer.setLabelProvider(new ITableLabelProvider() {

			@Override
			public Image getColumnImage(Object element, int columnIndex) {
				return null;
			}

			@Override
			public String getColumnText(Object element, int columnIndex) {
				return null;
			}

			@Override
			public void addListener(ILabelProviderListener listener) {
			}

			@Override
			public void dispose() {
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			@Override
			public void removeListener(ILabelProviderListener listener) {
			}
		});
	}

	@Test
	public void testLabelProviderListeners() {
		Table table = tableViewer.getTable();
		new TableColumn(table, SWT.NONE);
		new TableColumn(table, SWT.NONE);
		ILabelProvider mockLabelProvider = (ILabelProvider) Mocks
				.createOrderedMock(ILabelProvider.class);
		// setLabelProvider will cause the addListener and does a refresh,
		// so getText and getImage will be called for each element and both
		// columns
		mockLabelProvider.addListener(null);
		int count = table.getItemCount();
		for (int i = 0; i < count; i++) {
			mockLabelProvider.getText(null);
			mockLabelProvider.getImage(null);
			mockLabelProvider.getText(null);
			mockLabelProvider.getImage(null);
		}
		Mocks.startChecking(mockLabelProvider);
		tableViewer.setLabelProvider(mockLabelProvider);
		Mocks.verify(mockLabelProvider);

		// this will be caused by the dispose()
		mockLabelProvider.removeListener(null);
		mockLabelProvider.dispose();
		Mocks.startChecking(mockLabelProvider);
		tableViewer.getTable().dispose();
		Mocks.verify(mockLabelProvider);
	}

	@Test
	public void testLabelProviderListenersWithColumn() {
		Table table = tableViewer.getTable();
		new TableColumn(table, SWT.NONE);
		new TableViewerColumn(tableViewer, SWT.NONE);
		final int[] disposeCounter = { 0 };
		final int[] listenerCounter = { 0 };
		tableViewer.setLabelProvider(new LabelProvider() {
			@Override
			public void addListener(ILabelProviderListener listener) {
				listenerCounter[0]++;
				super.addListener(listener);
			}
			@Override
			public void removeListener(ILabelProviderListener listener) {
				super.removeListener(listener);
				listenerCounter[0]--;
			}
			@Override
			public void dispose() {
				disposeCounter[0]++;
			}
		});
		table.dispose();
		assertEquals(1, disposeCounter[0]);
		assertEquals(0, listenerCounter[0]);
	}

	@Test
	public void testColumnLabelProviderListeners() {
		Table table = tableViewer.getTable();
		new TableColumn(table, SWT.NONE);
		TableViewerColumn tvc = new TableViewerColumn(tableViewer, SWT.NONE);
		final int[] disposeCounter = { 0 };
		final int[] listenerCounter = { 0 };
		tvc.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public void addListener(ILabelProviderListener listener) {
				listenerCounter[0]++;
				super.addListener(listener);
			}
			@Override
			public void removeListener(ILabelProviderListener listener) {
				super.removeListener(listener);
				listenerCounter[0]--;
			}
			@Override
			public void dispose() {
				disposeCounter[0]++;
			}
		});
		table.dispose();
		assertEquals(0, listenerCounter[0]);
		assertEquals(1, disposeCounter[0]);
	}

	@Test
	public void testCellLabelProviderDispose() {
		final int[] disposeCounter = { 0 };
		tableViewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public void dispose() {
				disposeCounter[0]++;
			}
		});
		tableViewer.getTable().dispose();
		assertEquals(1, disposeCounter[0]);
	}
}
