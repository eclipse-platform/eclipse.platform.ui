/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.day;

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;

/**
 * A SelectionChangeEvent for selected Calendarables in a DayEditor.
 * 
 * @since 3.2
 */
public class SelectionChangeEvent {
	/**
	 * The previously selected Calendarable or null if no selection
	 */
	public final CalendarableItem oldSelection;
	
	/**
	 * The new selection or null if the selection is being cleared
	 */
	public final CalendarableItem newSelection;

	/**
	 * Constructor SelectionChangeEvent.  Construct a SelectionChangeEvent from
	 * the Calendarable objects that represent the old and new selection.
	 * 
	 * @param oldSelection The previously selected Calendarable or null if no selection
	 * @param newSelection The new selection or null if the selection is being cleared
	 */
	public SelectionChangeEvent(final CalendarableItem oldSelection, final CalendarableItem newSelection) {
		this.oldSelection = oldSelection;
		this.newSelection = newSelection;
	}
}
