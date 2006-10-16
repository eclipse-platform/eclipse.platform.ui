/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.contexts.provisional;

import java.util.EventObject;

import org.eclipse.jface.viewers.ISelection;

/**
 * A debug context event.
 * 
 * @since 3.3
 */
public class DebugContextEvent extends EventObject {

	/**
	 * The context
	 */
	private ISelection fContext;
	
	/**
	 * Type of context event - change or activation
	 */
	private int fEventType;
	
	/**
	 * Context activation event type.
	 */
	public static final int ACTIVATED = 1;
	
	/**
	 * Context change event type.
	 */
	public static final int CHANGED = 2;	

	/**
     * Generated serial version UID for this class.
     */
	private static final long serialVersionUID = 3395172504615255524L;

	/**
	 * @param source
	 */
	public DebugContextEvent(IDebugContextProvider source, ISelection context, int eventType) {
		super(source);
		fContext = context;
		fEventType = eventType;
	}
	
	/**
	 * Returns the context associated with this event.
	 * 
	 * @return
	 */
	public ISelection getContext() {
		return fContext;
	}
	
	/**
	 * Returns the event type.
	 * 
	 * @return event type
	 */
	public int getEventType() {
		return fEventType;
	}
	
	/**
	 * Returns the context provider that initiated this event.
	 * 
	 * @return context provider
	 */
	public IDebugContextProvider getDebugContextProvider() {
		return (IDebugContextProvider) getSource();
	}
}
