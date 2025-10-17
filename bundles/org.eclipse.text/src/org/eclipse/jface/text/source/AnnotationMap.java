/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jface.text.source;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ILog;

import org.eclipse.jface.text.Position;


/**
 * Internal implementation of {@link org.eclipse.jface.text.source.IAnnotationMap}.
 *
 * @since 3.0
 */
class AnnotationMap implements IAnnotationMap {

	/**
	 * The lock object used to synchronize the operations explicitly defined by
	 * <code>IAnnotationMap</code>
	 */
	private Object fLockObject;
	/**
	 * The internal lock object used if <code>fLockObject</code> is <code>null</code>.
	 * @since 3.2
	 */
	private final Object fInternalLockObject= new Object();

	/** The map holding the annotations */
	private final Map<Annotation, Position> fInternalMap;

	/**
	 * Creates a new annotation map with the given capacity.
	 *
	 * @param capacity the capacity
	 */
	public AnnotationMap(int capacity) {
		fInternalMap= new HashMap<>(capacity);
	}

	/**
	 * Sets the lock object for this object. Subsequent calls to specified methods of this object
	 * are synchronized on this lock object. Which methods are synchronized is specified by the
	 * implementer.
	 * <p>
	 * If the lock object is not explicitly set by this method, the annotation map uses its internal
	 * lock object for synchronization.
	 * </p>
	 *
	 * <p>
	 * <em>You should not override an existing lock object unless you own that lock object yourself.
	 * Use the existing lock object instead.</em>
	 * </p>
	 *
	 * @param lockObject the lock object. If <code>null</code> is given, internal lock object is
	 *            used for locking.
	 */
	@Override
	public synchronized void setLockObject(Object lockObject) {
		if(fLockObject != null && fLockObject != lockObject) {
			ILog log= ILog.of(AnnotationMap.class);
			String message = "Attempt to override existing lock object of AnnotationMap."; //$NON-NLS-1$
			log.warn(message, new IllegalStateException(message));
		}
		fLockObject= lockObject;
	}

	/**
	 * Lock object used to synchronize concurrent access to the internal map data.
	 *
	 * @return <b>never</b> returns null
	 */
	@Override
	public synchronized Object getLockObject() {
		if (fLockObject == null) {
			return fInternalLockObject;
		}
		return fLockObject;
	}

	@Override
	public Iterator<Position> valuesIterator() {
		synchronized (getLockObject()) {
			return new ArrayList<>(fInternalMap.values()).iterator();
		}
	}

	@Override
	public Iterator<Annotation> keySetIterator() {
		synchronized (getLockObject()) {
			return new ArrayList<>(fInternalMap.keySet()).iterator();
		}
	}

	@Override
	public boolean containsKey(Object annotation) {
		synchronized (getLockObject()) {
			return fInternalMap.containsKey(annotation);
		}
	}

	@Override
	public Position put(Annotation annotation, Position position) {
		synchronized (getLockObject()) {
			return fInternalMap.put(annotation, position);
		}
	}

	@Override
	public Position get(Object annotation) {
		synchronized (getLockObject()) {
			return fInternalMap.get(annotation);
		}
	}

	@Override
	public void clear() {
		synchronized (getLockObject()) {
			fInternalMap.clear();
		}
	}

	@Override
	public Position remove(Object annotation) {
		synchronized (getLockObject()) {
			return fInternalMap.remove(annotation);
		}
	}

	@Override
	public int size() {
		synchronized (getLockObject()) {
			return fInternalMap.size();
		}
	}

	@Override
	public boolean isEmpty() {
		synchronized (getLockObject()) {
			return fInternalMap.isEmpty();
		}
	}

	@Override
	public boolean containsValue(Object value) {
		synchronized(getLockObject()) {
			return fInternalMap.containsValue(value);
		}
	}

	@Override
	public void putAll(Map<? extends Annotation, ? extends Position> map) {
		synchronized (getLockObject()) {
			fInternalMap.putAll(map);
		}
	}

	@Override
	public Set<Entry<Annotation, Position>> entrySet() {
		synchronized (getLockObject()) {
			return fInternalMap.entrySet();
		}
	}

	@Override
	public Set<Annotation> keySet() {
		synchronized (getLockObject()) {
			return fInternalMap.keySet();
		}
	}

	@Override
	public Collection<Position> values() {
		synchronized (getLockObject()) {
			return fInternalMap.values();
		}
	}
}
