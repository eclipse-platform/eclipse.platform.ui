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
package org.eclipse.core.runtime.preferences;

import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * Clients contributing a scope to the Eclipse preference system must 
 * implement this interface to aid Eclipse in persisting/restoring preference
 * values stored in their scope.
 * <p>
 * Clients are free to implement this interface and the persistence mechanism as
 * they wish. Preferences may be stored on disk, stored on a server, or even
 * not stored at all.
 * </p>
 * @since 3.0
 */
public interface IScope {

	/**
	 * Flush any changes to the given node to the persistent store. If 
	 * the node does not contain preferences applicable to this scope, 
	 * then do nothing.
	 * </p><p>
	 * Clients are free to implement this method as they want and may
	 * choose not to persist preferences in their scope at all.
	 * </p>
	 * @param node the node to flush
	 * @throws BackingStoreException if there is a problem accessing the
	 * 	backing store or if it is not available
	 * @see #sync(org.osgi.service.prefs.Preferences)
	 * @see org.osgi.service.prefs.Preferences#flush()
	 */
	public void flush(Preferences node) throws BackingStoreException;

	/**
	 * Synchronize the preferences for the given node with the
	 * backing persistent store. If the node does not contain
	 * preferences which are applicable to this scope, then do nothing.
	 * <p>
	 * Clients are free to implement this method as they want and may
	 * choose not to persist preferences in their scope at all.
	 * </p>
	 * @param node the node to synchronize
	 * @throws BackingStoreException if there is a problem accessing the
	 * 	backing store or if it is not available
	 * @see #flush(org.osgi.service.prefs.Preferences)
	 * @see org.osgi.service.prefs.Preferences#sync()
	 */
	public void sync(Preferences node) throws BackingStoreException;
}
