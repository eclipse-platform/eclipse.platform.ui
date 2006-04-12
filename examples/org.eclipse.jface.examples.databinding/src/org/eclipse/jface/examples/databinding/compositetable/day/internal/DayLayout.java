/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.day.internal;

import java.util.List;

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.Calendarable;

/**
 * Represents the abstract layout information required for laying out all events
 * in a single day.
 * 
 * @since 3.2
 */
public class DayLayout {
	/**
	 * The List&lt;Calendarable> for the day.
	 */
	public final List model;
	
	/**
	 * The layout of how the Calendarables need to be arranged in the day's column.
	 */
	public final Calendarable[][] layout;
	
	/**
	 * Construct a DayLayout.
	 * 
	 * @param model
	 *            The List&lt;Calendarable> for the day.
	 * @param layout
	 *            The layout of how the Calendarables need to be arranged in the
	 *            day's column.
	 */
	public DayLayout(List model, Calendarable[][] layout) {
		this.model = model;
		this.layout = layout;
	}
}
