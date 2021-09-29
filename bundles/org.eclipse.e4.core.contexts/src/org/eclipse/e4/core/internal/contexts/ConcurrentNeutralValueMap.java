/*******************************************************************************
 * Copyright (c) 2021 Joerg Kubitz.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Joerg Kubitz              - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * A wrapper around ConcurrentHashMap which allows null values.
 *
 * For null values a neutral Element is internally used.
 *
 * Faster then a synchronized Map.
 */
public class ConcurrentNeutralValueMap<K, V> {// implements subset of Map<K, V>
	final ConcurrentHashMap<K, V> delegate = new ConcurrentHashMap<>();
	/** a value that is used for null elements **/
	final private V neutralValue;

	/**
	 * @param neutralValue a Element that does not equal any other Value
	 * @see #neutralObject()
	 */
	public ConcurrentNeutralValueMap(V neutralValue) {
		this.neutralValue = neutralValue;
	}

	private V wrapValue(V value) {
		return value == null ? neutralValue : value;
	}

	private V unwrapValue(V v) {
		return v == neutralValue ? null : v;
	}

	public V get(Object key) {
		return unwrapValue(delegate.get(key));
	}

	private static final class NullValue {
		@Override
		public String toString() {
			return "null"; //$NON-NLS-1$
		}

		@Override
		public boolean equals(Object obj) {
			return false;
		}

		@Override
		public int hashCode() {
			return 0x55555555;
		}
	}

	/** returns a neutral Element that does not equals any other **/
	public static Object neutralObject() {
		return new NullValue();
	}

	/*
	 * Method overloads from java.lang.Object:
	 */

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	/*
	 * Method overloads from java.util.Map:
	 */

	/**
	 * Like {@link java.util.Map#put} but we do not return a value.
	 *
	 * @see java.util.Map#put(Object, Object)
	 * @see ConcurrentHashMap#put(Object, Object)
	 * @see #putAndGetOld(Object, Object)
	 **/
	public void put(K key, V value) {
		delegate.put(key, wrapValue(value));
	}

	/**
	 * Like {@link java.util.Map#put} but we do not return a wrapped value.
	 *
	 * @see java.util.Map#put(Object, Object)
	 * @see Value
	 **/
	public Value<V> putAndGetOld(K key, V value) {
		return new Wrapped(delegate.put(key, wrapValue(value)));
	}

	/**
	 * @see java.util.Map#remove(Object)
	 * @see ConcurrentHashMap#remove(Object)
	 **/
	public V remove(Object key) {
		return unwrapValue(delegate.remove(key));
	}

	/**
	 * @see java.util.Map#size()
	 **/
	public int size() {
		return delegate.size();
	}

	/**
	 * @see java.util.Map#isEmpty()
	 **/
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	/**
	 * @see java.util.Map#clear()
	 **/
	public void clear() {
		delegate.clear();
	}

	/**
	 * Use is discouraged as the outcome might change concurrently
	 *
	 * @see java.util.Map#containsKey(Object)
	 * @see ConcurrentHashMap#containsKey(Object)
	 **/
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	/*
	 * Some helpful methods from ConcurrentHashMap which do not return V:
	 */

	/** @see ConcurrentHashMap#forEach(BiConsumer) **/
	public void forEach(BiConsumer<? super K, ? super V> action) {
		delegate.forEach( //
				(k, v) -> action.accept(k, unwrapValue(v) //
				));
	}

	/*
	 * Some methods from ConcurrentHashMap which return V. returning null is
	 * discouraged because it is not clear if that was a value or not:
	 */

	/**
	 * Like {@link ConcurrentHashMap#putIfAbsent(Object, Object)} but we do not
	 * return anything.
	 *
	 * @see ConcurrentHashMap#putIfAbsent(Object, Object)
	 **/
	public void putIfAbsent(K key, V value) {
		delegate.putIfAbsent(key, wrapValue(value));
	}

	public interface Value<V> {
		/**
		 * @return the result of map.contains(key)
		 */
		boolean isPresent();

		/**
		 * @return the result of map.get(key)
		 */
		V unwraped();
	}

	private final class Wrapped implements Value<V> {
		V wrappedValue;

		private Wrapped(V wrappedValue) {
			this.wrappedValue = wrappedValue;
		}

		@Override
		public boolean isPresent() {
			return wrappedValue != null;
		}

		@Override
		public V unwraped() {
			return unwrapValue(wrappedValue);
		}
	}

	/**
	 * @return a value that is wrapped into a Value Object.
	 * @see Value#isPresent()
	 * @see Value#unwraped()
	 */
	public Value<V> getValue(K key) {
		return new Wrapped(delegate.get(key));
	}

}
