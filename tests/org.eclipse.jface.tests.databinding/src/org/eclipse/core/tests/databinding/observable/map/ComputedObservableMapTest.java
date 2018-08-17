/*******************************************************************************
 * Copyright (c) 2008, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 247394)
 *     Matthew Hall - bug 266754
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.map.ComputedObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.tests.internal.databinding.beans.Bean;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class ComputedObservableMapTest extends AbstractDefaultRealmTestCase {
	private IObservableSet keySet;
	private ComputedObservableMapStub map;
	private String propertyName;
	private Bean bean;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		keySet = new WritableSet();
		map = new ComputedObservableMapStub(keySet);
		propertyName = "value";
		bean = new Bean("a");
	}

	@Test
	public void testGet_ElementNotInKeySet() {
		assertNull(map.get(bean));
	}

	@Test
	public void testGet_ElementInKeySet() {
		keySet.add(bean);
		assertEquals("a", map.get(bean));
	}

	@Test
	public void testPut_ElementNotInKeySet() {
		assertNull(map.put(bean, "b"));
		assertEquals("a", bean.getValue());
	}

	@Test
	public void testPut_ElementInKeySet() {
		keySet.add(bean);
		assertEquals("a", map.put(bean, "b"));
		assertEquals("b", map.get(bean));
	}

	@Test
	public void testAddToKeySet_BeforeFirstListenerAdded_DoesNotAddListenerToKey() {
		assertFalse(bean.hasListeners(propertyName));
		keySet.add(bean);
		assertFalse(bean.hasListeners(propertyName));
	}

	@Test
	public void testAddToKeySet_AfterFirstListenerAdded_AddsListenerToKey() {
		ChangeEventTracker.observe(map);
		assertFalse(bean.hasListeners(propertyName));
		keySet.add(bean);
		assertTrue(bean.hasListeners(propertyName));
	}

	@Test
	public void testRemoveFromKeySet_RemovesListenersFromKey() {
		ChangeEventTracker.observe(map);
		keySet.add(bean);
		assertTrue(bean.hasListeners(propertyName));
		keySet.remove(bean);
		assertFalse(bean.hasListeners(propertyName));
	}

	@Test
	public void testRemoveLastListener_DoNotDiscardKeySet() {
		IChangeListener listener = event -> {
			// do nothing
		};
		map.addChangeListener(listener); // first listener added
		map.removeChangeListener(listener); // last listener removed
		keySet.add(bean);
		assertEquals(1, map.size());
	}

	@Test
	public void testDispose_RemoveListenersFromKeySetElements() {
		ChangeEventTracker.observe(map);
		keySet.add(bean);
		assertTrue(bean.hasListeners(propertyName));
		map.dispose();
		assertFalse(bean.hasListeners(propertyName));
	}

	@Test
	public void testDisposeKeySet_DisposesMap() {
		assertFalse(map.isDisposed());
		keySet.dispose();
		assertTrue(map.isDisposed());
	}

	@Test
	public void testDisposeKeySet_RemoveListenersFromKeySetElements() {
		ChangeEventTracker.observe(map);
		keySet.add(bean);
		assertTrue(bean.hasListeners(propertyName));
		keySet.dispose();
		assertFalse(bean.hasListeners(propertyName));
	}

	static class ComputedObservableMapStub extends ComputedObservableMap {
		private PropertyChangeListener listener = evt -> fireSingleChange(evt.getSource(), evt.getOldValue(),
				evt.getNewValue());

		ComputedObservableMapStub(IObservableSet keySet) {
			super(keySet);
		}

		@Override
		protected Object doGet(Object key) {
			return ((Bean) key).getValue();
		}

		@Override
		protected Object doPut(Object key, Object value) {
			Object result = doGet(key);
			((Bean) key).setValue((String) value);
			return result;
		}

		@Override
		protected void hookListener(Object addedKey) {
			((Bean) addedKey).addPropertyChangeListener(listener);
		}

		@Override
		protected void unhookListener(Object removedKey) {
			((Bean) removedKey).removePropertyChangeListener(listener);
		}
	}
}
