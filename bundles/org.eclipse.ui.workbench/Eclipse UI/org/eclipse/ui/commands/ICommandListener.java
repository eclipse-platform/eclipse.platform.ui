/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
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
 * of changes to one or more instances of <code>ICommand</code>.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see ICommand#addCommandListener(ICommandListener)
 * @see ICommand#removeCommandListener(ICommandListener)
 * @see org.eclipse.core.commands.ICommandListener
 * @deprecated Please use the "org.eclipse.core.commands" plug-in instead.
 */
public interface ICommandListener {

    /**
     * Notifies that one or more properties of an instance of <code>ICommand</code>
     * have changed. Specific details are described in the <code>CommandEvent</code>.
     * 
     * @param commandEvent
     *            the command event. Guaranteed not to be <code>null</code>.
     */
    void commandChanged(CommandEvent commandEvent);
}
