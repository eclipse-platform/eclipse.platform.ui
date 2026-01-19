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
 *     Tom Schindl - bug 151205
 *     Jan-Ove Weichel <janove.weichel@vogella.com> - Bug 481490
 *     Lucas Bullen (Red Hat Inc.) - Bug 493357
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.jupiter.api.Test;

public abstract class StructuredViewerTest extends ViewerTestCase {
	public static class TestLabelFilter extends ViewerFilter {
		@Override
		public boolean select(Viewer viewer, Object parent, Object element) {
			String label = ((TestElement) element).getLabel();
			int count = label.indexOf('-');
			if (count < 0) {
				return false;
			}
			String number = label.substring(count + 1);
			return ((Integer.parseInt(number) % 2) == 0);
		}

		@Override
		public boolean isFilterProperty(Object element, String property) {
			return property.equals(IBasicPropertyConstants.P_TEXT);
		}
	}

	public static class TestLabelFilter2 extends ViewerFilter {
		@Override
		public boolean select(Viewer viewer, Object parent, Object element) {
			String label = ((TestElement) element).getLabel();
			int count = label.indexOf('-');
			if (count < 0) {
				return false;
			}
			String number = label.substring(count + 1);
			return Integer.parseInt(number) == 0;
		}

		@Override
		public boolean isFilterProperty(Object element, String property) {
			return property.equals(IBasicPropertyConstants.P_TEXT);
		}
	}

	public static class TestLabelComparator extends ViewerComparator {
		@Override
		public int compare(Viewer v, Object e1, Object e2) {
			// put greater labels first
			String name1 = ((TestElement) e1).getLabel();
			String name2 = ((TestElement) e2).getLabel();
			return name2.compareTo(name1);
		}

		@Override
		public boolean isSorterProperty(Object element, String property) {
			return property.equals(IBasicPropertyConstants.P_TEXT);
		}
	}

	public static class TestLabelProvider extends LabelProvider {
		public static String fgSuffix = "";

		static Image fgImage = ImageDescriptor.createFromFile(
				TestLabelProvider.class, "images/java.gif").createImage();

		@Override
		public String getText(Object element) {
			return providedString((TestElement) element);
		}

		@Override
		public Image getImage(Object element) {
			return fgImage;
		}

		public void setSuffix(String suffix) {
			fgSuffix = suffix;
			fireLabelProviderChanged(new LabelProviderChangedEvent(this));
		}
	}

