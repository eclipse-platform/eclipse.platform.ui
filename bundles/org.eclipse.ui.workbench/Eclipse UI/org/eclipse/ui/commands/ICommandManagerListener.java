/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.commands;

/**
 * An instance of this interface can be used by clients to receive notification
 * of changes to one or more instances of <code>ICommandManager</code>.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see ICommandManager#addCommandManagerListener(ICommandManagerListener)
 * @see ICommandManager#removeCommandManagerListener(ICommandManagerListener)
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 * @see org.eclipse.core.commands.ICommandManagerListener
 */
public interface ICommandManagerListener {

    /**
     * Notifies that one or more properties of an instance of
     * <code>ICommandManager</code> have changed. Specific details are
     * described in the <code>CommandManagerEvent</code>.
     * 
     * @param commandManagerEvent
     *            the commandManager event. Guaranteed not to be
     *            <code>null</code>.
     */
    void commandManagerChanged(CommandManagerEvent commandManagerEvent);
}
