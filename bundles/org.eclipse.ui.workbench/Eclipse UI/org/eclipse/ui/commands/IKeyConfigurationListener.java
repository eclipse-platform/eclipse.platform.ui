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
 * <p>
 * An instance of <code>IKeyConfigurationListener</code> can be used by
 * clients to receive notification of changes to one or more instances of
 * <code>IKeyConfiguration</code>.
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * 
 * @since 3.0
 * @see IKeyConfiguration#addKeyConfigurationListener(IKeyConfigurationListener)
 * @see IKeyConfiguration#removeKeyConfigurationListener(IKeyConfigurationListener)
 * @see org.eclipse.ui.commands.KeyConfigurationEvent
 * @deprecated Please use the bindings support in the "org.eclipse.jface"
 * plug-in instead.
 * @see org.eclipse.jface.bindings.ISchemeListener
 */
public interface IKeyConfigurationListener {

    /**
     * Notifies that one or more attributes of an instance of <code>IKeyConfiguration</code>
     * have changed. Specific details are described in the <code>KeyConfigurationEvent</code>.
     * 
     * @param keyConfigurationEvent
     *            the keyConfiguration event. Guaranteed not to be <code>null</code>.
     */
    void keyConfigurationChanged(KeyConfigurationEvent keyConfigurationEvent);
}
