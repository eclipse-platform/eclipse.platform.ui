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
package org.eclipse.ui.forms.events;

import org.eclipse.swt.events.TypedEvent;

/**
 * Notifies listeners about a hyperlink change.
 */
public class ExpansionEvent extends TypedEvent {
	
/**
 * Creates a new hyperlink
 * @param obj event source
 * @param href the hyperlink reference that will be followed upon when
 * the hyperlink is activated.
 * @param label the name of the hyperlink (the text that is rendered
 * as a link in the source widget).
 */
	public ExpansionEvent(Object obj, boolean state) {
		super(obj);
		data = state?Boolean.TRUE:Boolean.FALSE;
	}
	
	public boolean getState() {
		return data.equals(Boolean.TRUE)?true:false;
	}
}
