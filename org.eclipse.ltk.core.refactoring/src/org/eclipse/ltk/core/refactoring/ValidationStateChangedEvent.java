/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.core.refactoring;

import java.util.EventObject;



/**
 * Event object describing a validation state change. The source of
 * an event is the change object which validation state changed. This
 * is not necessarily the change event on which a client registered
 * a listener via <code>IDynamicValidationStateChange.addValidationStateListener(listener)
 * </code>. It might be one of its direct or indirect children.
 *
 * @since 3.0
 */
public class ValidationStateChangedEvent extends EventObject {

	/**
	 * Creates a new validation state change event.
	 *  
	 * @param source the change which validation state changed
	 */
	public ValidationStateChangedEvent(Change source) {
		super(source);
	}
	
	/**
	 * Returns the change that triggered this event.
	 * 
	 * @return the change that triggered this event
	 */
	public Change getChange() {
		return (Change)getSource();
	}

}
