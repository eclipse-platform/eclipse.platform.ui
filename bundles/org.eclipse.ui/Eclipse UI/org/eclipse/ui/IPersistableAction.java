package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.IAction;

/**
 * Interface for asking an action to store its state in a memento.
 * <p>
 * This interface is typically included in contributions where 
 * persistance is required.
 * </p><p>
 * When the workbench is shutdown action which implement this interface 
 * will be persisted.  At this time the <code>saveState</code> 
 * method is invoked to store the element data into a newly created memento. 
 * The resulting mementos are collected up and written out to a single file.
 * </p>
 * <p>
 * During workbench startup these mementos are read from the file and the
 * <code>init</code> method is called.
 *
 * @see IMemento
 * @see IViewActionDelegate
 * @see IEditorActionDelegate
 */
public interface IPersistableAction {
/**
 * Initializes this persistable action with the given part, action and memento.
 * <p>
 * This method is called automatically as the action is created
 * and initialized. Clients must not call this method.
 * </p>
 *
 * @param action the action proxy that handles the presentation portion of the action
 * @param part the part that provides the context for this delegate
 * @param memento the storage area which object's state will be restored from
 */
public void restoreState(IAction action,IWorkbenchPart part,IMemento memento);
/**
 * Saves the state of the object in the given memento.
 * <p>
 * This method is called automatically as the part is saved.
 * Clients must not call this method.
 * </p>
 * @param part the part that provides the context for this delegate
 * @param memento the storage area for object's state
 */	
public void saveState(IWorkbenchPart part,IMemento memento);
}

