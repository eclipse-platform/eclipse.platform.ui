/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
/**
 * A data structure returned by <code>addSaveParticipant</code>
 * containing a save number and an optional resource delta.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IWorkspace#addSaveParticipant
 */
public interface ISavedState {
/**
 * Returns the files mapped with the <code>ISaveContext.map</code>
 * facility. Returns an empty array if there are no mapped files.
 *
 * @return the files currently mapped by the participant
 *
 * @see #lookup
 * @see ISaveContext#map
 */
public IPath[] getFiles();
/**
 * Returns the save number for the save participant.
 * This is the save number of the last successful save in which the plug-in
 * <b>actively</b> participated, or <code>0</code> if the plug-in has
 * never actively participated in a successful save.
 *
 * @return the save number
 */
public int getSaveNumber();
/**
 * Returns the mapped location associated with the given path 
 * or <code>null</code> if none.
 *
 * @return the mapped location of a given path
 * @see #getFiles
 * @see ISaveContext#map
 */
public IPath lookup(IPath file);
/**
 * Used to receive notification of changes that might have happened
 * while this plug-in was not active.
 * The listener receives notifications of changes to the workspace 
 * resource tree since the time this state was saved.
 * After this method is run, the delta is forgotten. Subsequent calls to this method
 * will have no effect.
 * <p>
 * No notification is received in the following cases:
 * <ul>
 * <li>if a saved state was never recorded (<code>ISaveContext.needDelta</code> 
 * 		was not called) </li>
 * <li>a saved state has since been forgotten (using <code>IWorkspace.forgetSavedTree</code>) </li>
 * <li>a saved state has been deemed too old or has become invalid</li>
 * </ul>
 * <p>
 * All clients should have a contingency plan in place in case 
 * a changes are not available (the case should be very similar
 * to the first time a plug-in is activated, and only has the
 * current state of the workspace to work from).
 * </p>
 * <p>
 * The supplied event is of type <code>IResourceChangeEvent.POST_AUTO_BUILD</code>
 * and contains the delta detailing changes since this plug-in last participated
 * in a save. This event object (and the resource delta within it) is valid only
 * for the duration of the invocation of this method.
 * </p>
 *
 * @param listener the listener
 * @see ISaveContext#needDelta
 * @see IResourceChangeListener
 */
public void processResourceChangeEvents(IResourceChangeListener listener);
}
