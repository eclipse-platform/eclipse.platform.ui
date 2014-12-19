/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 215531)
 *     Matthew Hall - bug 228125
 *         (through ViewerElementMapTest.java)
 *     Matthew Hall - bug 262269
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.internal.databinding.identity.IdentityMap;

/**
 * @since 1.2
 */
public class IdentityMapTest extends TestCase {
	IdentityMap map;

	Object key;
	Object value;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		map = new IdentityMap();
		key = new Object();
		value = new Object();
	}

	public void testConstructor_NullComparer() {
		try {
			new IdentityMap(null);
			fail("Constructor should throw exception when null comparer passed in");
		} catch (RuntimeException expected) {
		}
	}

	public void testConstructorWithCollection_NullCollection() {
		try {
			new IdentityMap(null);
			fail("Constructor should throw exception when null collection passed in");
		} catch (RuntimeException expected) {
		}
	}

	public void testConstructorWithCollection_ContainsAllEntries() {
		Map<Object, Object> toCopy = new HashMap<Object, Object>();
		toCopy.put(new Object(), new Object());
		map = new IdentityMap(toCopy);
		assertEquals(toCopy, map);
	}

	public void testIsEmpty() {
		assertTrue(map.isEmpty());
		map.put(key, value);
		assertFalse(map.isEmpty());
	}

	public void testSize() {
		assertEquals(0, map.size());
		map.put(key, value);
		assertEquals(1, map.size());
	}

	public void testClear() {
		map.put(key, value);
		assertFalse(map.isEmpty());
		map.clear();
		assertTrue(map.isEmpty());
	}

	public void testGet() {
		assertNull(map.get(key));
		map.put(key, value);
		assertEquals(value, map.get(key));
	}

	public void testContainsKey() {
		String key1 = new String("key");
		String key2 = new String("key"); // equal but distinct instances
		assertFalse(map.containsKey(key1));
		map.put(key1, value);
		assertTrue(map.containsKey(key1));
		assertFalse(map.containsKey(key2));
	}

	public void testContainsValue() {
		assertFalse(map.containsValue(value));
		map.put(key, value);
		assertTrue(map.containsValue(value));
	}

	public void testPutAll() {
		Map<Object, Object> other = new HashMap<Object, Object>();
		other.put(key, value);

		assertTrue(map.isEmpty());
		map.putAll(other);
		assertEquals(1, map.size());
		assertTrue(map.containsKey(key));
		assertEquals(value, map.get(key));
	}

	public void testRemove() {
		map.put(key, value);
		assertTrue(map.containsKey(key));
		map.remove(key);
		assertFalse(map.containsKey(key));
	}

	public void testValues() {
		Collection<?> values = map.values();
		assertTrue(values.isEmpty());

		map.put(key, value);

		assertEquals(1, values.size());
		assertEquals(value, values.iterator().next());

		map.remove(key);
		assertTrue(map.values().isEmpty());
	}

	public void testKeySet() {
		Set<Object> keySet = map.keySet();
		assertTrue(keySet.isEmpty());

		map.put(key, value);
		assertEquals(1, keySet.size());
		assertTrue(keySet.contains(key));

		map.remove(key);
		assertTrue(keySet.isEmpty());
	}

	public void testKeySet_Add() {
		try {
			map.keySet().add(key);
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException expected) {
		}
	}

	public void testKeySet_AddAll() {
		try {
			map.keySet().addAll(Collections.singleton(key));
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException expected) {
		}
	}

	public void testKeySet_Clear() {
		map.put(key, value);
		Set<Object> keySet = map.keySet();
		assertTrue(keySet.contains(key));
		keySet.clear();
		assertTrue(keySet.isEmpty());
		assertTrue(map.isEmpty());
	}

	public void testKeySet_Contains() {
		Set<Object> keySet = map.keySet();
		assertFalse(keySet.contains(key));
		map.put(key, value);
		assertTrue(keySet.contains(key));
	}

	public void testKeySet_ContainsAll() {
		Set<Object> keySet = map.keySet();
		assertFalse(keySet.containsAll(Collections.singleton(key)));
		map.put(key, value);
		assertTrue(keySet.containsAll(Collections.singleton(key)));
	}

	public void testKeySet_IsEmpty() {
		Set<Object> keySet = map.keySet();
		assertTrue(keySet.isEmpty());
		map.put(key, value);
		assertFalse(keySet.isEmpty());
	}

	public void testKeySet_Iterator() {
		map.put(key, value);
		Iterator<Object> iterator = map.keySet().iterator();
		assertTrue(iterator.hasNext());
		assertEquals(key, iterator.next());

		assertEquals(1, map.size());
		iterator.remove();
		assertTrue(map.isEmpty());

		assertFalse(iterator.hasNext());
	}

	public void testKeySet_Remove() {
		map.put(key, value);
		assertEquals(1, map.size());
		map.keySet().remove(key);
		assertTrue(map.isEmpty());
	}

	public void testKeySet_RemoveAll() {
		map.put(key, value);
		Set<Object> keySet = map.keySet();
		assertFalse(keySet.removeAll(Collections.EMPTY_SET));
		assertEquals(1, map.size());
		assertTrue(keySet.removeAll(Collections.singleton(key)));
		assertTrue(map.isEmpty());
	}

	public void testKeySet_RetainAll() {
		map.put(key, value);
		Set<Object> keySet = map.keySet();
		assertFalse(keySet.retainAll(Collections.singleton(key)));
		assertEquals(1, map.size());
		assertTrue(keySet.retainAll(Collections.EMPTY_SET));
		assertTrue(map.isEmpty());
	}

	public void testKeySet_Size() {
		Set<Object> keySet = map.keySet();
		assertEquals(0, keySet.size());
		map.put(key, value);
		assertEquals(1, keySet.size());
		map.clear();
		assertEquals(0, keySet.size());
	}

	public void testKeySet_ToArray() {
		Set<Object> keySet = map.keySet();
		map.put(key, value);
		Object[] array = keySet.toArray();
		assertEquals(1, array.length);
		assertSame(key, array[0]);
	}

	public void testKeySet_ToArrayWithObjectArray() {
		key = new String("key");
		map.put(key, value);
		String[] array = (String[]) map.keySet().toArray(new String[0]);
		assertEquals(1, array.length);
		assertSame(key, array[0]);
	}

	public void testKeySet_Equals() {
		Set<Object> keySet = map.keySet();
		assertFalse(keySet.equals(null));
		assertTrue(keySet.equals(keySet));

		assertTrue(keySet.equals(Collections.EMPTY_SET));
		map.put(key, value);
		assertTrue(keySet.equals(Collections.singleton(key)));
	}

	public void testKeySet_HashCode() {
		Set<Object> keySet = map.keySet();
		assertEquals(0, keySet.hashCode());
		map.put(key, value);
		int hash = key.hashCode();
		assertEquals(hash, keySet.hashCode());
	}

	public void testEntrySet() {
		Set<Object> entrySet = map.entrySet();
		assertTrue(entrySet.isEmpty());

		map.put(key, value);
		assertEquals(1, entrySet.size());
		Map.Entry entry = (Map.Entry) entrySet.iterator().next();
		assertEquals(key, entry.getKey());
		assertEquals(value, entry.getValue());

		map.remove(key);
		assertTrue(entrySet.isEmpty());
	}

	public void testEntrySet_Add() {
		try {
			map.entrySet().add(key);
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException expected) {
		}
	}

	public void testEntrySet_AddAll() {
		try {
			map.entrySet().addAll(Collections.EMPTY_SET);
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException expected) {
		}
	}

	public void testEntrySet_Clear() {
		map.put(key, value);
		assertEquals(1, map.size());
		map.entrySet().clear();
		assertTrue(map.isEmpty());
	}

	public void testEntrySet_Contains() {
		map.put(key, value);
		Set<Object> entrySet = map.entrySet();
		assertTrue(entrySet.contains(new MapEntryStub(key, value)));
		map.remove(key);
		assertFalse(entrySet.contains(new MapEntryStub(key, value)));
	}

	public void testEntrySet_ContainsAll() {
		Set<Object> entrySet = map.entrySet();
		assertFalse(entrySet.containsAll(Collections.singleton(new MapEntryStub(key, value))));
		assertTrue(entrySet.containsAll(Collections.EMPTY_SET));

		map.put(key, value);
		assertTrue(entrySet.containsAll(Collections.singleton(new MapEntryStub(key, value))));
	}

	public void testEntrySet_IsEmpty() {
		Set<Object> entrySet = map.entrySet();
		assertTrue(entrySet.isEmpty());
		map.put(key, value);
		assertFalse(entrySet.isEmpty());
	}

	public void testEntrySet_Iterator() {
		map.put(key, value);
		Iterator<Object> iterator = map.entrySet().iterator();
		assertTrue(iterator.hasNext());
		Map.Entry entry = (Map.Entry) iterator.next();
		assertTrue(entry.equals(new MapEntryStub(key, value)));

		assertEquals(1, map.size());
		iterator.remove();
		assertTrue(map.isEmpty());
		assertFalse(iterator.hasNext());
	}

	public void testEntrySet_Remove() {
		map.put(key, value);
		assertEquals(1, map.size());

		assertTrue(map.entrySet().remove(new MapEntryStub(key, value)));
		assertTrue(map.isEmpty());
	}

	public void testEntrySet_RemoveAll() {
		Set<Object> entrySet = map.entrySet();
		assertFalse(entrySet.removeAll(Collections.EMPTY_SET));

		map.put(key, value);
		assertEquals(1, map.size());
		assertTrue(entrySet.removeAll(Collections.singleton(new MapEntryStub(key, value))));
		assertTrue(map.isEmpty());
	}

	public void testEntrySet_RetainAll() {
		Set<Object> entrySet = map.entrySet();
		assertFalse(entrySet.retainAll(Collections.EMPTY_SET));

		map.put(key, value);
		assertEquals(1, map.size());
		assertFalse(entrySet.retainAll(Collections.singleton(new MapEntryStub(key, value))));
		assertEquals(1, map.size());
		assertTrue(entrySet.retainAll(Collections.EMPTY_SET));
		assertTrue(map.isEmpty());
	}

	public void testEntrySet_Size() {
		Set<Object> entrySet = map.entrySet();
		assertEquals(0, entrySet.size());
		map.put(key, value);
		assertEquals(1, entrySet.size());
	}

	public void testEntrySet_ToArray() {
		Set<Object> entrySet = map.entrySet();
		assertEquals(0, entrySet.toArray().length);

		map.put(key, value);
		Object[] array = entrySet.toArray();
		assertEquals(1, array.length);
		assertTrue(array[0].equals(new MapEntryStub(key, value)));
	}

	public void testEntrySet_ToArrayWithObjectArray() {
		Set<Object> entrySet = map.entrySet();
		assertEquals(0, entrySet.toArray(new Object[0]).length);

		map.put(key, value);
		Map.Entry[] array = entrySet.toArray(new Map.Entry[0]);
		assertEquals(1, array.length);
		assertTrue(array[0].equals(new MapEntryStub(key, value)));
	}

	public void testEntrySet_Equals() {
		Set<Object> entrySet = map.entrySet();
		assertFalse(entrySet.equals(null));
		assertTrue(entrySet.equals(entrySet));

		assertTrue(entrySet.equals(Collections.EMPTY_SET));
		assertFalse(entrySet.equals(Collections.singleton(new MapEntryStub(key, value))));

		map.put(key, value);
		assertFalse(entrySet.equals(Collections.EMPTY_SET));
		assertTrue(entrySet.equals(Collections.singleton(new MapEntryStub(key, value))));
	}

	public void testEntrySet_HashCode() {
		// hash formula mandated by Map contract
		Set<Object> entrySet = map.entrySet();
		assertEquals(0, entrySet.hashCode());

		map.put(key, value);
		int hash = key.hashCode() ^ value.hashCode();
		assertEquals(hash, entrySet.hashCode());
	}

	public void testEntrySet_Entry_SetValue() {
		map.put(key, value);

		Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();

		Object newValue = new Object();
		Object oldValue = entry.setValue(newValue);
		assertEquals(value, oldValue);
		assertEquals(newValue, entry.getValue());
		assertEquals(newValue, map.get(key));
	}

	public void testEntrySet_Entry_Equals() {
		map.put(key, value);

		Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();
		assertFalse(entry.equals(null));
		assertTrue(entry.equals(entry));
		assertTrue(entry.equals(new MapEntryStub(key, value)));
	}

	public void testEntrySet_Entry_HashCode() {
		map.put(key, value);

		// hash computed as required by Map contract
		int hash = key.hashCode() ^ value.hashCode();
		assertEquals(hash, map.entrySet().iterator().next().hashCode());
	}

	public void testEquals() {
		assertFalse(map.equals(null));
		assertTrue(map.equals(map));

		Map<Object, Object> other = new HashMap<Object, Object>();
		other.put(key, value);

		assertTrue(map.equals(Collections.EMPTY_MAP));
		assertFalse(map.equals(other));

		map.put(key, value);

		assertFalse(map.equals(Collections.EMPTY_MAP));
		assertTrue(map.equals(other));
	}

	public void testHashCode() {
		assertEquals(0, map.hashCode());

		map.put(key, value);
		int hash = key.hashCode() ^ value.hashCode();
		assertEquals(hash, map.hashCode());
	}

	static class MapEntryStub implements Map.Entry {
		private final Object key;
		private final Object value;

		public MapEntryStub(Object key, Object value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public Object getKey() {
			return key;
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public Object setValue(Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals(Object obj) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int hashCode() {
			throw new UnsupportedOperationException();
		}
	}
}
