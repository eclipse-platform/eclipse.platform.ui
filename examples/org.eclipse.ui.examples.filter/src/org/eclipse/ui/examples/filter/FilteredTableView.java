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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.dialogs.filteredtree.FilteredTable;
import org.eclipse.e4.ui.dialogs.filteredtree.PatternFilter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class FilteredTableView extends ViewPart {
	protected FilteredTable filter;
	protected TableViewer viewer;
	protected List<String> input;

	@Override
	public void createPartControl(Composite parent) {
		input = generateInput(9);
		filter = createFilteredTable(parent);
		viewer = filter.getViewer();
		viewer.setContentProvider(createContentProvider());
		viewer.setInput(input);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	protected FilteredTable createFilteredTable(Composite parent) {
		return new FilteredTable(parent, SWT.NONE, new PatternFilter(), 500);
	}

	protected IContentProvider createContentProvider() {
		return ArrayContentProvider.getInstance();
	}

	private List<String> generateInput(int size) {
		List<String> input = new ArrayList<>();
		for (int i = 1; i <= size; ++i) {
			input.add(Messages.bind(Messages.FilteredTableView_Element, i));
		}
		return input;
	}
}
