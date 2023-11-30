/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.scenarios;

/**
 * @since 3.2
 */
public class CustomBeanModelType<T> {

	private final String propertyName;

	private final T object;

	private final Class<T> type;

	public CustomBeanModelType(T object, String propertyName, Class<T> type) {
		this.object = object;
		this.propertyName = propertyName;
		this.type = type;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public T getObject() {
		return object;
	}

	public Class<T> getType() {
		return type;
	}

}
