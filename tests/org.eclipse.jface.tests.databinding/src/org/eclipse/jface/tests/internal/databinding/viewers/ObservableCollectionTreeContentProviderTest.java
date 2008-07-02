/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 239015)
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.viewers;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.IObservableSet;
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

	private void createContentProvider(IObservableFactory factory) {
		contentProvider = new ObservableListTreeContentProvider(
				factory, null);
		viewer.setContentProvider(contentProvider);
	}

	public void testGetKnownElements_ExcludesInput() {
		final Object input = new Object();
		Object[] rootElements = new Object[] { "one", "two", "three" };
		final IObservableList rootElementList = new WritableList(Arrays
				.asList(rootElements), null);
		createContentProvider(new IObservableFactory() {
			public IObservable createObservable(Object target) {
				if (target == input)
					return rootElementList;
				return null;
			}
		});
		viewer.setInput(input);
		contentProvider.getElements(input);

		IObservableSet knownElements = contentProvider.getKnownElements();
		assertFalse(knownElements.contains(input));
		assertEquals(new HashSet(Arrays.asList(rootElements)), knownElements);
	}
}
