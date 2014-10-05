/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 247394)
 *     Matthew Hall - bug 266754
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.map.ComputedObservableMap;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.tests.internal.databinding.beans.Bean;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

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
	protected void setUp() throws Exception {
		super.setUp();
		keySet = new WritableSet();
		map = new ComputedObservableMapStub(keySet);
		propertyName = "value";
		bean = new Bean("a");
	}

	public void testGet_ElementNotInKeySet() {
		assertNull(map.get(bean));
	}

	public void testGet_ElementInKeySet() {
		keySet.add(bean);
		assertEquals("a", map.get(bean));
	}

	public void testPut_ElementNotInKeySet() {
		assertNull(map.put(bean, "b"));
		assertEquals("a", bean.getValue());
	}

	public void testPut_ElementInKeySet() {
		keySet.add(bean);
		assertEquals("a", map.put(bean, "b"));
		assertEquals("b", map.get(bean));
	}

	public void testAddToKeySet_BeforeFirstListenerAdded_DoesNotAddListenerToKey() {
		assertFalse(bean.hasListeners(propertyName));
		keySet.add(bean);
		assertFalse(bean.hasListeners(propertyName));
	}

	public void testAddToKeySet_AfterFirstListenerAdded_AddsListenerToKey() {
		ChangeEventTracker.observe(map);
		assertFalse(bean.hasListeners(propertyName));
		keySet.add(bean);
		assertTrue(bean.hasListeners(propertyName));
	}

	public void testRemoveFromKeySet_RemovesListenersFromKey() {
		ChangeEventTracker.observe(map);
		keySet.add(bean);
		assertTrue(bean.hasListeners(propertyName));
		keySet.remove(bean);
		assertFalse(bean.hasListeners(propertyName));
	}

	public void testRemoveLastListener_DoNotDiscardKeySet() {
		IChangeListener listener = new IChangeListener() {
			@Override
			public void handleChange(ChangeEvent event) {
				// do nothing
			}
		};
		map.addChangeListener(listener); // first listener added
		map.removeChangeListener(listener); // last listener removed
		keySet.add(bean);
		assertEquals(1, map.size());
	}

	public void testDispose_RemoveListenersFromKeySetElements() {
		ChangeEventTracker.observe(map);
		keySet.add(bean);
		assertTrue(bean.hasListeners(propertyName));
		map.dispose();
		assertFalse(bean.hasListeners(propertyName));
	}

	public void testDisposeKeySet_DisposesMap() {
		assertFalse(map.isDisposed());
		keySet.dispose();
		assertTrue(map.isDisposed());
	}

	public void testDisposeKeySet_RemoveListenersFromKeySetElements() {
		ChangeEventTracker.observe(map);
		keySet.add(bean);
		assertTrue(bean.hasListeners(propertyName));
		keySet.dispose();
		assertFalse(bean.hasListeners(propertyName));
	}

	static class ComputedObservableMapStub extends ComputedObservableMap {
		private PropertyChangeListener listener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				fireSingleChange(evt.getSource(), evt.getOldValue(), evt
						.getNewValue());
			}
		};

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
