/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.bindings;

/**
 * <p>
 * An instance of <code>ISchemeListener</code> can be used by clients to
 * receive notification of changes to one or more instances of
 * <code>IScheme</code>. It also provides notification of the set of active
 * bindings changing.
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>. The commands architecture is currently under
 * development for Eclipse 3.1. This class -- its existence, its name and its
 * methods -- are in flux. Do not use this class yet.
 * </p>
 * 
 * @since 3.1
 * @see BindingManager#addBindingManagerListener(IBindingManagerListener)
 * @see org.eclipse.jface.bindings.BindingManager#addBindingManagerListener(IBindingManagerListener)
 * @see BindingManagerEvent
 */
public interface IBindingManagerListener {

	/**
	 * Notifies that the set of defined or active scheme or bindings has changed
	 * in the binding manager.
	 * 
	 * @param event
	 *            the scheme event. Guaranteed not to be <code>null</code>.
	 */
	void bindingManagerChanged(BindingManagerEvent event);
}
