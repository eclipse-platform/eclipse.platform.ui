package org.eclipse.jface.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
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
