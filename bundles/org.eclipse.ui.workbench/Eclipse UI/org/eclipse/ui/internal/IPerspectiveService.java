/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

 
import org.eclipse.ui.*;

/**
 * A perspective service tracks the activation and reset of perspectives within a
 * workbench page.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IWorkbenchPage
 */
public interface IPerspectiveService {
/**
 * Adds the given listener for a page's perspective lifecycle events.
 * Has no effect if an identical listener is already registered.
 *
 * @param listener a perspective listener
 */
public void addPerspectiveListener(IInternalPerspectiveListener listener);
/*
 * Returns the active perspective descriptor in the active workbench page.
 *
 * @return the active perspective descriptor, or <code>null</code> if no perspective is currently active
 */
public IPerspectiveDescriptor getActivePerspective();
/**
 * Removes the given page's perspective listener.
 * Has no affect if an identical listener is not registered.
 *
 * @param listener a perspective listener
 */
public void removePerspectiveListener(IInternalPerspectiveListener listener);
}
