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

package org.eclipse.ui.internal.commands;

import java.util.Map;

import org.eclipse.ui.commands.ICommandManager;

/**
 * <p>
 * An instance of this interface allows clients to manage commands, as defined
 * by the extension point <code>org.eclipse.ui.commands</code>.  This interface
 * allows full control over the command manager -- changing the handlers,
 * contexts, locale or platform to arbitrary values.
 * </p>
 * <p>
 * This interface is not intended to be extended or implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see CommandManagerFactory
 */
public interface IMutableCommandManager extends ICommandManager {
   
	/**
     * Sets the map of identifiers to active contexts. The mapping represents
     * the hierarchical structure of the contexts.
     * 
     * @param activeContextIds
     *            a map of child context identifiers to parent context
     *            identifiers -- representing the hierarchy of active contexts.
     *            This tree may be empty, but it must not be <code>null</code>.
     *            If this tree is not empty, it must only contain instances of
     *            <code>String</code>.
     */
	void setActiveContextIds(Map activeContextIds);
    
	/**
     * Sets the active key configuration.
     * 
     * @param activeKeyConfigurationId
     *            the active key configuration; must not be <code>null</code>.
     */
    void setActiveKeyConfigurationId(String activeKeyConfigurationId);

	/**
     * Sets the active locale. The locale can be any arbitrary string, but is
     * typically the locale returned from {@link java.util.Locale#getDefault()}.
     * 
     * @param activeLocale
     *            the active locale; must not be <code>null</code>.
     */
    void setActiveLocale(String activeLocale);

	/**
	 * Sets the active platform.  The locale can be any arbitrary string, but is
	 * typically the locale returned from {@link org.eclipse.swt.SWT#getPlatform()}.
	 * 
	 * @param activePlatform
	 *            the active platform; must not be <code>null</code>.
	 */
    void setActivePlatform(String activePlatform);

    /**
     * Sets the map of handlers by command identifiers.
     * 
     * @param handlersByCommandId
     *            the map of handlers by command identifiers. This map may be
     *            empty, but it must not be <code>null</code>. If this map is
     *            not empty, its keys must be instances of <code>String</code>
     *            and its values must be instances of <code>IHandler</code>.
     */
    void setHandlersByCommandId(Map handlersByCommandId);
}
