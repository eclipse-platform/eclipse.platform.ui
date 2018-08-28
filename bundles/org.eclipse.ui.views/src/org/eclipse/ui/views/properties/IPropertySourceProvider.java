/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.views.properties;

/**
 * Interface used by {@link org.eclipse.ui.views.properties.PropertySheetEntry}
 * to obtain an {@link org.eclipse.ui.views.properties.IPropertySource} for a
 * given object.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 */
public interface IPropertySourceProvider {

	/**
	 * Returns a property source for the given object.
	 *
	 * @param object
	 *            the object
	 * @return the property source for the object passed in (maybe
	 *         <code>null</code>)
	 */
	public IPropertySource getPropertySource(Object object);
}
