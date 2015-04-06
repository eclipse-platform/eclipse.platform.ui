/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.expressions.tests;

import org.eclipse.core.runtime.IAdapterFactory;

public class TestAdapterFactory implements IAdapterFactory {

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (Adapter.class.equals(adapterType))
			return adapterType.cast(new Adapter());
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { Adapter.class };
	}
}
