/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui;

/**
 * Interface for listening to part lifecycle events.
 * <p>
 * This is a replacement for <code>IPartListener</code>.
 * <p> 
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPartService#addPartListener2
 */
public interface IPartListener2 {
	
/**
 * Notifies this listener that the given part has been activated.
 *
 * @param part the part that was activated
 * @see IWorkbenchPage#activate
 */
public void partActivated(IWorkbenchPartReference ref)
;
/**
 * Notifies this listener that the given part has been brought to the top.
 * <p>
 * These events occur when an editor is brought to the top in the editor area,
 * or when a view is brought to the top in a page book with multiple views.
 * They are normally only sent when a part is brought to the top 
 * programmatically (via <code>IPerspective.bringToTop</code>). When a part is
 * activated by the user clicking on it, only <code>partActivated</code> is sent.
 * </p>
 *
 * @param part the part that was surfaced
 * @see IWorkbenchPage#bringToTop
 */
public void partBroughtToTop(IWorkbenchPartReference ref);

/**
 * Notifies this listener that the given part has been closed.
 *
 * @param part the part that was closed
 * @see IWorkbenchPage#hideView
 */
public void partClosed(IWorkbenchPartReference ref);

/**
 * Notifies this listener that the given part has been deactivated.
 *
 * @param part the part that was deactivated
 * @see IWorkbenchPage#activate
 */
public void partDeactivated(IWorkbenchPartReference ref);

/**
 * Notifies this listener that the given part has been opened.
 *
 * @param part the part that was opened
 * @see IWorkbenchPage#showView
 */ 
public void partOpened(IWorkbenchPartReference ref);

/**
 * Notifies this listener that the given part is hidden.
 *
 * @param part the part that is hidden
 */	
public void partHidden(IWorkbenchPartReference ref);

/**
 * Notifies this listener that the given part is visible.
 *
 * @param part the part that is visible
 */
public void partVisible(IWorkbenchPartReference ref);

/**
 * Notifies this listener that the given part input was changed.
 *
 * @param part the part that is visible
 */
/* Commented out sender in PartListenerList2 as well.
 * public void partInputChanged(IWorkbenchPartReference ref);
*/
}
