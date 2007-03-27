/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.contexts;


/**
 * A debug context listener is notified of debug context events.
 * <p>
 * Clients may implement this interface.
 * </p>
 * @see DebugContextEvent
 * @see IDebugContextManager
 * @see IDebugContextService
 * @see IDebugContextProvider
 * @since 3.3
 */
public interface IDebugContextListener {
	
	/**
	 * Notification the debug context has changed as specified by the given event.
	 * 
	 * @param event debug context event
	 */
	public void debugContextChanged(DebugContextEvent event);

}
