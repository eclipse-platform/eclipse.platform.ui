/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ovidio Mallo - bug 247741
 *     Matthew Hall - bug 274450
 *******************************************************************************/

package org.eclipse.core.tests.databinding.observable.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.map.ObservableMap;
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
public class ObservableMapTest {
	ObservableMapStub map;

	@Before
	public void setUp() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
		map = new ObservableMapStub(new HashMap());
	}

	@After
	public void tearDown() throws Exception {
		RealmTester.setDefault(null);
	}

	@Test
	public void testDisposeMapChangeListeners() throws Exception {
		MapChangeEventTracker listener = MapChangeEventTracker.observe(map);

		assertEquals(0, listener.count);
		map.fireMapChange(null);
		assertEquals(1, listener.count);

		map.dispose();
		try {
			map.fireMapChange(null);
		} catch (Exception e) {
			// do nothing
		}

		assertEquals("listener should not have been notified", 1,
				listener.count);
	}

	@Test
	public void testIsStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> map.isStale());
	}

	@Test
	public void testSetStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> map.setStale(true));
	}

	@Test
	public void testFireMapChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> map.fireMapChange(null));
	}

	@Test
	public void testEquals() {
		assertTrue(map.equals(Collections.EMPTY_MAP));
	}

	static class ObservableMapStub extends ObservableMap {
		/**
		 * @param wrappedMap
		 */
		public ObservableMapStub(Map wrappedMap) {
			super(wrappedMap);
		}

		@Override
		protected void fireMapChange(MapDiff diff) {
			super.fireMapChange(diff);
		}
	}
}
