package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A listener which is notified of open events on viewers.
 */
public interface IOpenListener {
/**
 * Notifies of an open event.
 *
 * @param event event object describing the open event
 */
public void open(OpenEvent event);
}
