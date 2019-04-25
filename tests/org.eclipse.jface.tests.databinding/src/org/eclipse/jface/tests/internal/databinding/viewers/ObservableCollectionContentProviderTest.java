/*******************************************************************************
 * Copyright (c) 2008, 2017 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 266038)
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.viewers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.DisposeEventTracker;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class ObservableCollectionContentProviderTest extends
		AbstractDefaultRealmTestCase {
	private Shell shell;
	private TableViewer viewer;
	ObservableListContentProvider<String> contentProvider;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		shell = new Shell();
		viewer = new TableViewer(shell);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		shell.dispose();
		shell = null;
		viewer = null;
		super.tearDown();
	}

	@Test
	public void testGetKnownElements_DisposedWithoutModificationOnContentProviderDispose() {
		final IObservableList<String> input = new WritableList<>(Collections.singletonList("element"), null);
		contentProvider = new ObservableListContentProvider<>();
		contentProvider.inputChanged(viewer, null, input);

		IObservableSet<String> knownElements = contentProvider.getKnownElements();

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

	@Test
	public void testKnownElementsAreFilledOnSettingAFilledCollectionAsInput() {
		final IObservableList<String> input = new WritableList<>(Collections.singletonList("element"), null);
		contentProvider = new ObservableListContentProvider<>();
		contentProvider.inputChanged(viewer, null, input);

		IObservableSet<String> knownElements = contentProvider.getKnownElements();
		assertEquals(1, knownElements.size());
		assertTrue(knownElements.containsAll(input));
	}
}
