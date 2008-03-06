/*******************************************************************************
 * Copyright (c) 2006-2008 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 184830
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.map;

import java.util.Collections;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.MapChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.RealmTester;

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
	
	public void testPutWithExistingKeyMapChangeEvent() throws Exception {
		WritableMap map = new WritableMap();
		String key = "key";
		String value = "value";
		map.put(key, value);
		
		MapChangeEventTracker listener = new MapChangeEventTracker();
		map.addMapChangeListener(listener);
		
		assertEquals(0, listener.count);
		
		String newValue = "new value";
		map.put(key, newValue);
		
		assertEquals(1, listener.count);
		MapChangeEvent event = listener.event;
		
		
		Set changedKeys = event.diff.getChangedKeys();
		assertEquals(1, changedKeys.size());
		assertTrue(changedKeys.contains(key));
		assertEquals(value, event.diff.getOldValue(key));
		assertEquals(newValue, event.diff.getNewValue(key));
	}

	public void testPutSameValue_NoMapChangeEvent() {
		WritableMap map = new WritableMap();
		Object key = new Object();
		Object value = "value";
		map.put(key, value);

		MapChangeEventTracker tracker = MapChangeEventTracker.observe(map);

		assertEquals(0, tracker.count);

		Object equalValue = new String("value");
		map.put(key, equalValue);

		assertEquals(0, tracker.count);
		
	}
}
