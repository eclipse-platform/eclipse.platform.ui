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

package org.eclipse.core.tests.databinding.observable.map;

import java.util.Collections;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.jface.tests.databinding.RealmTester;
import org.eclipse.jface.tests.databinding.RealmTester.CurrentRealm;

/**
 * @since 3.2
 * 
 */
public class WritableMapTest extends TestCase {
	protected void setUp() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
	}

	protected void tearDown() throws Exception {
		RealmTester.setDefault(null);
	}

	public void testPutRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableMap map = new WritableMap();
				map.put("", "");
			}
		});
	}

	public void testRemoveRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableMap map = new WritableMap();
				CurrentRealm realm = (CurrentRealm) Realm.getDefault();
				boolean current = realm.isCurrent();
				realm.setCurrent(true);
				map.put("", "");
				realm.setCurrent(current);

				map.remove("");
			}
		});
	}

	public void testClearRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableMap map = new WritableMap();
				map.clear();
			}
		});
	}

	public void testPutAllRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableMap map = new WritableMap();
				map.putAll(Collections.EMPTY_MAP);
			}
		});
	}
}
