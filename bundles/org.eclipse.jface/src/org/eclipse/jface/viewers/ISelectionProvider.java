package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Interface common to all objects that provide a selection.
 *
 * @see ISelection
 * @see ISelectionChangedListener
 * @see SelectionChangedEvent
 */
public interface ISelectionProvider {
/**
 * Adds a listener for selection changes in this selection provider.
 * Has no effect if an identical listener is already registered.
 *
 * @param listener a selection changed listener
 */
public void addSelectionChangedListener(ISelectionChangedListener listener);
/**
 * Returns the current selection for this provider.
 * 
 * @return the current selection
 */
public ISelection getSelection();
/**
 * Removes the given selection change listener from this selection provider.
 * Has no affect if an identical listener is not registered.
 *
 * @param listener a selection changed listener
 */
public void removeSelectionChangedListener(ISelectionChangedListener listener);
/**
 * Sets the current selection for this selection provider.
 *
 * @param selection the new selection
 */
public void setSelection(ISelection selection);
}
