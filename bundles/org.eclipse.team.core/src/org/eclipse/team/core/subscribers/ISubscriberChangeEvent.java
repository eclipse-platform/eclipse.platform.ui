/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.subscribers;
import org.eclipse.core.resources.IResource;
public interface ISubscriberChangeEvent {
	/*====================================================================
	 * Constants defining the kinds of team changes to resources:
	 *====================================================================*/
	/**
	 * Delta kind constant indicating that the resource has not been changed in any way
	 * @see IResourceDelta#getKind
	 */
	public static final int NO_CHANGE = 0;
	/**
	 * Delta kind constant (bit mask) indicating that the synchronization state of a resource has changed.
	 */
	public static final int SYNC_CHANGED = 0x1;
	/**
	 * Delta kind constant (bit mask) indicating that a team provider has been configured on the resource.
	 * @see IResourceDelta#getKind
	 */
	public static final int ROOT_ADDED = 0x2;
	/**
	 * Delta kind constant (bit mask) indicating that a team provider has been de-configured on the resource.
	 * @see IResourceDelta#getKind
	 */
	public static final int ROOT_REMOVED = 0x4;
	public abstract int getFlags();
	public abstract IResource getResource();
	public abstract Subscriber getSubscriber();
}