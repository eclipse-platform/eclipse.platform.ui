/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 265727)
 ******************************************************************************/

package org.eclipse.core.databinding.property.list;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.MultiList;
import org.eclipse.core.internal.databinding.property.PropertyObservableUtil;

/**
 * A list property for observing multiple list properties in sequence in a
 * combined list.
 * 
 * @since 1.2
 */
public class MultiListProperty extends ListProperty {
	private IListProperty[] properties;
	private Object elementType;

	/**
	 * Constructs a MultiListProperty for observing the specified list
	 * properties in sequence
	 * 
	 * @param properties
	 *            the list properties
	 */
	public MultiListProperty(IListProperty[] properties) {
		this(properties, null);
	}

	/**
	 * Constructs a MultiListProperty for observing the specified list
	 * properties in sequence.
	 * 
	 * @param properties
	 *            the list properties
	 * @param elementType
	 *            the element type of the MultiListProperty
	 */
	public MultiListProperty(IListProperty[] properties, Object elementType) {
		this.properties = properties;
		this.elementType = elementType;
	}

	public Object getElementType() {
		return elementType;
	}

	public IObservableList observe(Realm realm, Object source) {
		IObservableList[] lists = new IObservableList[properties.length];
		for (int i = 0; i < lists.length; i++)
			lists[i] = properties[i].observe(realm, source);
		IObservableList multiList = new MultiList(lists, elementType);

		for (int i = 0; i < lists.length; i++)
			PropertyObservableUtil.cascadeDispose(multiList, lists[i]);

		return multiList;
	}
}
