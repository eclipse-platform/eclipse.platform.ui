/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 167204
 *******************************************************************************/

package org.eclipse.core.tests.databinding.observable.list;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.jface.conformance.databinding.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.conformance.databinding.ObservableListContractTest;
import org.eclipse.jface.conformance.databinding.SuiteBuilder;
import org.eclipse.jface.tests.databinding.RealmTester;
import org.eclipse.jface.tests.databinding.RealmTester.CurrentRealm;

/**
 * @since 3.2
 */
public class ObservableListTest extends TestCase {
	private ObservableListStub list;

	protected void setUp() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));

		list = new ObservableListStub(new ArrayList(0), Object.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		RealmTester.setDefault(null);
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

	public static Test suite() {
		return new SuiteBuilder().addTests(ObservableListTest.class)
				.addObservableContractTest(ObservableListContractTest.class,
						new Delegate()).build();
	}
	
	/* package */ static class Delegate extends AbstractObservableCollectionContractDelegate {
		public IObservableCollection createObservableCollection(Realm realm, final int elementCount) {
			List wrappedList = new ArrayList();
			for (int i = 0; i < elementCount; i++) {
				wrappedList.add(String.valueOf(i));
			}
			
			return new ObservableListStub(realm, wrappedList, String.class);
		}
		
		public void change(IObservable observable) {
			ObservableListStub list = (ObservableListStub) observable;
			Object element = "element";
			list.wrappedList.add(element);
			list.fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(list.size(), true, element)));
		}
		
		public Object getElementType(IObservableCollection collection) {
			return String.class;
		}
	}

	/* package */static class ObservableListStub extends ObservableList {
		List wrappedList;
		ObservableListStub(Realm realm, List wrappedList, Object elementType) {
			super(realm, wrappedList, elementType);
			this.wrappedList = wrappedList;
		}
		
		ObservableListStub(List wrappedList, Object elementType) {
			super(wrappedList, elementType);
			this.wrappedList = wrappedList;
		}
		
		protected void fireListChange(ListDiff diff) {
			super.fireListChange(diff);
		}
	}
}
