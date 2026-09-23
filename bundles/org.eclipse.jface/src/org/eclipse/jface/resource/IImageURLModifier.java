/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.net.URL;

/**
 * Rewrites image URLs before they are loaded, so icons can be substituted
 * without changing the code that creates the image descriptors. The workbench
 * installs the highest ranked OSGi service of this type.
 *
 * @see ImageDescriptor#setURLModifier(IImageURLModifier)
 * @since 3.41
 */
@FunctionalInterface
public interface IImageURLModifier {

	/**
	 * Returns the URL to load instead of the given one, or <code>null</code> to
	 * keep it. Called for every image load, so it must be fast and thread-safe.
	 */
	URL modifyURL(URL originalURL);
}
