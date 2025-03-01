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

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;

public class FilteredTreeView extends ViewPart {
	private FilteredTree filter;
	private TreeViewer viewer;

	@Override
	public void createPartControl(Composite parent) {
		filter = new FilteredTree(parent, SWT.NONE, new PatternFilter(), true, true, 500);
		viewer = filter.getViewer();
		viewer.setContentProvider(new TreeContentProvider(9));
		viewer.setInput(new Object());
		viewer.expandAll();
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private static class TreeContentProvider implements ITreeContentProvider {
		private Map<String, String> elements = new TreeMap<>();

		public TreeContentProvider(int size) {
			for (int i = 1; i <= size; ++i) {
				String key = Messages.bind(Messages.FilteredTreeView_Parent, i);
				String value = Messages.bind(Messages.FilteredTreeView_Child, i);
				elements.put(key, value);
			}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return elements.keySet().toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (!hasChildren(parentElement)) {
			return new Object[0];
		}
		return new Object[] { elements.get(parentElement) };
	}

	@Override
	public Object getParent(Object element) {
		for (Map.Entry<String, String> entry : elements.entrySet()) {
			if (entry.getValue().equals(element)) {
				return entry.getKey();
			}
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return elements.containsKey(element);
	}
}
}
