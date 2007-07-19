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

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.ObservableTracker;

/**
 * @since 3.2
 */
public class ObservableCollectionContractTest extends TestCase {
	private IObservableCollectionContractDelegate delegate;

	private IObservableCollection collection;

	public ObservableCollectionContractTest(
			IObservableCollectionContractDelegate delegate) {
		this.delegate = delegate;
	}

	public ObservableCollectionContractTest(String testName,
			IObservableCollectionContractDelegate delegate) {
		super(testName);
		this.delegate = delegate;
	}

	protected void setUp() throws Exception {
		collection = delegate.createObservableCollection();
	}

	protected IObservableCollection getObservableCollection() {
		return collection;
	}

	public void testIterator_GetterCalled() throws Exception {
		IObservable[] observables = ObservableTracker.runAndMonitor(
				new Runnable() {
					public void run() {
						collection.iterator();
					}
				}, null, null);

		assertEquals(
				"Collection.iterator() should invoke ObservableTracker.getterCalled() once.",
				1, observables.length);
	}
	
	public void testIterator_ObservableOfGetterCalled() throws Exception {
		IObservable[] observables = ObservableTracker.runAndMonitor(
				new Runnable() {
					public void run() {
						collection.iterator();
					}
				}, null, null);
		
		assertEquals(
				"Collection.iterator() should invoke ObservableTracker.getterCalled() for the Collection.",
				collection, observables[0]);		
	}

	public void testGetElementType_ExpectedType() throws Exception {
		assertEquals(
				"Element type of the collection should be returned from IObservableCollection.getElementType()",
				delegate.getElementType(collection), collection
						.getElementType());
	}
}
