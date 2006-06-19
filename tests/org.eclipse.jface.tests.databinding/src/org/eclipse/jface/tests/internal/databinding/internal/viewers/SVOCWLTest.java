/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brad Reynolds - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.viewers;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jface.internal.databinding.internal.viewers.StructuredViewerObservableCollectionWithLabels;
import org.eclipse.jface.internal.databinding.provisional.conversion.IConverter;
import org.eclipse.jface.internal.databinding.provisional.observable.mapping.IMultiMapping;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 * 
 */
public class SVOCWLTest extends TestCase {
	private Shell shell;

	private TableViewer viewer;

	/**
	 * {@inheritDoc}
	 */
	protected void setUp() throws Exception {
		super.setUp();
		shell = new Shell();
		shell.setLayout(new FillLayout());
		viewer = new TableViewer(shell);
	}

	private static class SVOCWL extends
			StructuredViewerObservableCollectionWithLabels {
		Object[] addedElements = null;

		public SVOCWL(StructuredViewer structuredViewer) {
			super(structuredViewer);
		}

		protected void addToViewer(Object element) {
		}

		protected void addToViewer(Object[] elements) {
			addedElements = elements;
		}

		protected void addToViewer(int index, Object element) {
		}

		public void init(IMultiMapping labelMapping) {
		}

		protected void removeFromViewer(Object element) {
		}

		protected void removeFromViewer(Object[] elements) {
		}

		public void updateElements(Object[] elements) {
		}

		public void setModelToTargetConverters(IConverter[] converters) {
		}
	};

	/**
	 * Asserts the order that items are added to the viewer is maintained in
	 * {@link StructuredViewerObservableCollectionWithLabels#addAll(java.util.Collection)}.
	 */
	public void test_addAllOrder() {
		SVOCWL svocwl = new SVOCWL(viewer);

		List list = new ArrayList();
		for (int i = 0; i < 10; i++) {
			list.add(Integer.toString(i));
		}

		svocwl.addAll(list);
		assertNotNull(svocwl.addedElements);
		assertEquals(list.size(), svocwl.size());
		for (int i = 0; i < list.size(); i++) {
			assertEquals(list.get(i), svocwl.addedElements[i]);
		}
	}

	/**
	 * Asserts that if <code>null</code> is passed to addAll() a NPE is
	 * thrown.
	 */
	public void test_checkNull() {
		SVOCWL sv = new SVOCWL(viewer);

		try {
			sv.addAll(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
}
