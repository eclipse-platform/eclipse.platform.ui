/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.di;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A list that holds weak references to the objects.
 */
public class WeakRefList {

	private List userObjects;

	public WeakRefList(int initialSize) {
		userObjects = new ArrayList(initialSize);
	}

	public Object[] getSafeCopy() {
		Object[] result;
		int pos = 0;
		synchronized (userObjects) {
			result = new Object[userObjects.size()];
			for (Iterator i = userObjects.iterator(); i.hasNext();) {
				WeakReference ref = (WeakReference) i.next();
				Object userObject = ref.get();
				if (userObject == null) {
					// user object got GCed, clean up refs for future
					i.remove();
					continue;
				}
				result[pos] = userObject;
				pos++;
			}
		}
		if (pos == result.length)
			return result;
		// reallocate the array
		Object[] tmp = new Object[pos];
		System.arraycopy(result, 0, tmp, 0, pos);
		return tmp;
	}

	public void add(Object object) {
		WeakReference ref = new WeakReference(object);
		synchronized (userObjects) {
			userObjects.add(ref);
		}
	}

	public boolean remove(Object object) {
		synchronized (userObjects) {
			for (Iterator i = userObjects.iterator(); i.hasNext();) {
				WeakReference ref = (WeakReference) i.next();
				Object userObject = ref.get();
				if (userObject == null) {
					i.remove();
					continue;
				}
				if (userObject == object) { // use pointer comparison
					i.remove();
					return true;
				}
			}
			return false;
		}
	}
}
