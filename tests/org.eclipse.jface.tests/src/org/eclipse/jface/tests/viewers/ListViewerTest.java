/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     Tom Schindl <tom.schindl@bestsolution.at> - test case for bug 157309, 177619
 *     Brad Reynolds - test case for bug 141435
 *     Jan-Ove Weichel <janove.weichel@vogella.com> - Bug 481490
 *     Lucas Bullen (Red Hat Inc.) - Bug 493357
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assume.assumeFalse;

import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.Test;

public class ListViewerTest extends StructuredViewerTest {

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		ListViewer viewer = new ListViewer(parent);
		viewer.setContentProvider(new TestModelContentProvider());
		return viewer;
	}

	@Override
	protected int getItemCount() {
		TestElement first = fRootElement.getFirstChild();
		List list = (List) fViewer.testFindItem(first);
		return list.getItemCount();
	}

	@Override
	protected String getItemText(int at) {
		List list = (List) fViewer.getControl();
		return list.getItem(at);
	}

	@Test
	public void testInsert() {
		ListViewer v = ((ListViewer)fViewer);
		TestElement element = new TestElement(fModel, fRootElement);
		v.insert(element, 1);
		assertSame("test insert", element, v.getElementAt(1));
		assertEquals("test insert", element.toString(), v.getList().getItem(1));

		v.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return true;
			}
		});

		TestElement element1 = new TestElement(fModel, fRootElement);
		v.insert(element1, 1);
		assertNotSame("test insert", element1, v.getElementAt(1));

		v.setFilters();
		v.remove(element);
		v.remove(element1);
	}

	@Test
	public void testRevealBug69076() {
		assumeFalse("See bug 116105", Util.isLinux());

		fViewer = null;
		if (fShell != null) {
			fShell.dispose();
			fShell = null;
		}
		openBrowser();
		for (int i = 40; i < 45; i++) {
			fRootElement = TestElement.createModel(1, i);
			fModel = fRootElement.getModel();
			fViewer.setInput(fRootElement);
			for (int j = 30; j < fRootElement.getChildCount(); j++) {
				fViewer.setSelection(new StructuredSelection(fRootElement
						.getFirstChild()), true);
				TestElement child = fRootElement.getChildAt(j);
				fViewer.reveal(child);
				List list = ((ListViewer) fViewer).getList();
				int topIndex = list.getTopIndex();
				// even though we pass in reveal=false, SWT still scrolls to show the selection (since 20020815)
				fViewer.setSelection(new StructuredSelection(child), false);
				assertEquals("topIndex should not change on setSelection", topIndex, list
						.getTopIndex());
				list.showSelection();
				assertEquals("topIndex should not change on showSelection", topIndex, list
						.getTopIndex());
			}
		}
	}

	/**
	 * Asserts the ability to refresh a List that contains no selection without losing vertically scrolled state.
	 */
	@Test
	public void testRefreshBug141435() throws Exception {
		fViewer = null;
		if (fShell != null) {
			fShell.dispose();
			fShell = null;
		}
		openBrowser();
		TestElement model = TestElement.createModel(1, 50);
		fViewer.setInput(model);

		int lastIndex = model.getChildCount() - 1;

		//Scroll...
		fViewer.reveal(model.getChildAt(lastIndex));
		List list = (List) fViewer.getControl();

		try {
			Device.DEBUG = true;
			int topIndex = list.getTopIndex();
			assertNotEquals("Top item should not be the first item.", 0, topIndex);
			fViewer.refresh();

			DisplayHelper.waitAndAssertCondition(fShell.getDisplay(), () -> {
				assertEquals("Top index was not preserved after refresh.", topIndex, list.getTopIndex());
			});

			// Assert that when the previous top index after refresh is invalid no
			// exceptions are thrown.
			model.deleteChildren();

			fViewer.refresh();

			DisplayHelper.waitAndAssertCondition(fShell.getDisplay(), () -> {
				assertEquals(0, list.getTopIndex());
			});
		} finally {
			Device.DEBUG = false;
		}
	}

	@Test
	public void testSelectionRevealBug177619() throws Exception {
		TestElement model = TestElement.createModel(1, 100);
		fViewer.setInput(model);

		fViewer.setSelection(new StructuredSelection(((ListViewer)fViewer).getElementAt(50)),true);
		List list = ((ListViewer) fViewer).getList();
		DisplayHelper.waitAndAssertCondition(fShell.getDisplay(), () -> {
			assertNotEquals(0, list.getTopIndex());
		});
	}

	@Test
	public void testSelectionNoRevealBug177619() throws Exception {
		TestElement model = TestElement.createModel(1, 100);
		fViewer.setInput(model);

		fViewer.setSelection(new StructuredSelection(((ListViewer)fViewer).getElementAt(50)),false);
		assertEquals(0, ((ListViewer) fViewer).getList().getTopIndex());
	}
}
