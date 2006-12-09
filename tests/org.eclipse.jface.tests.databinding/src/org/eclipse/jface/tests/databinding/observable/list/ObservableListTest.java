/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 167204
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.observable.list;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.jface.tests.databinding.util.RealmTester;
import org.eclipse.jface.tests.databinding.util.RealmTester.CurrentRealm;

/**
 * @since 3.2
 */
public class ObservableListTest extends TestCase {
	private ObservableListStub list;

	protected void setUp() throws Exception {
		Realm.setDefault(new CurrentRealm(true));
		
		list = new ObservableListStub(new ArrayList(0), Object.class);
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		Realm.setDefault(null);
	}
	
	public void testIsStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				list.isStale();
			}
		});
	}
	
	public void testSetStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				list.setStale(false);
			}
		});
	}
	
	public void testIteratorGetterCalled() throws Exception {
		final ObservableListStub list = new  ObservableListStub(new ArrayList(0), Object.class);
		
		IObservable[] observables = ObservableTracker.runAndMonitor(new Runnable() {
			public void run() {
				list.iterator();		
			}
		}, null, null);
		
		assertEquals("length", 1, observables.length);
		assertEquals("observable", list, observables[0]);
	}

	public void testListIteratorGetterCalled() throws Exception {
		ArrayList arrayList = new ArrayList();
		arrayList.add("");
		final ObservableListStub list = new  ObservableListStub(arrayList, Object.class);
		
		IObservable[] observables = ObservableTracker.runAndMonitor(new Runnable() {
			public void run() {
				list.listIterator();		
			}
		}, null, null);
		
		assertEquals("length", 1, observables.length);
		assertEquals("observable", list, observables[0]);		
	}
	
	public void testListIteratorByIndexGetterCalled() throws Exception {
		ArrayList arrayList = new ArrayList();
		arrayList.add("");
		final ObservableListStub list = new  ObservableListStub(arrayList, Object.class);
		
		IObservable[] observables = ObservableTracker.runAndMonitor(new Runnable() {
			public void run() {
				list.listIterator(1);		
			}
		}, null, null);
		
		assertEquals("length", 1, observables.length);
		assertEquals("observable", list, observables[0]);
	}
	
	static class ObservableListStub extends ObservableList {
		protected ObservableListStub(List wrappedList, Object elementType) {
			super(wrappedList, elementType);
		}		
	}
}
