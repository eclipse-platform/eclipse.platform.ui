package org.eclipse.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/**
 * A part service tracks the creation and activation of parts within a
 * workbench page.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IWorkbenchPage
 */
public interface IPartService {
/**
 * Adds the given listener for part lifecycle events.
 * Has no effect if an identical listener is already registered.
 *
 * @param listener a part listener
 */
public void addPartListener(IPartListener listener);
/*
 * Returns the active part.
 *
 * @return the active part, or <code>null</code> if no part is currently active
 */
public IWorkbenchPart getActivePart();
/**
 * Removes the given part listener.
 * Has no affect if an identical listener is not registered.
 *
 * @param listener a part listener
 */
public void removePartListener(IPartListener listener);
}
