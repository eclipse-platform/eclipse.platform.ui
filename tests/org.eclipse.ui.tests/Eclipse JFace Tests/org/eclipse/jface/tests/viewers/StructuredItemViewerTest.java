package org.eclipse.jface.tests.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ICheckable;


public abstract class StructuredItemViewerTest extends StructuredViewerTest {

	public StructuredItemViewerTest(String name) {
		super(name);
	}
public void testCheckElement() {

	if (fViewer instanceof ICheckable) {
		TestElement first = fRootElement.getFirstChild();
		TestElement firstfirst = first.getFirstChild();

		ICheckable ctv = (ICheckable) fViewer;
		ctv.setChecked(first, true);
		assertTrue(ctv.getChecked(first));

		// checking an invisible element
		if (fViewer instanceof AbstractTreeViewer) {
			// The first child of the first child can only be resolved in a tree
			assertTrue(ctv.setChecked(firstfirst, true));
			assertTrue(ctv.getChecked(firstfirst));
		} else {
			assertTrue(!ctv.setChecked(firstfirst, true));
			assertTrue(!ctv.getChecked(firstfirst));
		}

		ctv.setChecked(first, false);
		assertTrue(!ctv.getChecked(first));
	}
}
}
