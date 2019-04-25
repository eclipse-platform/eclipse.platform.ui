/*******************************************************************************
 * Copyright (c) 2008, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 278314
 *******************************************************************************/
package org.eclipse.jface.internal.databinding.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.databinding.observable.IDiff;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.NativePropertyListener;
import org.eclipse.core.databinding.property.value.SimpleValueProperty;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * Class that supports the use of {@link IObservableValue} with objects that
 * follow standard bean method naming conventions but notify an
 * {@link IPropertyChangeListener} when the property changes.
 *
 * @param <S>
 *            type of the source object
 * @param <T>
 *            type of the value of the property
 */
public class JFaceProperty<S, T> extends SimpleValueProperty<S, T> {

	private Class<?> returnType;
	private Method setterMethod;
	private Method getterMethod;
	private final String property;
	private Method removePropertyListenerMethod;
	private Method addPropertyListenerMethod;

	private static String getSetterName(String fieldName) {
		return "set" + toMethodSuffix(fieldName); //$NON-NLS-1$
	}

	private static String getGetterName(String fieldName) {
		return "get" + toMethodSuffix(fieldName); //$NON-NLS-1$
	}

	private static String getBooleanGetterName(String fieldName) {
		return "is" + toMethodSuffix(fieldName); //$NON-NLS-1$
	}

	private static String toMethodSuffix(String fieldName) {
		if (Character.isLowerCase(fieldName.charAt(0))) {
			return Character.toUpperCase(fieldName.charAt(0))
					+ fieldName.substring(1);
		}
		return fieldName;
	}

	class Listener<D extends IDiff> extends NativePropertyListener<S, D>
			implements IPropertyChangeListener {
		public Listener(ISimplePropertyListener<S, D> listener) {
			super(JFaceProperty.this, listener);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(JFaceProperty.this.property)) {
				fireChange((S) event.getSource(), null);
			}
		}

		@Override
		protected void doAddTo(Object model) {
			try {
				addPropertyListenerMethod.invoke(model, this);
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage());
			}
		}

		@Override
		protected void doRemoveFrom(Object model) {
			try {
				removePropertyListenerMethod.invoke(model, this);
			} catch (Exception e) {
				throw new IllegalStateException(e.getMessage());
			}
		}
	}

	/**
	 * @param fieldName
	 * @param property
	 * @param clazz
	 */
	public JFaceProperty(String fieldName, String property, Class<?> clazz) {
		this.property = property;
		// Create all the necessary method ahead of time to ensure they are
		// available
		try {
			try {
				String getterName = getGetterName(fieldName);
				getterMethod = clazz.getMethod(getterName);
			} catch (NoSuchMethodException e) {
				String getterName = getBooleanGetterName(fieldName);
				getterMethod = clazz.getMethod(getterName);
			}
			returnType = getterMethod.getReturnType();
			setterMethod = clazz.getMethod(getSetterName(fieldName), returnType);
			addPropertyListenerMethod = clazz.getMethod("addPropertyChangeListener", IPropertyChangeListener.class); //$NON-NLS-1$
			removePropertyListenerMethod = clazz.getMethod("removePropertyChangeListener", //$NON-NLS-1$
					IPropertyChangeListener.class);
		} catch (SecurityException e) {
			throw new IllegalArgumentException();
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public INativePropertyListener<S> adaptListener(ISimplePropertyListener<S, ValueDiff<? extends T>> listener) {
		return new Listener<>(listener);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected T doGetValue(Object model) {
		try {
			return (T) getterMethod.invoke(model);
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	@Override
	protected void doSetValue(Object model, Object value) {
		try {
			setterMethod.invoke(model, value);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	@Override
	public Object getValueType() {
		return returnType;
	}

}
