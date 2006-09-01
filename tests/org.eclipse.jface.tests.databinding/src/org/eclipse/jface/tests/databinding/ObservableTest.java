/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.databinding;

import junit.framework.TestCase;

import org.eclipse.jface.databinding.observable.Diffs;
import org.eclipse.jface.databinding.observable.IChangeListener;
import org.eclipse.jface.databinding.observable.value.WritableValue;
import org.eclipse.jface.tests.databinding.util.Mocks;

public class ObservableTest extends TestCase {

	private static class MyObservable extends WritableValue {
		/**
		 * @param initialValue
		 */
		public MyObservable(Object initialValue) {
			super(initialValue);
		}

		public void fireChange(Object oldValue, Object newValue) {
			fireValueChange(Diffs.createValueDiff(oldValue, newValue));
		}

	}

	private MyObservable observable;

	protected void setUp() throws Exception {
		super.setUp();
		observable = new MyObservable(null);
	}

	protected void tearDown() throws Exception {
		if (observable != null) {
			try {
				observable.dispose();
			} catch (Exception ex) {
				// ignore
			}
		}
		observable = null;
		super.tearDown();
	}

	/*
	 * Test method for
	 * 'org.eclipse.jface.databinding.Observable.addChangeListener(IChangeListener)'
	 */
	public void testAddChangeListener() {
		IChangeListener changeListenerMock = (IChangeListener) Mocks
				.createOrderedMock(IChangeListener.class);

		// testing that no methods on the observable are called when adding the
		// change listener
		Mocks.startChecking(changeListenerMock);
		observable.addChangeListener(changeListenerMock);

		// testing that handleChange is actually called
		Mocks.reset(changeListenerMock);
		changeListenerMock.handleChange(null);
		changeListenerMock.handleChange(null);
		Mocks.startChecking(changeListenerMock);
		observable.fireChange(null, null);
		observable.fireChange(null, null);
		Mocks.verify(changeListenerMock);

		// testing that handleChange is called just once
		Mocks.reset(changeListenerMock);
		changeListenerMock.handleChange(null);
		Mocks.startChecking(changeListenerMock);
		observable.fireChange(null, null);
		Mocks.verify(changeListenerMock);

		// dispose() will call another handleChange. Prevent this from causing a
		// test failure
		Mocks.reset(changeListenerMock);
		changeListenerMock.handleChange(null);
		Mocks.startChecking(changeListenerMock);
	}

	/*
	 * Test method for
	 * 'org.eclipse.jface.databinding.Observable.removeChangeListener(IChangeListener)'
	 */
	public void testRemoveChangeListener() {
		IChangeListener changeListenerMock = (IChangeListener) Mocks
				.createOrderedMock(IChangeListener.class);

		// testing that no methods on the observable are called when removing
		// the
		// change listener
		Mocks.startChecking(changeListenerMock);
		observable.removeChangeListener(changeListenerMock);

		// testing that no methods on the observable are called after adding and
		// removing the change listener
		observable.addChangeListener(changeListenerMock);
		observable.removeChangeListener(changeListenerMock);

		observable.fireChange(null, null);
	}

	/*
	 * Test method for
	 * 'org.eclipse.jface.databinding.Observable.fireChangeEvent(int, Object,
	 * Object)'
	 */
	public void testFireChangeEvent() {
		// IChangeListener changeListenerMock = (IChangeListener) Mocks
		// .createMock(IChangeListener.class,
		// new Mocks.EqualityComparator() {
		// public boolean equals(Object o1, Object o2) {
		// ChangeEvent changeEvent1 = (ChangeEvent) o1;
		// ChangeEvent changeEvent2 = (ChangeEvent) o2;
		// return changeEvent1.getChangeType() == changeEvent2
		// .getChangeType()
		// && changeEvent1.getPosition() == changeEvent2
		// .getPosition()
		// && changeEvent1.getOldValue() == changeEvent2
		// .getOldValue()
		// && changeEvent1.getNewValue() == changeEvent2
		// .getNewValue();
		// }
		// });
		// observable.addChangeListener(changeListenerMock);
		//
		// Object o1 = new Object();
		// Object o2 = new Object();
		//
		// changeListenerMock.handleChange(new ChangeEvent(observable, 0, null,
		// null, ChangeEvent.POSITION_UNKNOWN));
		// changeListenerMock.handleChange(new ChangeEvent(observable, 0, null,
		// null, 1));
		// changeListenerMock.handleChange(new ChangeEvent(observable,
		// ChangeEvent.CHANGE, o1, o2, ChangeEvent.POSITION_UNKNOWN));
		// changeListenerMock.handleChange(new ChangeEvent(observable,
		// ChangeEvent.CHANGE, o1, o2, 42));
		// Mocks.startChecking(changeListenerMock);
		// observable.fireChange(0, null, null);
		// observable.fireChange(0, null, null, 1);
		// observable.fireChange(ChangeEvent.CHANGE, o1, o2);
		// observable.fireChange(ChangeEvent.CHANGE, o1, o2, 42);
		// Mocks.verify(changeListenerMock);
		//
		// // dispose() will call another handleChange. Prevent this from
		// causing a test failure
		// Mocks.reset(changeListenerMock);
		// changeListenerMock.handleChange(null);
		// Mocks.startChecking(changeListenerMock);
	}

}
