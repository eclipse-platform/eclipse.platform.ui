package org.eclipse.jface.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

/** 
 * A content provider mediates between the viewer's model
 * and the viewer itself.
 * 
 * @see IViewer#setContentProvider
 */
public interface IContentProvider {
/**
 * Disposes of this content provider.  
 * This is called by the viewer when it is disposed.
 */
public void dispose();
/**
 * Notifies this content provider that the given viewer's input
 * has been switched to a different element.
 * <p>
 * A typical use for this method is registering the content provider as a listener
 * to changes on the new input (using model-specific means), and deregistering the viewer 
 * from the old input. In response to these change notifications, the content provider
 * propagates the changes to the viewer.
 * </p>
 *
 * @param viewer the viewer
 * @param oldInput the old input element, or <code>null</code> if the viewer
 *   did not previously have an input
 * @param newInput the new input element, or <code>null</code> if the viewer
 *   does not have an input
 */
public void inputChanged(Viewer viewer, Object oldInput, Object newInput);
}
