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

package org.eclipse.core.internal.dependencies;

/**
 * Represents a change that happened to an element's resolution status.
 * Not to be implemented by clients.
 */
public interface IElementChange {
	/*
	 * Possible statuses.
	 */
	public final static int UNKNOWN = 0;
	public final static int CHANGED = 1;
	public final static int RESOLVED = 2;
	public final static int UNRESOLVED = 4;	
	/*
	 * Transitions between statuses.
	 */
	public final static int RESOLVED_TO_UNKNOWN = (RESOLVED << 4) | UNKNOWN;
	public final static int RESOLVED_TO_UNRESOLVED = (RESOLVED << 4) | UNRESOLVED;
	public final static int UNRESOLVED_TO_UNKNOWN = (UNRESOLVED << 4) | UNKNOWN;
	public final static int UNRESOLVED_TO_RESOLVED = (UNRESOLVED << 4) | RESOLVED;
	public final static int UNKNOWN_TO_RESOLVED = (UNKNOWN << 4) | RESOLVED;
	public final static int UNKNOWN_TO_UNRESOLVED = (UNKNOWN << 4) | UNRESOLVED;
	public final static int DEPENDENCY = (RESOLVED << 4) | RESOLVED; 
	/**
	 * Returns the affected element.
	 */
	public IElement getElement();	
	/**
	 * Returns the kind of the transition.
	 * @see #RESOLVED_TO_UNKNOWN
	 * @see #RESOLVED_TO_UNRESOLVED
	 * @see #UNRESOLVED_TO_UNKNOWN
	 * @see #UNRESOLVED_TO_RESOLVED
	 * @see #UNKNOWN_TO_RESOLVED
	 * @see #UNKNOWN_TO_UNRESOLVED
	 * @see #DEPENDENCY
	 */
	public int getKind();
	/**
	 * Returns the previous status.
	 * @see #RESOLVED
	 * @see #UNRESOLVED
	 * @see #UNKNOWN
	 * @see #CHANGED
	 */
	public int getPreviousStatus();
	/**
	 * Returns the previous status.
	 * @see #RESOLVED
	 * @see #UNRESOLVED
	 * @see #UNKNOWN
	 * @see #CHANGED
	 */	
	public int getNewStatus();
}
