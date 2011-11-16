/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.services;

import javax.inject.Inject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.adapter.Adapter;

public class EclipseAdapter extends Adapter {

	private IAdapterManager adapterManager;
	private IEclipseContext context;

	@Inject
	public EclipseAdapter(IEclipseContext context) {
		super();
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T adapt(Object element, Class<T> adapterType) {
		Assert.isNotNull(adapterType);
		if (element == null) {
			return null;
		}
		if (adapterType.isInstance(element)) {
			return (T) element;
		}
		if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			Object result = adaptable.getAdapter(adapterType);
			if (result != null) {
				// Sanity-check
				Assert.isTrue(adapterType.isInstance(result));
				return (T) result;
			}
		}
		if (adapterManager == null)
			adapterManager = lookupAdapterManager();
		if (adapterManager == null) {
			// TODO should we log the fact that there is no adapter manager? Maybe just once
			return null;
		}

		Object result = adapterManager.loadAdapter(element, adapterType.getName());
		if (result != null) {
			// Sanity-check
			Assert.isTrue(adapterType.isInstance(result));
			return (T) result;
		}

		return null;
	}

	private IAdapterManager lookupAdapterManager() {
		return (IAdapterManager) context.get(IAdapterManager.class.getName());
	}

}
