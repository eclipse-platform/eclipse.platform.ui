/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @since 3.3
 * 
 */
public abstract class BeanPropertyListener extends NativePropertyListener
		implements PropertyChangeListener {
	private final PropertyDescriptor propertyDescriptor;

	protected BeanPropertyListener(IProperty property,
			PropertyDescriptor propertyDescriptor,
			ISimplePropertyListener listener) {
		super(property, listener);
		this.propertyDescriptor = propertyDescriptor;
	}

	public void propertyChange(java.beans.PropertyChangeEvent evt) {
		if (evt.getPropertyName() == null
				|| propertyDescriptor.getName().equals(evt.getPropertyName())) {
			Object oldValue = evt.getOldValue();
			Object newValue = evt.getNewValue();
			IDiff diff;
			if (evt.getPropertyName() == null || oldValue == null
					|| newValue == null)
				diff = null;
			else
				diff = computeDiff(oldValue, newValue);
			fireChange(evt.getSource(), diff);
		}
	}

	protected abstract IDiff computeDiff(Object oldValue, Object newValue);

	protected void doAddTo(Object source) {
		BeanPropertyListenerSupport.hookListener(source, propertyDescriptor
				.getName(), this);
	}

	protected void doRemoveFrom(Object source) {
		BeanPropertyListenerSupport.unhookListener(source, propertyDescriptor
				.getName(), this);
	}
}