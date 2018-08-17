/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime;

import org.eclipse.core.runtime.IAdapterFactory;
import org.junit.Assert;

/**
 */
public class TestAdapterFactory extends Assert implements IAdapterFactory {
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		assertTrue("Request for wrong adapter", adaptableObject instanceof TestAdaptable);
		return adapterType.cast(new TestAdapter());
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] {TestAdapter.class};
	}
}
