package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A listener which is notified of double-click events on viewers.
 */
public interface ISelectionActivatedListener {
/**
 * Notifies of a double click.
 *
 * @param event event object describing the double-click
 */
public void selectionActivated(SelectionActivatedEvent event);
}
