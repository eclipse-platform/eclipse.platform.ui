/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions;

import java.lang.ref.WeakReference;

/**
 * Avoid conflicts caused by multiple classloader contributions with the same
 * class name. It's a rare occurrence but is supported by the OSGi classloader.
 * 
 * @since 3.4.1
 */
class Key {
	String fClazzName;

	WeakReference fClassLoader;

	String fSubType;

	int fMyHash;

	public Key(Class clazz, String subType) {
		fClazzName= clazz.getName();
		fSubType= subType;
		fClassLoader= new WeakReference(clazz.getClassLoader());
		int clazzHash= clazz.hashCode();
		fMyHash= (fClazzName.hashCode() * 89 + clazzHash) * 89 + subType.hashCode();

	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof Key) {
			Key k= (Key) o;
			return fMyHash == k.fMyHash
					&& fClazzName.equals(k.fClazzName)
					&& fSubType.equals(k.fSubType)
					&& fClassLoader.get() == k.fClassLoader.get();
		}
		return false;
	}

	public int hashCode() {
		return fMyHash;
	}
}
