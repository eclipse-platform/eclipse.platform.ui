/*******************************************************************************
 * Copyright (c) 2006, 2018 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bugs 184830, 233306
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.MapChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class WritableMapTest {
	@Before
	public void setUp() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
	}

	@After
	public void tearDown() throws Exception {
		RealmTester.setDefault(null);
	}

	@Test
	public void testPutRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			WritableMap<String, String> map = new WritableMap<>();
			map.put("", "");
		});
	}

	@Test
	public void testRemoveRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			WritableMap<String, String> map = new WritableMap<>();
			CurrentRealm realm = (CurrentRealm) Realm.getDefault();
			boolean current = realm.isCurrent();
			realm.setCurrent(true);
			map.put("", "");
			realm.setCurrent(current);

			map.remove("");
		});
	}

	@Test
	public void testClearRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			WritableMap<?, ?> map = new WritableMap<>();
			map.clear();
		});
	}

	@Test
	public void testPutAllRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			WritableMap<Object, Object> map = new WritableMap<>();
			map.putAll(Collections.emptyMap());
		});
	}

	@Test
	public void testPutWithExistingKeyMapChangeEvent() throws Exception {
		WritableMap<String, String> map = new WritableMap<>();
		String key = "key";
		String value = "value";
		map.put(key, value);

		MapChangeEventTracker<String, String> listener = new MapChangeEventTracker<>();
		map.addMapChangeListener(listener);

		assertEquals(0, listener.count);

		String newValue = "new value";
		map.put(key, newValue);

		assertEquals(1, listener.count);
		MapChangeEvent<?, ?> event = listener.event;


		Set<?> changedKeys = event.diff.getChangedKeys();
		assertEquals(1, changedKeys.size());
		assertTrue(changedKeys.contains(key));
		assertEquals(value, event.diff.getOldValue(key));
		assertEquals(newValue, event.diff.getNewValue(key));
	}

	@Test
	public void testPutSameValue_NoMapChangeEvent() {
		WritableMap<Object, String> map = new WritableMap<>();
		Object key = new Object();
		String value = "value";
		map.put(key, value);

		MapChangeEventTracker<Object, String> tracker = MapChangeEventTracker.observe(map);

		assertEquals(0, tracker.count);

		String equalValue = new String("value");
		map.put(key, equalValue);

		assertEquals(0, tracker.count);

	}

	@Test
	public void testPutNullKey_SingleAdditionChangeEvent() {
		WritableMap<Object, Object> map = new WritableMap<>();
		MapChangeEventTracker<Object, Object> tracker = MapChangeEventTracker.observe(map);

		assertEquals(0, tracker.count);

		Object key = null;
		Object value = new Object();
		map.put(key, value);

		assertEquals(1, tracker.count);
		MapDiff<?, ?> diff = tracker.event.diff;
		assertEquals(Collections.singleton(key), diff.getAddedKeys());
		assertEquals(Collections.emptySet(), diff.getChangedKeys());
		assertEquals(Collections.emptySet(), diff.getRemovedKeys());
		assertEquals(value, diff.getNewValue(key));
	}

	@Test
	public void testRemoveNullKey_SingleRemovalChangeEvent() {
		WritableMap<Object, Object> map = new WritableMap<>();
		Object key = null;
		Object value = new Object();
		map.put(key, value);

		MapChangeEventTracker<Object, Object> tracker = MapChangeEventTracker.observe(map);

		map.remove(key);

		assertEquals(1, tracker.count);
		MapDiff<?, ?> diff = tracker.event.diff;
		assertEquals(Collections.emptySet(), diff.getAddedKeys());
		assertEquals(Collections.emptySet(), diff.getChangedKeys());
		assertEquals(Collections.singleton(key), diff.getRemovedKeys());
		assertEquals(value, diff.getOldValue(key));
	}

	@Test
	public void testPutNullValue_SingleAdditionChangeEvent() {
		WritableMap<Object, Object> map = new WritableMap<>();

		MapChangeEventTracker<Object, Object> tracker = MapChangeEventTracker.observe(map);

		assertEquals(0, tracker.count);

		Object key = new Object();
		Object value = null;
		map.put(key, value);

		assertEquals(1, tracker.count);
		MapDiff<?, ?> diff = tracker.event.diff;
		assertEquals(Collections.singleton(key), diff.getAddedKeys());
		assertEquals(Collections.EMPTY_SET, diff.getChangedKeys());
		assertEquals(Collections.EMPTY_SET, diff.getRemovedKeys());
		assertEquals(value, diff.getNewValue(key));
	}

	@Test
	public void testPutNullOverNonNullValue_SingleChangeEvent() {
		WritableMap<Object, Object> map = new WritableMap<>();
		Object key = new Object();
		Object oldValue = new Object();
		map.put(key, oldValue);

		MapChangeEventTracker<Object, Object> tracker = MapChangeEventTracker.observe(map);

		Object newValue = null;
		map.put(key, newValue);

		assertEquals(1, tracker.count);
		MapDiff<?, ?> diff = tracker.event.diff;
		assertEquals(Collections.emptySet(), diff.getAddedKeys());
		assertEquals(Collections.singleton(key), diff.getChangedKeys());
		assertEquals(Collections.emptySet(), diff.getRemovedKeys());
		assertEquals(oldValue, diff.getOldValue(key));
		assertEquals(newValue, diff.getNewValue(key));
	}

	@Test
	public void testPutNonNullOverNullValue_SingleChangeEvent() {
		WritableMap<Object, Object> map = new WritableMap<>();
		Object key = new Object();
		Object oldValue = null;
		map.put(key, oldValue);

		MapChangeEventTracker<Object, Object> tracker = MapChangeEventTracker.observe(map);

		Object newValue = new Object();
		map.put(key, newValue);

		assertEquals(1, tracker.count);
		MapDiff<?, ?> diff = tracker.event.diff;
		assertEquals(Collections.emptySet(), diff.getAddedKeys());
		assertEquals(Collections.singleton(key), diff.getChangedKeys());
		assertEquals(Collections.emptySet(), diff.getRemovedKeys());
		assertEquals(oldValue, diff.getOldValue(key));
		assertEquals(newValue, diff.getNewValue(key));
	}

	@Test
	public void testRemoveNullValue_SingleRemovalChangeEvent() {
		WritableMap<Object, Object> map = new WritableMap<>();
		Object key = new Object();
		Object value = null;
		map.put(key, value);

		MapChangeEventTracker<Object, Object> tracker = MapChangeEventTracker.observe(map);

		map.remove(key);

		assertEquals(1, tracker.count);
		MapDiff<?, ?> diff = tracker.event.diff;
		assertEquals(Collections.emptySet(), diff.getAddedKeys());
		assertEquals(Collections.emptySet(), diff.getChangedKeys());
		assertEquals(Collections.singleton(key), diff.getRemovedKeys());
		assertEquals(value, diff.getOldValue(key));
	}

	@Test
	public void testPutAllNullValue_SingleAdditionChangeEvent() {
		WritableMap<Object, Object> map = new WritableMap<>();

		MapChangeEventTracker<Object, Object> tracker = MapChangeEventTracker.observe(map);

		Object key = new Object();
		Object value = null;
		Map<Object, Object> other = new HashMap<>();
		other.put(key, value);
		map.putAll(other);

		assertEquals(1, tracker.count);
		MapDiff<?, ?> diff = tracker.event.diff;
		assertEquals(Collections.singleton(key), diff.getAddedKeys());
		assertEquals(Collections.emptySet(), diff.getChangedKeys());
		assertEquals(Collections.emptySet(), diff.getRemovedKeys());
		assertEquals(value, diff.getNewValue(key));
	}

	@Test
	public void testPutAllNullValueToNonNullValue_SingleChangeEvent() {
		WritableMap<Object, Object> map = new WritableMap<>();
		Object key = new Object();
		Object oldValue = null;
		map.put(key, oldValue);

		MapChangeEventTracker<Object, Object> tracker = MapChangeEventTracker.observe(map);

		Object newValue = new Object();
		Map<Object, Object> other = new HashMap<>();
		other.put(key, newValue);
		map.putAll(other);

		assertEquals(1, tracker.count);
		MapDiff<?, ?> diff = tracker.event.diff;
		assertEquals(Collections.emptySet(), diff.getAddedKeys());
		assertEquals(Collections.singleton(key), diff.getChangedKeys());
		assertEquals(Collections.emptySet(), diff.getRemovedKeys());
		assertEquals(oldValue, diff.getOldValue(key));
		assertEquals(newValue, diff.getNewValue(key));
	}

	@Test
	public void testPutAllNonNullValueToNullValue_SingleChangeEvent() {
		WritableMap<Object, Object> map = new WritableMap<>();
		Object key = new Object();
		Object oldValue = new Object();
		map.put(key, oldValue);

		MapChangeEventTracker<Object, Object> tracker = MapChangeEventTracker.observe(map);

		Object newValue = null;
		Map<Object, Object> other = new HashMap<>();
		other.put(key, newValue);
		map.putAll(other);

		assertEquals(1, tracker.count);
		MapDiff<?, ?> diff = tracker.event.diff;
		assertEquals(Collections.emptySet(), diff.getAddedKeys());
		assertEquals(Collections.singleton(key), diff.getChangedKeys());
		assertEquals(Collections.emptySet(), diff.getRemovedKeys());
		assertEquals(oldValue, diff.getOldValue(key));
		assertEquals(newValue, diff.getNewValue(key));
	}
}
