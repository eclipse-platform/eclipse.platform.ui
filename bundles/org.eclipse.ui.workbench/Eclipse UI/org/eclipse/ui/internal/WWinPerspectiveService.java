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
 * A perspective service for the workbench window.
 */
public class WWinPerspectiveService implements IPerspectiveService {
	private WorkbenchWindow workbenchWindow;
	private PerspectiveListenerList perspectiveListeners = new PerspectiveListenerList();
/**
 * WWinPerspectiveService constructor comment.
 */
public WWinPerspectiveService(WorkbenchWindow window) {
	super();
	this.workbenchWindow = window;
}
/**
 * Adds the given listener for a page's perspective lifecycle events.
 * Has no effect if an identical listener is already registered.
 *
 * @param listener a perspective listener
 */
public void addPerspectiveListener(IInternalPerspectiveListener listener) {
	perspectiveListeners.addPerspectiveListener(listener);
}
/**
 * Fires perspective activated
 */
protected void firePerspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	perspectiveListeners.firePerspectiveActivated(page, perspective);
}
/**
 * Fires perspective changed
 */
protected void firePerspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
	perspectiveListeners.firePerspectiveChanged(page, perspective, changeId);
}
/**
 * Fires perspective reset
 */
protected void firePerspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	perspectiveListeners.firePerspectiveClosed(page, perspective);
}
/**
 * Fires perspective reset
 */
protected void firePerspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
	perspectiveListeners.firePerspectiveOpened(page, perspective);
}
/**
 * Returns the active perspective descriptor in the active workbench page.
 *
 * @return the active perspective descriptor, or <code>null</code> if no perspective is currently active
 */
public IPerspectiveDescriptor getActivePerspective() {
	IWorkbenchPage page = workbenchWindow.getActivePage();
	if (page == null)
		return null;
	else
		return page.getPerspective();
}
/**
 * Removes the given page's perspective listener.
 * Has no affect if an identical listener is not registered.
 *
 * @param listener a perspective listener
 */
public void removePerspectiveListener(IInternalPerspectiveListener listener) {
	perspectiveListeners.removePerspectiveListener(listener);
}
}
