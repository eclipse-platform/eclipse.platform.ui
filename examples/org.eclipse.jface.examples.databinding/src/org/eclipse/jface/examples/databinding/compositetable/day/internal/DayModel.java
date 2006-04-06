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

package org.eclipse.jface.examples.databinding.compositetable.day.internal;

import java.util.List;

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.Calendarable;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor;

/**
 * Represents a model of how the events are laid out in a particular day
 * 
 * @since 3.2
 */
public class DayModel {
	
	private final int numberOfDivisionsInHour;
	
	/**
	 * Construct a DayModel for an IEventEditor.
	 * 
	 * @param parent The IEventEditor containing event data to model.
	 */
	public DayModel(IEventEditor parent) {
		numberOfDivisionsInHour = parent.getNumberOfDivisionsInHour();
	}
	
	/**
	 * Given an unsorted list of Calendarables, each of which has a start and an
	 * end time, this method will return a two dimensional array containing
	 * references to the Calendarables, where the 0th dimension represents the
	 * columns in which the Calendarables must be arranged in order to not
	 * overlap in 2D space, and where each row represents a time slice out of
	 * the day. The number of time slices is IEventEditor.DISPLAYED_HOURS * the
	 * number of divisions in the hour, as returned by the parent IEventEditor.
	 * 
	 * @param events
	 *            A list of events
	 * @return An array of columns, where each column contains references to the
	 *         events in that column for the corresponding time slice in the
	 *         day.
	 */
	public Calendarable[][] getColumnsForEvents(List events) {
		Calendarable[][] columns = new Calendarable[1][IEventEditor.DISPLAYED_HOURS * numberOfDivisionsInHour];
		for (int i = 0; i < columns.length; i++) {
			for (int j = 0; j < columns[i].length; j++) {
				columns[i][j] = null;
			}
		}                                                                              
		return columns;
	}
}
