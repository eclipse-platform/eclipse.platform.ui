/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.util.resources;

/**
 * Resources locator manage to register/unregister {@link IResourceLocator}.
 */
public interface IResourcesLocatorManager extends IResourceLocator {

	/**
	 * Register <code>resourceLocator</code>.
	 *
	 * @param resourceLocator
	 */
	public void registerResourceLocator(IResourceLocator resourceLocator);

	/**
	 * Unregister <code>resourceLocator</code>.
	 *
	 * @param resourceLocator
	 */
	public void unregisterResourceLocator(IResourceLocator resourceLocator);

}
