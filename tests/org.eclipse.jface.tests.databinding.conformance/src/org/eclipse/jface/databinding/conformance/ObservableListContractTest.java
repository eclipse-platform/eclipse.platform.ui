/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 213145
 *******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.jface.databinding.conformance.delegate.IObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;

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

	private IObservableCollectionContractDelegate delegate;

	/**
	 * @param delegate
	 */
	public ObservableListContractTest(
			IObservableCollectionContractDelegate delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	public ObservableListContractTest(String testName,
			IObservableCollectionContractDelegate delegate) {
		super(testName, delegate);
		this.delegate = delegate;
	}

	protected void setUp() throws Exception {
		super.setUp();

		list = (IObservableList) getObservable();
	}

	public void testListIterator_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			public void run() {
				list.listIterator();
			}
		}, "List.listIterator()", list);
	}

	public void testGet_GetterCalled() throws Exception {
		list = (IObservableList) delegate.createObservableCollection(new CurrentRealm(true), 1);
		assertGetterCalled(new Runnable() {
			public void run() {
				list.get(0);
			}
		}, "List.get(int)", list);
	}

	public void testIndexOf_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			public void run() {
				list.indexOf(delegate.createElement(list));
			}
		}, "List.indexOf(int)", list);
	}

	public void testLastIndexOf_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			public void run() {
				list.lastIndexOf(delegate.createElement(list));
			}
		}, "List.lastIndexOf(Object)", list);
	}

	public void testListIteratorAtIndex_GetterCalled() throws Exception {
		// Create a new list instead of adding an item because the list might
		// not be mutable
		list = (IObservableList) delegate.createObservableCollection(new CurrentRealm(true), 1);
		assertGetterCalled(new Runnable() {
			public void run() {
				list.listIterator(0);
			}
		}, "List.listIterator(int)", list);
	}

	public void testSubList_GetterCalled() throws Exception {
		list = (IObservableList) delegate.createObservableCollection(new CurrentRealm(true), 1);
		assertGetterCalled(new Runnable() {
			public void run() {
				list.subList(0, 1);
			}
		}, "List.subList(int, int)", list);
	}

	public static Test suite(IObservableCollectionContractDelegate delegate) {
		return new SuiteBuilder().addObservableContractTest(
				ObservableListContractTest.class, delegate).build();
	}
}
