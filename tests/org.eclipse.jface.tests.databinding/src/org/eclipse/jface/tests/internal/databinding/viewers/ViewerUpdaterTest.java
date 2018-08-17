/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Conrad Groth - bug 491682
 ******************************************************************************/
package org.eclipse.jface.tests.internal.databinding.viewers;

import java.util.Arrays;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 */
public class ViewerUpdaterTest extends AbstractSWTTestCase {

	IObservableList<String> elementsList;
	String[] elements = new String[] { "one", "two", "three" };

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		elementsList = new WritableList<>(Arrays.asList(elements), String.class);
	}

	@Test
	public void testTableViewer_ReplacingSelectedItemSelectsNewItem() {
		TableViewer tableViewer = new TableViewer(getShell());
		// only with sorter the TableViewerUpdater.replace method delegates to
		// ViewerUpdater.replace:
		tableViewer.setComparator(new ViewerComparator());
		tableViewer.setContentProvider(new ObservableListContentProvider());
		tableViewer.setInput(elementsList);
		tableViewer.getTable().selectAll();

		elementsList.set(0, "foo"); // replace "one"
		IStructuredSelection selection = tableViewer.getStructuredSelection();
		Assert.assertEquals(elements.length, selection.size());
		Assert.assertTrue(selection.toList().contains("foo"));
	}

	@Test
	public void testTreeViewer_ReplacingSelectedItemSelectsNewItem() {
		TreeViewer treeViewer = new TreeViewer(getShell());
		Object input = new Object();

		ITreeContentProvider contentProvider = new ObservableListTreeContentProvider(
				target -> target == input ? elementsList : null, null);
		// only with sorter the TreeViewerUpdater.replace method delegates to
		// ViewerUpdater.replace:
		treeViewer.setComparator(new ViewerComparator());
		treeViewer.setContentProvider(contentProvider);
		treeViewer.setInput(input);
		treeViewer.getTree().selectAll();

		elementsList.set(0, "foo"); // replace "one"
		IStructuredSelection selection = treeViewer.getStructuredSelection();
		Assert.assertEquals(elements.length, selection.size());
		Assert.assertTrue(selection.toList().contains("foo"));
	}
}
