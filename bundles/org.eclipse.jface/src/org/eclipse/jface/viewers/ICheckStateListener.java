package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A listener which is notified of changes to the checked
 * state of items in checkbox viewers.
 *
 * @see CheckStateChangedEvent
 */
public interface ICheckStateListener {
/**
 * Notifies of a change to the checked state of an element.
 *
 * @param event event object describing the change
 */
void checkStateChanged(CheckStateChangedEvent event);
}
