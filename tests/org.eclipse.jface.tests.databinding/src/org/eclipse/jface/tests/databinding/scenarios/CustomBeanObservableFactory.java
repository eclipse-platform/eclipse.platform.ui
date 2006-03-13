/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.scenarios;

import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;
import org.eclipse.jface.internal.databinding.provisional.description.Property;
import org.eclipse.jface.internal.databinding.provisional.factories.IObservableFactory;
import org.eclipse.jface.internal.databinding.provisional.observable.IChangeListener;
import org.eclipse.jface.internal.databinding.provisional.observable.IObservable;
import org.eclipse.jface.internal.databinding.provisional.observable.IStaleListener;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IObservableValue;
import org.eclipse.jface.internal.databinding.provisional.observable.value.IValueChangeListener;

/**
 * @since 3.2
 * 
 */
public class CustomBeanObservableFactory implements IObservableFactory {

	private final DataBindingContext parentDataBindingContext;

	public CustomBeanObservableFactory(
			DataBindingContext parentDataBindingContext) {
		this.parentDataBindingContext = parentDataBindingContext;
	}

	public IObservable createObservable(Object description) {
		IObservable result = parentDataBindingContext
				.createObservable(description);
		if (description instanceof Property) {
			final Property property = (Property) description;
			if (result instanceof IObservableValue) {
				final IObservableValue wrapped = (IObservableValue) result;
				if (wrapped.getValueType() instanceof Class
						&& property.getPropertyID() instanceof String) {
					return new IObservableValue() {

						public Object getValueType() {
							return new CustomBeanModelType(
									property.getObject(), (String) property
											.getPropertyID(), (Class) wrapped
											.getValueType());
						}

						public Object getValue() {
							return wrapped.getValue();
						}

						public void setValue(Object value) {
							wrapped.setValue(value);
						}

						public void addValueChangeListener(
								IValueChangeListener listener) {
							wrapped.addValueChangeListener(listener);
						}

						public void removeValueChangeListener(
								IValueChangeListener listener) {
							wrapped.removeValueChangeListener(listener);
						}

						public void addChangeListener(IChangeListener listener) {
							wrapped.addChangeListener(listener);
						}

						public void removeChangeListener(
								IChangeListener listener) {
							wrapped.removeChangeListener(listener);
						}

						public void addStaleListener(IStaleListener listener) {
							wrapped.addStaleListener(listener);
						}

						public void removeStaleListener(IStaleListener listener) {
							wrapped.removeStaleListener(listener);
						}

						public boolean isStale() {
							return wrapped.isStale();
						}

						public void dispose() {
							wrapped.dispose();
						}
					};
				}
			}
		}
		return result;
	}

}
