/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	private List<CSSProperty> properties;

	public CSSPropertyListImpl() {
	}

	@Override
	public int getLength() {
		return (properties != null) ? properties.size() : 0;
	}

	@Override
	public CSSProperty item(int index) {
		return (properties != null) ? properties.get(index)
				: null;
	}

	/**
	 * Add {@link CSSProperty}.
	 */
	public void add(CSSProperty property) {
		if (properties == null) {
			properties = new ArrayList<>();
		}
		properties.add(property);
	}

	/**
	 * Insert {@link CSSProperty} at <code>index</code>.
	 */
	public void insert(CSSProperty property, int index) {
		if (properties == null) {
			properties = new ArrayList<>();
		}
		properties.add(index, property);
	}

	/**
	 * Delete {@link CSSProperty} at <code>index</code>.
	 */
	public void delete(int index) {
		if (properties == null) {
			properties = new ArrayList<>();
		}
		properties.remove(index);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getLength(); i++) {
			sb.append(item(i).toString()).append("\r\n");
		}
		return sb.toString();
	}
}
