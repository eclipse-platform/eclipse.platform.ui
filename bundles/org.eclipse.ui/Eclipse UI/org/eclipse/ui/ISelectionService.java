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
 * Adds a part-specific selection listener which is notified when selection changes in the part with
 * the given id.  This is independent of part activation: the part need not
 * be active for notification to be sent.  The listener is also notified when the part is created
 * and disposed.  When the part is created, the listener is passed the part's initial selection.
 * When the part is disposed, the listener is passed a <code>null</code> selection.
 *
 * @param partId the id of the part to track
 * @param listener a selection listener
 * @since 2.0
 */
public void addSelectionListener(String partId, ISelectionListener listener);
/**
 * Adds the given post selection listener.
 * Has no effect if an identical listener is already registered.
 *
 * @param listener a selection listener
 */
public void addPostSelectionListener(ISelectionListener listener);

/**
 * Adds a part-specific post selection listener which is notified when selection changes in the part with
 * the given id.  This is independent of part activation: the part need not
 * be active for notification to be sent.  The listener is also notified when the part is created
 * and disposed.  When the part is created, the listener is passed the part's initial selection.
 * When the part is disposed, the listener is passed a <code>null</code> selection.
 *
 * @param partId the id of the part to track
 * @param listener a selection listener
 * @since 2.0
 */
public void addPostSelectionListener(String partId, ISelectionListener listener);
/**
 * Returns the current selection in the active part.  If the selection in the
 * active part is <em>undefined</em> (the active part has no selection provider)
 * the result will be <code>null</code>.
 *
 * @return the current selection, or <code>null</code> if undefined
 */
public ISelection getSelection();

/**
 * Returns the current selection in the part with the given id.  If the part is not open,
 * or if the selection in the active part is <em>undefined</em> (the active part has no selection provider)
 * the result will be <code>null</code>.
 *
 * @param partId the id of the part
 * @return the current selection, or <code>null</code> if undefined
 * @since 2.0
 */
public ISelection getSelection(String partId);
/**
 * Removes the given selection listener.
 * Has no effect if an identical listener is not registered.
 *
 * @param listener a selection listener
 */
public void removeSelectionListener(ISelectionListener listener);
/**
 * Removes the given part-specific selection listener.
 * Has no effect if an identical listener is not registered for the given part id.
 *
 * @param partId the id of the part to track
 * @param listener a selection listener
 * @since 2.0
 */
public void removeSelectionListener(String partId, ISelectionListener listener);
/**
 * Removes the given post selection listener.
 * Has no effect if an identical listener is not registered.
 *
 * @param listener a selection listener
 */
public void removePostSelectionListener(ISelectionListener listener);
/**
 * Removes the given part-specific post selection listener.
 * Has no effect if an identical listener is not registered for the given part id.
 *
 * @param partId the id of the part to track
 * @param listener a selection listener
 * @since 2.0
 */
public void removePostSelectionListener(String partId, ISelectionListener listener);
}
