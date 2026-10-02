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
 * <p>
 * The URL arrives unresolved, in the form the caller created it with:
 * </p>
 * <ul>
 * <li><code>platform:/plugin/&lt;bundle&gt;/&lt;path&gt;</code> for E4 model
 * <code>iconURI</code>s and CSS <code>url()</code> values</li>
 * <li><code>bundleentry://</code> or <code>bundleresource://</code> for
 * <code>plugin.xml</code> icons and descriptors created from a class or bundle;
 * the host is the numeric bundle id, not the symbolic name</li>
 * <li><code>file:</code> for images outside of bundles</li>
 * </ul>
 * <p>
 * Match on the end of {@link URL#getPath()}, since only the path within the
 * bundle is the same in all forms.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This interface has been added as part of a
 * work in progress. There is no guarantee that this API will remain the same.
 * </p>
 *
 * @see ImageDescriptor#setURLModifier(IImageURLModifier)
 * @since 3.41
 */
@FunctionalInterface
public interface IImageURLModifier {

	/**
	 * Returns the URL to load instead of the given one, or <code>null</code> to
	 * keep it. Called for every image load, so it must be fast and thread-safe.
	 *
	 * @param originalURL the URL the image would be loaded from
	 * @return the replacement URL, or <code>null</code> to keep the original
	 */
	URL modifyURL(URL originalURL);
}
