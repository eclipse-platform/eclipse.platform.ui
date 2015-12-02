/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl - bug 151205
 *     Jan-Ove Weichel <janove.weichel@vogella.com> - Bug 481490
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.graphics.Image;

public abstract class StructuredViewerTest extends ViewerTestCase {
    public static class TestLabelFilter extends ViewerFilter {
        @Override
		public boolean select(Viewer viewer, Object parent, Object element) {
            String label = ((TestElement) element).getLabel();
            int count = label.indexOf("-");
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
            int count = label.indexOf("-");
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

    public static class TestLabelSorter extends ViewerSorter {
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

    public StructuredViewerTest(String name) {
        super(name);
    }

    protected void bulkChange(TestModelChange eventToFire) {
        TestElement first = fRootElement.getFirstChild();
        TestElement newElement = first.getContainer().basicAddChild();
        fRootElement.basicDeleteChild(first);
        fModel.fireModelChanged(eventToFire);
        processEvents();
        assertNotNull("new sibling is visible", fViewer
                .testFindItem(newElement));
        assertNull("first child is not visible", fViewer.testFindItem(first));
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

    public void testClearSelection() {
        TestElement first = fRootElement.getFirstChild();
        StructuredSelection selection = new StructuredSelection(first);
        fViewer.setSelection(selection);
        fViewer.setSelection(new StructuredSelection());
        ISelection result = fViewer.getSelection();
        assertTrue(result.isEmpty());
    }

    public void testDeleteChild() {
        TestElement first = fRootElement.getFirstChild();
        TestElement first2 = first.getFirstChild();
        first.deleteChild(first2);
        assertNull("first child is not visible", fViewer.testFindItem(first2));
    }

    public void testDeleteInput() {
        TestElement first = fRootElement.getFirstChild();
        TestElement firstfirst = first.getFirstChild();
        fRootElement = first;
        setInput();
        fRootElement.deleteChild(first);
        assertNull("first child is not visible", fViewer
                .testFindItem(firstfirst));
    }

    public void testDeleteSibling() {
        TestElement first = fRootElement.getFirstChild();
        assertNotNull("first child is visible", fViewer.testFindItem(first));
        fRootElement.deleteChild(first);
        assertNull("first child is not visible", fViewer.testFindItem(first));
    }

    /**
     * Tests to ensure that the viewer is properly diposed.  Includes:
     *     removal of filters
     */
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

    public void testFilter() {
        ViewerFilter filter = new TestLabelFilter();
        fViewer.addFilter(filter);
        assertTrue("filtered count", getItemCount() == 5);
        fViewer.removeFilter(filter);
        assertTrue("unfiltered count", getItemCount() == 10);

    }

    public void testSetFilters() {
    	ViewerFilter filter = new TestLabelFilter();
		fViewer.setFilters(filter, new TestLabelFilter2());
//    	System.err.println("Item: " + getItemCount() );
    	assertTrue("2 filters count", getItemCount() == 1);

		fViewer.setFilters(filter);
    	assertTrue("1 filtered count", getItemCount() == 5);

		fViewer.setFilters();
    	assertTrue("unfiltered count", getItemCount() == 10);
    }

    public void testSetAndGetData() {

    	//get with no data
    	assertNull("get with no data", fViewer.getData("foo"));

    	//remove with no data
    	fViewer.setData("foo", null);

    	//get with no data after remove
    	assertNull("get with no data after remove", fViewer.getData("foo"));

    	//set
    	fViewer.setData("foo", "bar");

    	//remove key which does not exist
    	fViewer.setData("baz", null);

    	//get key which does not exist
    	assertNull("get key which does not exist", fViewer.getData("baz"));

    	//get value instead of key
    	assertNull("get value instead of key", fViewer.getData("bar"));

    	//get single value
    	assertEquals("get single value", "bar", fViewer.getData("foo"));

    	//set new value
    	fViewer.setData("foo", "baz");

    	//get overridden value
    	assertEquals("get overridden value", "baz", fViewer.getData("foo"));

    	//add more values
    	fViewer.setData("alpha", "1");
    	fViewer.setData("beta", "2");
    	fViewer.setData("delta", "3");

    	//get multiple values
    	assertEquals("get multiple values", "baz", fViewer.getData("foo"));
    	assertEquals("get multiple values", "1", fViewer.getData("alpha"));
    	assertEquals("get multiple values", "2", fViewer.getData("beta"));
    	assertEquals("get multiple values", "3", fViewer.getData("delta"));

    	//override with multiple values
    	fViewer.setData("alpha", "10");

    	//get overridden value
    	assertEquals("get overridden value", "10", fViewer.getData("alpha"));

    	//add more values
    	fViewer.setData("gamma", "4");
    	fViewer.setData("epsilon", "5");

    	//remove first value
    	fViewer.setData("foo", null);

    	//check remaining values
    	assertEquals("get after remove", null, fViewer.getData("foo"));
    	assertEquals("get after remove", "10", fViewer.getData("alpha"));
    	assertEquals("get after remove", "2", fViewer.getData("beta"));
    	assertEquals("get after remove", "3", fViewer.getData("delta"));
    	assertEquals("get after remove", "4", fViewer.getData("gamma"));
    	assertEquals("get after remove", "5", fViewer.getData("epsilon"));

    	//remove middle value
    	fViewer.setData("delta", null);

    	//check remaining values
    	assertEquals("get after remove", null, fViewer.getData("foo"));
    	assertEquals("get after remove", "10", fViewer.getData("alpha"));
    	assertEquals("get after remove", "2", fViewer.getData("beta"));
    	assertEquals("get after remove", null, fViewer.getData("delta"));
    	assertEquals("get after remove", "4", fViewer.getData("gamma"));
    	assertEquals("get after remove", "5", fViewer.getData("epsilon"));

    	//remove last value
    	fViewer.setData("epsilon", null);

    	//check remaining values
    	assertEquals("get after remove", null, fViewer.getData("foo"));
    	assertEquals("get after remove", "10", fViewer.getData("alpha"));
    	assertEquals("get after remove", "2", fViewer.getData("beta"));
    	assertEquals("get after remove", null, fViewer.getData("delta"));
    	assertEquals("get after remove", "4", fViewer.getData("gamma"));
    	assertEquals("get after remove", null, fViewer.getData("epsilon"));

    	//remove remaining values
    	fViewer.setData("alpha", null);
    	fViewer.setData("beta", null);
    	fViewer.setData("gamma", null);

    	//check final values
    	assertEquals("get after remove", null, fViewer.getData("foo"));
    	assertEquals("get after remove", null, fViewer.getData("alpha"));
    	assertEquals("get after remove", null, fViewer.getData("beta"));
    	assertEquals("get after remove", null, fViewer.getData("delta"));
    	assertEquals("get after remove", null, fViewer.getData("gamma"));
    	assertEquals("get after remove", null, fViewer.getData("epsilon"));
    }

    public void testInsertChild() {
        TestElement first = fRootElement.getFirstChild();
        TestElement newElement = first.addChild(TestModelChange.INSERT);
        assertNull("new sibling is not visible", fViewer
                .testFindItem(newElement));
    }

    public void testInsertSibling() {
        TestElement newElement = fRootElement.addChild(TestModelChange.INSERT);
        processEvents();
        assertNotNull("new sibling is visible", fViewer
                .testFindItem(newElement));
    }

    public void testInsertSiblingReveal() {
        TestElement newElement = fRootElement.addChild(TestModelChange.INSERT
                | TestModelChange.REVEAL);
        assertNotNull("new sibling is visible", fViewer
                .testFindItem(newElement));
    }

    public void testInsertSiblings() {
        TestElement[] newElements = fRootElement
                .addChildren(TestModelChange.INSERT);
        processEvents();
        for (TestElement newElement : newElements) {
			assertNotNull("new siblings are visible", fViewer
                    .testFindItem(newElement));
		}
    }

    public void testInsertSiblingSelectExpanded() {
        TestElement newElement = fRootElement.addChild(TestModelChange.INSERT
                | TestModelChange.REVEAL | TestModelChange.SELECT);
        processEvents();
        assertNotNull("new sibling is visible", fViewer
                .testFindItem(newElement));
        assertSelectionEquals("new element is selected", newElement);
    }

    public void testInsertSiblingWithFilterFiltered() {
        fViewer.addFilter(new TestLabelFilter());
        TestElement newElement = new TestElement(fModel, fRootElement);
        newElement.setLabel("name-111");
        fRootElement.addChild(newElement, new TestModelChange(
                TestModelChange.INSERT | TestModelChange.REVEAL
                        | TestModelChange.SELECT, fRootElement, newElement));
        assertNull("new sibling is not visible", fViewer
                .testFindItem(newElement));
        assertTrue(getItemCount() == 5);
    }

    public void testInsertSiblingWithFilterNotFiltered() {
        fViewer.addFilter(new TestLabelFilter());
        TestElement newElement = new TestElement(fModel, fRootElement);
        newElement.setLabel("name-222");
        fRootElement.addChild(newElement, new TestModelChange(
                TestModelChange.INSERT | TestModelChange.REVEAL
                        | TestModelChange.SELECT, fRootElement, newElement));
        assertNotNull("new sibling is visible", fViewer
                .testFindItem(newElement));
        assertTrue(getItemCount() == 6);
    }

    public void testInsertSiblingWithSorter() {
        fViewer.setSorter(new TestLabelSorter());
        TestElement newElement = new TestElement(fModel, fRootElement);
        newElement.setLabel("name-9999");
        fRootElement.addChild(newElement, new TestModelChange(
                TestModelChange.INSERT | TestModelChange.REVEAL
                        | TestModelChange.SELECT, fRootElement, newElement));
        String newLabel = newElement.toString();
        assertEquals("sorted first", newLabel, getItemText(0));
        assertSelectionEquals("new element is selected", newElement);
    }

    public void testLabelProvider() {
        // BUG: non-polymorphic behaviour
        // if (fViewer instanceof TableViewer || fViewer instanceof TableTreeViewer)
        // 	return;
        fViewer.setLabelProvider(getTestLabelProvider());
        TestElement first = fRootElement.getFirstChild();
        String newLabel = providedString(first);
        assertEquals("rendered label", newLabel, getItemText(0));
    }

    /**
     * @return IBaseLabelProvder used in this test
     */
    public IBaseLabelProvider getTestLabelProvider() {
        return new TestLabelProvider();
    }

    public void testLabelProviderStateChange() {
        // BUG: non-polymorphic behaviour
        // if (fViewer instanceof TableViewer || fViewer instanceof TableTreeViewer)
        // 	return;
        TestLabelProvider provider = new TestLabelProvider();
        fViewer.setLabelProvider(provider);
        provider.setSuffix("added suffix");
        TestElement first = fRootElement.getFirstChild();
        String newLabel = providedString(first);
        assertEquals("rendered label", newLabel, getItemText(0));
    }

    public void testRename() {
        TestElement first = fRootElement.getFirstChild();
        String newLabel = first.getLabel() + " changed";
        first.setLabel(newLabel);
        assertEquals("changed label", first.getID() + " " + newLabel,
                getItemText(0));
    }

    public void testRenameWithFilter() {
        fViewer.addFilter(new TestLabelFilter());
        TestElement first = fRootElement.getFirstChild();
        first.setLabel("name-1111"); // should disappear
        assertNull("changed sibling is not visible", fViewer
                .testFindItem(first));
        first.setLabel("name-2222"); // should reappear
        fViewer.refresh();
        assertNotNull("changed sibling is not visible", fViewer
                .testFindItem(first));
    }

    public void testRenameWithLabelProvider() {
        if (fViewer instanceof TableViewer
                || fViewer instanceof TableTreeViewer) {
			return;
		}
        fViewer.setLabelProvider(new TestLabelProvider());
        TestElement first = fRootElement.getFirstChild();
        first.setLabel("changed name");
        String newLabel = providedString(first);
        assertEquals("rendered label", newLabel, getItemText(0));
    }

    public void testRenameWithSorter() {
        fViewer.setSorter(new TestLabelSorter());
        TestElement first = fRootElement.getFirstChild();
        first.setLabel("name-9999");
        String newElementLabel = first.toString();
        assertEquals("sorted first", newElementLabel, getItemText(0));
    }

    public void testSetInput() {
        TestElement first = fRootElement.getFirstChild();
        TestElement firstfirst = first.getFirstChild();

        fRootElement = first;
        setInput();
        processEvents();
        assertNotNull("first child is visible", fViewer
                .testFindItem(firstfirst));
    }

    public void testSetSelection() {
        TestElement first = fRootElement.getFirstChild();
        StructuredSelection selection = new StructuredSelection(first);
        fViewer.setSelection(selection);
        IStructuredSelection result = (IStructuredSelection) fViewer
                .getSelection();
        assertTrue(result.size() == 1);
        assertTrue(result.getFirstElement() == first);
    }

    public void testSomeChildrenChanged() {
		bulkChange(new TestModelChange(TestModelChange.STRUCTURE_CHANGE,
				fRootElement));
    }

    public void testSorter() {
        TestElement first = fRootElement.getFirstChild();
        TestElement last = fRootElement.getLastChild();
        int size = fRootElement.getChildCount();

        String firstLabel = first.toString();
        String lastLabel = last.toString();
        assertEquals("unsorted", firstLabel, getItemText(0));
        assertEquals("unsorted", lastLabel, getItemText(size - 1));
        fViewer.setSorter(new TestLabelSorter());
        assertEquals("reverse sorted", firstLabel, getItemText(size - 1));
        assertEquals("reverse sorted", lastLabel, getItemText(0));

        fViewer.setSorter(null);
        assertEquals("unsorted", firstLabel, getItemText(0));
        assertEquals("unsorted", lastLabel, getItemText(size - 1));
    }

    public void testWorldChanged() {
		bulkChange(new TestModelChange(TestModelChange.STRUCTURE_CHANGE, null));
    }
}
