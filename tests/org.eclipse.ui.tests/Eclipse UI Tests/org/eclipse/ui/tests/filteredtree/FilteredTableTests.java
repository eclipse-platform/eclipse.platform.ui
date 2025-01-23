/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Ziegler - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.filteredtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.ui.dialogs.filteredtree.FilteredTable;
import org.eclipse.e4.ui.dialogs.filteredtree.PatternFilter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class FilteredTableTests {
	private final static int NUM_ITEMS = 8000;
	private TestFilteredTable tableViewer;
	private TestElement rootElement;

	@Before
	public void setUp() {
		rootElement = TestElement.createModel(1, NUM_ITEMS);
	}

	@Test
	public void testAddAndRemovePattern() {
		Dialog dialog = new FilteredTableDialog(null, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		dialog.create();

		assertNotNull(tableViewer);
		assertEquals(tableViewer.getItemCount(), NUM_ITEMS);

		tableViewer.applyPattern("0-0 name-");
		assertEquals(tableViewer.getItemCount(), 1);

		tableViewer.applyPattern("0-0 name unknownWord");
		assertEquals(tableViewer.getItemCount(), 0);

		tableViewer.applyPattern("");
		assertEquals(tableViewer.getItemCount(), NUM_ITEMS);

		dialog.close();
	}

	private class TestFilteredTable extends FilteredTable {
		private boolean jobScheduled;

		public TestFilteredTable(Composite parent, int style) {
			super(parent, style, new PatternFilter(), 0);
		}

		public int getItemCount() {
			return getViewer().getTable().getItemCount();
		}

		public void applyPattern(String pattern) {
			setFilterText(pattern);
			textChanged();

			while (jobScheduled) {
				getDisplay().readAndDispatch();
			}
		}

		@Override
		protected Job doCreateRefreshJob() {
			Job job = super.doCreateRefreshJob();
			job.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void scheduled(IJobChangeEvent event) {
					jobScheduled = true;
				}

				@Override
				public void done(IJobChangeEvent event) {
					if (event.getResult().isOK()) {
						jobScheduled = false;
					}
				}
			});
			return job;
		}
	}


	private class FilteredTableDialog extends Dialog {
		private final int style;

		public FilteredTableDialog(Shell shell, int tableStyle) {
			super(shell);
			style = tableStyle;
		}

		@Override
		protected Control createContents(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());

			tableViewer = new TestFilteredTable(composite, style);
			tableViewer.setLayoutData(GridDataFactory.fillDefaults().hint(400, 500).create());
			tableViewer.getViewer().setContentProvider(new TestModelContentProvider());
			tableViewer.getViewer().setLabelProvider(new LabelProvider());
			tableViewer.getViewer().setUseHashlookup(true);
			tableViewer.getViewer().setInput(rootElement);
			return parent;
		}
	}
}
