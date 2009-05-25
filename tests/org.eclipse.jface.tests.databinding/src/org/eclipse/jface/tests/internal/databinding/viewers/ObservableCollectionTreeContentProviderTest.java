/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 239015)
 *     Matthew Hall - bug 266038
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.viewers;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.DisposeEventTracker;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 * 
 */
public class ObservableCollectionTreeContentProviderTest extends
		AbstractDefaultRealmTestCase {
	private Shell shell;
	private TreeViewer viewer;
	ObservableListTreeContentProvider contentProvider;

	protected void setUp() throws Exception {
		super.setUp();
		shell = new Shell();
		viewer = new TreeViewer(shell);
	}

	protected void tearDown() throws Exception {
		shell.dispose();
		shell = null;
		viewer = null;
		super.tearDown();
	}

	public void testGetKnownElements_ExcludesInput() {
		final Object input = new Object();
		Object[] rootElements = new Object[] { "one", "two", "three" };
		final IObservableList rootElementList = new WritableList(Arrays
				.asList(rootElements), null);
		contentProvider = new ObservableListTreeContentProvider(
				new IObservableFactory() {
					public IObservable createObservable(Object target) {
						if (target == input)
							return rootElementList;
						return null;
					}
				}, null);
		viewer.setContentProvider(contentProvider);
		viewer.setInput(input);
		contentProvider.getElements(input);

		IObservableSet knownElements = contentProvider.getKnownElements();
		assertFalse(knownElements.contains(input));
		assertEquals(new HashSet(Arrays.asList(rootElements)), knownElements);
	}

	public void testGetKnownElements_DisposedWithoutModificationOnContentProviderDispose() {
		final Object input = new Object();
		final IObservableList rootElementList = new WritableList(Collections
				.singletonList("element"), null);
		contentProvider = new ObservableListTreeContentProvider(
				new IObservableFactory() {
					public IObservable createObservable(Object target) {
						if (target == input)
							return rootElementList;
						return null;
					}
				}, null);
		contentProvider.inputChanged(viewer, null, input);

		IObservableSet knownElements = contentProvider.getKnownElements();

		// ensure there is an element in knownElements so we're sure that
		// "no modification" is not due to the set being already empty.
		contentProvider.getElements(input);
		assertEquals(1, knownElements.size());

		DisposeEventTracker disposeTracker = DisposeEventTracker
				.observe(knownElements);
		ChangeEventTracker changeTracker = ChangeEventTracker
				.observe(knownElements);

		contentProvider.dispose();

		assertEquals(0, changeTracker.count);
		assertEquals(1, disposeTracker.count);
		assertTrue(knownElements.isDisposed());
	}
}
