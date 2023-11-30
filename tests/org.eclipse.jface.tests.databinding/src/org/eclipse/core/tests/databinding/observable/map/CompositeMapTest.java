/*******************************************************************************
 * Copyright (c) 2007, 2018 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.core.tests.databinding.observable.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collections;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.map.CompositeMap;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.databinding.conformance.util.MapChangeEventTracker;
import org.eclipse.jface.examples.databinding.model.SimpleCart;
import org.eclipse.jface.examples.databinding.model.SimplePerson;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 */
public class CompositeMapTest extends AbstractDefaultRealmTestCase {

	private WritableSet<SimplePerson> persons;
	private CompositeMap<SimplePerson, SimpleCart, Integer> composedMap;
	private IObservableMap<SimplePerson, SimpleCart> first;

	boolean hasListeners(AbstractObservable o) {
		try {
			Method method = AbstractObservable.class.getSuperclass().getDeclaredMethod("hasListeners", new Class[0]);
			method.setAccessible(true);
			return ((Boolean) method.invoke(o)).booleanValue();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		persons = new WritableSet<>();
		first = BeanProperties.value(SimplePerson.class, "cart", SimpleCart.class).observeDetail(persons);
		composedMap = new CompositeMap<>(first,
				target -> BeanProperties.value(SimpleCart.class, "numItems", Integer.class).observeDetail(target));
	}

	@Test
	public void testAddToFirstMap() {
		MapChangeEventTracker<Object, Object> tracker = new MapChangeEventTracker<>();
		composedMap.addMapChangeListener(tracker);
		assertEquals(0, tracker.count);
		SimplePerson newPerson = new SimplePerson("p1", "a1", "c1", "s1");
		newPerson.getCart().setNumItems(42);
		persons.add(newPerson);
		assertEquals(1, tracker.count);
		assertEquals(0, tracker.event.diff.getRemovedKeys().size());
		assertEquals(0, tracker.event.diff.getChangedKeys().size());
		assertEquals(Collections.singleton(newPerson), tracker.event.diff
				.getAddedKeys());
		assertEquals(Integer.valueOf(42), tracker.event.diff.getNewValue(newPerson));
		assertEquals(Integer.valueOf(42), composedMap.get(newPerson));
	}

	@Test
	public void testAddSharedToFirstMap() {
		SimplePerson person1 = new SimplePerson("p1", "a1", "c1", "s1");
		person1.getCart().setNumItems(42);
		persons.add(person1);
		MapChangeEventTracker<Object, Object> tracker = new MapChangeEventTracker<>();
		composedMap.addMapChangeListener(tracker);
		assertEquals(0, tracker.count);
		SimplePerson person2 = new SimplePerson("p1", "a1", "c1", "s1");
		person2.setCart(person1.getCart());
		persons.add(person2);
		assertEquals(1, tracker.count);
		assertEquals(0, tracker.event.diff.getRemovedKeys().size());
		assertEquals(0, tracker.event.diff.getChangedKeys().size());
		assertEquals(Collections.singleton(person2), tracker.event.diff
				.getAddedKeys());
		assertEquals(Integer.valueOf(42), tracker.event.diff.getNewValue(person2));
		assertEquals(Integer.valueOf(42), composedMap.get(person2));
		assertEquals(Integer.valueOf(42), composedMap.get(person1));
	}

	@Test
	public void testRemoveFromFirstMap() {
		MapChangeEventTracker<Object, Object> tracker = new MapChangeEventTracker<>();
		SimplePerson newPerson = new SimplePerson("p1", "a1", "c1", "s1");
		newPerson.getCart().setNumItems(42);
		persons.add(newPerson);
		assertTrue("newPerson should be added", composedMap.containsKey(newPerson));
		composedMap.addMapChangeListener(tracker);
		assertEquals(0, tracker.count);
		persons.remove(newPerson);
		assertEquals(1, tracker.count);
		assertEquals(0, tracker.event.diff.getAddedKeys().size());
		assertEquals(0, tracker.event.diff.getChangedKeys().size());
		assertEquals(Collections.singleton(newPerson), tracker.event.diff
				.getRemovedKeys());
		assertEquals(Integer.valueOf(42), tracker.event.diff.getOldValue(newPerson));
		assertFalse("newPerson should be removed", composedMap.containsKey(newPerson));
	}

	@Test
	public void testRemoveSharedFromFirstMap() {
		SimplePerson person1 = new SimplePerson("p1", "a1", "c1", "s1");
		person1.getCart().setNumItems(42);
		persons.add(person1);
		SimplePerson person2 = new SimplePerson("p1", "a1", "c1", "s1");
		person2.setCart(person1.getCart());
		persons.add(person2);
		assertTrue("person2 should be added", composedMap.containsKey(person2));
		MapChangeEventTracker<Object, Object> tracker = new MapChangeEventTracker<>();
		composedMap.addMapChangeListener(tracker);
		assertEquals(0, tracker.count);
		persons.remove(person2);
		assertEquals(1, tracker.count);
		assertEquals(0, tracker.event.diff.getAddedKeys().size());
		assertEquals(0, tracker.event.diff.getChangedKeys().size());
		assertEquals(Collections.singleton(person2), tracker.event.diff
				.getRemovedKeys());
		assertEquals(Integer.valueOf(42), tracker.event.diff.getOldValue(person2));
		assertFalse("person2 should be removed", composedMap.containsKey(person2));
		assertEquals(Integer.valueOf(42), composedMap.get(person1));
	}

	@Test
	public void testChangeInFirstMap() {
		SimplePerson person1 = new SimplePerson("p1", "a1", "c1", "s1");
		person1.getCart().setNumItems(42);
		persons.add(person1);
		MapChangeEventTracker<Object, Object> tracker = new MapChangeEventTracker<>();
		composedMap.addMapChangeListener(tracker);
		assertEquals(0, tracker.count);
		person1.setCart(new SimpleCart());
		assertEquals(1, tracker.count);
		assertEquals(0, tracker.event.diff.getAddedKeys().size());
		assertEquals(0, tracker.event.diff.getRemovedKeys().size());
		assertEquals(Collections.singleton(person1), tracker.event.diff
				.getChangedKeys());
		assertEquals(Integer.valueOf(42), tracker.event.diff.getOldValue(person1));
		assertEquals(Integer.valueOf(0), tracker.event.diff.getNewValue(person1));
		assertEquals(Integer.valueOf(0), composedMap.get(person1));
	}

	@Test
	public void testChangeInFirstMapToShared() {
		SimplePerson person0 = new SimplePerson("p0", "a0", "c0", "s0");
		person0.getCart().setNumItems(13);
		persons.add(person0);
		SimplePerson person1 = new SimplePerson("p1", "a1", "c1", "s1");
		person1.getCart().setNumItems(42);
		persons.add(person1);
		MapChangeEventTracker<Object, Object> tracker = new MapChangeEventTracker<>();
		composedMap.addMapChangeListener(tracker);
		assertEquals(0, tracker.count);
		person1.setCart(person0.getCart());
		assertEquals(1, tracker.count);
		assertEquals(0, tracker.event.diff.getAddedKeys().size());
		assertEquals(0, tracker.event.diff.getRemovedKeys().size());
		assertEquals(Collections.singleton(person1), tracker.event.diff
				.getChangedKeys());
		assertEquals(Integer.valueOf(42), tracker.event.diff.getOldValue(person1));
		assertEquals(Integer.valueOf(13), tracker.event.diff.getNewValue(person1));
		assertEquals(Integer.valueOf(13), composedMap.get(person1));
	}

	@Test
	public void testChangeInFirstMapFromShared() {
		SimplePerson person0 = new SimplePerson("p0", "a0", "c0", "s0");
		person0.getCart().setNumItems(13);
		persons.add(person0);
		SimplePerson person1 = new SimplePerson("p1", "a1", "c1", "s1");
		person1.setCart(person0.getCart());
		persons.add(person1);
		MapChangeEventTracker<Object, Object> tracker = new MapChangeEventTracker<>();
		composedMap.addMapChangeListener(tracker);
		assertEquals(0, tracker.count);
		person1.setCart(new SimpleCart());
		assertEquals(1, tracker.count);
		assertEquals(0, tracker.event.diff.getAddedKeys().size());
		assertEquals(0, tracker.event.diff.getRemovedKeys().size());
		assertEquals(Collections.singleton(person1), tracker.event.diff
				.getChangedKeys());
		assertEquals(Integer.valueOf(13), tracker.event.diff.getOldValue(person1));
		assertEquals(Integer.valueOf(0), tracker.event.diff.getNewValue(person1));
		assertEquals(Integer.valueOf(0), composedMap.get(person1));
	}

	@Test
	public void testChangeInSecondMap() {
		SimplePerson person0 = new SimplePerson("p0", "a0", "c0", "s0");
		person0.getCart().setNumItems(13);
		persons.add(person0);
		MapChangeEventTracker<Object, Object> tracker = new MapChangeEventTracker<>();
		composedMap.addMapChangeListener(tracker);
		assertEquals(0, tracker.count);
		person0.getCart().setNumItems(42);
		assertEquals(1, tracker.count);
		assertEquals(0, tracker.event.diff.getAddedKeys().size());
		assertEquals(0, tracker.event.diff.getRemovedKeys().size());
		assertEquals(Collections.singleton(person0), tracker.event.diff
				.getChangedKeys());
		assertEquals(Integer.valueOf(13), tracker.event.diff.getOldValue(person0));
		assertEquals(Integer.valueOf(42), tracker.event.diff.getNewValue(person0));
		assertEquals(Integer.valueOf(42), composedMap.get(person0));
	}

	@Test
	public void testDispose() {
		SimplePerson person0 = new SimplePerson("p0", "a0", "c0", "s0");
		person0.getCart().setNumItems(13);
		persons.add(person0);
		assertTrue(hasListeners((AbstractObservable) first));
		composedMap.dispose();
		assertFalse(hasListeners((AbstractObservable) first));
	}

}
