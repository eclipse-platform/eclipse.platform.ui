package org.eclipse.jface.tests.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;

public class CheckboxTreeViewerTest extends TreeViewerTest {
	public static class CheckboxTableTestLabelProvider
		extends TestLabelProvider
		implements ITableLabelProvider {

		public boolean fExtended = false;

		public String getText(Object element) {
			if (fExtended)
				return providedString((String) element);
			return element.toString();
		}
		public String getColumnText(Object element, int index) {
			if (fExtended)
				return providedString((TestElement) element);
			return element.toString();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}
	public CheckboxTreeViewerTest(String name) {
		super(name);
	}
	protected StructuredViewer createViewer(Composite parent) {
		fTreeViewer = new CheckboxTreeViewer(parent);
		fTreeViewer.setContentProvider(new TestModelContentProvider());
		return fTreeViewer;
	}
	public static void main(String args[]) {
		junit.textui.TestRunner.run(CheckboxTreeViewerTest.class);
	}
	public void testCheckSubtree() {
		CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
		TestElement first = fRootElement.getFirstChild();
		TestElement firstfirst = first.getFirstChild();
		TestElement firstfirstfirst = firstfirst.getFirstChild();
		fTreeViewer.expandToLevel(firstfirst, 0);

		ctv.setSubtreeChecked(first, true);
		assertTrue(ctv.getChecked(firstfirst));
		ctv.setSubtreeChecked(first, false);
		assertTrue(!ctv.getChecked(firstfirst));

		// uncheck invisible subtree
		assertTrue(ctv.setSubtreeChecked(firstfirstfirst, false));
		assertTrue(!ctv.getChecked(firstfirstfirst));
	}
	public void testGrayed() {
		CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
		TestElement element = fRootElement.getFirstChild();

		assertTrue(ctv.getGrayedElements().length == 0);
		assertTrue(!ctv.getGrayed(element));

		ctv.setGrayed(element, true);
		assertTrue(ctv.getGrayedElements().length == 1);
		assertTrue(ctv.getGrayed(element));

		ctv.setGrayed(element, false);
		assertTrue(ctv.getGrayedElements().length == 0);
		assertTrue(!ctv.getGrayed(element));
	}
	public void testParentGrayed() {
		CheckboxTreeViewer ctv = (CheckboxTreeViewer) fViewer;
		TestElement first = fRootElement.getFirstChild();
		TestElement firstfirst = first.getFirstChild();
		TestElement firstfirstfirst = firstfirst.getFirstChild();
		ctv.expandToLevel(firstfirstfirst, 0);

		ctv.setParentsGrayed(firstfirstfirst, true);
		Object[] elements = ctv.getGrayedElements();
		assertTrue(elements.length == 3);
		for (int i = 0; i < elements.length; ++i) {
			assertTrue(ctv.getGrayed(elements[i]));
		};

		assertTrue(elements[0] == first);
		assertTrue(elements[1] == firstfirst);
		assertTrue(elements[2] == firstfirstfirst);
		ctv.setParentsGrayed(firstfirstfirst, false);
	}
}
