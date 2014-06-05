/*******************************************************************************
 * Copyright (c) 2015 Google Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     C. Sean Young <csyoung@google.com> - Bug 436645
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * A reference meant to be a value object for an EvaluationReference key type;
 * weakly holds an {@link EvaluationReference} (the reference itself, not the
 * underlying object) representing this value's key. Used to support
 * ReferenceQueue based cleanups in maps.
 *
 * Does not override equals or hashCode; uses identity comparison inherited from
 * Object.
 *
 * @param <T>
 *            The type of object this reference points to.
 *
 * @see EvaluationReference
 * @since 3.3
 */
public class EvaluationValueReference<T> extends SoftReference<T> {
	private Reference<EvaluationReference<?>> refToKey;

	/**
	 * @param referrent
	 *            The object to be referenced
	 * @param key
	 *            The key this value is associated with
	 */
	public EvaluationValueReference(T referrent, EvaluationReference<?> key) {
		super(referrent);
		this.refToKey = new WeakReference<EvaluationReference<?>>(key);
	}

	/**
	 * @param referrent
	 *            The object to be referenced
	 * @param key
	 *            The key this value is associated with
	 * @param queue
	 *            The ReferenceQueue to register this instance in
	 */
	public EvaluationValueReference(T referrent, EvaluationReference<?> key,
			ReferenceQueue<? super T> queue) {
		super(referrent, queue);
		this.refToKey = new WeakReference<EvaluationReference<?>>(key);
	}

	/**
	 * @return the key that this value was associated with, or null if this
	 *         value has been cleared or the key has been collected.
	 */
	public EvaluationReference<?> getKey() {
		return refToKey.get();
	}

	/**
	 * Facilitates "handing off" a particular instance of a key.
	 *
	 * @param otherValue
	 *            the value ref to copy the key from.
	 */
	void swapKey(EvaluationValueReference<?> otherValue) {
		Reference<EvaluationReference<?>> tmp = refToKey;
		this.refToKey = otherValue.refToKey;
		otherValue.refToKey = tmp;
	}

	/**
	 * Clears this reference and the underlying reference to the key.
	 *
	 * @see java.lang.ref.Reference#clear()
	 */
	@Override
	public void clear() {
		super.clear();
		// This only clears our reference to the key, not the key itself.
		refToKey.clear();
	}

	private static String toStringArrayAware(Object o) {
		// Yea, this will miss primitive arrays, but nothing is using this for those yet.
		if (o instanceof Object[]) {
			return Arrays.toString((Object[]) o);
		}
		return String.valueOf(o);
	}

	@Override
	public String toString() {
		Object myRef = get();
		return "EvaluationValueReference[" + (myRef == null ? "(collected)" : toStringArrayAware(myRef)) + ']'; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
