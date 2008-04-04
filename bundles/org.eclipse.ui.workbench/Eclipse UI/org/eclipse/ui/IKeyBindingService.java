/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui;

import org.eclipse.jface.action.IAction;

/**
 * The key binding service allows one to query or set the scope of Eclipse for
 * the purposes of resolving key assignments to commands, and to register
 * actions to handle specific commands. See the <code>org.eclipse.ui.commands</code>
 * extension point for details.
 * <p>
 * A participating workbench part is responsible to register all its actions
 * with this service. The part is also responsible to set the current scope.
 * </p>
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 * 
 * @since 2.0
 * @deprecated See IContextService to manage <b>scopes</b> and
 * IHandlerService to manage handlers. IAction can
 * be proxied by org.eclipse.jface.commands.ActionHandler.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IKeyBindingService {

    /**
     * Returns the active accelerator scope ids.
     * 
     * @return the active accelerator scope ids.
     */
    String[] getScopes();

    /**
     * Registers an action with the key binding service.
     * 
     * @param action
     *            the action to be registered with the key binding service.
     */
    void registerAction(IAction action);

    /**
     * Sets the active accelerator scope ids.
     * 
     * @param scopes
     *            the active accelerator scope ids.
     */
    void setScopes(String[] scopes);

    /**
     * Unregisters an action with the key binding service.
     * 
     * @param action
     *            the action to be unregistered with the key binding service.
     */
    void unregisterAction(IAction action);
}
