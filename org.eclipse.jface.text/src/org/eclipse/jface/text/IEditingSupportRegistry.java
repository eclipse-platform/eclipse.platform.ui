/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

/**
 * A registry for <code>IEditingSupport</code>s.
 * <p>
 * This interface is not meant to be implemented outside the JFace text
 * framework.</p>
 *
 * @see IEditingSupport
 * @since 3.1
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IEditingSupportRegistry {

	/**
	 * Register a support with the registry. If the support is already
	 * registered, nothing happens.
	 *
	 * @param support an editor support
	 */
	public void register(IEditingSupport support);

	/**
	 * Deregister a support with the registry. If the support is not registered,
	 * or <code>support</code> is <code>null</code>, nothing happens.
	 *
	 * @param support the helper to deregister, or <code>null</code>
	 */
	public void unregister(IEditingSupport support);

	/**
	 * Returns the current editor helpers.
	 *
	 * @return an non- <code>null</code> array of currently registered editor
	 *         helpers
	 */
	public IEditingSupport[] getRegisteredSupports();
}
