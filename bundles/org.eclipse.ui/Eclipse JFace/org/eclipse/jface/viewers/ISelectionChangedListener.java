package org.eclipse.jface.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/** 
 * A listener which is notified when a viewer's selection changes.
 *
 * @see ISelection
 * @see ISelectionProvider
 * @see SelectionChangedEvent
 */
public interface ISelectionChangedListener {
/**
 * Notifies that the selection has changed.
 *
 * @param event event object describing the change
 */
public void selectionChanged(SelectionChangedEvent event);
}
