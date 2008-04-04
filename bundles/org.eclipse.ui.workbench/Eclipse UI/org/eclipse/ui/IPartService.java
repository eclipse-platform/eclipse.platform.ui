/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * A part service tracks the creation and activation of parts within a
 * workbench page.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * It can also be acquired from your service locator: e.g.
 * getSite().getService(IPartService.class)
 * </p>
 *
 * @see IWorkbenchPage
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IPartService {

    /**
     * Adds the given listener for part lifecycle events.
     * Has no effect if an identical listener is already registered.
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If
	 * not, they will be removed when the IServiceLocator used to acquire this
	 * service is disposed.
	 * </p>
     *
     * @param listener a part listener
     * @see #removePartListener(IPartListener)
     */
    public void addPartListener(IPartListener listener);

    /**
     * Adds the given listener for part lifecycle events.
     * Has no effect if an identical listener is already registered.
	 * <p>
	 * <b>Note:</b> listeners should be removed when no longer necessary. If
	 * not, they will be removed when the IServiceLocator used to acquire this
	 * service is disposed.
	 * </p>
     *
     * @param listener a part listener
     * see {@link #removePartListener(IPartListener2)}
     */
    public void addPartListener(IPartListener2 listener);

    /**
     * Returns the active part.
     *
     * @return the active part, or <code>null</code> if no part is currently active
     */
    public IWorkbenchPart getActivePart();

    /**
     * Returns the active part reference.
     *
     * @return the active part reference, or <code>null</code> if no part
     * is currently active
     */
    public IWorkbenchPartReference getActivePartReference();

    /**
     * Removes the given part listener.
     * Has no affect if an identical listener is not registered.
     *
     * @param listener a part listener
     */
    public void removePartListener(IPartListener listener);

    /**
     * Removes the given part listener.
     * Has no affect if an identical listener is not registered.
     *
     * @param listener a part listener
     */
    public void removePartListener(IPartListener2 listener);
}
