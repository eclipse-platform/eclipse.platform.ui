/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
package org.eclipse.ui.editors.text;

import java.net.URI;


/**
 * Extension interface for {@link org.eclipse.ui.editors.text.ILocationProvider}. Adds
 * ability to the location for a given object as URI.
 *
 * @since 3.3
 */
public interface ILocationProviderExtension {

	/**
	 * Returns the URI of the given object or <code>null</code>.
	 *
	 * @param element the object for which to get the location
	 * @return the URI of the given object or <code>null</code>
	 */
	URI getURI(Object element);
}
