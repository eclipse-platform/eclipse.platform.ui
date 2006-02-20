/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts.provisional;

/**
 * Adapter retrieved from an <code>ILaunch</code> that notifies
 * listeners when it suspends at a context where debugging should
 * be initiated by the user. For example, when a breakpoint is encountered.
 * <p>
 * It is important that the same instance of a suspend trigger adapter is
 * returned each time it is asked for the same object, such that listeners
 * can be added and removed from the same instance. When a listener is removed
 * and no more listeners are registered, this trigger can be disposed or replaced
 * with a new adapter the next time one is requested. 
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 3.2
 */
public interface ISuspendTriggerAdapter {
	
    /**
     * Registers the given listener for suspend notifications.
     * 
     * @param listener suspend listener
     */
	public void addSuspendTriggerListener(ISuspendTriggerListener listener);
    
    /**
     * Deregisters the given listener for suspend notifications.
     * 
     * @param listener suspend listener
     */
	public void removeSuspendTriggerListener(ISuspendTriggerListener listener);
	
}
