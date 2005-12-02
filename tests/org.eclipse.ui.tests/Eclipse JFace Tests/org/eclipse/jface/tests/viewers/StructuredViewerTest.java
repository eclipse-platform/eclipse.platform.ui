/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
        public boolean select(Viewer viewer, Object parent, Object element) {
            String label = ((TestElement) element).getLabel();
            int count = label.indexOf("-");
            if (count < 0)
                return false;
            String number = label.substring(count + 1);
            return ((Integer.parseInt(number) % 2) == 0);
        }

        public boolean isFilterProperty(Object element, String property) {
            return property.equals(IBasicPropertyConstants.P_TEXT);
        }
    }

    public static class TestLabelSorter extends ViewerSorter {
        public int compare(Viewer v, Object e1, Object e2) {
            // put greater labels first
            String name1 = ((TestElement) e1).getLabel();
            String name2 = ((TestElement) e2).getLabel();
            return name2.compareTo(name1);
        }

        public boolean isSorterProperty(Object element, String property) {
            return property.equals(IBasicPropertyConstants.P_TEXT);
        }
    }

    public static class TestLabelProvider extends LabelProvider {
        public static String fgSuffix = "";

        static Image fgImage = ImageDescriptor.createFromFile(
                TestLabelProvider.class, "images/java.gif").createImage();

        public String getText(Object element) {
            return providedString((TestElement) element);
        }

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

    public void testFilter() {
        ViewerFilter filter = new TestLabelFilter();
        fViewer.addFilter(filter);
        assertTrue("filtered count", getItemCount() == 5);
        fViewer.removeFilter(filter);
        assertTrue("unfiltered count", getItemCount() == 10);

    }

    public void testInsertChild() {
        TestElement first = fRootElement.getFirstChild();
        TestElement newElement = first.addChild(TestModelChange.INSERT);
        assertNull("new sibling is not visible", fViewer
                .testFindItem(newElement));
    }

    public void testInsertSibling() {
        TestElement newElement = fRootElement.addChild(TestModelChange.INSERT);
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
        for (int i = 0; i < newElements.length; ++i)
            assertNotNull("new siblings are visible", fViewer
                    .testFindItem(newElements[i]));
    }

    public void testInsertSiblingSelectExpanded() {
        TestElement newElement = fRootElement.addChild(TestModelChange.INSERT
                | TestModelChange.REVEAL | TestModelChange.SELECT);
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
                || fViewer instanceof TableTreeViewer)
            return;
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
