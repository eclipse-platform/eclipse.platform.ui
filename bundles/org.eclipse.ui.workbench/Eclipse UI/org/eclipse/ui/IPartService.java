package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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

public void addPartListener(IPartListener2 listener);
/*
 * Returns the active part.
 *
 * @return the active part, or <code>null</code> if no part is currently active
 */
public IWorkbenchPart getActivePart();
/*
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
public void removePartListener(IPartListener2 listener);
}
