/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.ui.css.core.dom.CSSProperty;
import org.eclipse.e4.ui.css.core.dom.CSSPropertyList;

/**
 * w3c {@link CSSPropertyList} implementation.
 */
public class CSSPropertyListImpl implements CSSPropertyList {

	private List properties = null;

	public CSSPropertyListImpl() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.e4.css.core.dom.CSSPropertyList#getLength()
	 */
	@Override
	public int getLength() {
		return (properties != null) ? properties.size() : 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.e4.css.core.dom.CSSPropertyList#item(int)
	 */
	@Override
	public CSSProperty item(int index) {
		return (properties != null) ? (CSSProperty) properties.get(index)
				: null;
	}

	/**
	 * Add {@link CSSProperty}.
	 */
	public void add(CSSProperty property) {
		if (properties == null) {
			properties = new ArrayList();
		}
		properties.add(property);
	}

	/**
	 * Insert {@link CSSProperty} at <code>index</code>.
	 * @param property
	 * @param index
	 */
	public void insert(CSSProperty property, int index) {
		if (properties == null) {
			properties = new ArrayList();
		}
		properties.add(index, property);
	}

	/**
	 * Delete {@link CSSProperty} at <code>index</code>.
	 * @param index
	 */
	public void delete(int index) {
		if (properties == null) {
			properties = new ArrayList();
		}
		properties.remove(index);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < getLength(); i++) {
			sb.append(item(i).toString()).append("\r\n");
		}
		return sb.toString();
	}
}
