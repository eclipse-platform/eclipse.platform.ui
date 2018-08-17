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
 *     Matthew Hall - bug 349038
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.map;

import java.util.Set;

import org.eclipse.core.databinding.observable.map.AbstractObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 */
public class AbstractObservableMapTest {
	private AbstractObservableMapStub map;

	@Before
	public void setUp() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
		map = new AbstractObservableMapStub();
	}

	@After
	public void tearDown() throws Exception {
		RealmTester.setDefault(null);
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
	public void testFireStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> map.fireStale());
	}

	@Test
	public void testFireChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> map.fireChange());
	}

	@Test
	public void testFireMapChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> map.fireMapChange(null));
	}

	@Test
	public void testAddListChangeListener_AfterDispose() {
		map.dispose();
		map.addMapChangeListener(event -> {
			// do nothing
		});
	}

	@Test
	public void testRemoveListChangeListener_AfterDispose() {
		map.dispose();
		map.removeMapChangeListener(event -> {
			// do nothing
		});
	}

	@Test
	public void testAddChangeListener_AfterDispose() {
		map.dispose();
		map.addChangeListener(event -> {
			// do nothing
		});
	}

	@Test
	public void testRemoveChangeListener_AfterDispose() {
		map.dispose();
		map.removeChangeListener(event -> {
			// do nothing
		});
	}

	@Test
	public void testAddStaleListener_AfterDispose() {
		map.dispose();
		map.addStaleListener(staleEvent -> {
			// do nothing
		});
	}

	@Test
	public void testRemoveStaleListener_AfterDispose() {
		map.dispose();
		map.removeStaleListener(staleEvent -> {
			// do nothing
		});
	}

	@Test
	public void testAddDisposeListener_AfterDispose() {
		map.dispose();
		map.addDisposeListener(event -> {
			// do nothing
		});
	}

	@Test
	public void testRemoveDisposeListener_AfterDispose() {
		map.dispose();
		map.removeDisposeListener(event -> {
			// do nothing
		});
	}

	@Test
	public void testHasListeners_AfterDispose() {
		map.dispose();
		map.hasListeners();
	}

	static class AbstractObservableMapStub extends AbstractObservableMap {
		@Override
		public Set entrySet() {
			return null;
		}

		@Override
		protected void fireChange() {
			super.fireChange();
		}

		@Override
		protected void fireMapChange(MapDiff diff) {
			super.fireMapChange(diff);
		}

		@Override
		protected void fireStale() {
			super.fireStale();
		}

		@Override
		protected synchronized boolean hasListeners() {
			return super.hasListeners();
		}
	}
}
