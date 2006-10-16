/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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
 * Notified of context events by a debug context provider.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @since 3.3
 * @see IDebugContextManager
 */
public interface IDebugContextEventListener {
	
	/**
	 * Notification of the specified debug context event.
	 * 
	 * @param event debug context event
	 */
	public void contextEvent(DebugContextEvent event);

}
