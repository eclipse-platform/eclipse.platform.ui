package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A listener which is notified of selection-activated events on viewers.
 */
public interface ISelectionActivatedListener {
/**
 * Notifies of a selection activated.
 *
 * @param event event object describing the selection-activated
 */
public void selectionActivated(SelectionActivatedEvent event);
}
