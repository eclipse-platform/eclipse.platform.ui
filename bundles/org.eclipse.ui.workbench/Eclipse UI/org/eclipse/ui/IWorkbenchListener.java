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

/**
 * Interface for listening to workbench lifecycle events.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IWorkbench#addWorkbenchListener
 * @see IWorkbench#removeWorkbenchListener
 * @since 3.2
 */
public interface IWorkbenchListener {
	
    /**
     * Notifies that the workbench is about to shut down.
     * <p>
     * This method is called immediately prior to workbench shutdown before any
     * windows have been closed.
     * </p>
     * <p>
     * The listener may veto a regular shutdown by returning <code>false</code>, 
     * although this will be ignored if the workbench is being forced to shut down.
     * </p>
     * <p>
     * Since other workbench listeners may veto the shutdown, the listener should
     * not dispose resources or do any other work during this notification that would 
     * leave the workbench in an inconsistent state.
     * </p>
     * 
     * @param workbench the workbench
     * @param forced <code>true</code> if the workbench is being forced to shutdown,
     *   <code>false</code> for a regular close
     * @return <code>true</code> to allow the workbench to proceed with shutdown,
     *   <code>false</code> to veto a non-forced shutdown
     */
    public boolean preShutdown(IWorkbench workbench, boolean forced);

    /**
     * Performs arbitrary finalization after the workbench stops running.
     * <p>
     * This method is called during workbench shutdown after all windows
     * have been closed.
     * </p>
     * 
     * @param workbench the workbench
     */
    public void postShutdown(IWorkbench workbench);

}
