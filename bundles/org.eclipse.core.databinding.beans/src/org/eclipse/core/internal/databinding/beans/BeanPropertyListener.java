/*******************************************************************************
 * Copyright (c) 2009, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 265561)
 *     Matthew Hall - bug 268336
 ******************************************************************************/

package org.eclipse.core.internal.databinding.beans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.observable.IDiff;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.NativePropertyListener;

/**
 * @param <S> type of the source object
 * @param <T> type of the property value that is being listened to
 * @param <D> type of the diff handled by this listener
 * @since 3.3
 *
 */
public abstract class BeanPropertyListener<S, T, D extends IDiff> extends NativePropertyListener<S, D>
		implements PropertyChangeListener {
	private final PropertyDescriptor propertyDescriptor;

	protected BeanPropertyListener(IProperty property, PropertyDescriptor propertyDescriptor,
			ISimplePropertyListener<S, D> listener) {
		super(property, listener);
		this.propertyDescriptor = propertyDescriptor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(java.beans.PropertyChangeEvent evt) {
		if (evt.getPropertyName() == null
				|| propertyDescriptor.getName().equals(evt.getPropertyName())) {
			Object oldValue = evt.getOldValue();
			Object newValue = evt.getNewValue();
			D diff;
			if (evt.getPropertyName() == null || oldValue == null || newValue == null) {
				diff = null;
			} else {
				diff = computeDiff((T) oldValue, (T) newValue);
			}
			fireChange((S) evt.getSource(), diff);
		}
	}

	protected abstract D computeDiff(T oldValue, T newValue);

	@Override
	protected void doAddTo(Object source) {
		BeanPropertyListenerSupport.hookListener(source, propertyDescriptor.getName(), this);
	}

	@Override
	protected void doRemoveFrom(Object source) {
		BeanPropertyListenerSupport.unhookListener(source, propertyDescriptor.getName(), this);
	}
}