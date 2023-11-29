/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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
 *     Matthew Hall - bug 213145
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 *******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.conformance.delegate.IObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for IObservableList that don't require mutating the collection.
 * <p>
 * This class is experimental and can change at any time. It is recommended to
 * not subclass or assume the test names will not change. The only API that is
 * guaranteed to not change are the constructors. The tests will remain public
 * and not final in order to allow for consumers to turn off a test if needed by
 * subclassing.
 * </p>
 *
 * @since 3.2
 */
public class ObservableListContractTest extends
		ObservableCollectionContractTest {
	private IObservableList list;

	private final IObservableCollectionContractDelegate delegate;

	public ObservableListContractTest(
			IObservableCollectionContractDelegate delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		list = (IObservableList) getObservable();
	}

	@Test
	public void testListIterator_GetterCalled() throws Exception {
		assertGetterCalled(() -> list.listIterator(), "List.listIterator()", list);
	}

	@Test
	public void testGet_GetterCalled() throws Exception {
		list = (IObservableList) delegate.createObservableCollection(
				new CurrentRealm(true), 1);
		assertGetterCalled(() -> list.get(0), "List.get(int)", list);
	}

	@Test
	public void testIndexOf_GetterCalled() throws Exception {
		assertGetterCalled(() -> list.indexOf(delegate.createElement(list)), "List.indexOf(int)", list);
	}

	@Test
	public void testLastIndexOf_GetterCalled() throws Exception {
		assertGetterCalled(() -> list.lastIndexOf(delegate.createElement(list)), "List.lastIndexOf(Object)", list);
	}

	@Test
	public void testListIteratorAtIndex_GetterCalled() throws Exception {
		// Create a new list instead of adding an item because the list might
		// not be mutable
		list = (IObservableList) delegate.createObservableCollection(
				new CurrentRealm(true), 1);
		assertGetterCalled(() -> list.listIterator(0), "List.listIterator(int)", list);
	}

	@Test
	public void testSubList_GetterCalled() throws Exception {
		list = (IObservableList) delegate.createObservableCollection(
				new CurrentRealm(true), 1);
		assertGetterCalled(() -> list.subList(0, 1), "List.subList(int, int)", list);
	}

}
