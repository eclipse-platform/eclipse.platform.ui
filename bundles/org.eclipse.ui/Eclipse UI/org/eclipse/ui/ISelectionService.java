package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.ISelection;

/**
 * A selection service tracks the selection within an object.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPerspective
 */
public interface ISelectionService {
/**
 * Adds the given selection listener.
 * Has no effect if an identical listener is already registered.
 *
 * @param listener a selection listener
 */
public void addSelectionListener(ISelectionListener listener);
/**
 * Returns the current selection in the active part.  If the selection in the
 * active part is <em>undefined</em> (the active part has no selection provider)
 * the result will be <code>null</code>.
 *
 * @return the current selection, or <code>null</code> if undefined.  
 */
public ISelection getSelection();
/**
 * Removes the given selection listener.
 * Has no affect if an identical listener is not registered.
 *
 * @param listener a selection listener
 */
public void removeSelectionListener(ISelectionListener listener);
}
