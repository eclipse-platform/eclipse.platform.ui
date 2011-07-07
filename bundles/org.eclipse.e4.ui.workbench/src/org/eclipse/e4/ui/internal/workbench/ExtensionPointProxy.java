/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.lang.reflect.Field;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.ui.model.application.MApplicationElement;

public class ExtensionPointProxy {

	private IConfigurationElement element;
	private String propertyName;
	private IDelegateInitializer delegateInitializer;

	private boolean failed = false;
	private Object delegate;

	public ExtensionPointProxy(IConfigurationElement element, String propertyName) {
		this(element, propertyName, null);
	}

	public ExtensionPointProxy(IConfigurationElement element, String propertyName,
			IDelegateInitializer delegateInitializer) {
		this.element = element;
		this.propertyName = propertyName;
		this.delegateInitializer = delegateInitializer;
	}

	public Object getDelegate() {
		return delegate;
	}

	public Object createDelegate(MApplicationElement model) {
		if (delegate == null && !failed) {
			try {
				delegate = element.createExecutableExtension(propertyName);
				if (delegateInitializer != null) {
					delegateInitializer.initialize(model, delegate);
				}
			} catch (CoreException e) {
				failed = true;
			}
		}
		return delegate;
	}

	public boolean setField(String name, Object value) {
		if (delegate != null) {
			Field field = getField(delegate.getClass(), name);
			if (field != null) {
				boolean accessible = field.isAccessible();
				try {
					field.setAccessible(true);
					field.set(delegate, value);
					return true;
				} catch (IllegalAccessException e) {
					return false;
				} finally {
					field.setAccessible(accessible);
				}
			}
		}
		return false;
	}

	private Field getField(Class<?> cls, String name) {
		try {
			return cls.getDeclaredField(name);
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchFieldException e) {
			return getField(cls.getSuperclass(), name);
		}
	}
}
