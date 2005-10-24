/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.menus;

/**
 * An instance of this interface can be used by clients to receive notification
 * of changes to one or more instances of <code>SMenu</code>.
 * <p>
 * Clients may implement this interface, but must not be extend.
 * </p>
 * 
 * @since 3.2
 * @see SMenu#addListener(IMenuListener)
 * @see SMenu#removeListener(IMenuListener)
 */
public interface IMenuListener {

	/**
	 * Notifies that one or more properties of an instance of <code>SMenu</code>
	 * have changed. Specific details are described in the
	 * <code>MenuEvent</code>.
	 * 
	 * @param event
	 *            The event; never <code>null</code>.
	 */
	void menuChanged(MenuEvent event);
}
