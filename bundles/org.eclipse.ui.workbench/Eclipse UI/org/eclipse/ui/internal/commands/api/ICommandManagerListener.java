/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.commands.api;

/**
 * <p>
 * An instance of <code>ICommandManagerListener</code> can be used by clients 
 * to receive notification of changes to one or more instances of 
 * <code>ICommandManager</code>.
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see ICommandManager#addCommandManagerListener
 * @see ICommandManager#removeCommandManagerListener
 * @see ICommandManagerEvent
 */
public interface ICommandManagerListener {

	/**
	 * Notifies that one or more attributes of an instance of 
	 * <code>ICommandManager</code> have changed. Specific details are 
	 * described in the <code>ICommandManagerEvent</code>.
	 *
	 * @param commandManagerEvent the command manager event. Guaranteed not to 
	 * 							  be <code>null</code>.
	 */
	void commandManagerChanged(ICommandManagerEvent commandManagerEvent);
}
