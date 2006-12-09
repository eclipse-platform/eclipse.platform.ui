/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.observable.list;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.jface.tests.databinding.util.RealmTester;

/**
 * @since 3.2
 */
public class ObservableListTest extends TestCase {
	private ObservableListStub list;

	protected void setUp() throws Exception {
		Realm.setDefault(new RealmTester.CurrentRealm());
		
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
	
	static class ObservableListStub extends ObservableList {
		protected ObservableListStub(List wrappedList, Object elementType) {
			super(wrappedList, elementType);
		}		
	}
}
