/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - bug 237718
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.observable.map.DecoratingObservableMap;
import org.eclipse.core.databinding.observable.map.IObservableMap;

/**
 * IObservableMap implementation that prevents modification by consumers. Events
 * in the originating wrapped map are propagated and thrown from this instance
 * when appropriate. All mutators throw an UnsupportedOperationException.
 * 
 * @since 1.0
 */
public class UnmodifiableObservableMap extends DecoratingObservableMap {
	Map unmodifiableMap;

	/**
	 * @param decorated
	 */
	public UnmodifiableObservableMap(IObservableMap decorated) {
		super(decorated, false);
		this.unmodifiableMap = Collections.unmodifiableMap(decorated);
	}

	public void clear() {
		throw new UnsupportedOperationException();
	}

	public Set entrySet() {
		getterCalled();
		return unmodifiableMap.entrySet();
	}

	public Set keySet() {
		getterCalled();
		return unmodifiableMap.keySet();
	}

	public Object put(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	public void putAll(Map m) {
		throw new UnsupportedOperationException();
	}

	public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	public Collection values() {
		getterCalled();
		return unmodifiableMap.values();
	}

	public synchronized void dispose() {
		unmodifiableMap = null;
		super.dispose();
	}
}
