package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Interface for listening to a perspective lifecycle events.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPageService#addPerspectiveListener
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
 * has change in some way (e.g. editor area hidden, perspective reset,
 * view show/hide, editor open/close, ...).
 *
 * @param page the page whose perspective was reset
 * @param perspective the perspective descriptor
 * @param changeId one of the <code>CHANGE_*</code> constants on IWorkbenchPage
 */
public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId);
}
