/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.content;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

public class ContentDescription implements IContentDescription {
	private static final byte ALL_OPTIONS = 0x01;
	private static final byte IMMUTABLE = 0x02;
	private IContentType contentType;
	private byte flags;
	private Object keys;
	private Object values;

	public ContentDescription(QualifiedName[] requested) {
		if (requested == IContentDescription.ALL) {
			flags |= ALL_OPTIONS;
			return;
		}
		if (requested.length > 1) {
			keys = requested;
			values = new Object[requested.length];
		} else if (requested.length == 1)
			keys = requested[0];
		// if requested.length == 0 then keys == null (no options to describe!)
	}

	private void assertMutable() {
		if ((flags & IMMUTABLE) != 0)
			throw new IllegalStateException("Content description is immutable"); //$NON-NLS-1$
	}

	/**
	 * @see IContentDescription
	 */
	public IContentType getContentType() {
		return contentType;
	}

	/**
	 * @see IContentDescription
	 */
	public Object getProperty(QualifiedName key) {
		// no values have been set
		if (values == null)
			return null;
		// a single property may have been set
		if (keys instanceof QualifiedName)
			return keys.equals(key) ? values : null;
		// multiple properties may have been set
		QualifiedName[] keys = (QualifiedName[]) this.keys;
		for (int i = 0; i < keys.length; i++)
			if (keys[i].equals(key))
				return ((Object[]) values)[i];
		return null;
	}

	/**
	 * @see IContentDescription
	 */
	public boolean isRequested(QualifiedName propertyKey) {
		// all options requested
		if ((flags & ALL_OPTIONS) != 0)
			return true;
		// no options requested
		if (keys == null)
			return false;
		// a single option requested
		if (keys instanceof QualifiedName)
			return keys.equals(propertyKey);
		// some (but not all) options requested
		QualifiedName[] keys = (QualifiedName[]) this.keys;
		for (int i = 0; i < keys.length; i++)
			if (keys[i].equals(propertyKey))
				return true;
		return false;
	}

	public void markImmutable() {
		assertMutable();
		flags |= IMMUTABLE;
	}

	void setContentType(IContentType contentType) {
		markImmutable();
		this.contentType = contentType;
	}

	/**
	 * @see IContentDescription
	 */
	public void setProperty(QualifiedName newKey, Object newValue) {
		assertMutable();
		if (keys == null) {
			if ((flags & ALL_OPTIONS) != 0) {
				keys = newKey;
				values = newValue;
			}
			return;
		}
		if (keys.equals(newKey)) {
			values = newValue;
			return;
		}
		if (keys instanceof QualifiedName) {
			if ((flags & ALL_OPTIONS) != 0) {
				keys = new QualifiedName[] {(QualifiedName) keys, newKey};
				values = new Object[] {values, newValue};
			}
			return;
		}
		QualifiedName[] keys = (QualifiedName[]) this.keys;
		for (int i = 0; i < keys.length; i++)
			if (keys[i].equals(newKey))
				((Object[]) values)[i] = newValue;
	}
}