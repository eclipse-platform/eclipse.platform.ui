/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/
package org.eclipse.ui;
/**
 * This is an extension of IPartListener. Part listeners that implement 
 * this interface will be notified when a part is made visible or hidden in
 * addition to the notification from IPartListener.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @see IPartService#addPartListener
 */
public interface IPartListener2 extends IPartListener {
/**
 * Notifies this listener that the given part is hidden.
 *
 * @param part the part that is hidden
 */	
public void partHidden(IWorkbenchPart part);
/**
 * Notifies this listener that the given part is visible.
 *
 * @param part the part that is visible
 */
public void partVisible(IWorkbenchPart part);
}
