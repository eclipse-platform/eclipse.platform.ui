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
public interface IInternalPerspectiveListener extends IPerspectiveListener {
	
	/**
	 * Notifies this listener that the given page's perspective
	 * has been closed.
	 *
	 * @param page the page whose perspective was closed
	 * @param perspective the descriptor of the perspective that was closed
	 */
	public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective);

	/**
	 * Notifies this listener that the given page's perspective
	 * has been opened. The perspective is not active yet.
	 *
	 * @param page the page whose perspective was opened
	 * @param perspective the descriptor of the perspective that was opened
	 */
	public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective);
}