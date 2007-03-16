/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.events;
import org.eclipse.swt.events.TypedEvent;
/**
 * Notifies listeners when expandable controls change expansion state.
 * 
 * @since 3.0
 */
public final class ExpansionEvent extends TypedEvent {
	private static final long serialVersionUID = 6009335074727417445L;
	/**
	 * Creates a new expansion ecent.
	 * 
	 * @param obj
	 *            event source
	 * @param state
	 *            the new expansion state
	 */
	public ExpansionEvent(Object obj, boolean state) {
		super(obj);
		data = state ? Boolean.TRUE : Boolean.FALSE;
	}
	/**
	 * Returns the new expansion state of the widget.
	 * 
	 * @return <code>true</code> if the widget is now expaned, <code>false</code>
	 *         otherwise.
	 */
	public boolean getState() {
		return data.equals(Boolean.TRUE) ? true : false;
	}
}
