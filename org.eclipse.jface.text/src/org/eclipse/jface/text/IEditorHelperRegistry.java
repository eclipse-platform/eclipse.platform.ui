/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

/**
 * A registry for <code>IEditorHelper</code>s.
 * <p>
 * XXX This interface is subject to change. Do not use.
 * </p>
 * 
 * @since 3.1
 */
public interface IEditorHelperRegistry {

	/**
	 * Register a helper with the registry. If the helper is already registered,
	 * nothing happens.
	 * 
	 * @param helper an editor helper
	 */
	public void register(IEditorHelper helper);
	
	/**
	 * Deregister a helper with the registry. If the helper is not registered,
	 * or <code>helper</code> is <code>null</code>, nothing happens.
	 * 
	 * @param helper the helper to deregister, or <code>null</code>
	 */
	public void deregister(IEditorHelper helper);
	
	/**
	 * Returns the current editor helpers.
	 * 
	 * @return an non- <code>null</code> array of currently registered editor
	 *         helpers
	 */
	public IEditorHelper[] getCurrentHelpers();
}