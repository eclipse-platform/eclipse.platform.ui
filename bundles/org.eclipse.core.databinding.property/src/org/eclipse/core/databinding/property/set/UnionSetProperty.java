/*******************************************************************************
 * Copyright (c) 2009, 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 265727)
 ******************************************************************************/

package org.eclipse.core.databinding.property.set;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.set.UnionSet;
import org.eclipse.core.internal.databinding.property.PropertyObservableUtil;

/**
 * A set property for observing the union of multiple set properties a combined
 * set.
 * 
 * @since 1.2
 */
public class UnionSetProperty extends SetProperty {
	private final ISetProperty[] properties;
	private final Object elementType;

	/**
	 * @param properties
	 */
	public UnionSetProperty(ISetProperty[] properties) {
		this(properties, null);
	}

	/**
	 * @param properties
	 * @param elementType
	 */
	public UnionSetProperty(ISetProperty[] properties, Object elementType) {
		this.properties = properties;
		this.elementType = elementType;
	}

	public Object getElementType() {
		return elementType;
	}

	protected Set doGetSet(Object source) {
		Set set = new HashSet();
		for (int i = 0; i < properties.length; i++)
			set.addAll(properties[i].getSet(source));
		return set;
	}

	protected void doSetSet(Object source, Set set) {
		throw new UnsupportedOperationException(
				"UnionSetProperty is unmodifiable"); //$NON-NLS-1$
	}

	protected void doUpdateSet(Object source, SetDiff diff) {
		throw new UnsupportedOperationException(
				"UnionSetProperty is unmodifiable"); //$NON-NLS-1$
	}

	public IObservableSet observe(Realm realm, Object source) {
		IObservableSet[] sets = new IObservableSet[properties.length];
		for (int i = 0; i < sets.length; i++)
			sets[i] = properties[i].observe(realm, source);
		IObservableSet unionSet = new UnionSet(sets, elementType);

		for (int i = 0; i < sets.length; i++)
			PropertyObservableUtil.cascadeDispose(unionSet, sets[i]);

		return unionSet;
	}
}
