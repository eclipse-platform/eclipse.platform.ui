package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.ui.*;

/**
 * Interface for listening to a perspective lifecycle events.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPerspectiveService
 */
public interface IPerspectiveListener {
/**
 * Notifies this listener that the given page's perspective
 * has been activated.
 *
 * @param page the page whose perspective was activated
 * @param perspective the perspective descriptor that was activated
 * @see IWorkbenchPage#setPerspective
 */
public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective);
/**
 * Notifies this listener that the given page's perspective
 * has been reset to its last saved layout.
 *
 * @param page the page whose perspective was reset
 * @param perspective the perspective descriptor
 */
public void perspectiveReset(IWorkbenchPage page, IPerspectiveDescriptor perspective);
}
