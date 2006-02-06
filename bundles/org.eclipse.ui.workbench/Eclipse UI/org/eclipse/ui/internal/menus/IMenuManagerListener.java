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
package org.eclipse.ui.internal.menus;

/**
 * An instance of this interface can be used by clients to receive notification
 * of changes to one or more instances of <code>SMenuManager</code>.
 * <p>
 * Clients may implement this interface, but must not be extend.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class will eventually exist in <code>org.eclipse.jface.menus</code>.
 * </p>
 * 
 * @since 3.2
 * @see SMenuManager#addListener(IMenuManagerListener)
 * @see SMenuManager#removeListener(IMenuManagerListener)
 */
public interface IMenuManagerListener {

	/**
	 * Notifies that one or more properties of an instance of
	 * <code>SMenuManager</code> have changed. Specific details are described
	 * in the <code>MenuManagerEvent</code>.
	 * 
	 * @param event
	 *            The event; never <code>null</code>.
	 */
	void menuManagerChanged(MenuManagerEvent event);
}
