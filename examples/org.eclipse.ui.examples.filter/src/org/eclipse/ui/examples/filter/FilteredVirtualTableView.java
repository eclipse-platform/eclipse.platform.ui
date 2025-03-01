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
package org.eclipse.ui.examples.filter;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.ui.dialogs.filteredtree.FilteredTable;
import org.eclipse.e4.ui.dialogs.filteredtree.PatternFilter;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class FilteredVirtualTableView extends FilteredTableView {
	protected FilteredTable createFilteredTable(Composite parent) {
		return new FilteredTable(parent, SWT.VIRTUAL, new PatternFilter(), 500) {
			@Override
			protected Job doCreateRefreshJob() {
				Job job = super.doCreateRefreshJob();
				job.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						if (viewer.getControl().isDisposed()) {
							return;
						}
						ViewerFilter filter = viewer.getFilters()[0];
						Object[] newInput = filter.filter(viewer, (Object) null, input.toArray());
						viewer.setInput(Arrays.asList(newInput));
					}
				});
				return job;
			}
		};
	}

	protected IContentProvider createContentProvider() {
		return new ContentProvider();
	}

	private static class ContentProvider implements ILazyContentProvider {
		private List<String> input;
		private TableViewer viewer;

		@Override
		public void updateElement(int index) {
			viewer.replace(input.get(index), index);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.input = (List<String>) newInput;
			this.viewer = (TableViewer) viewer;
			this.viewer.setItemCount(input == null ? 0 : input.size());
		}
	}
}
