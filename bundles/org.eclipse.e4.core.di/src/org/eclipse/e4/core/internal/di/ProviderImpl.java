/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.di;

import javax.inject.Provider;
import org.eclipse.e4.core.di.IInjector;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.PrimaryObjectSupplier;

public class ProviderImpl<T> implements Provider<T> {

	final private PrimaryObjectSupplier objectProvider;
	final private IObjectDescriptor objectDescriptor;
	final private IInjector injector;

	public ProviderImpl(IObjectDescriptor descriptor, IInjector injector, PrimaryObjectSupplier provider) {
		objectDescriptor = descriptor;
		objectProvider = provider;
		this.injector = injector;
	}

	@SuppressWarnings("unchecked")
	public T get() {
		try {
			return (T) ((InjectorImpl) injector).makeFromProvider(objectDescriptor, objectProvider);
		} catch (ClassCastException e) {
			return null;
		}
	}

}
