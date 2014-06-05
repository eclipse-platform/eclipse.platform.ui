/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     C. Sean Young <csyoung@google.com> - Bug 436645
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * A reference meant to be a key for an evaluation cache.
 *
 * Should only be used to point to objects that are immutable.
 *
 * @param <T>
 *            The type of object this reference points to.
 *
 * @since 3.3
 */
public class EvaluationReference<T> extends SoftReference<T> {
	private final int hashCode;

	/**
	 * @param referent
	 *            The object to be referenced, must be non-null
	 */
	public EvaluationReference(T referent) {
		super(referent);
		hashCode = referent.hashCode();
	}

	/**
	 *
	 * @param referent
	 *            The object to be referenced, must be non-null
	 * @param queue
	 *            The ReferenceQueue to register this instance in
	 */
	public EvaluationReference(T referent, ReferenceQueue<? super T> queue) {
		super(referent, queue);
		hashCode = referent.hashCode();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	/**
	 * Returns true if the Object given is also an EvaluationReference and if the
	 * referents are equal, or if the referent is null (implying clearing or collection),
	 * if the given Object is exactly this EvaluationReference object.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj == this) {
			return true;
		} else if (obj instanceof EvaluationReference) {
			// Don't get if not given an evaluation reference to prevent
			// unnecessary accesses keeping the SoftReference "fresh".
			Object myObj = get();
			// If the inner object is null, then the only EvaluationReference
			// that is equal is itself (checked above).
			if (myObj == null) return false;
			EvaluationReference<?> otherRef = (EvaluationReference<?>) obj;
			if (hashCode != otherRef.hashCode) return false;
			// Not comparing type; it is valid for two objects of different
			// types to be equal.
			Object otherObj = otherRef.get();
			if (otherObj == null) return false;
			return myObj == otherObj || myObj.equals(otherObj);
		}
		return false;
	}

	@Override
	public String toString() {
		Object referent = get();
		return "Evalutation[" + (referent == null ? "(collected)" : "referent=" + referent) + ']'; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