	protected void bulkChange(TestModelChange eventToFire) {
		TestElement first = fRootElement.getFirstChild();
		TestElement newElement = first.getContainer().basicAddChild();
		fRootElement.basicDeleteChild(first);
		fModel.fireModelChanged(eventToFire);
		processEvents();
		// Wait for events to be processed
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.testFindItem(newElement) != null;
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000);
		assertNotNull(fViewer
				.testFindItem(newElement), "new sibling is visible");
		assertNull(fViewer.testFindItem(first), "first child is not visible");
	}

	protected abstract int getItemCount();

	protected abstract String getItemText(int at);

	public static String providedString(String s) {
		return s + "<rendered>" + TestLabelProvider.fgSuffix;
	}

	public static String providedString(TestElement element) {
		return element.getID() + " " + element.getLabel() + "<rendered>"
				+ TestLabelProvider.fgSuffix;
	}

	@Test
	public void testClearSelection() {
		TestElement first = fRootElement.getFirstChild();
		StructuredSelection selection = new StructuredSelection(first);
		fViewer.setSelection(selection);
		fViewer.setSelection(new StructuredSelection());
		ISelection result = fViewer.getStructuredSelection();
		assertTrue(result.isEmpty());
	}

	@Test
	public void testDeleteChild() {
		TestElement first = fRootElement.getFirstChild();
		TestElement first2 = first.getFirstChild();
		first.deleteChild(first2);
		assertNull(fViewer.testFindItem(first2), "first child is not visible");
	}

	@Test
	public void testDeleteInput() {
		TestElement first = fRootElement.getFirstChild();
		TestElement firstfirst = first.getFirstChild();
		fRootElement = first;
		setInput();
		fRootElement.deleteChild(first);
		assertNull(fViewer
				.testFindItem(firstfirst), "first child is not visible");
	}

	@Test
	public void testDeleteSibling() {
		TestElement first = fRootElement.getFirstChild();
		assertNotNull(fViewer.testFindItem(first), "first child is visible");
		fRootElement.deleteChild(first);
		assertNull(fViewer.testFindItem(first), "first child is not visible");
	}

	/**
	 * Tests to ensure that the viewer is properly disposed.  Includes:
	 *     removal of filters
	 */
	@Test
	public void testDispose() {
		assertEquals(0, fViewer.getFilters().length);
		fViewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				return false;
			}});
		assertEquals(1, fViewer.getFilters().length);
		fViewer.getControl().dispose();
		assertEquals(0, fViewer.getFilters().length);
	}

	@Test
	public void testFilter() {
		ViewerFilter filter = new TestLabelFilter();
		fViewer.addFilter(filter);
		assertEquals(5, getItemCount(), "filtered count");
		fViewer.removeFilter(filter);
		assertEquals(10, getItemCount(), "unfiltered count");

	}

	@Test
	public void testSetFilters() {
		ViewerFilter filter = new TestLabelFilter();
		fViewer.setFilters(filter, new TestLabelFilter2());
		assertEquals(1, getItemCount(), "2 filters count");

		fViewer.setFilters(filter);
		assertEquals(5, getItemCount(), "1 filtered count");

		fViewer.setFilters();
		assertEquals(10, getItemCount(), "unfiltered count");
	}

	@Test
	public void testSetAndGetData() {

		//get with no data
		assertNull(fViewer.getData("foo"), "get with no data");

		//remove with no data
		fViewer.setData("foo", null);

		//get with no data after remove
		assertNull(fViewer.getData("foo"), "get with no data after remove");

		//set
		fViewer.setData("foo", "bar");

		//remove key which does not exist
		fViewer.setData("baz", null);

		//get key which does not exist
		assertNull(fViewer.getData("baz"), "get key which does not exist");

		//get value instead of key
		assertNull(fViewer.getData("bar"), "get value instead of key");

		//get single value
		assertEquals("bar", fViewer.getData("foo"), "get single value");

		//set new value
		fViewer.setData("foo", "baz");

		//get overridden value
		assertEquals("baz", fViewer.getData("foo"), "get overridden value");

		//add more values
		fViewer.setData("alpha", "1");
		fViewer.setData("beta", "2");
		fViewer.setData("delta", "3");

		//get multiple values
		assertEquals("baz", fViewer.getData("foo"), "get multiple values");
		assertEquals("1", fViewer.getData("alpha"), "get multiple values");
		assertEquals("2", fViewer.getData("beta"), "get multiple values");
		assertEquals("3", fViewer.getData("delta"), "get multiple values");

		//override with multiple values
		fViewer.setData("alpha", "10");

		//get overridden value
		assertEquals("10", fViewer.getData("alpha"), "get overridden value");

		//add more values
		fViewer.setData("gamma", "4");
		fViewer.setData("epsilon", "5");

		//remove first value
		fViewer.setData("foo", null);

		//check remaining values
		assertEquals(null, fViewer.getData("foo"), "get after remove");
		assertEquals("10", fViewer.getData("alpha"), "get after remove");
		assertEquals("2", fViewer.getData("beta"), "get after remove");
		assertEquals("3", fViewer.getData("delta"), "get after remove");
		assertEquals("4", fViewer.getData("gamma"), "get after remove");
		assertEquals("5", fViewer.getData("epsilon"), "get after remove");

		//remove middle value
		fViewer.setData("delta", null);

		//check remaining values
		assertEquals(null, fViewer.getData("foo"), "get after remove");
		assertEquals("10", fViewer.getData("alpha"), "get after remove");
		assertEquals("2", fViewer.getData("beta"), "get after remove");
		assertEquals(null, fViewer.getData("delta"), "get after remove");
		assertEquals("4", fViewer.getData("gamma"), "get after remove");
		assertEquals("5", fViewer.getData("epsilon"), "get after remove");

		//remove last value
		fViewer.setData("epsilon", null);

		//check remaining values
		assertEquals(null, fViewer.getData("foo"), "get after remove");
		assertEquals("10", fViewer.getData("alpha"), "get after remove");
		assertEquals("2", fViewer.getData("beta"), "get after remove");
		assertEquals(null, fViewer.getData("delta"), "get after remove");
		assertEquals("4", fViewer.getData("gamma"), "get after remove");
		assertEquals(null, fViewer.getData("epsilon"), "get after remove");

		//remove remaining values
		fViewer.setData("alpha", null);
		fViewer.setData("beta", null);
		fViewer.setData("gamma", null);

		//check final values
		assertEquals(null, fViewer.getData("foo"), "get after remove");
		assertEquals(null, fViewer.getData("alpha"), "get after remove");
		assertEquals(null, fViewer.getData("beta"), "get after remove");
		assertEquals(null, fViewer.getData("delta"), "get after remove");
		assertEquals(null, fViewer.getData("gamma"), "get after remove");
		assertEquals(null, fViewer.getData("epsilon"), "get after remove");
	}

	@Test
	public void testInsertChild() {
		TestElement first = fRootElement.getFirstChild();
		TestElement newElement = first.addChild(TestModelChange.INSERT);
		assertNull(fViewer
				.testFindItem(newElement), "new sibling is not visible");
	}

	@Test
	public void testInsertSibling() {
		TestElement newElement = fRootElement.addChild(TestModelChange.INSERT);
		processEvents();
		// Wait for events to be processed
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.testFindItem(newElement) != null;
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000);
		assertNotNull(fViewer
				.testFindItem(newElement), "new sibling is visible");
	}

	@Test
	public void testInsertSiblingReveal() {
		TestElement newElement = fRootElement.addChild(TestModelChange.INSERT
				| TestModelChange.REVEAL);
		assertNotNull(fViewer
				.testFindItem(newElement), "new sibling is visible");
	}

	@Test
	public void testInsertSiblings() {
		TestElement[] newElements = fRootElement
				.addChildren(TestModelChange.INSERT);
		processEvents();
		// Wait for events to be processed
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.testFindItem(newElements[newElements.length - 1]) != null;
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000);
		for (TestElement newElement : newElements) {
			assertNotNull(fViewer
					.testFindItem(newElement), "new siblings are visible");
		}
	}

	@Test
	public void testInsertSiblingSelectExpanded() {
		TestElement newElement = fRootElement.addChild(TestModelChange.INSERT
				| TestModelChange.REVEAL | TestModelChange.SELECT);
		processEvents();
		// Wait for events to be processed
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.testFindItem(newElement) != null;
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000);
		assertNotNull(fViewer
				.testFindItem(newElement), "new sibling is visible");
		assertSelectionEquals("new element is selected", newElement);
	}

	@Test
	public void testInsertSiblingWithFilterFiltered() {
		fViewer.addFilter(new TestLabelFilter());
		TestElement newElement = new TestElement(fModel, fRootElement);
		newElement.setLabel("name-111");
		fRootElement.addChild(newElement, new TestModelChange(
				TestModelChange.INSERT | TestModelChange.REVEAL
						| TestModelChange.SELECT, fRootElement, newElement));
		assertNull(fViewer
				.testFindItem(newElement), "new sibling is not visible");
		assertEquals(5, getItemCount());
	}

	@Test
	public void testInsertSiblingWithFilterNotFiltered() {
		fViewer.addFilter(new TestLabelFilter());
		TestElement newElement = new TestElement(fModel, fRootElement);
		newElement.setLabel("name-222");
		fRootElement.addChild(newElement, new TestModelChange(
				TestModelChange.INSERT | TestModelChange.REVEAL
						| TestModelChange.SELECT, fRootElement, newElement));
		assertNotNull(fViewer
				.testFindItem(newElement), "new sibling is visible");
		assertEquals(6, getItemCount());
	}

	@Test
	public void testInsertSiblingWithSorter() {
		fViewer.setComparator(new TestLabelComparator());
		TestElement newElement = new TestElement(fModel, fRootElement);
		newElement.setLabel("name-9999");
		fRootElement.addChild(newElement, new TestModelChange(
				TestModelChange.INSERT | TestModelChange.REVEAL
						| TestModelChange.SELECT, fRootElement, newElement));
		String newLabel = newElement.toString();
		assertEquals(newLabel, getItemText(0), "sorted first");
		assertSelectionEquals("new element is selected", newElement);
	}

	@Test
	public void testLabelProvider() {
		fViewer.setLabelProvider(getTestLabelProvider());
		TestElement first = fRootElement.getFirstChild();
		String newLabel = providedString(first);
		assertEquals(newLabel, getItemText(0), "rendered label");
	}

	/**
	 * @return IBaseLabelProvder used in this test
	 */
	public IBaseLabelProvider getTestLabelProvider() {
		return new TestLabelProvider();
	}

	@Test
	public void testLabelProviderStateChange() {
		TestLabelProvider provider = new TestLabelProvider();
		fViewer.setLabelProvider(provider);
		provider.setSuffix("added suffix");
		TestElement first = fRootElement.getFirstChild();
		String newLabel = providedString(first);
		assertEquals(newLabel, getItemText(0), "rendered label");
	}

	@Test
	public void testRename() {
		TestElement first = fRootElement.getFirstChild();
		String newLabel = first.getLabel() + " changed";
		first.setLabel(newLabel);
		assertEquals(first.getID() + " " + newLabel,
				getItemText(0), "changed label");
	}

	@Test
	public void testRenameWithFilter() {
		fViewer.addFilter(new TestLabelFilter());
		TestElement first = fRootElement.getFirstChild();
		first.setLabel("name-1111"); // should disappear
		assertNull(fViewer
				.testFindItem(first), "changed sibling is not visible");
		first.setLabel("name-2222"); // should reappear
		fViewer.refresh();
		assertNotNull(fViewer
				.testFindItem(first), "changed sibling is not visible");
	}

	@Test
	public void testRenameWithLabelProvider() {
		if (fViewer instanceof TableViewer) {
			return;
		}
		fViewer.setLabelProvider(new TestLabelProvider());
		TestElement first = fRootElement.getFirstChild();
		first.setLabel("changed name");
		String newLabel = providedString(first);
		assertEquals(newLabel, getItemText(0), "rendered label");
	}

	@Test
	public void testRenameWithSorter() {
		fViewer.setComparator(new TestLabelComparator());
		TestElement first = fRootElement.getFirstChild();
		first.setLabel("name-9999");
		String newElementLabel = first.toString();
		assertEquals(newElementLabel, getItemText(0), "sorted first");
	}

	@Test
	public void testSetInput() {
		TestElement first = fRootElement.getFirstChild();
		TestElement firstfirst = first.getFirstChild();

		fRootElement = first;
		setInput();
		processEvents();
		// Wait for events to be processed
		new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.testFindItem(firstfirst) != null;
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000);
		assertNotNull(fViewer
				.testFindItem(firstfirst), "first child is visible");
	}

	@Test
	public void testSetSelection() {
		TestElement first = fRootElement.getFirstChild();
		StructuredSelection selection = new StructuredSelection(first);
		fViewer.setSelection(selection);
		IStructuredSelection result = fViewer.getStructuredSelection();
		assertEquals(1, result.size());
		assertEquals(first, result.getFirstElement());
	}

	@Test
	public void testSomeChildrenChanged() {
		bulkChange(new TestModelChange(TestModelChange.STRUCTURE_CHANGE,
				fRootElement));
	}

	@Test
	public void testSorter() {
		TestElement first = fRootElement.getFirstChild();
		TestElement last = fRootElement.getLastChild();
		int size = fRootElement.getChildCount();

		String firstLabel = first.toString();
		String lastLabel = last.toString();
		assertEquals(firstLabel, getItemText(0), "unsorted");
		assertEquals(lastLabel, getItemText(size - 1), "unsorted");
		fViewer.setComparator(new TestLabelComparator());
		assertEquals(firstLabel, getItemText(size - 1), "reverse sorted");
		assertEquals(lastLabel, getItemText(0), "reverse sorted");

		fViewer.setComparator(null);
		assertEquals(firstLabel, getItemText(0), "unsorted");
		assertEquals(lastLabel, getItemText(size - 1), "unsorted");
	}

	@Test
	public void testWorldChanged() {
		bulkChange(new TestModelChange(TestModelChange.STRUCTURE_CHANGE, null));
	}
}
