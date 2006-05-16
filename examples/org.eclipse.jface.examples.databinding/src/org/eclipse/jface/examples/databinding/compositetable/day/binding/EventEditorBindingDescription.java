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

package org.eclipse.jface.examples.databinding.compositetable.day.binding;

import org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor;
import org.eclipse.jface.internal.databinding.provisional.DataBindingContext;

/**
 * A binding description for an IEventEditor control.
 * 
 * @since 3.3
 */
public class EventEditorBindingDescription {

	/**
	 * The IEventEditor to bind
	 */
	public final IEventEditor editor;
	/**
	 * A data binding context for binding CalendarableItems
	 */
	public final DataBindingContext dbc;
	/**
	 * The name of the model-side start time property
	 */
	public final String startTimePropertyName;
	/**
	 * The name of the model-side end time property
	 */
	public final String endTimePropertyName;
	/**
	 * The name of the model-side text property
	 */
	public final String textPropertyName;
	/**
	 * The name of the model-side tool tip text property
	 */
	public final String toolTipTextPropertyName;
	/**
	 * The name of the model-side image property
	 */
	public final String imagePropertyName;
	/**
	 * The name of the all-day event flag property
	 */
	public final String allDayEventPropertyName;
	
	/**
	 * Construct a binding description for a particular IEventEditor
	 * 
	 * @param editor The IEventEditor to bind
	 * @param dbc A data binding context for binding CalendarableItems
	 * @param startTimePropertyName The name of the model-side start time property
	 * @param endTimePropertyName The name of the model-side end time property
	 * @param allDayEventPropertyName The name of the all-day event flag property
	 * @param textPropertyName The name of the model-side text property
	 * @param toolTipTextPropertyName The name of the model-side tool tip text property
	 * @param imagePropertyName The name of the model-side image property
	 */
	public EventEditorBindingDescription(final IEventEditor editor, final DataBindingContext dbc, final String startTimePropertyName, final String endTimePropertyName, String allDayEventPropertyName, final String textPropertyName, final String toolTipTextPropertyName, final String imagePropertyName) {
		this.editor = editor;
		this.dbc = dbc;
		this.startTimePropertyName = startTimePropertyName;
		this.endTimePropertyName = endTimePropertyName;
		this.allDayEventPropertyName = allDayEventPropertyName;
		this.textPropertyName = textPropertyName;
		this.toolTipTextPropertyName = toolTipTextPropertyName;
		this.imagePropertyName = imagePropertyName;
	}
}
