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

package org.eclipse.ui.commands;

/**
 * <p>
 * An instance of <code>IKeyConfigurationListener</code> can be used by clients to 
 * receive notification of changes to one or more instances of 
 * <code>IKeyConfiguration</code>.
 * </p>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 * 
 * @since 3.0
 * @see IKeyConfiguration#addKeyConfigurationListener
 * @see IKeyConfiguration#removeKeyConfigurationListener
 * @see IKeyConfigurationEvent
 */
public interface IKeyConfigurationListener {

	/**
	 * Notifies that one or more attributes of an instance of 
	 * <code>IKeyConfiguration</code> have changed. Specific details are described in the 
	 * <code>IKeyConfigurationEvent</code>.
	 *
	 * @param keyConfigurationEvent the keyConfiguration event. Guaranteed not to be 
	 *                     <code>null</code>.
	 */
	void keyConfigurationChanged(IKeyConfigurationEvent keyConfigurationEvent);
}
