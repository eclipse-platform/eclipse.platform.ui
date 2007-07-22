/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.conformance.databinding;

import java.util.Arrays;

import org.eclipse.core.databinding.observable.IObservableCollection;

/**
 * Tests for IObservableCollection that don't mutate the collection.
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
public class ObservableCollectionContractTest extends ObservableContractTest {
	private IObservableCollectionContractDelegate delegate;

	private IObservableCollection collection;

	public ObservableCollectionContractTest(
			IObservableCollectionContractDelegate delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	public ObservableCollectionContractTest(String testName,
			IObservableCollectionContractDelegate delegate) {
		super(testName, delegate);
		this.delegate = delegate;
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		collection = (IObservableCollection) getObservable();
	}

	public void testIterator_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			public void run() {
				collection.iterator();
			}
		}, "Collection.iterator()", collection);
	}

	public void testSize_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			public void run() {
				collection.size();
			}
		}, "Collection.size()", collection);
	}

	public void testIsEmpty_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			public void run() {
				collection.isEmpty();
			}
		}, "Collection.isEmpty()", collection);
	}

	public void testContains_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			public void run() {
				collection.contains(delegate.createElement(collection));
			}
		}, "Collection.contains(...)", collection);
	}

	public void testContainsAll_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			public void run() {
				collection.containsAll(Arrays.asList(new Object[] { delegate
						.createElement(collection) }));
			}
		}, "Collection.containsAll(Collection)", collection);
	}

	public void testToArray_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			public void run() {
				collection.toArray();
			}
		}, "Collection.toArray()", collection);
	}

	public void testToArrayWithObjectArray_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			public void run() {
				collection.toArray(new Object[collection.size()]);
			}
		}, "Collection.toArray(Object[])", collection);
	}

	public void testEquals_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			public void run() {
				collection.equals(collection);
			}
		}, "Collection.equals(Object)", collection);
	}

	public void testHashCode_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			public void run() {
				collection.hashCode();
			}
		}, "Collection.hashCode()", collection);
	}

	public void testGetElementType() throws Exception {
		assertEquals(
				"Element type of the collection should be returned from IObservableCollection.getElementType()",
				delegate.getElementType(collection), collection
						.getElementType());
	}

	
}
