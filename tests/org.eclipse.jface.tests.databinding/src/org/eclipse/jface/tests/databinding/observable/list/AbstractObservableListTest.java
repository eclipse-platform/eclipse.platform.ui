/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.observable.list;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.AbstractObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.jface.tests.databinding.util.RealmTester;

/**
 * @since 3.2
 */
public class AbstractObservableListTest extends TestCase {
	private AbstractObservableListStub list;

	protected void setUp() throws Exception {
		Realm.setDefault(new RealmTester.CurrentRealm());	
		list = new AbstractObservableListStub();
	}
	
	protected void tearDown() throws Exception {
		Realm.setDefault(null);
	}

	public void testFireChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				list.fireChange();
			}
		});
	}

	public void testFireStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				list.fireStale();
			}
		});
	}

	public void testFireListChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				list.fireListChange(null);
			}
		});
	}

	static class AbstractObservableListStub extends AbstractObservableList {
		protected int doGetSize() {
			return 0;
		}

		public Object get(int arg0) {
			return null;
		}

		public Object getElementType() {
			return null;
		}

		protected void fireChange() {
			super.fireChange();
		}

		protected void fireStale() {
			super.fireStale();
		}

		protected void fireListChange(ListDiff diff) {
			super.fireListChange(diff);
		}
	}
}
